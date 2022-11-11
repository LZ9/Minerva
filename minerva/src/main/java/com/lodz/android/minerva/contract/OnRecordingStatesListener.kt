package com.lodz.android.minerva.contract

import com.lodz.android.minerva.bean.states.RecordingStates

/**
 * 录音状态监听器
 * @author zhouL
 * @date 2021/11/9
 */
fun interface OnRecordingStatesListener {
    /** 回调录音状态[state]变化 */
    fun onStateChange(state: RecordingStates)
}