package com.lodz.android.minerva

import android.content.Context
import android.media.AudioFormat
import androidx.annotation.IntDef
import com.lodz.android.minerva.contract.*
import com.lodz.android.minerva.modules.RecordingImpl
import com.lodz.android.minerva.modules.VadImpl
import com.lodz.android.minerva.modules.service.RecordingServiceImpl
import com.lodz.android.minerva.bean.AudioFormats
import java.io.File

/**
 * 录音控制代理
 * @author zhouL
 * @date 2021/11/10
 */
class MinervaAgent private constructor(private val openType: Int) {

    /** 录音启动类型 */
    @IntDef(RECORDING, RECORDING_SERVICE, VAD)
    @Retention(AnnotationRetention.SOURCE)
    annotation class AudioOpenType

    companion object {
        /** 录音 */
        const val RECORDING = 1

        /** 后台服务录音 */
        const val RECORDING_SERVICE = 2

        /** 端点检测 */
        const val VAD = 3

        /** 创建 */
        @JvmStatic
        fun create(@AudioOpenType openType: Int = RECORDING): MinervaAgent = MinervaAgent(openType)
    }


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
    private var mOnRecordingStateListener: OnRecordingStatesListener? = null

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

    /** 设置音频存储路径[dirPath] */
    fun setSaveDirPath(dirPath: String): MinervaAgent = this.apply {
        this.mSaveDirPath = dirPath
    }

    /** 设置录音文件格式[format] */
    fun setAudioFormat(format: AudioFormats): MinervaAgent = this.apply {
        this.mRecordingFormat = format
    }

    /** 设置录音状态监听器[listener] */
    fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?) = this.apply {
        mOnRecordingStateListener = listener
    }

    /** 完成构建，上下文[context] */
    fun build(context: Context): Minerva {
        val audio = when (openType) {
            RECORDING -> RecordingImpl()
            RECORDING_SERVICE -> RecordingServiceImpl()
            VAD -> VadImpl()
            else -> null
        } ?: throw IllegalArgumentException("unsupport open type")
        if (mSaveDirPath.isEmpty()) {
            throw IllegalArgumentException("save path is empty")
        }
        if (!mSaveDirPath.endsWith(File.separator)) {
            mSaveDirPath += File.separator
        }
        audio.init(context, mSampleRate, mChannel, mEncoding, mSaveDirPath, mRecordingFormat)
        audio.setOnRecordingStatesListener(mOnRecordingStateListener)
        return audio
    }

}