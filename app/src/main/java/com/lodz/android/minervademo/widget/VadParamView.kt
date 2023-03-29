package com.lodz.android.minervademo.widget

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.widget.FrameLayout
import com.konovalov.vad.VadFrameSizeType
import com.konovalov.vad.VadMode
import com.konovalov.vad.VadSampleRate
import com.lodz.android.corekt.anko.append
import com.lodz.android.corekt.anko.toastShort
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.databinding.ViewVadParamBinding
import com.lodz.android.minervademo.enums.AudioStatus
import com.lodz.android.minervademo.enums.Encodings
import com.lodz.android.minervademo.enums.SampleRates
import com.lodz.android.minervademo.ui.dialog.RecordConfigDialog
import com.lodz.android.minervademo.ui.dialog.VadConfigDialog
import com.lodz.android.pandora.utils.viewbinding.bindingLayout

/**
 * 参数配置页面
 * @author zhouL
 * @date 2023/2/15
 */
class VadParamView :FrameLayout{

    private val mBinding: ViewVadParamBinding by context.bindingLayout(ViewVadParamBinding::inflate)

    /** 音频状态 */
    private var mStatus: AudioStatus = AudioStatus.IDLE
    /** 保存音频格式 */
    private var mAudioFormat: AudioFormats = AudioFormats.WAV
    /** 端点检测采样率 */
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
    private var mListener: OnParamChangedListener? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        addView(mBinding.root)
        findViews()
        setListeners()
    }

    private fun findViews() {
        setStatusText(mStatus)
        setSoundSizeText("")
        setVadResultText("")
        setSaveActiveVoiceText(isSaveActiveVoice.toString())
        setAudioFormatText(mAudioFormat.suffix)
        setSampleRateText(mSampleRate.value.toString().append("Hz"))
        setEncodingText(mEncoding.text)
        setFrameSizeTypeText(mFrameSizeType.name.lowercase())
        setVadModeText(mVadMode.name.lowercase())
    }

    private fun setListeners() {
        mBinding.configBtn.setOnClickListener {
            if (mStatus == AudioStatus.IDLE) {
                showConfigDialog()
                return@setOnClickListener
            }
            toastShort(R.string.simple_config_disable)
        }
    }

    /** 设置录音状态 */
    fun setStatusText(status: AudioStatus) {
        mStatus = status
        mBinding.statusTv.text = context.getString(R.string.simple_status).append(status.text)
    }

    /** 设置当前分贝 */
    fun setSoundSizeText(text: String = "") {
        mBinding.soundSizeTv.text = context.getString(R.string.simple_sound_size).append(text)
    }

    /** 设置检测结果 */
    fun setVadResultText(text: String = "") {
        mBinding.vadResultTv.text = context.getString(R.string.simple_vad_result).append(text)
    }

    /** 是否保存活动语音 */
    fun setSaveActiveVoiceText(text: String = "") {
        mBinding.saveVoiceTv.text = context.getString(R.string.vad_is_save_active_voice).append(text)
    }

    /** 保存音频格式 */
    fun setAudioFormatText(text: String = "") {
        mBinding.audioFormatTv.text = context.getString(R.string.vad_audio_format).append(text)
    }

    /** 端点检测采样率 */
    fun setSampleRateText(text: String = "") {
        mBinding.sampleRateTv.text = context.getString(R.string.vad_sample_rate).append(text)
    }

    /** 设置音频位宽 */
    fun setEncodingText(text: String = "") {
        mBinding.encodingTv.text = context.getString(R.string.simple_encoding).append(text)
    }

    /** 设置端点检测帧大小类型 */
    fun setFrameSizeTypeText(text: String = "") {
        mBinding.frameSizeTv.text = context.getString(R.string.vad_frame_size_type).append(text)
    }

    /** 设置端点检测模式 */
    fun setVadModeText(text: String = "") {
        mBinding.modeTv.text = context.getString(R.string.vad_mode).append(text)
    }

    /** 获取音频状态 */
    fun getStatus(): AudioStatus = mStatus

    /** 获取保存音频格式 */
    fun getAudioFormat(): AudioFormats = mAudioFormat

    /** 获取端点检测采样率*/
    fun getSampleRate(): VadSampleRate = mSampleRate

    /** 获取位宽 */
    fun getEncoding(): Encodings = mEncoding

    /** 获取帧大小类型 */
    fun getVadFrameSizeType(): VadFrameSizeType = mFrameSizeType

    /** 获取检测模式 */
    fun getVadMode(): VadMode = mVadMode

    /** 获取是否保存语音 */
    fun getSaveActiveVoice(): Boolean = isSaveActiveVoice

    /** 显示配置弹框 */
    private fun showConfigDialog() {
        val dialog = VadConfigDialog(context)
        dialog.setData(mAudioFormat, mSampleRate, mEncoding, mFrameSizeType, mVadMode, isSaveActiveVoice)
        dialog.setOnClickConfirmListener { dif, audioFormat, sampleRate, encoding, frameSizeType, vadMode, isSaveActiveVoice ->
            mAudioFormat = audioFormat
            mSampleRate = sampleRate
            mEncoding = encoding
            mFrameSizeType = frameSizeType
            mVadMode = vadMode
            this.isSaveActiveVoice = isSaveActiveVoice
            findViews()
            mListener?.onChanged(mAudioFormat, mSampleRate, mEncoding, mFrameSizeType, mVadMode, this.isSaveActiveVoice)
            dif.dismiss()
        }
        dialog.show()
    }

    fun setOnParamChangedListener(listener: OnParamChangedListener) {
        mListener = listener
    }

    fun interface OnParamChangedListener {
        fun onChanged(
            audioFormat: AudioFormats,
            sampleRate: VadSampleRate,
            encoding: Encodings,
            frameSizeType: VadFrameSizeType,
            vadMode: VadMode,
            isSaveActiveVoice: Boolean
        )
    }
}