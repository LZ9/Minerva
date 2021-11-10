package com.lodz.android.minerva.contract

import android.content.Context
import com.lodz.android.minerva.recorder.RecordingFormat
import com.lodz.android.minerva.recorder.RecordingState

/**
 * 录音控制
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
        format: RecordingFormat
    )

    /** 启动 */
    fun start()

    /** 停止 */
    fun stop()

    /** 暂停 */
    fun pause()

    /** 设置录音状态监听器[listener] */
    fun setOnRecordingStateListener(listener: OnRecordingStateListener?)

    /** 设置录音数据流监听器[listener] */
    fun setOnRecordingDataListener(listener: OnRecordingDataListener?)

    /** 设置傅里叶转换后的录音数据流监听器[listener] */
    fun setOnRecordingFftDataListener(listener: OnRecordingFftDataListener?)

    /** 设置录音结束监听器[listener] */
    fun setOnRecordingFinishListener(listener: OnRecordingFinishListener?)

    /** 设置录音音量大小监听器[listener] */
    fun setOnRecordingSoundSizeListener(listener: OnRecordingSoundSizeListener?)

    /** 获取当前录音状态 */
    fun getRecordingState(): RecordingState

}