package com.lodz.android.minerva.contract

/**
 * 傅里叶转换后的录音数据流监听器
 * @author zhouL
 * @date 2021/11/9
 */
fun interface OnRecordingFftDataListener {

    /** 回调傅里叶转换后的录音数据流[data] */
    fun onFftData(data: ByteArray)
}