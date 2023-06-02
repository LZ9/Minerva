package com.lodz.android.minerva.modules.vad

import com.konovalov.vad.Vad

/**
 * 只根据Vad返回值做判断
 * @author zhouL
 * @date 2023/4/19
 */
class VadOnlyInterceptor : VadSpeechInterceptor {
    override fun isSpeech(vad: Vad, buffer: ShortArray, end: Int, db: Double): Boolean = vad.isSpeech(buffer)
}