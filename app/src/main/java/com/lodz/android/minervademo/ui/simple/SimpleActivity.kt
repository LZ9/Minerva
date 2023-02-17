package com.lodz.android.minervademo.ui.simple

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.lodz.android.corekt.anko.*
import com.lodz.android.corekt.utils.FileUtils
import com.lodz.android.minerva.MinervaAgent
import com.lodz.android.minerva.contract.Minerva
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.utils.RecordUtils
import com.lodz.android.minerva.wav.WavUtils
import com.lodz.android.minervademo.BuildConfig
import com.lodz.android.minervademo.utils.FileManager
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.databinding.ActivitySimpleBottomBinding
import com.lodz.android.minervademo.databinding.ActivitySimpleContentBinding
import com.lodz.android.minervademo.databinding.ActivitySimpleTopBinding
import com.lodz.android.minervademo.enums.AudioStatus
import com.lodz.android.minervademo.enums.Encodings
import com.lodz.android.minervademo.enums.SampleRates
import com.lodz.android.pandora.base.activity.BaseSandwichActivity
import com.lodz.android.pandora.rx.subscribe.single.BaseSingleObserver
import com.lodz.android.pandora.rx.utils.RxUtils
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import com.lodz.android.pandora.widget.rv.anko.linear
import com.lodz.android.pandora.widget.rv.anko.setup
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.util.*

/**
 * 简单实现
 * @author zhouL
 * @date 2021/10/26
 */
@SuppressLint("NotifyDataSetChanged")
class SimpleActivity : BaseSandwichActivity() {

    companion object {
        fun start(context: Context){
            val intent = Intent(context, SimpleActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val mTopBinding: ActivitySimpleTopBinding by bindingLayout(ActivitySimpleTopBinding::inflate)
    private val mContentBinding: ActivitySimpleContentBinding by bindingLayout(ActivitySimpleContentBinding::inflate)
    private val mBottomBinding: ActivitySimpleBottomBinding by bindingLayout(ActivitySimpleBottomBinding::inflate)

    override fun getTopViewBindingLayout(): View = mTopBinding.root
    override fun getViewBindingLayout(): View = mContentBinding.root
    override fun getBottomViewBindingLayout(): View = mBottomBinding.root

    /** 状态 */
    private var mStatus: AudioStatus = AudioStatus.IDLE
    /** 音频格式 */
    private var mAudioFormat: AudioFormats = AudioFormats.WAV
    /** 采样率 */
    private var mSampleRate: SampleRates = SampleRates.SAMPLE_RATE_16K
    /** 音频位宽 */
    private var mEncoding: Encodings = Encodings.BIT_16

    private var mMinerva: Minerva? = null

    /** 音频文件适配器 */
    private lateinit var mAdapter: AudioFilesAdapter

    override fun findViews(savedInstanceState: Bundle?) {
        super.findViews(savedInstanceState)
        setSwipeRefreshEnabled(true)
        setTitleBar()
        initRecyclerView()
        mTopBinding.savePathTv.text = getString(R.string.simple_save_path).append(FileManager.getContentFolderPath())
        mBottomBinding.startBtn.isEnabled = true
        mTopBinding.deleteAllBtn.isEnabled = true
        mBottomBinding.pauseBtn.isEnabled = false
    }

    private fun setTitleBar() {
        mTopBinding.titleBarLayout.setBackgroundColor(getColorCompat(R.color.color_00a1d5))
        mTopBinding.titleBarLayout.setTitleName(R.string.main_simple)
    }

    private fun initRecyclerView() {
        mAdapter = mContentBinding.audioRv.linear().setup(AudioFilesAdapter(getContext()))
    }

    override fun onDataRefresh() {
        super.onDataRefresh()
        updateAudioFileList()
        setSwipeRefreshFinish()
    }

    override fun setListeners() {
        super.setListeners()

        mTopBinding.titleBarLayout.setOnBackBtnClickListener {
            finish()
        }

        mTopBinding.paramView.setOnParamChangedListener { audioFormat, sampleRate, encoding ->
            mAudioFormat = audioFormat
            mMinerva?.changeAudioFormat(audioFormat)
            mSampleRate = sampleRate
            mMinerva?.changeSampleRate(sampleRate.rate)
            mEncoding = encoding
            mMinerva?.changeEncoding(encoding.encoding)
        }

        mTopBinding.deleteAllBtn.setOnClickListener {
            FileUtils.delFile(FileManager.getContentFolderPath())
            updateAudioFileList()
        }

        mBottomBinding.startBtn.setOnClickListener {
            if (mStatus == AudioStatus.PAUSE) {
                mMinerva?.resume()
            } else {
                mMinerva?.start()
            }
        }

        mBottomBinding.stopBtn.setOnClickListener {
            mMinerva?.stop()
        }

        mBottomBinding.pauseBtn.setOnClickListener {
            mMinerva?.pause()
        }

        mAdapter.setOnAudioFileListener(object : AudioFilesAdapter.OnAudioFileListener {
            override fun onClickPlay(file: File) {
                val intent = Intent(Intent.ACTION_VIEW)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val uri = FileProvider.getUriForFile(getContext(), BuildConfig.AUTHORITY, file)
                    intent.setDataAndType(uri, "audio/*")
                } else {
                    intent.setDataAndType(file.toUri(), "audio/*")
                }
                getContext().startActivity(intent)
            }

            override fun onClickDelete(file: File) {
                FileUtils.delFile(file.absolutePath)
                updateAudioFileList()
            }

            override fun onClickPcmToWav(file: File) {
                AlertDialog.Builder(getContext())
                    .setMessage("是否按当前采样率：${mSampleRate.text} 和位宽：${mEncoding.text} 来进行转换？（若转换配置和PCM录音配置不同，则转出来的wav音频会失真）")
                    .setPositiveButton("是") { dif, which ->
                        val header = WavUtils.generateHeader(file.length().toInt(), mSampleRate.rate, 1, mEncoding.encoding.toShort())
                        WavUtils.pcmToWav(file, header)
                        updateAudioFileList()
                        dif.dismiss()
                    }
                    .setNegativeButton("否") { dif, which ->
                        dif.dismiss()
                    }
                    .create()
                    .show()
            }
        })
    }


    override fun initData() {
        super.initData()
        updateAudioFileList()
        initMinerva()
    }

    private fun initMinerva() {
        mMinerva = MinervaAgent.create()
            .setChannel(AudioFormat.CHANNEL_IN_MONO)
            .setSampleRate(mSampleRate.rate)
            .setEncoding(mEncoding.encoding)
            .setAudioFormat(mAudioFormat)
            .setSaveDirPath(FileManager.getContentFolderPath())
            .setOnRecordingStatesListener{
                when (it) {
                    is Idle -> {
                        mStatus = AudioStatus.IDLE
                        Log.v("testtag", "空闲")
                    }
                    is Recording -> {
                        mStatus = AudioStatus.RECORDING
                        val tips = if (mEncoding == Encodings.BIT_16) {
                            val data = it.data
                            val db = if (data == null) 0 else RecordUtils.getDbFor16Bit(data, it.end)
                            "$db db"
                        } else {
                            "非16bit位宽，暂无解析"
                        }
                        mTopBinding.paramView.setSoundSizeText(tips)
                        Log.d("testtag", "录音中")
                    }
                    is Pause -> {
                        mStatus = AudioStatus.PAUSE
                        Log.i("testtag", "暂停")
                    }
                    is Stop -> {
                        mStatus = AudioStatus.IDLE
                        Log.i("testtag", "停止")
                    }
                    is Finish -> {
                        mStatus = AudioStatus.IDLE
                        toastShort(it.file.absolutePath)
                        updateAudioFileList()
                        Log.v("testtag", "完成")
                    }
                    is Error -> {
                        mStatus = AudioStatus.IDLE
                        toastShort("${it.msg} , ${it.t}")
                        Log.e("testtag", "异常")
                    }
                    else -> {}
                }
                mTopBinding.paramView.setStatusText(mStatus.text)
                mBottomBinding.startBtn.isEnabled = mStatus != AudioStatus.RECORDING
                mTopBinding.deleteAllBtn.isEnabled = mStatus != AudioStatus.RECORDING
                mBottomBinding.pauseBtn.isEnabled = mStatus == AudioStatus.RECORDING
            }
            .buildRecording(getContext())
    }

    /** 更新音频文件列表数据 */
    private fun updateAudioFileList(){
        Observable.fromIterable(FileUtils.getFileList(FileManager.getContentFolderPath()))
            .sorted { file1, file2 ->
                val diff = file1.lastModified() - file2.lastModified()
                return@sorted when {
                    diff > 0 -> -1
                    diff == 0L -> 0
                    else -> 1
                }
            }
            .toList()
            .compose(RxUtils.ioToMainSingle())
            .subscribe(BaseSingleObserver.action(
                success = {
                    mAdapter.setData(it)
                    mAdapter.notifyDataSetChanged()
                    if (mAdapter.itemCount == 0) {
                        showStatusNoData()
                    } else {
                        showStatusCompleted()
                    }
                }
            ))
    }
}