package com.lodz.android.minerva.recorder

/**
 * 录音状态
 * @author zhouL
 * @date 2021/11/9
 */
enum class RecordingState {

    /** 空闲状态 */
    IDLE,

    /** 录音中 */
    RECORDING,

    /** 暂停中 */
    PAUSE,

    /** 正在停止 */
    STOP,

    /** 录音流程结束（转换结束） */
    FINISH,
}