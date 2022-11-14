package com.lodz.android.minerva.utils

import android.util.Log
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.fftlib.FFT
import com.lodz.android.minerva.modules.RecordingImpl
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10

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

    /** 获取16位宽的录音[data]音量（分贝） */
    fun getDbFor16Bit(data: ByteArray, end: Int): Double {
        val r = if (end < 128) 128 else end
        var sum = 0.0
        val buffer = bytesToShort(data)
        for (i in buffer.indices) {
            sum += buffer[i] * buffer[i]
        }
        val mean = sum / r
        return if (mean == 0.0) 0.0 else 10 * log10(mean)
    }

    /** 将ByteArray转为ShortArray */
    private fun bytesToShort(data: ByteArray): ShortArray {
        val shorts = ShortArray(data.size / 2)
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }

    /** 获取傅里叶转换后的录音数据流 */
    fun getFFT(data: ByteArray): ByteArray {
        val doubles = ByteUtil.toHardDouble(ByteUtil.toShorts(data))
        val fft = FFT.fft(doubles, 0)
        return ByteUtil.toSoftBytes(fft)
    }
}