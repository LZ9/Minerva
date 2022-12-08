package com.lodz.android.minerva.utils

import kotlin.experimental.and
import kotlin.experimental.or

/**
 * Byte工具类
 * @author zhouL
 * @date 2022/11/10
 */
object ByteUtil {

    /** byte数组转short数组 */
    fun toShorts(src: ByteArray): ShortArray {
        val count = src.size shr 1
        val dest = ShortArray(count)
        for (i in 0 until count) {
            dest[i] = ((src[i *2] and  0xff.toByte()) or ((src[2*i + 1] and 0xff.toByte()).toInt() shl 8).toByte()).toShort()
        }
        return dest
    }

    /** short数组转byte数组 */
    fun toBytes(src: ShortArray): ByteArray {
        val count = src.size
        val dest = ByteArray(count shl 1)
        for (i in 0 until count) {
            dest[i * 2] = src[i].toByte()
            dest[i * 2 + 1] = (src[i].toInt() shr 8).toByte()
        }

        return dest
    }

    /** short数组转double数组 */
    fun toHardDouble(shorts: ShortArray): DoubleArray {
        val length = 512
        val array = DoubleArray(length)
        for (i in 0 until length) {
            array[i] = shorts[i].toDouble()
        }
        return array
    }

    fun toSoftBytes(doubles: DoubleArray): ByteArray {
        val max = getMax(doubles)
        var sc = 1.0
        if (max > 127) {
            sc = max / 128
        }
        val bytes = ByteArray(doubles.size)
        for (i in doubles.indices) {
            val item = doubles[i] / sc
            bytes[i] = (if (item > 127) 127 else item).toByte()
        }
        return bytes
    }

    fun getMax(data: DoubleArray): Double {
        var max = 0.0
        for (i in data.indices) {
            if (data[i] > max) {
                max = data[i]
            }
        }
        return max
    }
}