package com.lodz.android.minervademo.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.lodz.android.corekt.anko.*
import com.lodz.android.minervademo.App
import com.lodz.android.minervademo.utils.FileManager
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.config.Constant
import com.lodz.android.minervademo.databinding.ActivityMainBinding
import com.lodz.android.minervademo.ui.dialog.ConfigDialog
import com.lodz.android.minervademo.utils.DictManager
import com.lodz.android.pandora.base.activity.BaseActivity
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import com.lodz.android.pandora.widget.base.TitleBarLayout
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.constructPermissionsRequest

class MainActivity : BaseActivity() {

    /** 状态 */
    private var mStatus = Constant.STATUS_IDLE
    /** 音频格式 */
    private var mAudioFormat = Constant.AUDIO_FORMAT_WAV
    /** 采样率 */
    private var mSampleRate = Constant.SAMPLE_RATE_16000
    /** 音频位宽 */
    private var mEncoding = Constant.ENCODING_16_BIT

    private val hasRecordAudioPermissions by lazy {
        constructPermissionsRequest(
            Manifest.permission.RECORD_AUDIO,// 手机状态
            onShowRationale = ::onShowRationaleBeforeRequest,
            onPermissionDenied = ::onDenied,
            onNeverAskAgain = ::onNeverAsk,
            requiresPermission = ::onRequestPermission
        )
    }

    private val hasWriteExternalStoragePermissions by lazy {
        constructPermissionsRequest(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,// 存储卡读写
            onShowRationale = ::onShowRationaleBeforeRequest,
            onPermissionDenied = ::onDenied,
            onNeverAskAgain = ::onNeverAsk,
            requiresPermission = ::onRequestPermission
        )
    }

    private val hasReadExternalStoragePermissions by lazy {
        constructPermissionsRequest(
            Manifest.permission.READ_EXTERNAL_STORAGE,// 存储卡读写
            onShowRationale = ::onShowRationaleBeforeRequest,
            onPermissionDenied = ::onDenied,
            onNeverAskAgain = ::onNeverAsk,
            requiresPermission = ::onRequestPermission
        )
    }


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0以上的手机对权限进行动态申请
            onRequestPermission()
        } else {
            init()
        }
    }

    /** 初始化 */
    private fun init() {
        showStatusCompleted()
    }

    /** 权限申请成功 */
    private fun onRequestPermission() {
        if (!isPermissionGranted(Manifest.permission.RECORD_AUDIO)){
            hasRecordAudioPermissions.launch()
            return
        }
        if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            hasWriteExternalStoragePermissions.launch()
            return
        }
        if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)){
            hasReadExternalStoragePermissions.launch()
            return
        }
        init()
    }

    /** 用户拒绝后再次申请前告知用户为什么需要该权限 */
    private fun onShowRationaleBeforeRequest(request: PermissionRequest) {
        request.proceed()//请求权限
    }

    /** 被拒绝 */
    private fun onDenied() {
        onRequestPermission()//申请权限
    }

    /** 被拒绝并且勾选了不再提醒 */
    private fun onNeverAsk() {
        toastShort(R.string.main_check_permission_tips)
        showPermissionCheckDialog()
        goAppDetailSetting()
    }

    /** 显示权限核对弹框 */
    private fun showPermissionCheckDialog(){
        val checkDialog = AlertDialog.Builder(getContext())
            .setMessage(R.string.main_check_permission_title)
            .setPositiveButton(R.string.main_check_permission_confirm){dialog,  which->
                onRequestPermission()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.main_check_permission_unconfirmed){dialog,  which->
                goAppDetailSetting()
            }
            .setOnCancelListener {
                toastShort(R.string.main_check_permission_cancel)
                App.get().exit()
            }
            .create()
        checkDialog.setCanceledOnTouchOutside(false)
        checkDialog.show()
    }
}