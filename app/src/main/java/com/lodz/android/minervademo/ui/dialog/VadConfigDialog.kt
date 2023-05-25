package com.lodz.android.minervademo.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.media.AudioFormat
import android.view.View
import com.konovalov.vad.VadFrameSizeType
import com.konovalov.vad.VadMode
import com.konovalov.vad.VadSampleRate
import com.lodz.android.corekt.anko.append
import com.lodz.android.corekt.anko.toArrayList
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.bean.DictBean
import com.lodz.android.minervademo.databinding.DialogVadConfigBinding
import com.lodz.android.minervademo.enums.Encodings
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import com.lodz.android.pandora.widget.dialog.BaseBottomDialog

/**
 * 端点检测配置弹框
 * @author zhouL
 * @date 2021/10/21
 */
class VadConfigDialog(context: Context) : BaseBottomDialog(context) {

    /** 音频格式 */
    private var mAudioFormat: AudioFormats = AudioFormats.WAV
    /** 采样率 */
    private var mSampleRate: VadSampleRate = VadSampleRate.SAMPLE_RATE_16K
    /** 位宽 */
    private var mEncoding: Encodings = Encodings.BIT_16
    /** 帧大小类型 */
    private var mFrameSizeType: VadFrameSizeType = VadFrameSizeType.SMALL
    /** 检测模式 */
    private var mVadMode: VadMode = VadMode.VERY_AGGRESSIVE
    /** 是否保存语音 */
    private var isSaveActiveVoice = false

    /** 监听器 */
    private var mListener :OnClickConfirmListener? = null

    private val mBinding : DialogVadConfigBinding by bindingLayout(DialogVadConfigBinding::inflate)

    override fun getViewBindingLayout(): View = mBinding.root

    override fun findViews() {
        super.findViews()

        val audioFormatList = createAudioFormatList()
        mBinding.audioFormatCrg.setDataList(audioFormatList.toMutableList())
        mBinding.audioFormatCrg.setSelectedId(mAudioFormat.id.toString())

        val sampleRateList = createSampleRateList()
        mBinding.sampleRateCrg.setDataList(sampleRateList.toMutableList())
        mBinding.sampleRateCrg.setSelectedId(mSampleRate.value.toString())

        val encodingList = createEncodingList()
        mBinding.encodingCrg.setDataList(encodingList.toMutableList())
        mBinding.encodingCrg.setSelectedId(mEncoding.encoding.toString())

        val frameSizeTypeList = createFrameSizeTypeList()
        mBinding.frameSizeCrg.setDataList(frameSizeTypeList.toMutableList())
        mBinding.frameSizeCrg.setSelectedId(mFrameSizeType.value.toString())

        val vadModeList = createVadModeList()
        mBinding.modeCrg.setDataList(vadModeList.toMutableList())
        mBinding.modeCrg.setSelectedId(mVadMode.value.toString())

        val saveAudioList = createSaveAudioList()
        mBinding.saveAudioCrg.setDataList(saveAudioList.toMutableList())
        mBinding.saveAudioCrg.setSelectedId(toInt(isSaveActiveVoice).toString())
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
            mFrameSizeType = getSelectedFrameSizeType(mBinding.frameSizeCrg.getSelectedId()[0])
            mVadMode = getSelectedVadMode(mBinding.modeCrg.getSelectedId()[0])
            isSaveActiveVoice = toBoolean(mBinding.saveAudioCrg.getSelectedId()[0].toInt())
            mListener?.onClick(getDialogInterface(), mAudioFormat, mSampleRate, mEncoding, mFrameSizeType, mVadMode, isSaveActiveVoice)
        }
    }

    /** 设置数据[audioFormat]音频格式，[sampleRate]采样率，[encoding]音频位宽，[fameSizeType]帧大小类型，[vadMode]检测模式，[isSaveActiveVoice]是否保存语音 */
    fun setData(
        audioFormat: AudioFormats,
        sampleRate: VadSampleRate,
        encoding: Encodings,
        fameSizeType: VadFrameSizeType,
        vadMode: VadMode,
        isSaveActiveVoice: Boolean
    ) {
        mAudioFormat = audioFormat
        mSampleRate = sampleRate
        mEncoding = encoding
        mFrameSizeType = fameSizeType
        mVadMode = vadMode
        this.isSaveActiveVoice = isSaveActiveVoice
    }

    /** 创建音频格式列表 */
    private fun createAudioFormatList(): ArrayList<DictBean> {
        val list = ArrayList<DictBean>()
        enumValues<AudioFormats>().toArrayList()
            .filter {
                it != AudioFormats.MP3
            }.forEach {
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
        enumValues<VadSampleRate>().toArrayList().forEach {
            list.add(DictBean(it.value, it.value.toString().append("Hz")))
        }
        return list
    }

    /** 根据[key]获取选中的采样率 */
    private fun getSelectedSampleRate(key: String): VadSampleRate {
        enumValues<VadSampleRate>().toArrayList().forEach {
            if (it.value.toString() == key) {
                return it
            }
        }
        return VadSampleRate.SAMPLE_RATE_16K
    }

    /** 创建位宽列表 */
    private fun createEncodingList(): ArrayList<DictBean> {
        val list = ArrayList<DictBean>()
        list.add(DictBean(AudioFormat.ENCODING_PCM_16BIT, "16 bit"))
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

    /** 创建帧大小类型列表 */
    private fun createFrameSizeTypeList(): ArrayList<DictBean> {
        val list = ArrayList<DictBean>()
        enumValues<VadFrameSizeType>().toArrayList().forEach {
            list.add(DictBean(it.value, it.name))
        }
        return list
    }

    /** 根据[key]获取选中的帧大小类型 */
    private fun getSelectedFrameSizeType(key: String): VadFrameSizeType {
        enumValues<VadFrameSizeType>().toArrayList().forEach {
            if (it.value.toString() == key) {
                return it
            }
        }
        return VadFrameSizeType.SMALL
    }

    /** 创建检测模式列表 */
    private fun createVadModeList(): ArrayList<DictBean> {
        val list = ArrayList<DictBean>()
        enumValues<VadMode>().toArrayList().forEach {
            list.add(DictBean(it.value, it.name))
        }
        return list
    }

    /** 根据[key]获取选中的检测模式 */
    private fun getSelectedVadMode(key: String): VadMode {
        enumValues<VadMode>().toArrayList().forEach {
            if (it.value.toString() == key) {
                return it
            }
        }
        return VadMode.VERY_AGGRESSIVE
    }

    /** 创建检测模式列表 */
    private fun createSaveAudioList(): ArrayList<DictBean> {
        val list = ArrayList<DictBean>()
        list.add(DictBean(0, context.getString(R.string.vad_no)))
        list.add(DictBean(1, context.getString(R.string.vad_yes)))
        return list
    }


    private fun toInt(b: Boolean): Int = if (b) 1 else 0

    private fun toBoolean(i: Int): Boolean = i == 1

    /** 设置监听器[listener] */
    fun setOnClickConfirmListener(listener: OnClickConfirmListener?) {
        mListener = listener
    }

    fun interface OnClickConfirmListener {
        fun onClick(
            dif: DialogInterface,
            audioFormat: AudioFormats,
            sampleRate: VadSampleRate,
            encoding: Encodings,
            frameSizeType: VadFrameSizeType,
            vadMode: VadMode,
            isSaveActiveVoice: Boolean
        )
    }
}