package com.lodz.android.minerva.bean.states

/**
 * 端点检测状态
 * @author zhouL
 * @date 2022/11/10
 */
open class VadDetect(val data: ShortArray?, val end: Int, val db: Double, val isSpeech: Boolean) : RecordingStates()
