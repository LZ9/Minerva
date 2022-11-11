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
import com.lodz.android.minerva.recorder.RecordingState
import com.lodz.android.minerva.contract.OnRecordingStateListener
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minerva.bean.states.*
import com.lodz.android.minerva.wav.WavUtils
import com.lodz.android.minervademo.BuildConfig
import com.lodz.android.minervademo.utils.FileManager
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.config.Constant
import com.lodz.android.minervademo.databinding.ActivitySimpleBottomBinding
import com.lodz.android.minervademo.databinding.ActivitySimpleContentBinding
import com.lodz.android.minervademo.databinding.ActivitySimpleTopBinding
import com.lodz.android.minervademo.ui.dialog.ConfigDialog
import com.lodz.android.minervademo.utils.DictManager
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
    private var mStatus = Constant.STATUS_IDLE
    /** 音频格式 */
    private var mAudioFormat = Constant.AUDIO_FORMAT_WAV
    /** 采样率 */
    private var mSampleRate = Constant.SAMPLE_RATE_16000
    /** 音频位宽 */
    private var mEncoding = Constant.ENCODING_16_BIT

    private var mMinerva: Minerva? = null

    /** 音频文件适配器 */
    private lateinit var mAdapter: AudioFilesAdapter

    override fun startCreate() {
        super.startCreate()
        DictManager.get().init()
    }

    override fun findViews(savedInstanceState: Bundle?) {
        super.findViews(savedInstanceState)
        setSwipeRefreshEnabled(true)
        setTitleBar()
        updateConfigView()
        initRecyclerView()
        mTopBinding.statusTv.text = getString(R.string.simple_status)
            .append(DictManager.get().getDictBean(Constant.DICT_STATUS, mStatus)?.value ?: "未知")
        mTopBinding.savePathTv.text = getString(R.string.simple_save_path).append(FileManager.getContentFolderPath())
        mBottomBinding.startBtn.isEnabled = true
        mBottomBinding.pauseBtn.isEnabled = false
    }

    private fun setTitleBar() {
        mTopBinding.titleBarLayout.setBackgroundColor(getColorCompat(R.color.color_00a1d5))
        mTopBinding.titleBarLayout.setTitleName(R.string.main_simple)
    }

    private fun initRecyclerView() {
        mAdapter = mContentBinding.audioRv.linear()
            .setup(AudioFilesAdapter(getContext()))
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

        mTopBinding.configBtn.setOnClickListener {
            if (mStatus == Constant.STATUS_IDLE) {
                showConfigDialog()
                return@setOnClickListener
            }
            toastShort(R.string.simple_config_disable)
        }

        mTopBinding.deleteAllBtn.setOnClickListener {
            FileUtils.delFile(FileManager.getContentFolderPath())
            updateAudioFileList()
        }

        mBottomBinding.startBtn.setOnClickListener {
            if (mStatus == Constant.STATUS_PAUSE) {
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
                val sampleRate = when (mSampleRate) {
                    Constant.SAMPLE_RATE_8000 -> 8000
                    Constant.SAMPLE_RATE_16000 -> 16000
                    else -> 44100
                }
                val encoding = when (mEncoding) {
                    Constant.ENCODING_16_BIT -> 16
                    else -> 8
                }
                AlertDialog.Builder(getContext())
                    .setMessage("是否按当前采样率：$sampleRate 和位宽：$encoding 来进行转换？（若转换配置和PCM录音配置不同，则转出来的wav音频会失真）")
                    .setPositiveButton("是") { dif, which ->
                        val header = WavUtils.generateHeader(file.length().toInt(), sampleRate, 1, encoding.toShort())
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

    /** 更新配置相关控件 */
    private fun updateConfigView(){
        mTopBinding.audioFormatTv.text = getString(R.string.simple_audio_format).append(
            DictManager.get().getDictBean(Constant.DICT_AUDIO_FORMAT, mAudioFormat)?.value ?: "-"
        )
        mTopBinding.sampleRateTv.text = getString(R.string.simple_sample_rate).append(
            DictManager.get().getDictBean(Constant.DICT_SAMPLE_RATE, mSampleRate)?.value ?: "-"
        )
        mTopBinding.encodingTv.text = getString(R.string.simple_encoding).append(
            DictManager.get().getDictBean(Constant.DICT_ENCODING, mEncoding)?.value ?: "-"
        )
    }

    /** 显示配置弹框 */
    private fun showConfigDialog() {
        val dialog = ConfigDialog(getContext())
        dialog.setData(mAudioFormat, mSampleRate, mEncoding)
        dialog.setOnClickConfirmListener { dif, audioFormat, sampleRate, encoding ->
            mAudioFormat = audioFormat
            mSampleRate = sampleRate
            mEncoding = encoding
            updateConfigView()
            mMinerva?.changeSampleRate(getSampleRateValue(mSampleRate))
            mMinerva?.changeEncoding(mEncoding)
            mMinerva?.changeAudioFormat(getAudioFormat(mAudioFormat))
            dif.dismiss()
        }
        dialog.show()
    }

    override fun initData() {
        super.initData()
        updateAudioFileList()
        initMinerva()
    }

    private fun initMinerva() {
        mMinerva = MinervaAgent.create(MinervaAgent.RECORDING)
            .setChannel(AudioFormat.CHANNEL_IN_MONO)
            .setSampleRate(getSampleRateValue(mSampleRate))
            .setEncoding(mEncoding)
            .setAudioFormat(getAudioFormat(mAudioFormat))
            .setSaveDirPath(FileManager.getContentFolderPath())
            .setOnRecordingStatesListener{
                when (it) {
                    is Idle -> {
                        mStatus = Constant.STATUS_IDLE
                        Log.v("testtag", "空闲")
                    }
                    is Recording -> {
                        mStatus = Constant.STATUS_RECORDING
                        mTopBinding.soundSizeTv.text = getString(R.string.simple_sound_size).append("${it.db} db")
                        Log.d("testtag", "录音中")
                    }
                    is Pause -> {
                        mStatus = Constant.STATUS_PAUSE
                        Log.i("testtag", "暂停")
                    }
                    is Stop -> {
                        mStatus = Constant.STATUS_IDLE
                        Log.i("testtag", "停止")
                    }
                    is Finish -> {
                        mStatus = Constant.STATUS_IDLE
                        toastShort(it.file.absolutePath)
                        updateAudioFileList()
                        Log.v("testtag", "完成")
                    }
                    is Error -> {
                        mStatus = Constant.STATUS_IDLE
                        toastShort("${it.msg} , ${it.t}")
                        Log.e("testtag", "异常")
                    }
                }
                mTopBinding.statusTv.text = getString(R.string.simple_status)
                    .append(DictManager.get().getDictBean(Constant.DICT_STATUS, mStatus)?.value ?: "未知")
                mBottomBinding.startBtn.isEnabled = mStatus != Constant.STATUS_RECORDING
                mBottomBinding.pauseBtn.isEnabled = mStatus == Constant.STATUS_RECORDING
            }
            .build(getContext())
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

    /** 获取音频格式 */
    private fun getAudioFormat(audioFormat: Int): AudioFormats = when (audioFormat) {
        Constant.AUDIO_FORMAT_PCM -> AudioFormats.PCM
        Constant.AUDIO_FORMAT_WAV -> AudioFormats.WAV
        Constant.AUDIO_FORMAT_MP3 -> AudioFormats.MP3
        else -> AudioFormats.PCM
    }

    /** 获取采样率值 */
    private fun getSampleRateValue(sampleRate: Int): Int = when (sampleRate) {
        Constant.SAMPLE_RATE_8000 -> 8000
        Constant.SAMPLE_RATE_16000 -> 16000
        Constant.SAMPLE_RATE_44100 -> 44100
        else -> 44100
    }
}