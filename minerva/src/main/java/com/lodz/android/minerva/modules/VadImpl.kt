package com.lodz.android.minerva.modules

import android.content.Context
import android.media.AudioFormat
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.Idle
import com.lodz.android.minerva.bean.states.RecordingStates
import com.lodz.android.minerva.contract.*

/**
 * 端点检测实现
 * @author zhouL
 * @date 2021/11/10
 */
class VadImpl : Minerva {
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

    }

    override fun stop() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?) {
        mOnRecordingStatesListener= listener
    }


    override fun getRecordingState(): RecordingStates = mRecordingState
}