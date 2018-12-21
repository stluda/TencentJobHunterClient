package luda.tencentjobhunterclient.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.model.IQueryResultSource
import luda.tencentjobhunterclient.model.Job
import luda.tencentjobhunterclient.model.JobQueryResult
import luda.tencentjobhunterclient.model.QueryResultFilterHolder
import luda.tencentjobhunterclient.viewholder.LoadMoreViewHolder

/**
 * Created by luda on 2018/4/5
 * QQ 340071887.
 */
class QueryResultAdapter(private val mJobQueryResult: IQueryResultSource,private val mIsTask:Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onAddTaskButtonClickListener: ((IQueryResultSource) -> Unit)? = null
    var onItemClickedListener : ((Int)->Unit)? = null
    var onBookmarkClickedListener : ((Boolean)->Unit)? = null

    companion object {
        private const val ITEM_TYPE_DATA = 0
        private const val ITEM_TYPE_FOOTER = 1
        private const val ITEM_TYPE_HEADER = 2
    }



    private var mLastItemCount =  2 + mJobQueryResult.viewSize
    private var mLoadMoreText = "加载更多"

    private var mHasFilter : Boolean = false
    private lateinit var mIndexByFilter : ArrayList<Int>

    val hasFilter get() = mHasFilter

    init {
        if(!mJobQueryResult.canLoadMore)mLoadMoreText = "没有更多数据了"
    }

    private fun getJobItemAt(index : Int) : Job
        = mJobQueryResult.getItemAt(getRealIndex(index))

    private fun getRealIndex(index: Int)
        = if(mHasFilter)mIndexByFilter[index] else index

    fun setFilter(filterHolder: QueryResultFilterHolder){
        if(!filterHolder.isEmpty){
            mIndexByFilter = mJobQueryResult.filter(filterHolder,0,mJobQueryResult.viewSize)
            mLoadMoreText = "过滤器状态下不能加载更多，请先取消过滤器"
        }
        mHasFilter = !filterHolder.isEmpty
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int{
        val extra = if(mJobQueryResult.totalCount>5) 1 else 0
        return extra + 1 + if(mHasFilter)mIndexByFilter.size else mJobQueryResult.viewSize
    }



    //根据是否是底部分别返回是底部界面或常规数据项
    override fun getItemViewType(position: Int) =  if(mJobQueryResult.totalCount>5) {
        when (position) {
            0 -> ITEM_TYPE_HEADER
            itemCount - 1 -> ITEM_TYPE_FOOTER
            else -> ITEM_TYPE_DATA
        }
    }
    else{
        when (position) {
            0 -> ITEM_TYPE_HEADER
            else -> ITEM_TYPE_DATA
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        when(viewType){
            ITEM_TYPE_HEADER->{
                val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_job_query_result_header,parent,false)
                val holder = JobQueryResultHeaderViewHolder(view)
                holder.createTaskButton.setOnClickListener {
                    onAddTaskButtonClickListener?.invoke(mJobQueryResult)
                }
                return holder
            }
            ITEM_TYPE_FOOTER->{
                val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_footer_load_more,parent,false)
                val loadMoreTextView = view.findViewById<TextView>(R.id.tv_load_more)
                loadMoreTextView.text = mLoadMoreText
                return LoadMoreViewHolder(view)
            }
            else->{
                val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_job_query_result,parent,false)
                view.setOnClickListener{v:View->
                    onItemClickedListener?.invoke(v.tag as Int)
                }
                val holder = JobQueryResultDataViewHolder(view)
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
                return holder
            }
        }
    }



    //加载中期间调用此方法
    fun notifyStateLoading(){
        mLoadMoreText = "加载中"
        notifyItemChanged(itemCount-1)
    }

    //加载出错
    fun notifyStateLoadError(){
        try {
            mLoadMoreText = "网络错误，加载失败，请稍后重试"
            notifyItemChanged(itemCount-1)
        }
        catch (ex:Exception){
            Toast.makeText(MyApplication.instance,"网络错误，加载失败，请稍后重试",Toast.LENGTH_LONG).show()
        }

    }

    fun notifyStateCacheMode(){
        try {
            mLoadMoreText = "快照模式无法加载更多数据"
            notifyItemChanged(itemCount-1)
        }
        catch(e:Throwable){
        }
    }

    fun notifyItemChanged(job:Job){
        val index = mJobQueryResult.indexOf(job)
        if(index<0)return

        if(mHasFilter){
            for(i in 0 until mIndexByFilter.size){
                if(mIndexByFilter[i]==index){
                    notifyItemChanged(i+1)
                    return
                }
            }
        }
        else{
            notifyItemChanged(index+1)
        }
    }



    fun notifyJobQueryResultInserted(startIndex:Int, count:Int)
    {
        if(itemCount<mLastItemCount||mLastItemCount==2){
            notifyDataSetChanged()
        }
        else{
            notifyItemRangeInserted(startIndex,count)
        }
        mLoadMoreText =
                if(mHasFilter)
                    "过滤器状态下不能加载更多，请先取消过滤器"
                else if(mJobQueryResult.canLoadMore)
                    "加载更多" else "没有更多数据了"

        try {
            notifyItemChanged(itemCount-1)
        }
        catch(e:Throwable){
        }



        mLastItemCount = itemCount
    }

    fun notifyJobQueryResultInserted(jobQueryResult: JobQueryResult)
    {
        notifyJobQueryResultInserted(jobQueryResult.startIndex,jobQueryResult.size)
    }



    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
        super.onViewRecycled(holder)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(getItemViewType(position)){
            ITEM_TYPE_HEADER->{
                val headerViewHolder = holder as JobQueryResultHeaderViewHolder
                headerViewHolder.headerView.text = "共"+ mJobQueryResult.totalCount +"个结果"
                headerViewHolder.contentView.text = "查询表达式：\n"+ mJobQueryResult.queryExpression
                if(mIsTask)headerViewHolder.createTaskButton.visibility = View.GONE
            }
            ITEM_TYPE_DATA->{
                val job = getJobItemAt(position-1)
                val resultHolder = holder as JobQueryResultDataViewHolder
                resultHolder.setRead(job.hasBeenSeen) // "(已阅)" else ""
                if(job.isBookmarked)resultHolder.bookmark.isActivated = true
                resultHolder.titleView.text =  job.title
                resultHolder.infoView.text = "${job.location} / ${job.type} / ${job.hiringNumber}人 / ${job.date}"
                resultHolder.bookmark.tag = job
                resultHolder.itemView.tag = position-1
            }
            ITEM_TYPE_FOOTER->{
                val loadMoreViewHolder = holder as LoadMoreViewHolder
                loadMoreViewHolder.contentView.text = mLoadMoreText
            }
        }
    }

    private class JobQueryResultDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val titleView : TextView get() = itemView.findViewById(R.id.job_query_result_item_title)
        val infoView : TextView get() = itemView.findViewById(R.id.job_query_result_item_info)
        val bookmark : ImageButton get() = itemView.findViewById(R.id.bookmark)

        fun setRead(flag:Boolean){
            itemView.findViewById<TextView>(R.id.job_query_result_item_read).visibility = if(flag) View.VISIBLE else View.INVISIBLE
            itemView.findViewById<TextView>(R.id.job_query_result_item_unread).visibility = if(flag) View.INVISIBLE else View.VISIBLE
        }
    }

    private class JobQueryResultHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {
        //tv_job_query_result_header_info
        private val mHeaderView : TextView = itemView.findViewById(R.id.tv_job_query_result_header_info)
        val headerView : TextView get() = mHeaderView

        private val mContentView : TextView = itemView.findViewById(R.id.tv_job_query_expression)
        val contentView : TextView get() = mContentView

        private val mCreateTaskButton : Button = itemView.findViewById(R.id.btn_create_task_from_result)
        val createTaskButton : Button get() = mCreateTaskButton
    }
}