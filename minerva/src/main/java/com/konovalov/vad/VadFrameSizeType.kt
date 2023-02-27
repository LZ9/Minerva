package com.konovalov.vad

/**
 * 端点检测帧大小类型
 * @author zhouL
 * @date 2023/2/21
 */
enum class VadFrameSizeType(val value: Int) {
    SMALL(0),
    MIDDLE(1),
    BIG(2)
}