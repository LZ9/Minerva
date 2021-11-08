package com.lodz.android.minerva.wav


/**
 * 获取WAV文件格式头信息数据
 * @author zhouL
 * @date 2021/10/26
 */
object WavHeader {

    @JvmStatic
    fun getHeader(totalAudioLen: Int, sampleRate: Int, channels: Short, sampleBits: Short): ByteArray {
        // RIFF数据块
        var result: ByteArray = "RIFF".toByteArray()
        result = merge(result, toByteArray(totalAudioLen))
        result = merge(result, "WAVE".toByteArray())


        // FORMAT 格式参数
        result = merge(result, "fmt ".toByteArray())
        result = merge(result, toByteArray(16))
        result = merge(result, toByteArray(1.toShort()))
        result = merge(result, toByteArray(channels))
        result = merge(result, toByteArray(sampleRate))
        result = merge(result, toByteArray(sampleRate * sampleBits / 8 * channels))
        result = merge(result, toByteArray((channels * sampleBits / 8).toShort()))
        result = merge(result, toByteArray(sampleBits))

        // FORMAT 数据块
        result = merge(result, "data".toByteArray())
        result = merge(result, toByteArray(totalAudioLen - 44))
        return result
    }

    /** 线性合并[start]数组和[end]数组 */
    private fun merge(start: ByteArray, end: ByteArray): ByteArray {
        val result = ByteArray(start.size + end.size)
        System.arraycopy(start, 0, result, 0, start.size)
        System.arraycopy(end, 0, result, start.size, end.size)
        return result
    }

    /** Int 转 ByteArray */
    private fun toByteArray(i: Int): ByteArray {
        val b = ByteArray(4)
        b[0] = (i and 0xff).toByte()
        b[1] = (i shr 8 and 0xff).toByte()
        b[2] = (i shr 16 and 0xff).toByte()
        b[3] = (i shr 24 and 0xff).toByte()
        return b
    }

    /** Short 转 ByteArray */
    private fun toByteArray(src: Short): ByteArray {
        val dest = ByteArray(2)
        dest[0] = src.toByte()
        dest[1] = (src.toInt() shr 8).toByte()
        return dest
    }

}