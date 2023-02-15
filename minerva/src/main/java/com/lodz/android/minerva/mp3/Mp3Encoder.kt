package com.lodz.android.minerva.mp3

/**
 * @author zhouL
 * @date 2023/2/14
 */
object Mp3Encoder {

    init {
        System.loadLibrary("mp3lame")
    }

    @JvmStatic
    external fun close()

    @JvmStatic
    external fun encode(buffer_l: ShortArray, buffer_r: ShortArray, samples: Int, mp3buf: ByteArray): Int

    @JvmStatic
    external fun flush(mp3buf: ByteArray): Int

    @JvmStatic
    external fun init(inSampleRate: Int, outChannel: Int, outSampleRate: Int, outBitrate: Int, quality: Int)

    @JvmStatic
    fun init(inSampleRate: Int, outChannel: Int, outSampleRate: Int, outBitrate: Int) {
        init(inSampleRate, outChannel, outSampleRate, outBitrate, 7)
    }

}