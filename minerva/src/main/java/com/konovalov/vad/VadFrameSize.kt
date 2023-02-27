package com.konovalov.vad

/**
 * 端点检测帧大小
 * @author zhouL
 * @date 2023/2/14
 */
enum class VadFrameSize(val value: Int) {
    FRAME_SIZE_80(80),
    FRAME_SIZE_160(160),
    FRAME_SIZE_240(240),
    FRAME_SIZE_320(320),
    FRAME_SIZE_480(480),
    FRAME_SIZE_640(640),
    FRAME_SIZE_960(960),
    FRAME_SIZE_1440(1440)
}