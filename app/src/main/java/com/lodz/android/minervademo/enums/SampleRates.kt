package com.lodz.android.minervademo.enums

/**
 * 采样率
 * @author zhouL
 * @date 2023/2/14
 */
enum class SampleRates(val rate: Int, val text: String) {
    SAMPLE_RATE_8K(8000, "8000Hz"),
    SAMPLE_RATE_16K(16000, "16000Hz"),
    SAMPLE_RATE_44K1(44100, "44100Hz")
}