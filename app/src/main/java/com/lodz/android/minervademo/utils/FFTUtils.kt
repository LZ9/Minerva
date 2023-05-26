package com.lodz.android.minervademo.utils

import org.jtransforms.fft.DoubleFFT_1D

/**
 * 傅里叶变换工具类
 * @author zhouL
 * @date 2023/5/26
 */
object FFTUtils {

    // 音频数据进行傅里叶变化
    fun fft(data:ShortArray):DoubleArray{
        val sample = ShortArray(data.size)
        System.arraycopy(data, 0, sample, 0, data.size)
        val fft = DoubleFFT_1D(sample.size.toLong())
        val doubleArray = sample.map { it.toDouble() }.toDoubleArray()
        fft.realForward(doubleArray)
        return doubleArray
    }


}