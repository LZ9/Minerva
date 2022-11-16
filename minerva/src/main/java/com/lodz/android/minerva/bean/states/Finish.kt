package com.lodz.android.minerva.bean.states

import java.io.File

/**
 * 录音状态 - 录音流程结束（或转换结束）
 * @author zhouL
 * @date 2022/11/10
 */
class Finish(val file: File) : RecordingStates()
