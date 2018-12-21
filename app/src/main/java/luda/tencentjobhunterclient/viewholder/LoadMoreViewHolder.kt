package luda.tencentjobhunterclient.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import luda.tencentjobhunterclient.R

/**
 * Created by luda on 2018/4/18
 * QQ 340071887.
 */
class LoadMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val mContentView : TextView = itemView.findViewById(R.id.tv_load_more)
    val contentView : TextView get() = mContentView
}