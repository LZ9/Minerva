package com.lodz.android.minervademo.utils

import com.lodz.android.minervademo.bean.DictBean
import com.lodz.android.minervademo.bean.DictListBean
import com.lodz.android.minervademo.config.Constant

/**
 * 字典管理
 * @author zhouL
 * @date 2021/10/21
 */
class DictManager private constructor() {

    companion object {
        private val sInstance = DictManager()
        fun get(): DictManager = sInstance
    }

    private var mDictList = ArrayList<DictListBean>()

    fun init(){
        mDictList.add(createStatusDictList())
        mDictList.add(createAudioFormatDictList())
        mDictList.add(createSampleRateDictList())
        mDictList.add(createEncodingDictList())
    }

    /** 创建状态字典列表 */
    private fun createStatusDictList(): DictListBean = DictListBean(
        Constant.DICT_STATUS,
        arrayListOf(
            DictBean(Constant.STATUS_IDLE, Constant.STATUS_IDLE_NAME),
            DictBean(Constant.STATUS_RECORDING, Constant.STATUS_RECORDING_NAME),
            DictBean(Constant.STATUS_PAUSE, Constant.STATUS_PAUSE_NAME),
            DictBean(Constant.STATUS_VAD_DETECT, Constant.STATUS_VAD_DETECT_NAME),
        )
    )

    /** 创建音频格式字典列表 */
    private fun createAudioFormatDictList(): DictListBean = DictListBean(
        Constant.DICT_AUDIO_FORMAT,
        arrayListOf(
            DictBean(Constant.AUDIO_FORMAT_PCM, Constant.AUDIO_FORMAT_PCM_NAME),
            DictBean(Constant.AUDIO_FORMAT_MP3, Constant.AUDIO_FORMAT_MP3_NAME),
            DictBean(Constant.AUDIO_FORMAT_WAV, Constant.AUDIO_FORMAT_WAV_NAME),
        )
    )

    /** 创建采样率字典列表 */
    private fun createSampleRateDictList(): DictListBean = DictListBean(
        Constant.DICT_SAMPLE_RATE,
        arrayListOf(
            DictBean(Constant.SAMPLE_RATE_8000, Constant.SAMPLE_RATE_8000_NAME),
            DictBean(Constant.SAMPLE_RATE_16000, Constant.SAMPLE_RATE_16000_NAME),
            DictBean(Constant.SAMPLE_RATE_44100, Constant.SAMPLE_RATE_44100_NAME),
        )
    )

    /** 创建音频位宽字典列表 */
    private fun createEncodingDictList(): DictListBean = DictListBean(
        Constant.DICT_ENCODING,
        arrayListOf(
            DictBean(Constant.ENCODING_8_BIT, Constant.ENCODING_8_BIT_NAME),
            DictBean(Constant.ENCODING_16_BIT, Constant.ENCODING_16_BIT_NAME),
        )
    )

    /** 获取字典数据 */
    fun getDictBean(id: String, key: Int): DictBean? {
        if (mDictList.isEmpty()) {
            return null
        }
        val listBean = getDictListBean(id) ?: return null
        listBean.list.forEach {
            if (it.key == key) {
                return it
            }
        }
        return null
    }

    /** 获取字典列表数据 */
    fun getDictListBean(id: String): DictListBean? {
        mDictList.forEach {
            if (it.id == id) {
                return it
            }
        }
        return null
    }
}