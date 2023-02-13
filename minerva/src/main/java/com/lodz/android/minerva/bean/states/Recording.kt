package com.lodz.android.minerva.bean.states

/**
 * 录音状态 - 录音中
 * @author zhouL
 * @date 2022/11/10
 */
open class Recording(val data: ShortArray?, val end: Int) : RecordingStates()
