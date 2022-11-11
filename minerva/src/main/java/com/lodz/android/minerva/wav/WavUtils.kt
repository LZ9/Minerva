package com.lodz.android.minerva.wav

import com.lodz.android.minerva.bean.AudioFormats
import java.io.File
import java.io.RandomAccessFile

/**
 * WAV帮助类
 * @author zhouL
 * @date 2021/11/8
 */
object WavUtils {

    /** 将WAV的[header]写入到文件[file]中，不修改后缀名 */
    @JvmStatic
    fun writeHeader(file: File, header: ByteArray): Boolean {
        if (!file.exists()) {
            return false
        }
        if (!file.isFile) {
            return false
        }
        RandomAccessFile(file, "rw").use {
            it.seek(0)
            it.write(header)
            return true
        }
    }

    /** 将WAV的[header]写入到文件[file]中，并后缀改为.wav */
    @JvmStatic
    fun pcmToWav(file: File, header: ByteArray): Boolean {
        val writeHeaderSuccess = writeHeader(file, header)
        if (!writeHeaderSuccess){
            return false
        }
        renameWavSuffix(file)
        return true
    }

    /** 将文件[file]后缀改为.wav */
    private fun renameWavSuffix(file: File){
        val path = file.absolutePath
        var subffix = ""
        val index = path.lastIndexOf('.')
        if (index != -1) {//存在后缀
            subffix = path.substring(index)
        }
        if (subffix.lowercase().equals(AudioFormats.WAV.suffix)){
            return
        }
        val newPath = path.substring(0, index) + AudioFormats.WAV.suffix
        file.renameTo(File(newPath))
    }

    /**
     * 生成wav格式的Header，wave是RIFF文件结构，每一部分为一个chunk，按顺序包含：
     *
     * 1、RIFF WAVE chunk
     * 2、FMT Chunk
     * 3、Fact chunk（可选）
     * 4、Data chunk
     *
     * 不包括header的音频数据总长度[totalAudioLen]，采样率[sampleRate]，频道数量[channels]，位宽[sampleBits]
     */
    @JvmStatic
    fun generateHeader(totalAudioLen: Int, sampleRate: Int, channels: Short, sampleBits: Short): ByteArray {
        // RIFF WAVE 块
        var result: ByteArray = "RIFF".toByteArray()
        result = merge(result, toByteArray(totalAudioLen))
        result = merge(result, "WAVE".toByteArray())

        // FMT 块
        result = merge(result, "fmt ".toByteArray())
        result = merge(result, toByteArray(16))
        result = merge(result, toByteArray(1.toShort()))
        result = merge(result, toByteArray(channels))
        result = merge(result, toByteArray(sampleRate))
        result = merge(result, toByteArray(sampleRate * sampleBits / 8 * channels))
        result = merge(result, toByteArray((channels * sampleBits / 8).toShort()))
        result = merge(result, toByteArray(sampleBits))

        // Data 块
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