package com.lodz.android.minervademo

import com.lodz.android.pandora.base.application.BaseApplication
import com.lodz.android.minervademo.utils.FileManager

/**
 *
 * @author zhouL
 * @date 2021/10/16
 */
class App :BaseApplication(){

    companion object {
        @JvmStatic
        fun get(): App = BaseApplication.get() as App
    }

    override fun onStartCreate() {
        FileManager.init(this)
    }

    override fun onExit() {
    }
}