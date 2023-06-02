package com.lodz.android.minerva.agent

/**
 * 录音控制代理
 * @author zhouL
 * @date 2021/11/10
 */
object MinervaAgent {

    /** 录音 */
    fun recording() = RecordingAgent()

    /** 端点检测 */
    fun vad() = VadAgent()
}