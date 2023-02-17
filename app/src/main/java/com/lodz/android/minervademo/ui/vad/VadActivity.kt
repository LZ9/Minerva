package com.lodz.android.minervademo.ui.vad

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import com.lodz.android.corekt.anko.append
import com.lodz.android.corekt.anko.toastShort
import com.lodz.android.minerva.MinervaAgent
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.contract.Minerva
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.databinding.ActivityVadBinding
import com.lodz.android.minervademo.enums.AudioStatus
import com.lodz.android.minervademo.enums.Encodings
import com.lodz.android.minervademo.enums.SampleRates
import com.lodz.android.minervademo.utils.FileManager
import com.lodz.android.pandora.base.activity.BaseActivity
import com.lodz.android.pandora.utils.viewbinding.bindingLayout

/**
 * 端点检测demo类
 * @author zhouL
 * @date 2023/2/9
 */
class VadActivity : BaseActivity() {

    companion object {
        fun start(context: Context){
            val intent = Intent(context, VadActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val mBinding: ActivityVadBinding by bindingLayout(ActivityVadBinding::inflate)

    /** 音频状态 */
    private var mStatus = AudioStatus.IDLE

    private var mMinerva: Minerva? = null

    override fun getViewBindingLayout(): View = mBinding.root

    override fun findViews(savedInstanceState: Bundle?) {
        super.findViews(savedInstanceState)
    }

    override fun setListeners() {
        super.setListeners()
        mBinding.startBtn.setOnClickListener {
            mMinerva?.start()
        }

        mBinding.stopBtn.setOnClickListener {
            mMinerva?.stop()
        }
    }

    override fun initData() {
        super.initData()
        initMinerva()
        showStatusCompleted()
    }

    private fun initMinerva() {
        mMinerva = MinervaAgent.create()
            .setChannel(AudioFormat.CHANNEL_IN_MONO)
            .setSampleRate(SampleRates.SAMPLE_RATE_16K.rate)
            .setEncoding(Encodings.BIT_16.encoding)
            .setAudioFormat(AudioFormats.WAV)
            .setSaveDirPath(FileManager.getContentFolderPath())
            .setOnRecordingStatesListener{
                when (it) {
                    is Idle -> {
                        mStatus = AudioStatus.IDLE
                        Log.v("testtag", "空闲")
                    }
                    is VadDetect -> {
                        mStatus = AudioStatus.VAD_DETECT
                        Log.d("testtag", "端点检测中，是否有声音：${it.isSpeech}")
                        mBinding.speechTv.text = "端点检测中，是否有声音：${it.isSpeech}"
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
                        Log.v("testtag", "完成")
                    }
                    is Error -> {
                        mStatus = AudioStatus.IDLE
                        toastShort("${it.msg} , ${it.t}")
                        Log.e("testtag", "异常")
                    }
                    else -> {}
                }
                mBinding.statusTv.text = getString(R.string.simple_status).append(mStatus.text)
            }
            .buildVad(getContext(), true, MinervaAgent.MIDDLE)
    }



}