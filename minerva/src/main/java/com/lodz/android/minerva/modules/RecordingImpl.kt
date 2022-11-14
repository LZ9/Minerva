package com.lodz.android.minerva.modules

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.contract.*
import com.lodz.android.minerva.utils.RecordUtils
import com.lodz.android.minerva.wav.WavUtils
import kotlinx.coroutines.*
import java.io.*

/**
 * 常规录音实现
 * @author zhouL
 * @date 2021/11/10
 */
@SuppressLint("MissingPermission")
class RecordingImpl : Minerva {

    companion object {
        const val TAG = "MinervaTag"
    }

    /** 上下文 */
    private lateinit var mContext: Context

    /** 采样率 */
    private var mSampleRate = 16000
    /** 声道 */
    private var mChannel = AudioFormat.CHANNEL_IN_MONO
    /** 位宽编码 */
    private var mEncoding = AudioFormat.ENCODING_PCM_16BIT
    /** 保存音频文件夹路径 */
    private var mSaveDirPath = ""
    /** 保存音频格式 */
    private var mRecordingFormat = AudioFormats.PCM

    /** 录音状态监听器 */
    private var mOnRecordingStatesListener: OnRecordingStatesListener? = null

    /** 当前录音状态 */
    private var mRecordingState: RecordingStates = Idle

    /** 合并后的最终录音文件 */
    private var mResultFile: File? = null
    /** 临时录音文件 */
    private var mTempFile: File? = null
    /** 多段录音文件列表 */
    private var mFiles = ArrayList<File>()

    override fun init(
        context: Context,
        sampleRate: Int,
        channel: Int,
        encoding: Int,
        dirPath: String,
        format: AudioFormats
    ) {
        mContext = context
        mSampleRate = sampleRate
        mChannel = channel
        mEncoding = encoding
        mSaveDirPath = dirPath
        mRecordingFormat = format
    }

    override fun changeSampleRate(sampleRate: Int) {
        mSampleRate = sampleRate
    }

    override fun changeEncoding(encoding: Int) {
        mEncoding = encoding
    }

    override fun changeAudioFormat(format: AudioFormats) {
        mRecordingFormat = format
    }

    override fun start() {
        if (mRecordingState !is Idle) {
            Log.e(TAG, "当前状态为 ${mRecordingState.javaClass.name} , 不为空闲")
            return
        }
        val resultPath = mSaveDirPath + RecordUtils.getRecordFileName(mRecordingFormat)
        val tempPath = mSaveDirPath + RecordUtils.getRecordFileName(AudioFormats.PCM)
        Log.v(TAG, "------------ 开始录音 ------------")
        Log.i(TAG, "采样率：$mSampleRate ; 声道 : $mChannel ; 位宽编码(2-16bit,3-8bit) : $mEncoding")
        Log.d(TAG, "录音文件路径：$resultPath")
        Log.d(TAG, "PCM临时文件路径：$tempPath")
        mResultFile = File(resultPath)
        val tempFile = File(tempPath)
        mTempFile = tempFile

        if (mRecordingFormat == AudioFormats.MP3) {
            startMp3Recorder()
        } else {
            startPcmRecorder(tempFile, mSampleRate, mChannel, mEncoding)
        }
    }

    private fun startMp3Recorder() {


    }

    /** 录音成PCM文件 */
    private fun startPcmRecorder(tempFile: File, sampleRate: Int, channel: Int, encoding: Int) = MainScope().launch(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, encoding, bufferSize)
        val byteBuffer = ByteArray(bufferSize)
        mRecordingState = Recording(0.0, byteBuffer)
        notifyStates(mRecordingState)
        FileOutputStream(tempFile).use {
            try {
                audioRecord.startRecording()
                while (mRecordingState is Recording) {
                    val end = audioRecord.read(byteBuffer, 0, byteBuffer.size)
                    it.write(byteBuffer, 0, end)
                    it.flush()
                    val db = if (getEncoding().toInt() == 8) -1.0 else RecordUtils.getDbFor16Bit(byteBuffer, end)
                    notifyStates(Recording(db, byteBuffer))
                }
                notifyStates(Recording(0.0, null))
                audioRecord.stop()
                audioRecord.release()
            } catch (e: Exception) {
                e.printStackTrace()
                mRecordingState = Idle
                notifyStates(Error(e, "录音发生异常"))
            }
        }
        mFiles.add(tempFile)//把临时PCM文件缓存起来
        if (mRecordingState is Pause) {
            Log.i(TAG, "录音暂停")
            notifyStates(mRecordingState)
            return@launch
        }
        if (mRecordingState is Stop) {
            makeFile()//整合录音
            if (mRecordingState is Idle) {// 说明合并过程发生失败
                return@launch
            }
            val file = mResultFile
            if (file != null) {
                notifyStates(Finish(file))
            }
            mRecordingState = Idle
            Log.i(TAG, "录音结束")
        }
    }

    override fun stop() {
        mRecordingState = Stop
    }

    override fun pause() {
        mRecordingState = Pause
    }

    override fun resume() {
        if (mRecordingState !is Pause){
            Log.e(TAG, "当前状态为 ${mRecordingState.javaClass.name} , 不为暂停")
            return
        }
        val tempPath = mSaveDirPath + RecordUtils.getRecordFileName(AudioFormats.PCM)
        val tempFile = File(tempPath)
        mTempFile = tempFile

        if (mRecordingFormat == AudioFormats.MP3) {
            startMp3Recorder()
        } else {
            startPcmRecorder(tempFile, mSampleRate, mChannel, mEncoding)
        }
    }

    override fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?) {
        mOnRecordingStatesListener = listener
    }

    override fun getRecordingState(): RecordingStates = mRecordingState

    private fun makeFile() {
        when (mRecordingFormat) {
            AudioFormats.MP3 -> {}
            AudioFormats.WAV -> {
                mergePcmFile(mResultFile, mFiles)
                makeWav()
            }
            AudioFormats.PCM -> {
                mergePcmFile(mResultFile, mFiles)
            }
        }
        Log.v(TAG, "------------ 结束录音 ------------")
    }

    private fun makeWav() {
        val file = mResultFile
        if (file == null || file.length() == 0L){
            notifyStates(Error(NullPointerException(), "未识别到录音文件"))
            mRecordingState = Idle
            return
        }
        val header = WavUtils.generateHeader(file.length().toInt(), mSampleRate, getChannel(), getEncoding())
        WavUtils.writeHeader(file, header)
    }

    /** 合并Pcm文件 */
    private fun mergePcmFile(file: File?, files: ArrayList<File>) {
        if (file == null || files.isEmpty()) {
            return
        }
        FileOutputStream(file).use { fos ->
            BufferedOutputStream(fos).use { bos ->
                val buffer = ByteArray(1024)
                try {
                    for (f in files) {
                        BufferedInputStream(FileInputStream(f)).use { bis ->
                            var readCount = 0
                            while (bis.read(buffer).also { readCount = it } > 0) {
                                bos.write(buffer, 0, readCount)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    notifyStates(Error(e, "录音合并发生异常"))
                    mRecordingState = Idle
                }
            }
        }
        for (f in files) {
            f.delete()
        }
        files.clear()
    }

    private fun notifyStates(state: RecordingStates) {
        MainScope().launch { mOnRecordingStatesListener?.onStateChange(state) }
    }

    private fun getChannel(): Short = when (mChannel) {
        AudioFormat.CHANNEL_IN_MONO -> 1
        AudioFormat.CHANNEL_IN_STEREO -> 2
        else -> 0
    }

    private fun getEncoding(): Short = when (mEncoding) {
        AudioFormat.ENCODING_PCM_8BIT -> 8
        AudioFormat.ENCODING_PCM_16BIT -> 16
        else -> 0
    }
}