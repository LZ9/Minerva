package com.lodz.android.minerva.modules.record

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.contract.*
import com.lodz.android.minerva.modules.BaseMinervaImpl
import com.lodz.android.minerva.mp3.Mp3Encoder
import com.lodz.android.minerva.utils.ByteUtil.toByteArray
import com.lodz.android.minerva.utils.RecordUtils
import com.lodz.android.minerva.wav.WavUtils
import kotlinx.coroutines.*
import java.io.*

/**
 * 常规录音实现
 * @author zhouL
 * @date 2021/11/10
 */
open class RecordingImpl : BaseMinervaImpl() {

    /** 合并后的最终录音文件 */
    protected var mResultFile: File? = null
    /** 多段录音文件列表 */
    protected var mFiles = ArrayList<File>()

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun start() {
        if (mRecordingState is Pause){//如果当前状态是暂停
            checkSaveDirPath()
            val tempPath =
                if (mRecordingFormat == AudioFormats.MP3) {
                    mSaveDirPath + RecordUtils.getRecordTempFileName(AudioFormats.MP3)
                } else {
                    mSaveDirPath + RecordUtils.getRecordTempFileName(AudioFormats.PCM)
                }
            val tempFile = File(tempPath)
            startRecorder(tempFile, mSampleRate, mChannel, mEncoding, mRecordingFormat)
            return
        }

        if (mRecordingState !is Idle) {
            Log.e(TAG, "当前状态为 ${mRecordingState.javaClass.name} , 重置为空闲")
            mRecordingState = Idle
        }
        checkSaveDirPath()
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
        if (mFiles.isNotEmpty()){
            for (f in mFiles) {
                f.delete()
            }
            mFiles = ArrayList()
        }
        val resultFile = File(resultPath)
        mResultFile = resultFile
        val tempFile = File(tempPath)
        startRecorder(tempFile, mSampleRate, mChannel, mEncoding, mRecordingFormat)
    }

    /** 录音成PCM文件 */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    protected fun startRecorder(tempFile: File, sampleRate: Int, channel: Int, encoding: Int, format: AudioFormats) = MainScope().launch(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, encoding, bufferSize)
        mRecordingState = Recording(null, -1)
        notifyStates(mRecordingState)
        FileOutputStream(tempFile).use {fos->
            DataOutputStream(fos).use { dos ->
                try {
                    audioRecord.startRecording()
                    if (format == AudioFormats.MP3) {
                        recordByMp3(audioRecord, dos, bufferSize, sampleRate)
                    } else {
                        recordByPcm(audioRecord, dos, bufferSize)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mRecordingState = Idle
                    notifyStates(Error(e, "录音发生异常"))
                    return@launch
                }
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
    protected fun recordByPcm(audioRecord: AudioRecord, dos: DataOutputStream, bufferSize: Int) {
        val byteBuffer = ShortArray(bufferSize)
        while (mRecordingState is Recording) {
            val end = audioRecord.read(byteBuffer, 0, byteBuffer.size)
            byteBuffer.forEach {
                dos.write(it.toByteArray())
            }
            notifyStates(Recording(byteBuffer, end))
        }
        notifyStates(Recording(null, -1))
        audioRecord.stop()
        audioRecord.release()
    }

    /** MP3格式录音 */
    protected fun recordByMp3(audioRecord: AudioRecord, dos: DataOutputStream, bufferSize: Int, sampleRate: Int) {
        val byteBuffer = ShortArray(bufferSize)
        val mp3Buffer = ByteArray((7200 + (bufferSize * 2 * 1.25)).toInt())
        Mp3Encoder.init(sampleRate, getChannel().toInt(), sampleRate, getEncoding().toInt())
        while (mRecordingState is Recording) {
            val end = audioRecord.read(byteBuffer, 0, byteBuffer.size)
            val encodedSize = Mp3Encoder.encode(byteBuffer, byteBuffer, end, mp3Buffer)
            dos.write(mp3Buffer, 0, encodedSize)
            notifyStates(Recording(byteBuffer, end))
        }
        notifyStates(Recording(null, -1))
        audioRecord.stop()
        audioRecord.release()
        if (mRecordingState is Stop) {
            val flushResult = Mp3Encoder.flush(mp3Buffer)
            if (flushResult > 0) {
                dos.write(mp3Buffer, 0, flushResult)
            }
        }
    }

    override fun stop() {
        mRecordingState = Stop
    }

    override fun pause() {
        mRecordingState = Pause
    }

    protected fun makeFile() {
        mergeTempFile(mResultFile, mFiles)
        if (mRecordingFormat == AudioFormats.WAV) {
            makeWav()
        }
        Log.v(TAG, "------------ 结束录音 ------------")
    }

    protected fun makeWav() {
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
    protected fun mergeTempFile(file: File?, files: ArrayList<File>) {
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
}