package com.lodz.android.minervademo.ui.vad

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Bundle
import android.view.View
import com.konovalov.vad.VadFrameSizeType
import com.konovalov.vad.VadMode
import com.konovalov.vad.VadSampleRate
import com.lodz.android.corekt.anko.getColorCompat
import com.lodz.android.corekt.anko.toastShort
import com.lodz.android.minerva.agent.MinervaAgent
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.contract.MinervaVad
import com.lodz.android.minerva.utils.RecordUtils
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.databinding.ActivityVadBinding
import com.lodz.android.minervademo.enums.AudioStatus
import com.lodz.android.minervademo.utils.FileManager
import com.lodz.android.pandora.base.activity.BaseRefreshActivity
import com.lodz.android.pandora.utils.viewbinding.bindingLayout

/**
 * 端点检测demo类
 * @author zhouL
 * @date 2023/2/9
 */
class VadActivity : BaseRefreshActivity() {

    companion object {
        fun start(context: Context){
            val intent = Intent(context, VadActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val mBinding: ActivityVadBinding by bindingLayout(ActivityVadBinding::inflate)

    private val mFilePath = FileManager.getVadFolderPath()

    /** 音频状态 */
    private var mStatus: AudioStatus = AudioStatus.IDLE
    /** 保存音频格式 */
    private var mAudioFormat: AudioFormats = AudioFormats.WAV
    /** 端点检测采样率 */
    private var mSampleRate: VadSampleRate = VadSampleRate.SAMPLE_RATE_16K
    /** 帧大小类型 */
    private var mFrameSizeType: VadFrameSizeType = VadFrameSizeType.SMALL
    /** 检测模式 */
    private var mVadMode: VadMode = VadMode.VERY_AGGRESSIVE
    /** 是否保存语音 */
    private var isSaveActiveVoice = false

    private var mMinerva: MinervaVad? = null

    override fun getViewBindingLayout(): View = mBinding.root

    override fun findViews(savedInstanceState: Bundle?) {
        super.findViews(savedInstanceState)
        getTitleBarLayout().setBackgroundColor(getColorCompat(R.color.color_00a1d5))
        getTitleBarLayout().setTitleName(R.string.main_vad)
    }

    override fun onDataRefresh() {
        mBinding.audioFileView.updateAudioFileList()
        setSwipeRefreshFinish()
    }

    override fun setListeners() {
        super.setListeners()

        mBinding.vadParamView.setOnParamChangedListener { audioFormat, sampleRate, frameSizeType, vadMode, isSaveActiveVoice ->
            mAudioFormat = audioFormat
            mMinerva?.changeAudioFormat(audioFormat)
            mSampleRate = sampleRate
            mMinerva?.changeSampleRate(sampleRate.value)
            mFrameSizeType = frameSizeType
            mMinerva?.changeFrameSizeType(frameSizeType)
            mVadMode = vadMode
            mMinerva?.changeVadMode(vadMode)
            this.isSaveActiveVoice = isSaveActiveVoice
            mMinerva?.changeSaveActiveVoice(isSaveActiveVoice)
        }

        mBinding.startBtn.setOnClickListener {
            mMinerva?.start()
        }

        mBinding.stopBtn.setOnClickListener {
            mMinerva?.stop()
        }
    }

    override fun onClickBackBtn() {
        finish()
    }

    override fun initData() {
        super.initData()
        initMinerva()
        mBinding.audioFileView.setFilePath(mFilePath)
        mBinding.audioFileView.updateAudioFileList()
        showStatusCompleted()
    }

    private fun initMinerva() {
        mMinerva = MinervaAgent.vad()
            .setChannel(AudioFormat.CHANNEL_IN_MONO)
            .setSampleRate(mSampleRate.value)
            .setAudioFormat(AudioFormats.WAV)
            .setSaveDirPath(FileManager.getVadFolderPath())
            .setSaveActivityVoice(isSaveActiveVoice)
            .setFrameSizeType(mFrameSizeType)
            .setVadMode(mVadMode)
            .setVadInterceptor { vad, buffer, end ->
                val db = RecordUtils.getDbFor16Bit(buffer, end)
                vad.isSpeech(buffer) && db > 40//端点检测识别成功且声音大于40分贝再响应
            }
            .setOnRecordingStatesListener{
                when (it) {
                    is Idle -> {
                        mStatus = AudioStatus.IDLE
                    }
                    is VadDetect -> {
                        mStatus = AudioStatus.VAD_DETECT
                        mBinding.vadParamView.setSoundSizeText("${it.db} db")
                        mBinding.vadParamView.setVadResultText(it.isSpeech.toString())
                    }
                    is Pause -> {
                        mStatus = AudioStatus.PAUSE
                    }
                    is Stop -> {
                        mStatus = AudioStatus.IDLE
                    }
                    is VadFileSave -> {
                        toastShort(it.file.absolutePath)
                        mBinding.audioFileView.updateAudioFileList()
                        mStatus = AudioStatus.IDLE
                    }
                    is Error -> {
                        mStatus = AudioStatus.IDLE
                        toastShort("${it.msg} , ${it.t}")
                    }
                    else -> {}
                }
                mBinding.vadParamView.setStatusText(mStatus)
                mBinding.startBtn.isEnabled = mStatus != AudioStatus.VAD_DETECT
                mBinding.audioFileView.setDeleteAllBtnEnabled(mStatus != AudioStatus.VAD_DETECT)
            }
            .build(getContext())
    }



}