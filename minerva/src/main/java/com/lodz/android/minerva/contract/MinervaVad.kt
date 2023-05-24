package com.lodz.android.minerva.contract

import com.konovalov.vad.VadConfig
import com.konovalov.vad.VadFrameSizeType
import com.konovalov.vad.VadMode
import com.lodz.android.minerva.modules.vad.VadSpeechInterceptor

/**
 * 端点检测控制器
 * @author zhouL
 * @date 2021/11/10
 */
interface MinervaVad : Minerva {

    /** 设置端点检测配置项[config] */
    fun setVadConfig(config: VadConfig)

    /** 改变帧大小类型[frameSizeType] */
    fun changeFrameSizeType(frameSizeType: VadFrameSizeType): Boolean

    /** 改变检测模式[mode] */
    fun changeVadMode(mode: VadMode): Boolean

    /** 改变是否保存语音[isSaveActiveVoice] */
    fun changeSaveActiveVoice(isSaveActiveVoice: Boolean): Boolean

    /** 设置端点检测话音判断拦截器[interceptor] */
    fun setVadInterceptor(interceptor : VadSpeechInterceptor?)
}