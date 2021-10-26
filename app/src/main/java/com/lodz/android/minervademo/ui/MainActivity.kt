package com.lodz.android.minervademo.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.lodz.android.corekt.anko.getColorCompat
import com.lodz.android.corekt.anko.goAppDetailSetting
import com.lodz.android.corekt.anko.isPermissionGranted
import com.lodz.android.corekt.anko.toastShort
import com.lodz.android.minervademo.App
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.databinding.ActivityMainBinding
import com.lodz.android.minervademo.ui.simple.SimpleActivity
import com.lodz.android.pandora.base.activity.BaseActivity
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import com.lodz.android.pandora.widget.base.TitleBarLayout
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.constructPermissionsRequest

/**
 * 主页
 * @author zhouL
 * @date 2021/10/26
 */
class MainActivity :BaseActivity(){

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

    override fun findViews(savedInstanceState: Bundle?) {
        super.findViews(savedInstanceState)
        setTitleBar(getTitleBarLayout())
    }


    private fun setTitleBar(titleBarLayout: TitleBarLayout) {
        titleBarLayout.needBackButton(false)
        titleBarLayout.setBackgroundColor(getColorCompat(R.color.color_00a1d5))
        titleBarLayout.setTitleName(R.string.app_name)
    }

    override fun setListeners() {
        super.setListeners()
        mBinding.simpleBtn.setOnClickListener {
            SimpleActivity.start(getContext())
        }

        mBinding.serviceBtn.setOnClickListener {

        }

        mBinding.vadBtn.setOnClickListener {

        }
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
            .setPositiveButton(R.string.main_check_permission_confirm){ dialog, which->
                onRequestPermission()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.main_check_permission_unconfirmed){ dialog, which->
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
}