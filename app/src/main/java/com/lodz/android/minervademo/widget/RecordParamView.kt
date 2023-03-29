package com.lodz.android.minervademo.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.lodz.android.corekt.anko.append
import com.lodz.android.corekt.anko.toastShort
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.databinding.ViewRecordParamBinding
import com.lodz.android.minervademo.enums.AudioStatus
import com.lodz.android.minervademo.enums.Encodings
import com.lodz.android.minervademo.enums.SampleRates
import com.lodz.android.minervademo.ui.dialog.RecordConfigDialog
import com.lodz.android.pandora.utils.viewbinding.bindingLayout

/**
 * 参数配置页面
 * @author zhouL
 * @date 2023/2/15
 */
class RecordParamView :FrameLayout{

    private val mBinding: ViewRecordParamBinding by context.bindingLayout(ViewRecordParamBinding::inflate)

    /** 音频状态 */
    private var mStatus: AudioStatus = AudioStatus.IDLE
    /** 音频格式 */
    private var mAudioFormat: AudioFormats = AudioFormats.WAV
    /** 采样率 */
    private var mSampleRate: SampleRates = SampleRates.SAMPLE_RATE_16K
    /** 位宽 */
    private var mEncoding: Encodings = Encodings.BIT_16
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
        setStatus(mStatus)
        setSoundSizeText("")
        setAudioFormatText(mAudioFormat.suffix)
        setSampleRateText(mSampleRate.text)
        setEncodingText(mEncoding.text)
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
    fun setStatus(status: AudioStatus) {
        mStatus = status
        mBinding.statusTv.text = context.getString(R.string.simple_status).append(status.text)
    }

    /** 设置当前分贝 */
    fun setSoundSizeText(text: String = "") {
        mBinding.soundSizeTv.text = context.getString(R.string.simple_sound_size).append(text)
    }

    /** 设置音频格式 */
    fun setAudioFormatText(text: String = "") {
        mBinding.audioFormatTv.text = context.getString(R.string.simple_audio_format).append(text)
    }

    /** 设置音频采样率 */
    fun setSampleRateText(text: String = "") {
        mBinding.sampleRateTv.text = context.getString(R.string.simple_sample_rate).append(text)
    }

    /** 设置音频位宽 */
    fun setEncodingText(text: String = "") {
        mBinding.encodingTv.text = context.getString(R.string.simple_encoding).append(text)
    }

    /** 获取音频状态 */
    fun getStatus(): AudioStatus = mStatus

    /** 获取音频格式 */
    fun getAudioFormat(): AudioFormats = mAudioFormat

    /** 获取采样率 */
    fun getSampleRate(): SampleRates = mSampleRate

    /** 获取位宽 */
    fun getEncoding(): Encodings = mEncoding

    /** 显示配置弹框 */
    private fun showConfigDialog() {
        val dialog = RecordConfigDialog(context)
        dialog.setData(mAudioFormat, mSampleRate, mEncoding)
        dialog.setOnClickConfirmListener { dif, audioFormat, sampleRate, encoding ->
            mAudioFormat = audioFormat
            mSampleRate = sampleRate
            mEncoding = encoding
            setAudioFormatText(audioFormat.suffix)
            setSampleRateText(sampleRate.text)
            setEncodingText(encoding.text)
            mListener?.onChanged(mAudioFormat, mSampleRate, mEncoding)
            dif.dismiss()
        }
        dialog.show()
    }

    fun setOnParamChangedListener(listener: OnParamChangedListener) {
        mListener = listener
    }

    fun interface OnParamChangedListener {
        fun onChanged(audioFormat: AudioFormats, sampleRate: SampleRates, encoding: Encodings)
    }
}