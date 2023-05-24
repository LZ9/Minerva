package com.lodz.android.minerva.modules.vad

import com.konovalov.vad.Vad

/**
 * 端点检测话音判断拦截器
 * @author zhouL
 * @date 2023/4/19
 */
interface VadSpeechInterceptor {
    /** 补充定义判断规则 */
    fun isSpeech(vad: Vad, buffer: ShortArray): Boolean
}