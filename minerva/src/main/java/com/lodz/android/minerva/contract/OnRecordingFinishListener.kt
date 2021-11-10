package com.lodz.android.minerva.contract

import java.io.File

/**
 * 录音结束监听器
 * @author zhouL
 * @date 2021/11/9
 */
fun interface OnRecordingFinishListener {

    /** 回调录音完成文件[file] */
    fun onFinish(file: File)
}