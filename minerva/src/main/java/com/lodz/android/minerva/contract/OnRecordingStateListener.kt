package com.lodz.android.minerva.contract

import com.lodz.android.minerva.recorder.RecordingState

/**
 * 录音状态监听器
 * @author zhouL
 * @date 2021/11/9
 */
interface OnRecordingStateListener {

    /** 回调录音状态[state]变化 */
    fun onStateChange(state: RecordingState)

    /** 回调录音错误[error] */
    fun onError(error: String)
}