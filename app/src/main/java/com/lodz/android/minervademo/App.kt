package com.lodz.android.minervademo

import com.lodz.android.corekt.log.PrintLog
import com.lodz.android.minerva.recorder.wav.WavUtils
import com.lodz.android.pandora.base.application.BaseApplication
import com.lodz.android.minerva.fftlib.ByteUtils
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

        PrintLog.w("zlwTest", "TEST-----------------")
        val header1 = WavUtils.generateWavFileHeader(1024, 16000, 1, 16)
        val header2 = WavUtils.generateWavFileHeader(1024, 16000, 1, 16)

        PrintLog.d("zlwTest", "Wav1: " + WavUtils.headerToString(header1))
        PrintLog.d("zlwTest", "Wav2: " + WavUtils.headerToString(header2))

        PrintLog.w("zlwTest", "TEST-2----------------")

        PrintLog.d("zlwTest", "Wav1: " + ByteUtils.toString(header1))
        PrintLog.d("zlwTest", "Wav2: " + ByteUtils.toString(header2))
        FileManager.init(this)
    }

    override fun onExit() {
    }
}