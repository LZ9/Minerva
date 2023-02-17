package com.lodz.android.minervademo.ui.simple

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import com.lodz.android.corekt.anko.*
import com.lodz.android.minerva.MinervaAgent
import com.lodz.android.minerva.contract.Minerva
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.utils.RecordUtils
import com.lodz.android.minervademo.utils.FileManager
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.databinding.ActivitySimpleBinding
import com.lodz.android.minervademo.enums.AudioStatus
import com.lodz.android.minervademo.enums.Encodings
import com.lodz.android.minervademo.enums.SampleRates
import com.lodz.android.pandora.base.activity.BaseRefreshActivity
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import java.util.*

/**
 * 简单实现
 * @author zhouL
 * @date 2021/10/26
 */
@SuppressLint("NotifyDataSetChanged")
class SimpleActivity : BaseRefreshActivity() {

    companion object {
        fun start(context: Context){
            val intent = Intent(context, SimpleActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val mBinding: ActivitySimpleBinding by bindingLayout(ActivitySimpleBinding::inflate)

    override fun getViewBindingLayout(): View = mBinding.root

    /** 状态 */
    private var mStatus: AudioStatus = AudioStatus.IDLE
    /** 音频格式 */
    private var mAudioFormat: AudioFormats = AudioFormats.WAV
    /** 采样率 */
    private var mSampleRate: SampleRates = SampleRates.SAMPLE_RATE_16K
    /** 音频位宽 */
    private var mEncoding: Encodings = Encodings.BIT_16

    private var mMinerva: Minerva? = null

    override fun findViews(savedInstanceState: Bundle?) {
        super.findViews(savedInstanceState)
        setTitleBar()
        mBinding.startBtn.isEnabled = true
        mBinding.pauseBtn.isEnabled = false
    }

    private fun setTitleBar() {
        getTitleBarLayout().setBackgroundColor(getColorCompat(R.color.color_00a1d5))
        getTitleBarLayout().setTitleName(R.string.main_simple)
    }

    override fun onDataRefresh() {
        mBinding.audioFileView.updateAudioFileList()
        setSwipeRefreshFinish()
    }

    override fun setListeners() {
        super.setListeners()

        getTitleBarLayout().setOnBackBtnClickListener {
            finish()
        }

        mBinding.paramView.setOnParamChangedListener { audioFormat, sampleRate, encoding ->
            mAudioFormat = audioFormat
            mMinerva?.changeAudioFormat(audioFormat)
            mSampleRate = sampleRate
            mMinerva?.changeSampleRate(sampleRate.rate)
            mEncoding = encoding
            mMinerva?.changeEncoding(encoding.encoding)
        }

        mBinding.startBtn.setOnClickListener {
            if (mStatus == AudioStatus.PAUSE) {
                mMinerva?.resume()
            } else {
                mMinerva?.start()
            }
        }

        mBinding.stopBtn.setOnClickListener {
            mMinerva?.stop()
        }

        mBinding.pauseBtn.setOnClickListener {
            mMinerva?.pause()
        }
    }


    override fun initData() {
        super.initData()
        mBinding.audioFileView.updateAudioFileList()
        initMinerva()
        showStatusCompleted()
    }

    private fun initMinerva() {
        mMinerva = MinervaAgent.create()
            .setChannel(AudioFormat.CHANNEL_IN_MONO)
            .setSampleRate(mSampleRate.rate)
            .setEncoding(mEncoding.encoding)
            .setAudioFormat(mAudioFormat)
            .setSaveDirPath(FileManager.getContentFolderPath())
            .setOnRecordingStatesListener{
                when (it) {
                    is Idle -> {
                        mStatus = AudioStatus.IDLE
                        Log.v("testtag", "空闲")
                    }
                    is Recording -> {
                        mStatus = AudioStatus.RECORDING
                        val tips = if (mEncoding == Encodings.BIT_16) {
                            val data = it.data
                            val db = if (data == null) 0 else RecordUtils.getDbFor16Bit(data, it.end)
                            "$db db"
                        } else {
                            "非16bit位宽，暂无解析"
                        }
                        mBinding.paramView.setSoundSizeText(tips)
                        Log.d("testtag", "录音中")
                    }
                    is Pause -> {
                        mStatus = AudioStatus.PAUSE
                        Log.i("testtag", "暂停")
                    }
                    is Stop -> {
                        mStatus = AudioStatus.IDLE
                        Log.i("testtag", "停止")
                    }
                    is Finish -> {
                        mStatus = AudioStatus.IDLE
                        toastShort(it.file.absolutePath)
                        mBinding.audioFileView.updateAudioFileList()
                        Log.v("testtag", "完成")
                    }
                    is Error -> {
                        mStatus = AudioStatus.IDLE
                        toastShort("${it.msg} , ${it.t}")
                        Log.e("testtag", "异常")
                    }
                    else -> {}
                }
                mBinding.paramView.setStatusText(mStatus.text)
                mBinding.startBtn.isEnabled = mStatus != AudioStatus.RECORDING
                mBinding.audioFileView.setDeleteAllBtnEnabled(mStatus != AudioStatus.RECORDING)
                mBinding.pauseBtn.isEnabled = mStatus == AudioStatus.RECORDING
            }
            .buildRecording(getContext())
    }

}