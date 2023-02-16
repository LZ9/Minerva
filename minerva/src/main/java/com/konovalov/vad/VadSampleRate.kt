package com.konovalov.vad

/**
 * 端点检测采样率
 * @author zhouL
 * @date 2023/2/14
 */
enum class VadSampleRate(val value: Int) {
    SAMPLE_RATE_8K(8000),
    SAMPLE_RATE_16K(16000),
    SAMPLE_RATE_32K(32000),
    SAMPLE_RATE_48K(48000)
}