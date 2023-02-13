package com.lodz.android.minerva.modules.vad

import android.Manifest
import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.VadDetect
import com.lodz.android.minerva.modules.BaseMinervaImpl
import com.lodz.android.minerva.utils.ByteUtil
import com.konovalov.vad.Vad
import com.konovalov.vad.VadConfig
import com.lodz.android.minerva.bean.states.Stop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * 端点检测实现
 * @author zhouL
 * @date 2021/11/10
 */
class VadImpl : BaseMinervaImpl() {

    private var mVad: Vad? = null

    override fun init(
        context: Context,
        sampleRate: Int,
        channel: Int,
        encoding: Int,
        dirPath: String,
        format: AudioFormats
    ) {
        super.init(context, sampleRate, channel, encoding, dirPath, format)
        mVad = Vad(
            VadConfig.newBuilder()
                .setSampleRate(VadConfig.SampleRate.SAMPLE_RATE_16K)
                .setFrameSize(VadConfig.FrameSize.FRAME_SIZE_160)
                .setMode(VadConfig.Mode.VERY_AGGRESSIVE)
//                .setSilenceDurationMillis(500)
//                .setVoiceDurationMillis(500)
                .build()
        )
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun start() {
        MainScope().launch(Dispatchers.IO) {
            mVad?.start()
            val bufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mEncoding)
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate, mChannel, mEncoding, bufferSize)
            mRecordingState = VadDetect(null, -1, false)
            notifyStates(mRecordingState)
            audioRecord.startRecording()

            val byteBuffer = ShortArray(bufferSize)
            while (mRecordingState is VadDetect) {
                val end = audioRecord.read(byteBuffer, 0, byteBuffer.size)
                val isSpeech = mVad?.isSpeech(byteBuffer) ?: false
                notifyStates(VadDetect(byteBuffer, end, isSpeech))
            }
            notifyStates(VadDetect(null, -1, false))
            audioRecord.stop()
            audioRecord.release()
            mVad?.stop()
            notifyStates(mRecordingState)
        }
    }

    override fun stop() {
        mRecordingState = Stop
    }

    override fun pause() {
        stop()
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun resume() {
        start()
    }

}