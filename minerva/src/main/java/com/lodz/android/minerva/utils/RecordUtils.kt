package com.lodz.android.minerva.utils

import com.lodz.android.minerva.bean.AudioFormats
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * 录音工具类
 * @author zhouL
 * @date 2022/11/10
 */
object RecordUtils {

    /** 获取录音文件名 */
    fun getRecordFileName(formats: AudioFormats) =
        "record_${getCurrentFormatString("yyyyMMdd_HHmmss")}${formats.suffix}"

    /** 获取录音文件名 */
    fun getRecordTempFileName(formats: AudioFormats) =
        "record_${getCurrentFormatString("yyyyMMdd_HHmmss")}_temp${formats.suffix}"

    /** 格式化[formatType]当前时间 */
    fun getCurrentFormatString(formatType: String): String = getFormatString(formatType, Date(System.currentTimeMillis()))

    /** 格式化[formatType]当前时间 */
    fun getFormatString(formatType: String, date: Date?): String {
        if (date == null) {
            return ""
        }
        try {
            val format = SimpleDateFormat(formatType, Locale.CHINA)
            return format.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /** 获取16位宽的录音[data]音量（分贝） */
    fun getDbFor16Bit(data: ShortArray, end: Int): Double {
        val r = if (end < Short.MAX_VALUE) end else Short.MAX_VALUE.toInt()
        var sum = 0.0
        for (i in data.indices) {
            sum += data[i] * data[i]
        }
        val mean = abs(sum / r)
        return if (mean <= 0.0) 0.0 else 10 * log10(mean)
    }

    /** 将ByteArray转为ShortArray */
    fun bytesToShort(data: ByteArray): ShortArray {
        val shorts = ShortArray(data.size / 2)
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }
}