package luda.tencentjobhunterclient.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.internal.util.HalfSerializer.onNext
import io.reactivex.subjects.PublishSubject
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.model.IQuerySource
import luda.tencentjobhunterclient.model.JobQueryResult

/**
 * Created by luda on 2018/7/29
 * QQ 340071887.
 */
class QueryAdapter(private val source: IQuerySource) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    //val onItemClickSubject = PublishSubject.create<JobQueryResult>()
    var onItemClickListener : ((JobQueryResult)->Unit)? = null

    override fun getItemCount(): Int = source.count
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val query = source.getItemAt(position)
        val viewHolder = holder as QueryDataViewHolder
        viewHolder.nameView.text = "查询${source.getNumberAt(position)}"
        viewHolder.queryExpressionView.text = "表达式：${query.queryExpression}"
        viewHolder.resultCountView.text = "结果个数：${query.maxLength}"
        viewHolder.itemView.tag  = query
    }
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_job_query,parent,false)
        view.setOnClickListener{v: View ->
            onItemClickListener?.invoke(v.tag as JobQueryResult)
        }
        return QueryDataViewHolder(view)
    }

    private class QueryDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView : TextView = itemView.findViewById(R.id.query_item_name)
        val queryExpressionView : TextView = itemView.findViewById(R.id.query_item_expression)
        val resultCountView : TextView = itemView.findViewById(R.id.query_item_result_count)
    }

}