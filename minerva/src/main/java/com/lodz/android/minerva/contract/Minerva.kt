package com.lodz.android.minerva.contract

import android.content.Context
import com.konovalov.vad.VadConfig
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.RecordingStates

/**
 * 录音控制器
 * @author zhouL
 * @date 2021/11/10
 */
interface Minerva {

    /** 初始化上下文[context]，采样率[sampleRate]，声道[channel]，位宽编码[encoding]，保存路径[dirPath]，音频格式[format] */
    fun init(
        context: Context,
        sampleRate: Int,
        channel: Int,
        encoding: Int,
        dirPath: String,
        format: AudioFormats,
    )

    /** 改变采样率[sampleRate] */
    fun changeSampleRate(sampleRate: Int): Boolean

    /** 改变位宽编码[encoding] */
    fun changeEncoding(encoding: Int): Boolean

    /** 改变音频格式[format] */
    fun changeAudioFormat(format: AudioFormats): Boolean

    /** 启动 */
    fun start()

    /** 停止 */
    fun stop()

    /** 暂停 */
    fun pause()

    /** 设置录音状态监听器[listener] */
    fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?)

    /** 获取当前录音状态 */
    fun getRecordingState(): RecordingStates

}