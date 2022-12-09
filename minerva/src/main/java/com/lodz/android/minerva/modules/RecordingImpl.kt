package com.lodz.android.minerva.modules

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.contract.*
import com.lodz.android.minerva.mp3.Mp3Encoder
import com.lodz.android.minerva.utils.ByteUtil
import com.lodz.android.minerva.utils.RecordUtils
import com.lodz.android.minerva.wav.WavUtils
import kotlinx.coroutines.*
import java.io.*

/**
 * 常规录音实现
 * @author zhouL
 * @date 2021/11/10
 */
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

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun start() {
        if (mRecordingState !is Idle) {
            Log.e(TAG, "当前状态为 ${mRecordingState.javaClass.name} , 重置为空闲")
            mRecordingState = Idle
        }
        val resultPath = mSaveDirPath + RecordUtils.getRecordFileName(mRecordingFormat)
        val tempPath =
            if (mRecordingFormat == AudioFormats.MP3) {
                mSaveDirPath + RecordUtils.getRecordTempFileName(AudioFormats.MP3)
            } else {
                mSaveDirPath + RecordUtils.getRecordTempFileName(AudioFormats.PCM)
            }
        Log.v(TAG, "------------ 开始录音 ------------")
        Log.i(TAG, "采样率：$mSampleRate ; 声道 : $mChannel ; 位宽编码(2-16bit,3-8bit) : $mEncoding")
        Log.d(TAG, "录音文件路径：$resultPath")
        Log.d(TAG, "PCM临时文件路径：$tempPath")
        val resultFile = File(resultPath)
        mResultFile = resultFile
        val tempFile = File(tempPath)
        startRecorder(tempFile, mSampleRate, mChannel, mEncoding, mRecordingFormat)
    }

    /** 录音成PCM文件 */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecorder(tempFile: File, sampleRate: Int, channel: Int, encoding: Int, format: AudioFormats) = MainScope().launch(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, encoding, bufferSize)
        mRecordingState = Recording(0.0, null)
        notifyStates(mRecordingState)
        FileOutputStream(tempFile).use {
            try {
                audioRecord.startRecording()
                if (format == AudioFormats.MP3) {
                    recordByMp3(audioRecord, it, bufferSize, sampleRate)
                } else {
                    recordByPcm(audioRecord, it, bufferSize)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mRecordingState = Idle
                notifyStates(Error(e, "录音发生异常"))
                return@launch
            }
        }
        mFiles.add(tempFile)//把临时文件缓存起来
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

    /** PCM格式录音 */
    private fun recordByPcm(audioRecord: AudioRecord, fos: FileOutputStream, bufferSize: Int) {
        val byteBuffer = ByteArray(bufferSize)
        while (mRecordingState is Recording) {
            val end = audioRecord.read(byteBuffer, 0, byteBuffer.size)
            fos.write(byteBuffer, 0, end)
            fos.flush()
            val db = if (getEncoding().toInt() == 8) 0.0 else RecordUtils.getDbFor16Bit(byteBuffer, end)
            notifyStates(Recording(db, byteBuffer))
        }
        notifyStates(Recording(0.0, null))
        audioRecord.stop()
        audioRecord.release()
    }

    /** MP3格式录音 */
    private fun recordByMp3(audioRecord: AudioRecord, fos: FileOutputStream, bufferSize: Int, sampleRate: Int) {
        val byteBuffer = ShortArray(bufferSize)
        val mp3Buffer = ByteArray((7200 + (bufferSize * 2 * 1.25)).toInt())
        Mp3Encoder.init(sampleRate, getChannel().toInt(), sampleRate, getEncoding().toInt())
        while (mRecordingState is Recording) {
            val end = audioRecord.read(byteBuffer, 0, byteBuffer.size)
            val encodedSize = Mp3Encoder.encode(byteBuffer, byteBuffer, end, mp3Buffer)
            fos.write(mp3Buffer, 0, encodedSize)
            val db = if (getEncoding().toInt() == 8) 0.0 else RecordUtils.getDbFor16Bit(byteBuffer, end)
            notifyStates(Recording(db, ByteUtil.toBytes(byteBuffer)))
        }
        notifyStates(Recording(0.0, null))
        audioRecord.stop()
        audioRecord.release()
        if (mRecordingState is Stop) {
            val flushResult = Mp3Encoder.flush(mp3Buffer)
            if (flushResult > 0) {
                fos.write(mp3Buffer, 0, flushResult)
            }
        }
    }

    override fun stop() {
        mRecordingState = Stop
    }

    override fun pause() {
        mRecordingState = Pause
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun resume() {
        if (mRecordingState !is Pause){
            Log.e(TAG, "当前状态为 ${mRecordingState.javaClass.name} , 不为暂停")
            return
        }
        val tempPath =
            if (mRecordingFormat == AudioFormats.MP3) {
                mSaveDirPath + RecordUtils.getRecordTempFileName(AudioFormats.MP3)
            } else {
                mSaveDirPath + RecordUtils.getRecordTempFileName(AudioFormats.PCM)
            }
        val tempFile = File(tempPath)
        startRecorder(tempFile, mSampleRate, mChannel, mEncoding, mRecordingFormat)
    }

    override fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?) {
        mOnRecordingStatesListener = listener
    }

    override fun getRecordingState(): RecordingStates = mRecordingState

    private fun makeFile() {
        mergeTempFile(mResultFile, mFiles)
        if (mRecordingFormat == AudioFormats.WAV) {
            makeWav()
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
    private fun mergeTempFile(file: File?, files: ArrayList<File>) {
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