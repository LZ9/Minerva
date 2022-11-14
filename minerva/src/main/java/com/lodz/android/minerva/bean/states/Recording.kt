package com.lodz.android.minerva.bean.states

/**
 * 录音状态 - 录音中
 * @author zhouL
 * @date 2022/11/10
 */
data class Recording(
    val db: Double, // 仅支持16Bit
    val data: ByteArray?
) : RecordingStates()
