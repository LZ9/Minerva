package com.konovalov.vad

/**
 * 端点检测监听器
 * @author zhouL
 * @date 2023/2/13
 */
interface VadListener {

    /** 语音活动回调 */
    fun onSpeechDetected()

    /** 无活动回调 */
    fun onNoiseDetected()
}