package com.lodz.android.minervademo.ui

import android.os.Bundle
import android.view.View
import com.lodz.android.corekt.anko.append
import com.lodz.android.corekt.anko.getColorCompat
import com.lodz.android.corekt.anko.toastShort
import com.lodz.android.minervademo.utils.FileManager
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.config.Constant
import com.lodz.android.minervademo.databinding.ActivityMainBinding
import com.lodz.android.minervademo.ui.dialog.ConfigDialog
import com.lodz.android.minervademo.utils.DictManager
import com.lodz.android.pandora.base.activity.BaseActivity
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import com.lodz.android.pandora.widget.base.TitleBarLayout

class MainActivity : BaseActivity() {

    /** 状态 */
    private var mStatus = Constant.STATUS_IDLE
    /** 音频格式 */
    private var mAudioFormat = Constant.AUDIO_FORMAT_WAV
    /** 采样率 */
    private var mSampleRate = Constant.SAMPLE_RATE_16000
    /** 音频位宽 */
    private var mEncoding = Constant.ENCODING_16_BIT

    private val mBinding: ActivityMainBinding by bindingLayout(ActivityMainBinding::inflate)

    override fun getViewBindingLayout(): View = mBinding.root

    override fun startCreate() {
        super.startCreate()
        DictManager.get().init()
    }

    override fun findViews(savedInstanceState: Bundle?) {
        super.findViews(savedInstanceState)
        setTitleBar(getTitleBarLayout())
        updateConfigView()
        mBinding.savePathTv.text = getString(R.string.main_save_path).append(FileManager.getContentFolderPath())
        mBinding.startBtn.isEnabled = true
        mBinding.pauseBtn.isEnabled = false
    }

    private fun setTitleBar(titleBarLayout: TitleBarLayout) {
        titleBarLayout.needBackButton(false)
        titleBarLayout.setBackgroundColor(getColorCompat(R.color.color_00a1d5))
        titleBarLayout.setTitleName(R.string.app_name)
    }

    override fun setListeners() {
        super.setListeners()

        mBinding.configBtn.setOnClickListener {
            if (mStatus == Constant.STATUS_IDLE) {
                showConfigDialog()
                return@setOnClickListener
            }
            toastShort(R.string.main_config_disable)
        }

        mBinding.startBtn.setOnClickListener {

        }

        mBinding.stopBtn.setOnClickListener {

        }

        mBinding.pauseBtn.setOnClickListener {

        }
    }

    /** 更新配置相关控件 */
    private fun updateConfigView(){
        mBinding.audioFormatTv.text = getString(R.string.main_audio_format).append(
            DictManager.get().getDictBean(Constant.DICT_AUDIO_FORMAT, mAudioFormat)?.value ?: "-"
        )
        mBinding.sampleRateTv.text = getString(R.string.main_sample_rate).append(
            DictManager.get().getDictBean(Constant.DICT_SAMPLE_RATE, mSampleRate)?.value ?: "-"
        )
        mBinding.encodingTv.text = getString(R.string.main_encoding).append(
            DictManager.get().getDictBean(Constant.DICT_ENCODING, mEncoding)?.value ?: "-"
        )
    }

    /** 显示配置弹框 */
    private fun showConfigDialog() {
        val dialog = ConfigDialog(getContext())
        dialog.setData(mAudioFormat, mSampleRate, mEncoding)
        dialog.setOnClickConfirmListener { dif, audioFormat, sampleRate, encoding ->
            mAudioFormat = audioFormat
            mSampleRate = sampleRate
            mEncoding = encoding
            updateConfigView()
            dif.dismiss()
        }
        dialog.show()
    }

    override fun initData() {
        super.initData()
        showStatusCompleted()
    }
}