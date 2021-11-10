package com.lodz.android.minerva.contract

/**
 * 录音数据流监听器
 * @author zhouL
 * @date 2021/11/9
 */
fun interface OnRecordingDataListener {

    /** 回调录音数据流[data] */
    fun onData(data:ByteArray)
}