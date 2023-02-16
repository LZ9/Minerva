package com.lodz.android.minerva

import android.content.Context
import android.media.AudioFormat
import androidx.annotation.IntDef
import com.konovalov.vad.*
import com.lodz.android.minerva.contract.*
import com.lodz.android.minerva.modules.record.RecordingImpl
import com.lodz.android.minerva.modules.vad.VadImpl
import com.lodz.android.minerva.bean.AudioFormats
import java.io.File

/**
 * 录音控制代理
 * @author zhouL
 * @date 2021/11/10
 */
class MinervaAgent private constructor() {

    /** 端点检测帧大小类型 */
    @IntDef(SMALL, MIDDLE, BIG)
    @Retention(AnnotationRetention.SOURCE)
    annotation class VadFrameSizeType

    companion object {
        /** 小 */
        const val SMALL = 0

        /** 中 */
        const val MIDDLE = 1

        /** 大 */
        const val BIG = 2

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
    /** 保存音频文件夹路径 */
    private var mSaveDirPath = ""
    /** 保存音频格式 */
    private var mRecordingFormat = AudioFormats.PCM

    /** 录音状态监听器 */
    private var mOnRecordingStateListener: OnRecordingStatesListener? = null

    /** 设置采样率[sampleRate]，
     * 一般常见采样率为：8000（低质量），16000（普通质量语音），32000（较高质量语音），44100（CD质量），48000（数字音频）
     * 默认16000
     * */
    fun setSampleRate(sampleRate: Int): MinervaAgent = this.apply {
        this.mSampleRate = sampleRate
    }

    /** 设置声道[channel]，默认为：AudioFormat.CHANNEL_IN_MONO */
    fun setChannel(channel: Int): MinervaAgent = this.apply {
        this.mChannel = channel
    }

    /** 设置位宽编码[encoding]，默认为：AudioFormat.ENCODING_PCM_16BIT */
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

    /** 完成录音构建，上下文[context] */
    fun buildRecording(context: Context): Minerva {
        val audio = RecordingImpl()
        if (mSaveDirPath.isEmpty()) {
            throw IllegalArgumentException("save path is empty")
        }
        if (!mSaveDirPath.endsWith(File.separator)) {
            mSaveDirPath += File.separator
        }
        audio.init(context, mSampleRate, mChannel, mEncoding, mSaveDirPath, mRecordingFormat, null)
        audio.setOnRecordingStatesListener(mOnRecordingStateListener)
        return audio
    }

    /** 完成端点检测构建， 上下文[context]，是否保存活动语音[isSaveActivityVoice]，端点检测帧大小类型[frameSizeType]，端点检测模式[mode] */
    fun buildVad(
        context: Context,
        isSaveActivityVoice: Boolean,
        @VadFrameSizeType frameSizeType: Int,
        mode: VadMode = VadMode.VERY_AGGRESSIVE,
    ): Minerva {
        val audio = VadImpl()
        if (isSaveActivityVoice && mSaveDirPath.isEmpty()) {
            throw IllegalArgumentException("save path is empty")
        }
        if (!mSaveDirPath.endsWith(File.separator)) {
            mSaveDirPath += File.separator
        }
        if (mEncoding != AudioFormat.ENCODING_PCM_16BIT){
            throw IllegalArgumentException("vad only support 16bit")
        }
        val sampleRate = getVadSampleRate(mSampleRate)
        val frameSize = getVadFrameSize(sampleRate, frameSizeType)

        val config = VadConfig.create()
            .setSampleRate(sampleRate)
            .setFrameSize(frameSize)
            .setMode(mode)
        audio.init(context, mSampleRate, mChannel, mEncoding, mSaveDirPath, mRecordingFormat, config)
        audio.setOnRecordingStatesListener(mOnRecordingStateListener)
        return audio
    }

    /** 获取端点检测的采样率参数 */
    private fun getVadSampleRate(sampleRate: Int): VadSampleRate =
        if (sampleRate == VadSampleRate.SAMPLE_RATE_8K.value) {
            VadSampleRate.SAMPLE_RATE_8K
        } else if (sampleRate == VadSampleRate.SAMPLE_RATE_16K.value) {
            VadSampleRate.SAMPLE_RATE_16K
        } else if (sampleRate == VadSampleRate.SAMPLE_RATE_32K.value) {
            VadSampleRate.SAMPLE_RATE_32K
        } else if (sampleRate == VadSampleRate.SAMPLE_RATE_48K.value) {
            VadSampleRate.SAMPLE_RATE_48K
        } else {
            throw IllegalArgumentException("unsupport vad SampleRate")
        }

    /** 获取端点检测的帧大小参数 */
    private fun getVadFrameSize(sampleRate: VadSampleRate, frameSizeType: Int): VadFrameSize =
        Vad.getValidFrameSize(sampleRate)[frameSizeType]

}