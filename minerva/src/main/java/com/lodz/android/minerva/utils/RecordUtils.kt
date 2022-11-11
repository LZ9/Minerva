package com.lodz.android.minerva.utils

import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.fftlib.ByteUtils
import com.lodz.android.minerva.fftlib.FFT
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * 录音工具类
 * @author zhouL
 * @date 2022/11/10
 */
object RecordUtils {

    /** 获取录音文件名 */
    fun getRecordFileName(formats: AudioFormats) =
        "record_${getCurrentFormatString("yyyyMMdd_HHmmss")}${formats.suffix}"

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

    /** 获取录音[data]音量（分贝） */
    fun getDb(data: ByteArray): Int {
        var sum = 0.0
        var ave = 0.0
        val length = min(data.size, 128)
        val offsetStart = 0
        for (i in offsetStart until length) {
            sum += data[i] * data[i]
        }
        ave = sum / (length - offsetStart)
        return (Math.log10(ave) * 20).toInt()
    }

    /** 获取傅里叶转换后的录音数据流 */
    fun getFFT(data: ByteArray): ByteArray {
        val doubles = ByteUtil.toHardDouble(ByteUtil.toShorts(data))
        val fft = FFT.fft(doubles, 0)
        return ByteUtil.toSoftBytes(fft)
    }
}