package com.lodz.android.minerva.bean.states

/**
 * 录音状态 - 发生异常
 * @author zhouL
 * @date 2022/11/10
 */
open class Error(val t: Throwable, val msg: String) : RecordingStates()
