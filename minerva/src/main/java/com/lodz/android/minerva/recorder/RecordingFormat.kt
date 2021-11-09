package com.lodz.android.minerva.recorder

/**
 * 录音格式
 * @author zhouL
 * @date 2021/11/9
 */
enum class RecordingFormat(val id: Int, val suffix: String) {

    /** MP3格式 */
    MP3(1, ".mp3"),
    /** WAV格式 */
    WAV(2, ".wav"),
    /** PCM格式 */
    PCM(1, ".pcm")
}