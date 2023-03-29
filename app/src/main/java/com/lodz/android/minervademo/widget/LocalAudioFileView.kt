package com.lodz.android.minervademo.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.lodz.android.corekt.anko.append
import com.lodz.android.corekt.anko.toastShort
import com.lodz.android.corekt.file.getFileSuffix
import com.lodz.android.corekt.utils.FileUtils
import com.lodz.android.minerva.bean.AudioFormats
import com.lodz.android.minervademo.BuildConfig
import com.lodz.android.minervademo.R
import com.lodz.android.minervademo.databinding.ViewLocalAudioFileBinding
import com.lodz.android.minervademo.ui.adapter.AudioFilesAdapter
import com.lodz.android.pandora.rx.subscribe.single.BaseSingleObserver
import com.lodz.android.pandora.rx.utils.RxUtils
import com.lodz.android.pandora.utils.viewbinding.bindingLayout
import com.lodz.android.pandora.widget.rv.anko.linear
import com.lodz.android.pandora.widget.rv.anko.setup
import io.reactivex.rxjava3.core.Observable
import java.io.File

/**
 * 本地音频文件控件
 * @author zhouL
 * @date 2023/2/17
 */
@SuppressLint("NotifyDataSetChanged")
class LocalAudioFileView :FrameLayout{

    private val mBinding: ViewLocalAudioFileBinding by context.bindingLayout(ViewLocalAudioFileBinding::inflate)

    private var mFilePath = ""

    /** 音频文件适配器 */
    private lateinit var mAdapter: AudioFilesAdapter

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        addView(mBinding.root)
        findViews()
        setListeners()
    }

    private fun findViews() {
        mAdapter = mBinding.audioRv.linear().setup(AudioFilesAdapter(context))
    }

    private fun setListeners() {
        mBinding.deleteAllBtn.setOnClickListener {
            FileUtils.delFile(mFilePath)
            updateAudioFileList()
        }

        mAdapter.setOnAudioFileListener(object : AudioFilesAdapter.OnAudioFileListener {
            override fun onClickPlay(file: File) {
                if (file.absolutePath.getFileSuffix().lowercase() == AudioFormats.PCM.suffix){
                    toastShort(R.string.simple_pcm_cannot_play)
                    return
                }
                val intent = Intent(Intent.ACTION_VIEW)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val uri = FileProvider.getUriForFile(context, BuildConfig.AUTHORITY, file)
                    intent.setDataAndType(uri, "audio/*")
                } else {
                    intent.setDataAndType(file.toUri(), "audio/*")
                }
                context.startActivity(intent)
            }

            override fun onClickDelete(file: File) {
                FileUtils.delFile(file.absolutePath)
                updateAudioFileList()
            }
        })
    }

    fun setDeleteAllBtnEnabled(isEnabled: Boolean) {
        mBinding.deleteAllBtn.isEnabled = isEnabled
    }

    fun setFilePath(filePath: String) {
        if (filePath.isEmpty()) {
            throw IllegalArgumentException("filePath is empty")
        }
        mFilePath = filePath
        mBinding.savePathTv.text = context.getString(R.string.simple_save_path).append(filePath)
    }

    /** 更新音频文件列表数据 */
    fun updateAudioFileList() {
        showStatusLoading()
        Observable.fromIterable(FileUtils.getFileList(mFilePath))
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
            .subscribe(
                BaseSingleObserver.action(
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

    private fun showStatusNoData(){
        mBinding.audioLoadingLayout.visibility = View.GONE
        mBinding.audioNoDataLayout.visibility = View.VISIBLE
        mBinding.audioRv.visibility = View.GONE
    }

    private fun showStatusCompleted(){
        mBinding.audioLoadingLayout.visibility = View.GONE
        mBinding.audioNoDataLayout.visibility = View.GONE
        mBinding.audioRv.visibility = View.VISIBLE
    }

    private fun showStatusLoading(){
        mBinding.audioLoadingLayout.visibility = View.VISIBLE
        mBinding.audioNoDataLayout.visibility = View.GONE
        mBinding.audioRv.visibility = View.GONE
    }

}