package com.lodz.android.minerva.agent

import android.content.Context
import android.media.AudioFormat
import com.konovalov.vad.VadConfig
import com.konovalov.vad.VadFrameSizeType
import com.konovalov.vad.VadMode
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.contract.MinervaVad
import com.lodz.android.minerva.contract.OnRecordingStatesListener
import com.lodz.android.minerva.modules.vad.VadImpl
import com.lodz.android.minerva.modules.vad.VadSpeechInterceptor
import com.lodz.android.minerva.utils.VadUtils
import java.io.File

/**
 * 端点检测代理器
 * @author zhouL
 * @date 2023/6/1
 */
class VadAgent {

    /** 采样率 */
    private var mSampleRate = 16000
    /** 声道 */
    private var mChannel = AudioFormat.CHANNEL_IN_MONO
    /** 保存音频文件夹路径 */
    private var mSaveDirPath = ""
    /** 保存音频格式 */
    private var mRecordingFormat = AudioFormats.PCM
    /** 录音状态监听器 */
    private var mOnRecordingStateListener: OnRecordingStatesListener? = null
    /** 是否保存活动语音 */
    private var isSaveActivityVoice = false
    /** 端点检测帧大小类型 */
    private var mFrameSizeType: VadFrameSizeType = VadFrameSizeType.SMALL
    /** 端点检测模式 */
    private var  mVadMode: VadMode = VadMode.VERY_AGGRESSIVE
    /** 端点检测话音判断拦截器 */
    private var mVadSpeechInterceptor: VadSpeechInterceptor? = null
    /** 文件大小最小值 */
    private var mFileMinSize = 0L
    /** 停顿长度阈值 */
    private var mSilenceValue = 0
    /** 缓存音频数量 */
    private var mCacheCount = 0

    /** 设置采样率[sampleRate]，
     * 一般常见采样率为：8000（低质量），16000（普通质量语音），32000（较高质量语音），44100（CD质量），48000（数字音频）
     * 默认16000
     * */
    fun setSampleRate(sampleRate: Int): VadAgent = this.apply {
        this.mSampleRate = sampleRate
    }

    /** 设置声道[channel]，默认为：AudioFormat.CHANNEL_IN_MONO */
    fun setChannel(channel: Int): VadAgent = this.apply {
        this.mChannel = channel
    }

    /** 设置音频存储路径[dirPath] */
    fun setSaveDirPath(dirPath: String): VadAgent = this.apply {
        this.mSaveDirPath = dirPath
    }

    /** 设置录音文件格式[format] */
    fun setAudioFormat(format: AudioFormats): VadAgent = this.apply {
        this.mRecordingFormat = format
    }

    /** 设置录音状态监听器[listener] */
    fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?) = this.apply {
        mOnRecordingStateListener = listener
    }

    /** 设置是否保存活动语音[isSaveActivityVoice] */
    fun setSaveActivityVoice(isSaveActivityVoice: Boolean) = this.apply {
        this.isSaveActivityVoice = isSaveActivityVoice
    }

    /** 设置端点检测帧大小类型[frameSizeType] */
    fun setFrameSizeType(frameSizeType: VadFrameSizeType) = this.apply {
        this.mFrameSizeType = frameSizeType
    }

    /** 端点检测模式[mode] */
    fun setVadMode(mode: VadMode) = this.apply {
        this.mVadMode = mode
    }

    /** 设置端点检测话音判断拦截器[interceptor] */
    fun setVadInterceptor(interceptor: VadSpeechInterceptor?) = this.apply {
        mVadSpeechInterceptor = interceptor
    }

    /** 改变文件大小最小判断值[size] */
    fun setFileMinSize(size: Long) = this.apply {
        mFileMinSize = size
    }

    /** 改变停顿长度阈值[value] */
    fun setSilenceValue(value: Int) = this.apply {
        mSilenceValue = value
    }

    /** 改变语言存储开始前的缓存声音数量[count] */
    fun setCacheCount(count: Int) = this.apply {
        mCacheCount = count
    }

    /** 完成端点检测构建， 上下文[context] */
    fun build(context: Context): MinervaVad {
        val audio = VadImpl()
        if (isSaveActivityVoice && mSaveDirPath.isEmpty()) {
            throw IllegalArgumentException("save path is empty")
        }
        if (!mSaveDirPath.endsWith(File.separator)) {
            mSaveDirPath += File.separator
        }
        if (mRecordingFormat == AudioFormats.MP3) {
            throw IllegalArgumentException("vad only support wav or pcm")
        }
        val sampleRate = VadUtils.getVadSampleRate(mSampleRate)
        val frameSize = VadUtils.getVadFrameSize(sampleRate, mFrameSizeType.value)
        val config = VadConfig.create()
            .setSampleRate(sampleRate)
            .setFrameSize(frameSize)
            .setMode(mVadMode)
        audio.init(context, mSampleRate, mChannel, AudioFormat.ENCODING_PCM_16BIT, mSaveDirPath, mRecordingFormat)
        audio.setVadConfig(config)
        audio.setOnRecordingStatesListener(mOnRecordingStateListener)
        audio.changeSaveActiveVoice(isSaveActivityVoice)
        audio.setVadInterceptor(mVadSpeechInterceptor)
        if (mFileMinSize > 0L){
            audio.changeFileMinSize(mFileMinSize)
        }
        if (mSilenceValue > 0){
            audio.changeSilenceValue(mSilenceValue)
        }
        if (mCacheCount > 0){
            audio.changeCacheCount(mCacheCount)
        }
        return audio
    }
}