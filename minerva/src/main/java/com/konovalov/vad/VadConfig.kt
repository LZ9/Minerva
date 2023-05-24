package com.konovalov.vad

/**
 * 端点检测配置
 * @author zhouL
 * @date 2023/2/13
 */
class VadConfig private constructor() {

    companion object {
        fun create(): VadConfig = VadConfig()
    }

    var sampleRate = VadSampleRate.SAMPLE_RATE_16K
    var mode = VadMode.VERY_AGGRESSIVE
    var frameSize: VadFrameSize? = null
    var voiceDurationMillis = 500
    var silenceDurationMillis = 500


    /** 设置采样率[sampleRate] */
    fun setSampleRate(sampleRate: VadSampleRate): VadConfig {
        this.sampleRate = sampleRate
        return this
    }

    /** 设置端点检测模式[mode] */
    fun setMode(mode: VadMode): VadConfig {
        this.mode = mode
        return this
    }

    /** 设置端点检测帧大小[frameSize] */
    fun setFrameSize(frameSize: VadFrameSize): VadConfig {
        this.frameSize = frameSize
        return this
    }

    fun setVoiceDurationMillis(voiceDurationMillis: Int): VadConfig {
        this.voiceDurationMillis = voiceDurationMillis
        return this
    }

    fun setSilenceDurationMillis(silenceDurationMillis: Int): VadConfig {
        this.silenceDurationMillis = silenceDurationMillis
        return this
    }
}