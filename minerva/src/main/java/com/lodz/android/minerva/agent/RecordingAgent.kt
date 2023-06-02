package com.lodz.android.minerva.agent

import android.content.Context
import android.media.AudioFormat
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.contract.Minerva
import com.lodz.android.minerva.contract.OnRecordingStatesListener
import com.lodz.android.minerva.modules.record.RecordingImpl
import java.io.File

/**
 * 录音代理器
 * @author zhouL
 * @date 2023/6/1
 */
class RecordingAgent {

    /** 采样率 */
    private var mSampleRate = 16000
    /** 声道 */
    private var mChannel = AudioFormat.CHANNEL_IN_MONO
    /** 位宽编码 */
    private var mEncoding = AudioFormat.ENCODING_PCM_16BIT
    /** 保存音频文件夹路径 */
    private var mSaveDirPath = ""
    /** 保存音频格式 */
    private var mRecordingFormat = AudioFormats.PCM
    /** 录音状态监听器 */
    private var mOnRecordingStateListener: OnRecordingStatesListener? = null

    /** 设置采样率[sampleRate]，
     * 一般常见采样率为：8000（低质量），16000（普通质量语音），32000（较高质量语音），44100（CD质量），48000（数字音频）
     * 默认16000
     * */
    fun setSampleRate(sampleRate: Int): RecordingAgent = this.apply {
        this.mSampleRate = sampleRate
    }

    /** 设置声道[channel]，默认为：AudioFormat.CHANNEL_IN_MONO */
    fun setChannel(channel: Int): RecordingAgent = this.apply {
        this.mChannel = channel
    }

    /** 设置位宽编码[encoding]，默认为：AudioFormat.ENCODING_PCM_16BIT */
    fun setEncoding(encoding: Int): RecordingAgent = this.apply {
        this.mEncoding = encoding
    }

    /** 设置音频存储路径[dirPath] */
    fun setSaveDirPath(dirPath: String): RecordingAgent = this.apply {
        this.mSaveDirPath = dirPath
    }

    /** 设置录音文件格式[format] */
    fun setAudioFormat(format: AudioFormats): RecordingAgent = this.apply {
        this.mRecordingFormat = format
    }

    /** 设置录音状态监听器[listener] */
    fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?) = this.apply {
        mOnRecordingStateListener = listener
    }

    /** 完成录音构建，上下文[context] */
    fun build(context: Context): Minerva {
        val audio = RecordingImpl()
        if (mSaveDirPath.isEmpty()) {
            throw IllegalArgumentException("save path is empty")
        }
        if (!mSaveDirPath.endsWith(File.separator)) {
            mSaveDirPath += File.separator
        }
        audio.init(context, mSampleRate, mChannel, mEncoding, mSaveDirPath, mRecordingFormat)
        audio.setOnRecordingStatesListener(mOnRecordingStateListener)
        return audio
    }
}