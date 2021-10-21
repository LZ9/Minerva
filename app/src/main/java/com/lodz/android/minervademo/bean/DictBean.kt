package com.lodz.android.minervademo.bean

import com.lodz.android.pandora.widget.collect.radio.Radioable

/**
 * 字典数据
 * @author zhouL
 * @date 2021/10/21
 */
class DictBean(var key: Int, var value: String) : Radioable {

    override fun getIdTag(): String  = key.toString()

    override fun getNameText(): String = value
}