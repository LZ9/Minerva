package com.lodz.android.minervademo.ui.simple

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lodz.android.corekt.anko.append
import com.lodz.android.corekt.utils.FileUtils
import com.lodz.android.minervademo.databinding.RvItemAudioFileBinding
import com.lodz.android.pandora.widget.rv.recycler.BaseRecyclerViewAdapter
import com.lodz.android.pandora.widget.rv.recycler.DataVBViewHolder
import java.io.File

/**
 * 音频文件列表适配器
 * @author zhouL
 * @date 2021/10/23
 */
class AudioFilesAdapter(context: Context) :BaseRecyclerViewAdapter<File>(context){

    private var mListener : OnAudioFileListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        DataVBViewHolder(getViewBindingLayout(RvItemAudioFileBinding::inflate, parent))

    override fun onBind(holder: RecyclerView.ViewHolder, position: Int) {
        val bean = getItem(position) ?: return
        if (holder !is DataVBViewHolder) {
            return
        }
        showItem(holder, bean)
    }

    private fun showItem(holder: DataVBViewHolder, bean: File) {
        holder.getVB<RvItemAudioFileBinding>().apply {
            fileNameTv.text = bean.name.append("\n").append("(").append(FileUtils.getFileTotalLengthUnit(bean.absolutePath)).append(")")
            deleteBtn.setOnClickListener {
                mListener?.onClickDelete(bean)
            }
            playBtn.setOnClickListener {
                mListener?.onClickPlay(bean)
            }
            val suffix = FileUtils.getSuffix(bean.absolutePath)
            toWavBtn.visibility = if (suffix.lowercase() == ".pcm") View.VISIBLE else View.GONE
            toWavBtn.setOnClickListener {
                mListener?.onClickPcmToWav(bean)
            }
        }
    }

    /** 设置监听器[listener] */
    fun setOnAudioFileListener(listener: OnAudioFileListener?) {
        mListener = listener
    }

    interface OnAudioFileListener {

        fun onClickPlay(file: File)

        fun onClickDelete(file: File)

        fun onClickPcmToWav(file: File)
    }
}