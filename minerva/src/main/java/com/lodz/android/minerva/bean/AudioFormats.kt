package com.lodz.android.minerva.bean

/**
 * 音频格式
 * @author zhouL
 * @date 2021/11/9
 */
enum class AudioFormats(val id: Int, val suffix: String) {

    /** PCM格式 */
    PCM(1, ".pcm"),

    /** WAV格式 */
    WAV(2, ".wav"),

    /** MP3格式 */
    MP3(3, ".mp3")
}