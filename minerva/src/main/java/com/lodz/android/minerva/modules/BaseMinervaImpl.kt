package com.lodz.android.minerva.modules

import android.content.Context
import android.media.AudioFormat
import com.konovalov.vad.VadConfig
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.Idle
import com.lodz.android.minerva.bean.states.RecordingStates
import com.lodz.android.minerva.contract.Minerva
import com.lodz.android.minerva.contract.OnRecordingStatesListener
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * 录音功能基础实现
 * @author zhouL
 * @date 2023/2/9
 */
abstract class BaseMinervaImpl : Minerva {

    companion object {
        const val TAG = "MinervaTag"
    }

    /** 上下文 */
    protected lateinit var mContext: Context

    /** 采样率 */
    protected var mSampleRate = 16000
    /** 声道 */
    protected var mChannel = AudioFormat.CHANNEL_IN_MONO
    /** 位宽编码 */
    protected var mEncoding = AudioFormat.ENCODING_PCM_16BIT
    /** 保存音频文件夹路径 */
    protected var mSaveDirPath = ""
    /** 保存音频格式 */
    protected var mRecordingFormat = AudioFormats.PCM

    /** 录音状态监听器 */
    protected var mOnRecordingStatesListener: OnRecordingStatesListener? = null

    /** 当前录音状态 */
    protected var mRecordingState: RecordingStates = Idle

    /** 端点检测配置项 */
    protected var mVadConfig: VadConfig? = null

    override fun init(
        context: Context,
        sampleRate: Int,
        channel: Int,
        encoding: Int,
        dirPath: String,
        format: AudioFormats,
        vadConfig: VadConfig?
    ) {
        mContext = context
        mSampleRate = sampleRate
        mChannel = channel
        mEncoding = encoding
        mSaveDirPath = dirPath
        mRecordingFormat = format
        mVadConfig = vadConfig
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

    override fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?) {
        mOnRecordingStatesListener = listener
    }

    override fun getRecordingState(): RecordingStates = mRecordingState

    protected fun notifyStates(state: RecordingStates) {
        MainScope().launch { mOnRecordingStatesListener?.onStateChange(state) }
    }
}