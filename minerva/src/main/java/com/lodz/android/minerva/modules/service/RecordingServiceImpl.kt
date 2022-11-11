package com.lodz.android.minerva.modules.service

import android.annotation.SuppressLint
import android.content.Context
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.contract.Minerva
import com.lodz.android.minerva.contract.OnRecordingStatesListener
import java.io.*

/**
 * 常规录音实现
 * @author zhouL
 * @date 2021/11/10
 */
@SuppressLint("MissingPermission")
class RecordingServiceImpl : Minerva {
    override fun init(
        context: Context,
        sampleRate: Int,
        channel: Int,
        encoding: Int,
        dirPath: String,
        format: AudioFormats
    ) {

    }

    override fun changeSampleRate(sampleRate: Int) {

    }

    override fun changeEncoding(encoding: Int) {

    }

    override fun changeAudioFormat(format: AudioFormats) {

    }

    override fun start() {

    }

    override fun stop() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun setOnRecordingStatesListener(listener: OnRecordingStatesListener?) {

    }

    override fun getRecordingState(): RecordingStates {
        TODO("Not yet implemented")
    }

}