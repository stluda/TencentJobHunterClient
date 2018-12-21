package luda.tencentjobhunterclient.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import io.reactivex.subjects.PublishSubject
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.model.Job

/**
 * Created by luda on 2018/7/29
 * QQ 340071887.
 */
class JobHistoryAdapter(private val mJobHistory : List<Job>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener : ((Job)->Unit)? = null

    companion object {
        private const val ITEM_TYPE_DATA = 0
        private const val ITEM_TYPE_HEADER = 1
        private const val ITEM_TYPE_NOHISTORY = 2
    }

    override fun getItemCount() = mJobHistory.size+1

    override fun getItemViewType(position: Int) = when(position){
        0 -> if(itemCount>1) ITEM_TYPE_HEADER else ITEM_TYPE_NOHISTORY
        else -> ITEM_TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
            : RecyclerView.ViewHolder = when(viewType){
        ITEM_TYPE_HEADER->{
            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_job_history_header,parent,false)
            JobHistoryHeaderViewHolder(view)
        }
        ITEM_TYPE_DATA->{
            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_job_history,parent,false)
            view.setOnClickListener{v:View->
                onItemClickListener?.invoke(mJobHistory[view.tag as Int])
            }
            val holder = JobHistoryDataViewHolder(view)
            holder.bookmark.setOnClickListener {v:View->
                holder.bookmark.isActivated = !holder.bookmark.isActivated
                val job = v.tag as Job
                if(holder.bookmark.isActivated){
                    job.addToMyFavorites()
                }
                else{
                    job.deleteFromMyFavorites()
                }
            }
            holder
        }
        else->{
            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_simple_text,parent,false)
            NoJobHistoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(getItemViewType(position)){
            ITEM_TYPE_DATA->{
                val job = mJobHistory[mJobHistory.size - position]
                val resultHolder = holder as JobHistoryDataViewHolder
                if(job.isBookmarked)resultHolder.bookmark.isActivated = true
                resultHolder.titleView.text = job.title
                resultHolder.infoView.text = "${job.location} / ${job.type} / ${job.hiringNumber}人 / ${job.date}"
                resultHolder.bookmark.tag = job
                resultHolder.itemView.tag = mJobHistory.size - position
            }
            ITEM_TYPE_NOHISTORY->{
                val loadMoreViewHolder = holder as NoJobHistoryViewHolder
                loadMoreViewHolder.textView.text = "没有历史记录"
            }
        }
    }



    private class JobHistoryHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clearHistoryView : TextView get() = itemView.findViewById(R.id.btn_clear_history)
    }

    private class NoJobHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView : TextView get() = itemView.findViewById(R.id.text)
    }

    private class JobHistoryDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView : TextView get() = itemView.findViewById(R.id.job_query_result_item_title)
        val infoView : TextView get() = itemView.findViewById(R.id.job_query_result_item_info)
        val bookmark : ImageButton get() = itemView.findViewById(R.id.bookmark)
    }

}