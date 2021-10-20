package com.lodz.android.minervademo.ui

import android.os.Bundle
import android.view.View
import com.lodz.android.corekt.anko.append
import com.lodz.android.corekt.anko.getColorCompat
import com.lodz.android.minervademo.utils.FileManager
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.databinding.ActivityMainBinding
import com.lodz.android.pandora.base.activity.BaseActivity
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import com.lodz.android.pandora.widget.base.TitleBarLayout

class MainActivity : BaseActivity() {

    private val mBinding: ActivityMainBinding by bindingLayout(ActivityMainBinding::inflate)

    override fun getViewBindingLayout(): View = mBinding.root

    override fun findViews(savedInstanceState: Bundle?) {
        super.findViews(savedInstanceState)
        setTitleBar(getTitleBarLayout())
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

        }

        mBinding.startBtn.setOnClickListener {

        }

        mBinding.stopBtn.setOnClickListener {

        }

        mBinding.pauseBtn.setOnClickListener {

        }
    }

    override fun initData() {
        super.initData()
        showStatusCompleted()
    }
}