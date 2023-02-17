package com.lodz.android.minervademo.enums

/**
 * 录音状态
 * @author zhouL
 * @date 2023/2/15
 */
enum class AudioStatus(val text: String) {

    IDLE("空闲中"),

    RECORDING("录音中"),

    PAUSE("暂停中"),

    VAD_DETECT("端点检测中"),
}