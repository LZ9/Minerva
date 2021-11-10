package com.lodz.android.minerva.contract

/**
 * 录音音量大小监听器
 * @author zhouL
 * @date 2021/11/9
 */
fun interface OnRecordingSoundSizeListener {

    /** 音量大小[db]回调 */
    fun onSoundSize(db: Int)
}