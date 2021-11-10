package com.lodz.android.minerva.modules

import android.content.Context
import android.media.AudioFormat
import com.lodz.android.minerva.contract.*
import com.lodz.android.minerva.recorder.RecordingFormat
import com.lodz.android.minerva.recorder.RecordingState

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
    private var mRecordingFormat = RecordingFormat.PCM

    /** 录音状态监听器 */
    private var mOnRecordingStateListener: OnRecordingStateListener? = null
    /** 录音数据流监听器 */
    private var mOnRecordingDataListener: OnRecordingDataListener? = null
    /** 傅里叶转换后的录音数据流监听器 */
    private var mOnRecordingFftDataListener: OnRecordingFftDataListener? = null
    /** 录音结束监听器 */
    private var mOnRecordingFinishListener: OnRecordingFinishListener? = null
    /** 录音音量大小监听器 */
    private var mOnRecordingSoundSizeListener: OnRecordingSoundSizeListener? = null

    /** 当前录音状态 */
    private var mRecordingState = RecordingState.IDLE

    override fun init(
        context: Context,
        sampleRate: Int,
        channel: Int,
        encoding: Int,
        dirPath: String,
        format: RecordingFormat
    ) {
        mContext = context
        mSampleRate = sampleRate
        mChannel = channel
        mEncoding = encoding
        mSaveDirPath = dirPath
        mRecordingFormat = format
    }

    override fun start() {

    }

    override fun stop() {

    }

    override fun pause() {

    }

    override fun setOnRecordingStateListener(listener: OnRecordingStateListener?) {
        mOnRecordingStateListener = listener
    }

    override fun setOnRecordingDataListener(listener: OnRecordingDataListener?) {
        mOnRecordingDataListener = listener
    }

    override fun setOnRecordingFftDataListener(listener: OnRecordingFftDataListener?) {
        mOnRecordingFftDataListener = listener
    }

    override fun setOnRecordingFinishListener(listener: OnRecordingFinishListener?) {
        mOnRecordingFinishListener = listener
    }

    override fun setOnRecordingSoundSizeListener(listener: OnRecordingSoundSizeListener?) {
        mOnRecordingSoundSizeListener = listener
    }

    override fun getRecordingState(): RecordingState = mRecordingState
}