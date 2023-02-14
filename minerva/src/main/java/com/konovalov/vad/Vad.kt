package com.konovalov.vad

import java.util.*

/**
 * 端点检测类
 * @author zhouL
 * @date 2023/2/14
 */
class Vad(private val config: VadConfig) {

    init {
        System.loadLibrary("vad_jni")
    }

    companion object {

        /** 返回特定采样率[sampleRate]对应的帧大小列表 */
        fun getValidFrameSize(sampleRate: VadSampleRate): LinkedList<VadFrameSize> {
            val map = LinkedHashMap<VadSampleRate, LinkedList<VadFrameSize>>()
            map[VadSampleRate.SAMPLE_RATE_8K] = LinkedList<VadFrameSize>().apply {
                add(VadFrameSize.FRAME_SIZE_80)
                add(VadFrameSize.FRAME_SIZE_160)
                add(VadFrameSize.FRAME_SIZE_240)
            }
            map[VadSampleRate.SAMPLE_RATE_16K] = LinkedList<VadFrameSize>().apply {
                add(VadFrameSize.FRAME_SIZE_160)
                add(VadFrameSize.FRAME_SIZE_320)
                add(VadFrameSize.FRAME_SIZE_480)
            }
            map[VadSampleRate.SAMPLE_RATE_32K] = LinkedList<VadFrameSize>().apply {
                add(VadFrameSize.FRAME_SIZE_320)
                add(VadFrameSize.FRAME_SIZE_640)
                add(VadFrameSize.FRAME_SIZE_960)
            }
            map[VadSampleRate.SAMPLE_RATE_48K] = LinkedList<VadFrameSize>().apply {
                add(VadFrameSize.FRAME_SIZE_480)
                add(VadFrameSize.FRAME_SIZE_960)
                add(VadFrameSize.FRAME_SIZE_1440)
            }
            return map[sampleRate] ?: LinkedList<VadFrameSize>()
        }
    }

    private var needResetDetectedSamples = true
    private var detectedVoiceSamplesMillis: Long = 0
    private var detectedSilenceSamplesMillis: Long = 0
    private var previousTimeMillis = System.currentTimeMillis()


    /** 获取端点检测配置 */
    fun getVadConfig(): VadConfig = config

    /** 是否有语音活动 */
    fun isSpeech(audio: ShortArray): Boolean = nativeIsSpeech(audio)

    /** 开始检测 */
    fun start() {
        val frameSize = config.frameSize ?: throw IllegalArgumentException("you need setup frameSize in VadConfig")
        if (!isSampleRateAndFrameSizeValid()) {
            throw UnsupportedOperationException("VAD doesn't support this SampleRate and FrameSize!")
        }
        val result = nativeStart(config.sampleRate.value, frameSize.value, config.mode.value)
        if (result < 0) {
            throw RuntimeException("Error can't set parameters for VAD!")
        }
    }

    /** 停止检测 */
    fun stop() {
        nativeStop()
    }

    /** 可用来监听长句，不会在长句停顿的过程中返回误报结果 */
    fun addContinuousSpeechListener(audio: ShortArray, listener: VadListener) {
        val currentTimeMillis = System.currentTimeMillis()
        if (isSpeech(audio)) {
            detectedVoiceSamplesMillis += currentTimeMillis - previousTimeMillis
            needResetDetectedSamples = true
            if (detectedVoiceSamplesMillis > config.voiceDurationMillis) {
                previousTimeMillis = currentTimeMillis
                listener.onSpeechDetected()
            }
        } else {
            if (needResetDetectedSamples) {
                needResetDetectedSamples = false
                detectedSilenceSamplesMillis = 0
                detectedVoiceSamplesMillis = 0
            }
            detectedSilenceSamplesMillis += currentTimeMillis - previousTimeMillis
            if (detectedSilenceSamplesMillis > config.silenceDurationMillis) {
                previousTimeMillis = currentTimeMillis
                listener.onNoiseDetected()
            }
        }
        previousTimeMillis = currentTimeMillis
    }

    /** 采样率和帧大小是否匹配 */
    private fun isSampleRateAndFrameSizeValid(): Boolean {
        val list = getValidFrameSize(config.sampleRate)
        if (list.size == 0) {
            return false
        }
        return list.contains(config.frameSize)
    }

    private external fun nativeStart(sampleRate: Int, frameSize: Int, mode: Int): Int

    private external fun nativeIsSpeech(audio: ShortArray): Boolean

    private external fun nativeStop()
}