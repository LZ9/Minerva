package com.lodz.android.minervademo.config

import android.media.AudioFormat

/**
 * 常量
 * @author zhouL
 * @date 2021/10/21
 */
object Constant {


    /** 状态字典 */
    const val DICT_STATUS = "DICT_STATUS"

    /** 空闲中 */
    const val STATUS_IDLE = 1
    /** 空闲中 */
    const val STATUS_IDLE_NAME = "空闲中"

    /** 录音中 */
    const val STATUS_RECORDING = 2
    /** 录音中 */
    const val STATUS_RECORDING_NAME = "录音中"

    /** 暂停中 */
    const val STATUS_PAUSE = 3
    /** 暂停中 */
    const val STATUS_PAUSE_NAME = "暂停中"

    /** 录音中 */
    const val STATUS_VAD_DETECT = 4
    /** 录音中 */
    const val STATUS_VAD_DETECT_NAME = "端点检测中"


    /** 音频格式字典 */
    const val DICT_AUDIO_FORMAT = "DICT_AUDIO_FORMAT"

    /** pcm格式 */
    const val AUDIO_FORMAT_PCM = 1
    /** pcm格式 */
    const val AUDIO_FORMAT_PCM_NAME = ".pcm"

    /** mp3格式 */
    const val AUDIO_FORMAT_MP3 = 2
    /** mp3格式 */
    const val AUDIO_FORMAT_MP3_NAME = ".mp3"

    /** wav格式 */
    const val AUDIO_FORMAT_WAV = 3
    /** wav格式 */
    const val AUDIO_FORMAT_WAV_NAME = ".wav"


    /** 采样率字典 */
    const val DICT_SAMPLE_RATE = "DICT_SAMPLE_RATE"

    /** 采样率8000Hz */
    const val SAMPLE_RATE_8000 = 1
    /** 采样率8000Hz */
    const val SAMPLE_RATE_8000_NAME = "8000Hz"

    /** 采样率16000Hz */
    const val SAMPLE_RATE_16000 = 2
    /** 采样率16000Hz */
    const val SAMPLE_RATE_16000_NAME = "16000Hz"

    /** 采样率44100Hz */
    const val SAMPLE_RATE_44100 = 3
    /** 采样率44100Hz */
    const val SAMPLE_RATE_44100_NAME = "44100Hz"


    /** 音频位宽字典 */
    const val DICT_ENCODING = "DICT_ENCODING"

    /** 8 bit */
    const val ENCODING_8_BIT = AudioFormat.ENCODING_PCM_8BIT
    /** 8 bit */
    const val ENCODING_8_BIT_NAME = "8 bit"

    /** 16 bit */
    const val ENCODING_16_BIT = AudioFormat.ENCODING_PCM_16BIT
    /** 16 bit */
    const val ENCODING_16_BIT_NAME = "16 bit"

}