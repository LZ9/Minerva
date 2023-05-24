package com.lodz.android.minerva.bean.states

import java.io.File

/**
 * 端点检测语音文件保存
 * @author zhouL
 * @date 2022/11/10
 */
open class VadFileSave(val file: File) : RecordingStates()
