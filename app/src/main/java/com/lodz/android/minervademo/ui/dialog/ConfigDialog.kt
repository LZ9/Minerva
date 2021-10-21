package com.lodz.android.minervademo.ui.dialog

import android.content.Context
import android.view.View
import com.lodz.android.corekt.anko.then
import com.lodz.android.minervademo.config.Constant
import com.lodz.android.minervademo.databinding.DialogConfigBinding
import com.lodz.android.minervademo.utils.DictManager
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import com.lodz.android.pandora.widget.dialog.BaseBottomDialog

/**
 * 配置弹框
 * @author zhouL
 * @date 2021/10/21
 */
class ConfigDialog(context: Context) : BaseBottomDialog(context) {

    /** 音频格式 */
    private var mAudioFormat = Constant.AUDIO_FORMAT_WAV
    /** 采样率 */
    private var mSampleRate = Constant.SAMPLE_RATE_16000
    /** 音频位宽 */
    private var mEncoding = Constant.ENCODING_16_BIT

    private val mBinding : DialogConfigBinding by bindingLayout(DialogConfigBinding::inflate)

    override fun getViewBindingLayout(): View = mBinding.root

    override fun findViews() {
        super.findViews()
        val audioFormatList = DictManager.get().getDictListBean(Constant.DICT_AUDIO_FORMAT)
        if (audioFormatList != null){
            mBinding.audioFormatCrg.setDataList(audioFormatList.list.toMutableList())
            mBinding.audioFormatCrg.setSelectedId(mAudioFormat.toString())
        }
        mBinding.audioFormatCrg.visibility = (audioFormatList != null).then { View.VISIBLE } ?: View.GONE

        val sampleRateList = DictManager.get().getDictListBean(Constant.DICT_SAMPLE_RATE)
        if (sampleRateList != null){
            mBinding.sampleRateCrg.setDataList(sampleRateList.list.toMutableList())
            mBinding.sampleRateCrg.setSelectedId(mSampleRate.toString())
        }
        mBinding.sampleRateCrg.visibility = (audioFormatList != null).then { View.VISIBLE } ?: View.GONE

        val encodingList = DictManager.get().getDictListBean(Constant.DICT_ENCODING)
        if (encodingList != null){
            mBinding.encodingCrg.setDataList(encodingList.list.toMutableList())
            mBinding.encodingCrg.setSelectedId(mEncoding.toString())
        }
        mBinding.encodingCrg.visibility = (audioFormatList != null).then { View.VISIBLE } ?: View.GONE
    }

    override fun setListeners() {
        super.setListeners()

        mBinding.cancelBtn.setOnClickListener {
            dismiss()
        }

        mBinding.okBtn.setOnClickListener {

        }
    }

    fun setData(audioFormat: Int, sampleRate: Int, encoding: Int) {
        mAudioFormat = audioFormat
        mSampleRate = sampleRate
        mEncoding = encoding
    }

}