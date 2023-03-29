package com.lodz.android.minerva.utils

import com.konovalov.vad.Vad
import com.konovalov.vad.VadFrameSize
import com.konovalov.vad.VadSampleRate

/**
 * 端点检测工具类
 * @author zhouL
 * @date 2023/3/1
 */
object VadUtils {

    /** 获取端点检测的采样率参数 */
    fun getVadSampleRate(sampleRate: Int): VadSampleRate =
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
    fun getVadFrameSize(sampleRate: VadSampleRate, frameSizeType: Int): VadFrameSize =
        Vad.getValidFrameSize(sampleRate)[frameSizeType]
}