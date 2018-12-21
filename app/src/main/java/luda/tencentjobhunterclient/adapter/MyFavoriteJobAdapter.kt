package luda.tencentjobhunterclient.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.model.Job

/**
 * Created by luda on 2018/10/27
 * QQ 340071887.
 */
class MyFavoriteJobAdapter(private val mJobs : List<Job>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener : ((Job)->Unit)? = null

    companion object {
        private const val ITEM_TYPE_DATA = 0
        private const val ITEM_TYPE_NO_DATA = 1
    }

    override fun getItemCount() = if(mJobs.isNotEmpty())mJobs.size else 1

    override fun getItemViewType(position: Int) =
            if(mJobs.isEmpty())
                ITEM_TYPE_NO_DATA
            else
                ITEM_TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
            : RecyclerView.ViewHolder = when(viewType){
        ITEM_TYPE_DATA->{
            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_job_history,parent,false)
            view.setOnClickListener{v: View ->
                onItemClickListener?.invoke(mJobs[view.tag as Int])
            }
            val holder = FavoriteJobsViewHolder(view)
            holder.bookmark.setOnClickListener {v: View ->
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
            NoFavoriteJobsViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(getItemViewType(position)){
            ITEM_TYPE_DATA->{
                val job = mJobs[mJobs.size - position-1]
                val resultHolder = holder as FavoriteJobsViewHolder
                if(job.isBookmarked)resultHolder.bookmark.isActivated = true
                resultHolder.titleView.text = job.title
                resultHolder.infoView.text = "${job.location} / ${job.type} / ${job.hiringNumber}人 / ${job.date}"
                resultHolder.bookmark.tag = job
                resultHolder.itemView.tag = mJobs.size - position-1
            }
            ITEM_TYPE_NO_DATA->{
                val noFavoriteJobsViewHolder = holder as NoFavoriteJobsViewHolder
                noFavoriteJobsViewHolder.textView.text = "没有收藏记录"
            }
        }
    }



    private class NoFavoriteJobsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView : TextView get() = itemView.findViewById(R.id.text)
    }

    private class FavoriteJobsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView : TextView get() = itemView.findViewById(R.id.job_query_result_item_title)
        val infoView : TextView get() = itemView.findViewById(R.id.job_query_result_item_info)
        val bookmark : ImageButton get() = itemView.findViewById(R.id.bookmark)
    }

}