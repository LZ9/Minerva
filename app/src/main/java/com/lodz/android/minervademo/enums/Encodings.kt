package com.lodz.android.minervademo.enums

import android.media.AudioFormat

/**
 * 位宽
 * @author zhouL
 * @date 2023/2/16
 */
enum class Encodings(val encoding: Int, val text: String) {

    BIT_8(AudioFormat.ENCODING_PCM_8BIT, "8 bit"),

    BIT_16(AudioFormat.ENCODING_PCM_16BIT, "16 bit")
}