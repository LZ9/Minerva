package com.lodz.android.minerva.modules.vad

import android.Manifest
import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.VadDetect
import com.lodz.android.minerva.modules.BaseMinervaImpl
import com.konovalov.vad.Vad
import com.konovalov.vad.VadConfig
import com.konovalov.vad.VadFrameSizeType
import com.konovalov.vad.VadMode
import com.lodz.android.minerva.bean.states.Idle
import com.lodz.android.minerva.bean.states.Stop
import com.lodz.android.minerva.contract.MinervaVad
import com.lodz.android.minerva.utils.VadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * 端点检测实现
 * @author zhouL
 * @date 2021/11/10
 */
open class VadImpl : BaseMinervaImpl(), MinervaVad {

    protected var mVad: Vad? = null

    /** 是否保存活动语音 */
    protected var isSaveActiveVoice = false

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
            mRecordingState = Idle
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

    override fun changeSampleRate(sampleRate: Int): Boolean {
        val isChange = super.changeSampleRate(sampleRate)
        if (isChange) {
            mVad?.getVadConfig()?.setSampleRate(VadUtils.getVadSampleRate(mSampleRate))
        }
        return isChange
    }

    override fun changeEncoding(encoding: Int): Boolean = false

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

}



