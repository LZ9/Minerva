package com.lodz.android.minerva.modules

import android.content.Context
import android.media.AudioFormat
import com.konovalov.vad.VadConfig
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.Error
import com.lodz.android.minerva.bean.states.Idle
import com.lodz.android.minerva.bean.states.RecordingStates
import com.lodz.android.minerva.contract.Minerva
import com.lodz.android.minerva.contract.OnRecordingStatesListener
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * 录音功能基础实现
 * @author zhouL
 * @date 2023/2/9
 */
abstract class BaseMinervaImpl : Minerva {

    companion object {
        const val TAG = "MinervaTag"
    }

    /** 上下文 */
    protected lateinit var mContext: Context

    /** 采样率 */
    protected var mSampleRate = 16000
    /** 声道 */
    protected var mChannel = AudioFormat.CHANNEL_IN_MONO
    /** 位宽编码 */
    protected var mEncoding = AudioFormat.ENCODING_PCM_16BIT
    /** 保存音频文件夹路径 */
    protected var mSaveDirPath = ""
    /** 保存音频格式 */
    protected var mRecordingFormat = AudioFormats.PCM

    /** 录音状态监听器 */
    protected var mOnRecordingStatesListener: OnRecordingStatesListener? = null

    /** 当前录音状态 */
    protected var mRecordingState: RecordingStates = Idle

    override fun init(
        context: Context,
        sampleRate: Int,
        channel: Int,
        encoding: Int,
        dirPath: String,
        format: AudioFormats,
    ) {
        mContext = context
        mSampleRate = sampleRate
        mChannel = channel
        mEncoding = encoding
        mSaveDirPath = dirPath
        mRecordingFormat = format
    }

    override fun changeSampleRate(sampleRate: Int): Boolean {
        if (checkChangeParam()){
            mSampleRate = sampleRate
            return true
        }
        return false
    }

    override fun changeEncoding(encoding: Int): Boolean {
        if (checkChangeParam()) {
            mEncoding = encoding
            return true
        }
        return false
    }

    override fun changeAudioFormat(format: AudioFormats): Boolean {
        if (checkChangeParam()){
            mRecordingFormat = format
            return true
        }
        return false
    }

    override fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?) {
        mOnRecordingStatesListener = listener
    }

    override fun getRecordingState(): RecordingStates = mRecordingState

    protected fun notifyStates(state: RecordingStates) {
        MainScope().launch { mOnRecordingStatesListener?.onStateChange(state) }
    }

    /** 校验保存目录是否创建 */
    protected fun checkSaveDirPath() {
        val file = File(mSaveDirPath)
        if (!file.isDirectory){
            file.mkdirs()
        }
    }

    /** 校验是否允许更改参数 */
    protected fun checkChangeParam(): Boolean {
        if (mRecordingState != Idle) {
            notifyStates(Error(RuntimeException(), "audio is working , you cannot change"))
            return false
        }
        return true
    }

    /** 转换声道参数 */
    protected fun getChannel(): Short = when (mChannel) {
        AudioFormat.CHANNEL_IN_MONO -> 1
        AudioFormat.CHANNEL_IN_STEREO -> 2
        else -> 0
    }

    /** 转换位宽编码参数 */
    protected fun getEncoding(): Short = when (mEncoding) {
        AudioFormat.ENCODING_PCM_8BIT -> 8
        AudioFormat.ENCODING_PCM_16BIT -> 16
        else -> 0
    }

    /** 转换位宽编码参数 */
    fun renameFile(file: File?, newName: String): File? {
        val replaceName = newName.replace(" ", "", true)//去掉新名称中的空格
        if (file == null || !file.exists()) {//文件为空或者文件不存在
            return null
        }
        if (newName == file.name) {// 新名称与旧名称一致
            return null
        }
        val parentPath = file.parent ?: return null
        val newFile = File(parentPath + File.separator + replaceName)
        return if (file.renameTo(newFile)) newFile else null
    }
}