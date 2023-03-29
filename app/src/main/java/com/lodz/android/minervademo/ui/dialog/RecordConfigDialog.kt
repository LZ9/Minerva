package com.lodz.android.minervademo.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.View
import com.lodz.android.corekt.anko.toArrayList
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minervademo.bean.DictBean
import com.lodz.android.minervademo.databinding.DialogRecordConfigBinding
import com.lodz.android.minervademo.enums.Encodings
import com.lodz.android.minervademo.enums.SampleRates
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import com.lodz.android.pandora.widget.dialog.BaseBottomDialog

/**
 * 录音配置弹框
 * @author zhouL
 * @date 2021/10/21
 */
class RecordConfigDialog(context: Context) : BaseBottomDialog(context) {

    /** 音频格式 */
    private var mAudioFormat: AudioFormats = AudioFormats.WAV
    /** 采样率 */
    private var mSampleRate: SampleRates = SampleRates.SAMPLE_RATE_16K
    /** 位宽 */
    private var mEncoding: Encodings = Encodings.BIT_16

    /** 监听器 */
    private var mListener :OnClickConfirmListener? = null

    private val mBinding : DialogRecordConfigBinding by bindingLayout(DialogRecordConfigBinding::inflate)

    override fun getViewBindingLayout(): View = mBinding.root

    override fun findViews() {
        super.findViews()

        val audioFormatList = createAudioFormatList()
        mBinding.audioFormatCrg.setDataList(audioFormatList.toMutableList())
        mBinding.audioFormatCrg.setSelectedId(mAudioFormat.id.toString())

        val sampleRateList = createSampleRateList()
        mBinding.sampleRateCrg.setDataList(sampleRateList.toMutableList())
        mBinding.sampleRateCrg.setSelectedId(mSampleRate.rate.toString())

        val encodingList = createEncodingList()
        mBinding.encodingCrg.setDataList(encodingList.toMutableList())
        mBinding.encodingCrg.setSelectedId(mEncoding.encoding.toString())
    }

    override fun setListeners() {
        super.setListeners()

        mBinding.cancelBtn.setOnClickListener {
            dismiss()
        }

        mBinding.okBtn.setOnClickListener {
            mAudioFormat = getSelectedAudioFormat(mBinding.audioFormatCrg.getSelectedId()[0])
            mSampleRate = getSelectedSampleRate(mBinding.sampleRateCrg.getSelectedId()[0])
            mEncoding = getSelectedEncoding(mBinding.encodingCrg.getSelectedId()[0])
            mListener?.onClick(getDialogInterface(), mAudioFormat, mSampleRate, mEncoding)
        }
    }

    /** 设置数据[audioFormat]音频格式，[sampleRate]采样率，[encoding]音频位宽 */
    fun setData(audioFormat: AudioFormats, sampleRate: SampleRates, encoding: Encodings) {
        mAudioFormat = audioFormat
        mSampleRate = sampleRate
        mEncoding = encoding
    }

    /** 创建音频格式列表 */
    private fun createAudioFormatList(): ArrayList<DictBean> {
        val list = ArrayList<DictBean>()
        enumValues<AudioFormats>().toArrayList().forEach {
            list.add(DictBean(it.id, it.suffix))
        }
        return list
    }

    /** 根据[key]获取选中的音频格式 */
    private fun getSelectedAudioFormat(key: String): AudioFormats {
        enumValues<AudioFormats>().toArrayList().forEach {
            if (it.id.toString() == key) {
                return it
            }
        }
        return AudioFormats.WAV
    }

    /** 创建采样率列表 */
    private fun createSampleRateList(): ArrayList<DictBean> {
        val list = ArrayList<DictBean>()
        enumValues<SampleRates>().toArrayList().forEach {
            list.add(DictBean(it.rate, it.text))
        }
        return list
    }

    /** 根据[key]获取选中的采样率 */
    private fun getSelectedSampleRate(key: String): SampleRates {
        enumValues<SampleRates>().toArrayList().forEach {
            if (it.rate.toString() == key) {
                return it
            }
        }
        return SampleRates.SAMPLE_RATE_16K
    }

    /** 创建位宽列表 */
    private fun createEncodingList(): ArrayList<DictBean> {
        val list = ArrayList<DictBean>()
        enumValues<Encodings>().toArrayList().forEach {
            list.add(DictBean(it.encoding, it.text))
        }
        return list
    }

    /** 根据[key]获取选中的位宽 */
    private fun getSelectedEncoding(key: String): Encodings {
        enumValues<Encodings>().toArrayList().forEach {
            if (it.encoding.toString() == key) {
                return it
            }
        }
        return Encodings.BIT_16
    }

    /** 设置监听器[listener] */
    fun setOnClickConfirmListener(listener: OnClickConfirmListener?) {
        mListener = listener
    }

    fun interface OnClickConfirmListener {
        fun onClick(dif: DialogInterface, audioFormat: AudioFormats, sampleRate: SampleRates, encoding: Encodings)
    }
}