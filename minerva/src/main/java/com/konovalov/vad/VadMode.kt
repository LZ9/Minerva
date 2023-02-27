package com.konovalov.vad

/**
 * 端点检测模式
 * @author zhouL
 * @date 2023/2/14
 */
enum class VadMode(val value: Int) {
    NORMAL(0),
    LOW_BITRATE(1),
    AGGRESSIVE(2),
    VERY_AGGRESSIVE(3)
}