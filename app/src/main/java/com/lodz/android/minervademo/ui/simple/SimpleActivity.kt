package com.lodz.android.minervademo.ui.simple

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lodz.android.corekt.anko.*
import com.lodz.android.corekt.utils.FileUtils
import com.lodz.android.minerva.RecordManager
import com.lodz.android.minerva.recorder.RecordConfig
import com.lodz.android.minerva.recorder.RecordHelper
import com.lodz.android.minerva.recorder.listener.RecordStateListener
import com.lodz.android.minervademo.App
import com.lodz.android.minervademo.BuildConfig
import com.lodz.android.minervademo.utils.FileManager
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.config.Constant
import com.lodz.android.minervademo.databinding.ActivitySimpleBottomBinding
import com.lodz.android.minervademo.databinding.ActivitySimpleContentBinding
import com.lodz.android.minervademo.databinding.ActivitySimpleTopBinding
import com.lodz.android.minervademo.ui.AudioFilesAdapter
import com.lodz.android.minervademo.ui.dialog.ConfigDialog
import com.lodz.android.minervademo.utils.DictManager
import com.lodz.android.pandora.base.activity.BaseSandwichActivity
import com.lodz.android.pandora.rx.subscribe.single.BaseSingleObserver
import com.lodz.android.pandora.rx.utils.RxUtils
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import io.reactivex.rxjava3.core.Observable
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.constructPermissionsRequest
import java.io.File
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class SimpleActivity : BaseSandwichActivity() {

    /** 状态 */
    private var mStatus = Constant.STATUS_IDLE
    /** 音频格式 */
    private var mAudioFormat = Constant.AUDIO_FORMAT_WAV
    /** 采样率 */
    private var mSampleRate = Constant.SAMPLE_RATE_16000
    /** 音频位宽 */
    private var mEncoding = Constant.ENCODING_16_BIT

    private lateinit var mAdapter: AudioFilesAdapter

    private val mRecordManager = RecordManager.getInstance()

    private val hasRecordAudioPermissions by lazy {
        constructPermissionsRequest(
            Manifest.permission.RECORD_AUDIO,// 手机状态
            onShowRationale = ::onShowRationaleBeforeRequest,
            onPermissionDenied = ::onDenied,
            onNeverAskAgain = ::onNeverAsk,
            requiresPermission = ::onRequestPermission
        )
    }

    private val hasWriteExternalStoragePermissions by lazy {
        constructPermissionsRequest(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,// 存储卡读写
            onShowRationale = ::onShowRationaleBeforeRequest,
            onPermissionDenied = ::onDenied,
            onNeverAskAgain = ::onNeverAsk,
            requiresPermission = ::onRequestPermission
        )
    }

    private val hasReadExternalStoragePermissions by lazy {
        constructPermissionsRequest(
            Manifest.permission.READ_EXTERNAL_STORAGE,// 存储卡读写
            onShowRationale = ::onShowRationaleBeforeRequest,
            onPermissionDenied = ::onDenied,
            onNeverAskAgain = ::onNeverAsk,
            requiresPermission = ::onRequestPermission
        )
    }

    private val mTopBinding: ActivitySimpleTopBinding by bindingLayout(ActivitySimpleTopBinding::inflate)
    private val mContentBinding: ActivitySimpleContentBinding by bindingLayout(ActivitySimpleContentBinding::inflate)
    private val mBottomBinding: ActivitySimpleBottomBinding by bindingLayout(ActivitySimpleBottomBinding::inflate)

    override fun getTopViewBindingLayout(): View = mTopBinding.root
    override fun getViewBindingLayout(): View = mContentBinding.root
    override fun getBottomViewBindingLayout(): View = mBottomBinding.root

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
        mTopBinding.statusTv.text = getString(R.string.main_status)
            .append(DictManager.get().getDictBean(Constant.DICT_STATUS, mStatus)?.value ?: "未知")
        mTopBinding.savePathTv.text = getString(R.string.main_save_path).append(FileManager.getContentFolderPath())
        mBottomBinding.startBtn.isEnabled = true
        mBottomBinding.pauseBtn.isEnabled = false
    }

    private fun setTitleBar() {
        mTopBinding.titleBarLayout.needBackButton(false)
        mTopBinding.titleBarLayout.setBackgroundColor(getColorCompat(R.color.color_00a1d5))
        mTopBinding.titleBarLayout.setTitleName(R.string.app_name)
    }

    private fun initRecyclerView() {
        mAdapter = AudioFilesAdapter(getContext())
        val layoutManager = LinearLayoutManager(getContext())
        layoutManager.orientation = RecyclerView.VERTICAL
        mContentBinding.audioRv.layoutManager = layoutManager
        mAdapter.onAttachedToRecyclerView(mContentBinding.audioRv)// 如果使用网格布局请设置此方法
        mContentBinding.audioRv.setHasFixedSize(true)
        mContentBinding.audioRv.adapter = mAdapter
    }

    override fun onDataRefresh() {
        super.onDataRefresh()
        updateAudioFileList()
        setSwipeRefreshFinish()
    }

    override fun setListeners() {
        super.setListeners()

        mTopBinding.configBtn.setOnClickListener {
            if (mStatus == Constant.STATUS_IDLE) {
                showConfigDialog()
                return@setOnClickListener
            }
            toastShort(R.string.main_config_disable)
        }

        mTopBinding.deleteAllBtn.setOnClickListener {
            FileUtils.delFile(FileManager.getContentFolderPath())
            updateAudioFileList()
        }

        mBottomBinding.startBtn.setOnClickListener {
            if (mStatus == Constant.STATUS_PAUSE) {
                mRecordManager.resume()
            } else {
                mRecordManager.start()
            }
        }

        mBottomBinding.stopBtn.setOnClickListener {
            mRecordManager.stop()
        }

        mBottomBinding.pauseBtn.setOnClickListener {
            mRecordManager.pause()
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
        })


        mRecordManager.setRecordStateListener(object : RecordStateListener {
            override fun onStateChange(state: RecordHelper.RecordState) {
                when (state) {
                    RecordHelper.RecordState.PAUSE -> {// 暂停中
                        mStatus = Constant.STATUS_PAUSE
                    }
                    RecordHelper.RecordState.IDLE -> {// 空闲中
                        mStatus = Constant.STATUS_IDLE
                    }
                    RecordHelper.RecordState.RECORDING -> {//录音中
                        mStatus = Constant.STATUS_RECORDING
                    }
                    RecordHelper.RecordState.STOP -> {//停止
                        mStatus = Constant.STATUS_IDLE
                    }
                    RecordHelper.RecordState.FINISH -> {//录音结束
                        mStatus = Constant.STATUS_IDLE
                    }
                }
                mTopBinding.statusTv.text = getString(R.string.main_status)
                    .append(DictManager.get().getDictBean(Constant.DICT_STATUS, mStatus)?.value ?: "未知")

                mBottomBinding.startBtn.isEnabled = mStatus != Constant.STATUS_RECORDING
                mBottomBinding.pauseBtn.isEnabled = mStatus == Constant.STATUS_RECORDING
            }

            override fun onError(error: String?) {
                toastShort(error ?: "未知异常")
            }
        })

        mRecordManager.setRecordSoundSizeListener {
            mTopBinding.soundSizeTv.text = getString(R.string.main_sound_size).append("$it db")
        }

        mRecordManager.setRecordResultListener {
            toastShort(it.absolutePath)
            updateAudioFileList()
        }

        mRecordManager.setRecordFftDataListener {

        }
    }

    /** 更新配置相关控件 */
    private fun updateConfigView(){
        mTopBinding.audioFormatTv.text = getString(R.string.main_audio_format).append(
            DictManager.get().getDictBean(Constant.DICT_AUDIO_FORMAT, mAudioFormat)?.value ?: "-"
        )
        mTopBinding.sampleRateTv.text = getString(R.string.main_sample_rate).append(
            DictManager.get().getDictBean(Constant.DICT_SAMPLE_RATE, mSampleRate)?.value ?: "-"
        )
        mTopBinding.encodingTv.text = getString(R.string.main_encoding).append(
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
            updateRecordConfig()
            dif.dismiss()
        }
        dialog.show()
    }

    override fun initData() {
        super.initData()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0以上的手机对权限进行动态申请
            onRequestPermission()
        } else {
            init()
        }
    }

    /** 初始化 */
    private fun init() {
        updateAudioFileList()
        initRecord()
    }

    private fun initRecord() {
        mRecordManager.init(App.get(), true)
        updateRecordConfig()
        mRecordManager.changeRecordDir(FileManager.getContentFolderPath())
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

    /** 更新录音配置 */
    private fun updateRecordConfig(){
        mRecordManager.changeFormat(
            when (mAudioFormat) {
                Constant.AUDIO_FORMAT_WAV -> RecordConfig.RecordFormat.WAV
                Constant.AUDIO_FORMAT_MP3 -> RecordConfig.RecordFormat.MP3
                else -> RecordConfig.RecordFormat.PCM
            }
        )
        mRecordManager.changeRecordConfig(
            when (mSampleRate) {
                Constant.SAMPLE_RATE_8000 -> mRecordManager.getRecordConfig().setSampleRate(8000)
                Constant.SAMPLE_RATE_16000 -> mRecordManager.getRecordConfig().setSampleRate(16000)
                else -> mRecordManager.getRecordConfig().setSampleRate(44100)
            }
        )
        mRecordManager.changeRecordConfig(
            when (mEncoding) {
                Constant.ENCODING_8_BIT -> mRecordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_8BIT)
                else -> mRecordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT)
            }
        )
    }

    /** 权限申请成功 */
    private fun onRequestPermission() {
        if (!isPermissionGranted(Manifest.permission.RECORD_AUDIO)){
            hasRecordAudioPermissions.launch()
            return
        }
        if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            hasWriteExternalStoragePermissions.launch()
            return
        }
        if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)){
            hasReadExternalStoragePermissions.launch()
            return
        }
        init()
    }

    /** 用户拒绝后再次申请前告知用户为什么需要该权限 */
    private fun onShowRationaleBeforeRequest(request: PermissionRequest) {
        request.proceed()//请求权限
    }

    /** 被拒绝 */
    private fun onDenied() {
        onRequestPermission()//申请权限
    }

    /** 被拒绝并且勾选了不再提醒 */
    private fun onNeverAsk() {
        toastShort(R.string.main_check_permission_tips)
        showPermissionCheckDialog()
        goAppDetailSetting()
    }

    /** 显示权限核对弹框 */
    private fun showPermissionCheckDialog(){
        val checkDialog = AlertDialog.Builder(getContext())
            .setMessage(R.string.main_check_permission_title)
            .setPositiveButton(R.string.main_check_permission_confirm){dialog,  which->
                onRequestPermission()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.main_check_permission_unconfirmed){dialog,  which->
                goAppDetailSetting()
            }
            .setOnCancelListener {
                toastShort(R.string.main_check_permission_cancel)
                App.get().exit()
            }
            .create()
        checkDialog.setCanceledOnTouchOutside(false)
        checkDialog.show()
    }
}