package com.lodz.android.minerva

import android.content.Context
import android.media.AudioFormat
import androidx.annotation.IntDef
import com.lodz.android.minerva.contract.*
import com.lodz.android.minerva.modules.RecordingImpl
import com.lodz.android.minerva.modules.VadImpl
import com.lodz.android.minerva.recorder.RecordingFormat

/**
 * 录音控制代理
 * @author zhouL
 * @date 2021/11/10
 */
class MinervaAgent private constructor() {

    /** 录音启动类型 */
    @IntDef(Recording, Vad)
    @Retention(AnnotationRetention.SOURCE)
    annotation class AudioOpenType

    companion object {
        /** 录音 */
        const val Recording = 1
        /** 端点检测 */
        const val Vad = 2

        /** 创建 */
        @JvmStatic
        fun create(): MinervaAgent = MinervaAgent()
    }


    /** 采样率 */
    private var mSampleRate = 16000
    /** 声道 */
    private var mChannel = AudioFormat.CHANNEL_IN_MONO
    /** 位宽编码 */
    private var mEncoding = AudioFormat.ENCODING_PCM_16BIT
    /** 启动类型 */
    private var mOpenType = Recording
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


    /** 初始化配置录音启动类型[type] */
    fun setOpenType(@AudioOpenType type: Int): MinervaAgent = this.apply {
        this.mOpenType = type
    }

    /** 设置采样率[sampleRate]，一般常见采样率为：8000，16000，44100 */
    fun setSampleRate(sampleRate: Int): MinervaAgent = this.apply {
        this.mSampleRate = sampleRate
    }

    /** 设置声道[channel]，一般常见参数为：AudioFormat.CHANNEL_IN_MONO */
    fun setChannel(channel: Int): MinervaAgent = this.apply {
        this.mChannel = channel
    }

    /** 设置位宽编码[encoding]，一般常见参数为：AudioFormat.ENCODING_PCM_16BIT、AudioFormat.ENCODING_PCM_8BIT */
    fun setEncoding(encoding: Int): MinervaAgent = this.apply {
        this.mEncoding = encoding
    }

    /** 设置自动保存音频文件[isAutoSave] */
    fun setSaveDirPath(dirPath: String): MinervaAgent = this.apply {
        this.mSaveDirPath = dirPath
    }

    /** 设置录音文件格式[format] */
    fun setRecordingFormat(format: RecordingFormat): MinervaAgent = this.apply {
        this.mRecordingFormat = format
    }

    /** 设置录音状态监听器[listener] */
    fun setOnRecordingStateListener(listener: OnRecordingStateListener?) {
        mOnRecordingStateListener = listener
    }

    /** 设置录音数据流监听器[listener] */
    fun setOnRecordingDataListener(listener: OnRecordingDataListener?) {
        mOnRecordingDataListener = listener
    }

    /** 设置傅里叶转换后的录音数据流监听器[listener] */
    fun setOnRecordingFftDataListener(listener: OnRecordingFftDataListener?) {
        mOnRecordingFftDataListener = listener
    }

    /** 设置录音结束监听器[listener] */
    fun setOnRecordingFinishListener(listener: OnRecordingFinishListener?) {
        mOnRecordingFinishListener = listener
    }

    /** 设置录音音量大小监听器[listener] */
    fun setOnRecordingSoundSizeListener(listener: OnRecordingSoundSizeListener?) {
        mOnRecordingSoundSizeListener = listener
    }

    /** 完成构建，上下文[context] */
    fun build(context: Context): Minerva {
        val audio = when (mOpenType) {
            Recording -> RecordingImpl()
            Vad -> VadImpl()
            else -> null
        } ?: throw NullPointerException("unsupport open type")
        audio.init(context, mSampleRate, mChannel, mEncoding, mSaveDirPath, mRecordingFormat)
        audio.setOnRecordingStateListener(mOnRecordingStateListener)
        audio.setOnRecordingDataListener(mOnRecordingDataListener)
        audio.setOnRecordingFftDataListener(mOnRecordingFftDataListener)
        audio.setOnRecordingFinishListener(mOnRecordingFinishListener)
        audio.setOnRecordingSoundSizeListener(mOnRecordingSoundSizeListener)
        return audio
    }
}