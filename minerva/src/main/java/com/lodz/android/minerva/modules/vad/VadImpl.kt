package com.lodz.android.minerva.modules.vad

import android.Manifest
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import com.konovalov.vad.Vad
import com.konovalov.vad.VadConfig
import com.konovalov.vad.VadFrameSizeType
import com.konovalov.vad.VadMode
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.contract.MinervaVad
import com.lodz.android.minerva.modules.BaseMinervaImpl
import com.lodz.android.minerva.utils.ByteUtil.toByteArray
import com.lodz.android.minerva.utils.RecordUtils
import com.lodz.android.minerva.utils.VadUtils
import com.lodz.android.minerva.wav.WavUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import java.util.*


/**
 * 端点检测实现
 * @author zhouL
 * @date 2021/11/10
 */
open class VadImpl : BaseMinervaImpl(), MinervaVad {

    companion object{
        /** 缓存音频数量 */
        const val CACHE_COUNT = 10
        /** 停顿长度阈值 */
        const val SILENCE_VALUE= 4
        /** 文件最小大小 */
        const val FILE_MIN_SIZE = 35 * 1024
    }

    protected var mVad: Vad? = null

    /** 是否保存活动语音 */
    protected var isSaveActiveVoice = false
    /** 端点检测话音判断拦截器 */
    protected var mVadSpeechInterceptor: VadSpeechInterceptor? = null
    /** 文件大小最小值 */
    protected var mFileMinSize = FILE_MIN_SIZE.toLong()
    /** 停顿长度阈值 */
    protected var mSilenceValue = SILENCE_VALUE
    /** 缓存音频数量 */
    protected var mCacheCount = CACHE_COUNT


    override fun setVadConfig(config: VadConfig) {
        if (!checkChangeParam()){
            return
        }
        if (mVad != null) {
            stop()
            mVad?.stop()
            mVad = null
        }
        mVad = Vad(config)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun start() {
        if (mRecordingState !is Idle) {
            Log.e(TAG, "当前状态为 ${mRecordingState.javaClass.name} , 重置为空闲")
            mRecordingState = Idle
        }
        if (isSaveActiveVoice) {
            checkSaveDirPath()// 如果需要保存活动语音先校验存储目录
        }
        startVad(isSaveActiveVoice, mSampleRate, mChannel, mEncoding, mRecordingFormat)
    }

    /** 开始端点检测 */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startVad(isSaveActiveVoice: Boolean, sampleRate: Int, channel: Int, encoding: Int, format: AudioFormats) = MainScope().launch(Dispatchers.IO) {
        mVad?.start()
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, encoding, bufferSize)
        mRecordingState = VadDetect(null, -1, -1.0, false)
        notifyStates(mRecordingState)
        audioRecord.startRecording()
        val buffer = ShortArray(bufferSize)
        if (isSaveActiveVoice) {
            try {
                vadWithSave(audioRecord, buffer, bufferSize)
            } catch (e: Exception) {
                e.printStackTrace()
                mRecordingState = Idle
                notifyStates(Error(e, "端点检测发生异常"))
                return@launch
            }
        } else {
            vadOnly(audioRecord, buffer)
        }
        notifyStates(VadDetect(null, -1, -1.0, false))
        audioRecord.stop()
        audioRecord.release()
        mVad?.stop()
        notifyStates(mRecordingState)
        mRecordingState = Idle

    }

    /** 只进行端点检测 */
    private fun vadOnly(audioRecord: AudioRecord, buffer: ShortArray) {
        val interceptor = mVadSpeechInterceptor ?: VadOnlyInterceptor()
        while (mRecordingState is VadDetect) {
            val vad = mVad ?: throw NullPointerException("vad is null")
            val end = audioRecord.read(buffer, 0, buffer.size)
            val db = RecordUtils.getDbFor16Bit(buffer, end)
            notifyStates(VadDetect(buffer, end, db, interceptor.isSpeech(vad, buffer, end, db)))
        }
    }

    /** 端点检测时保存活跃音频 */
    @Throws
    private fun vadWithSave(audioRecord: AudioRecord, buffer: ShortArray, bufferSize: Int) {
        val interceptor = mVadSpeechInterceptor ?: VadOnlyInterceptor()
        var file: File? = null
        var fos: FileOutputStream? = null
        var isTalk = false//是否在说话状态
        val silentLinkQueue: Queue<Int> = LinkedList() // vad识别无声音次数队列
        val cacheQueue: Queue<ShortArray> = LinkedList() //录音流缓存
        while (mRecordingState is VadDetect) {
            val vad = mVad ?: throw NullPointerException("vad is null")
            if (cacheQueue.size > mCacheCount){
                cacheQueue.poll()//抛弃超出缓存个数的音频流
            }
            val end = audioRecord.read(buffer, 0, buffer.size)
            val cacheBuffer = ShortArray(bufferSize)
            System.arraycopy(buffer, 0, cacheBuffer, 0, buffer.size)
            cacheQueue.offer(cacheBuffer)
            val db = RecordUtils.getDbFor16Bit(buffer, end)
            val isSpeech = interceptor.isSpeech(vad, buffer, end, db)
            notifyStates(VadDetect(buffer, end, db, isSpeech))

            if (!isTalk) {// 未在说话状态
                Log.d(TAG, "语音检测结果：$isSpeech")
                if (isSpeech){//检测到语音活动
                    Log.i(TAG, "开始说话")
                    isTalk = true
                    file = File(mSaveDirPath + RecordUtils.getRecordFileName(AudioFormats.PCM))
                    fos = FileOutputStream(file)
                }
                continue
            }
            // 在说话状态
            if (!isSpeech) {//如果说话期间出现停顿，未识别到语音活动，则进行计数
                silentLinkQueue.offer(0)
            } else {
                silentLinkQueue.poll()
            }
            val cacheData = cacheQueue.poll() ?: continue
            cacheData.forEach {
                fos?.write(it.toByteArray())
            }
            if (silentLinkQueue.size >= mSilenceValue) {//停顿时间过长则认为语音活动结束
                while (cacheQueue.size > 0) {
                    cacheQueue.poll()?.forEach {
                        fos?.write(it.toByteArray())
                    }
                }

                isTalk = false
                silentLinkQueue.clear()
                Log.i(TAG, "结束说话")
                if (file != null) {
                    val fileSize = file.length()
                    if (fileSize <= mFileMinSize){//文件小于指定大小
                        file.delete()
                        Log.d(TAG, "文件大小小于默认值，可能是杂音，删除文件")
                        notifyStates(Error(IllegalStateException(), "file size is smaller than the default value , delete it"))
                    } else {
                        if (mRecordingFormat == AudioFormats.WAV) {
                            file = makeWav(file)
                        }
                        if (file != null) {
                            notifyStates(VadFileSave(file))
                            Log.i(TAG, "保存音频：${file.absolutePath}")
                        }
                    }
                }
                file = null
                fos?.close()
                fos = null
            }
        }
    }

    /** 将PCM音频文件转为WAV格式，并返回文件路径 */
    private fun makeWav(file: File): File? {
        val header = WavUtils.generateHeader(file.length().toInt(), mSampleRate, getChannel(), getEncoding())
        WavUtils.writeHeader(file, header)
        return renameFile(file, RecordUtils.getRecordFileName(AudioFormats.WAV))
    }

    override fun stop() {
        if (mRecordingState != Idle){
            mRecordingState = Stop
        }
    }

    override fun pause() {
        stop()
    }

    override fun changeAudioFormat(format: AudioFormats): Boolean {
        if (format == AudioFormats.MP3) {
            notifyStates(Error(IllegalArgumentException(), "vad only support wav or pcm"))
            return false
        }
        return super.changeAudioFormat(format)
    }

    override fun changeSampleRate(sampleRate: Int): Boolean {
        val isChange = super.changeSampleRate(sampleRate)
        if (isChange) {
            mVad?.getVadConfig()?.setSampleRate(VadUtils.getVadSampleRate(mSampleRate))
        }
        return isChange
    }

    override fun changeEncoding(encoding: Int): Boolean {
        notifyStates(Error(RuntimeException(), "vad only support 16bit"))
        return false
    }

    override fun changeFrameSizeType(frameSizeType: VadFrameSizeType): Boolean {
        if (checkChangeParam()) {
            val sampleRate = VadUtils.getVadSampleRate(mSampleRate)
            val frameSize = VadUtils.getVadFrameSize(sampleRate, frameSizeType.value)
            mVad?.getVadConfig()?.setFrameSize(frameSize)
            return true
        }
        return false
    }

    override fun changeVadMode(mode: VadMode): Boolean {
        if (checkChangeParam()) {
            mVad?.getVadConfig()?.setMode(mode)
            return true
        }
        return false
    }

    override fun changeSaveActiveVoice(isSaveActiveVoice: Boolean): Boolean {
        if (checkChangeParam()) {
            this.isSaveActiveVoice = isSaveActiveVoice
            return true
        }
        return false
    }

    override fun changeFileMinSize(size: Long): Boolean {
        if (checkChangeParam()) {
            mFileMinSize = size
            return true
        }
        return false
    }

    override fun changeSilenceValue(value: Int): Boolean {
        if (checkChangeParam()) {
            mSilenceValue = value
            return true
        }
        return false
    }

    override fun changeCacheCount(count: Int): Boolean {
        if (checkChangeParam()) {
            mCacheCount = count
            return true
        }
        return false
    }

    override fun setVadInterceptor(interceptor: VadSpeechInterceptor?): MinervaVad {
        mVadSpeechInterceptor = interceptor
        return this
    }

}



