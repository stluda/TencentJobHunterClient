package luda.tencentjobhunterclient.adapter

import android.nfc.Tag
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import io.reactivex.subjects.PublishSubject
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.model.QueryResultFilterHolder

/**
 * Created by luda on 2018/6/27
 * QQ 340071887.
 */
class QueryResultFilterAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items : Array<FilterItem>

    val filters = QueryResultFilterHolder()

    var filterChangedListener : ((QueryResultFilterHolder)->Unit)? = null

    init {
        items = arrayOf(
                FilterItem("可见级别",FilterItem.FilterTagCategory.TOPIC),
                FilterItem("只显示未读",FilterItem.FilterTagCategory.VISIBLE_LEVEL),
                //FilterItem("全部",FilterItem.FilterTagCategory.VISIBLE_LEVEL),

                FilterItem("类别",FilterItem.FilterTagCategory.TOPIC),
                FilterItem("技术类",FilterItem.FilterTagCategory.TYPE),
                FilterItem("产品/项目类",FilterItem.FilterTagCategory.TYPE),
                FilterItem("市场类",FilterItem.FilterTagCategory.TYPE),
                FilterItem("设计类",FilterItem.FilterTagCategory.TYPE),
                FilterItem("职能类",FilterItem.FilterTagCategory.TYPE),
                FilterItem("内容编辑类",FilterItem.FilterTagCategory.TYPE),
                FilterItem("客户服务类",FilterItem.FilterTagCategory.TYPE),

                FilterItem("地点",FilterItem.FilterTagCategory.TOPIC),
                FilterItem("深圳",FilterItem.FilterTagCategory.LOCATION),
                FilterItem("北京",FilterItem.FilterTagCategory.LOCATION),
                FilterItem("上海",FilterItem.FilterTagCategory.LOCATION),
                FilterItem("广州",FilterItem.FilterTagCategory.LOCATION),
                FilterItem("成都",FilterItem.FilterTagCategory.LOCATION),
                FilterItem("美国",FilterItem.FilterTagCategory.LOCATION),
                FilterItem("昆明",FilterItem.FilterTagCategory.LOCATION),
                FilterItem("香港",FilterItem.FilterTagCategory.LOCATION),
                FilterItem("长春",FilterItem.FilterTagCategory.LOCATION)
        )
    }

    companion object {
        private const val ITEM_TYPE_FILTER = 0
        private const val ITEM_TYPE_TOPIC = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder
        = when(viewType){
            ITEM_TYPE_FILTER->{
                val view = LayoutInflater.from(parent!!.context).inflate(R.layout.list_item_filter_drawer,parent,false)
                QueryResultFilterViewHolder(view)
            }
            ITEM_TYPE_TOPIC-> {
                val view = LayoutInflater.from(parent!!.context).inflate(R.layout.list_item_filter_drawer_topic_header,parent,false)
                QueryResultFilterTopicViewHolder(view)
            }
            else->throw NotImplementedError()
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(getItemViewType(position)){
            ITEM_TYPE_FILTER->(holder as QueryResultFilterViewHolder).bind(items[position],false)
            ITEM_TYPE_TOPIC-> (holder as QueryResultFilterTopicViewHolder).label.text = items[position].content
        }
    }

    override fun getItemCount(): Int
        = items.size

    override fun getItemViewType(position: Int): Int
        = when(items[position].category){
            FilterItem.FilterTagCategory.TOPIC-> ITEM_TYPE_TOPIC
            else-> ITEM_TYPE_FILTER
        }

    private class FilterItem(val content:String,val category: FilterTagCategory){
        enum class FilterTagCategory{
            VISIBLE_LEVEL,
            TYPE,
            LOCATION,
            TOPIC
        }
    }

    private inner class QueryResultFilterTopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: TextView = itemView.findViewById(R.id.topic) as TextView
    }

    private inner class QueryResultFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) ,CompoundButton.OnCheckedChangeListener{
        val label: TextView = itemView.findViewById(R.id.filter_label) as TextView
        val checkBox: CheckBox = itemView.findViewById(R.id.filter_checkbox) as CheckBox
        lateinit var filter : FilterItem

        var isChecked : Boolean
            set(value) {
                checkBox.setOnCheckedChangeListener(null)
                checkBox.isChecked = value
                checkBox.setOnCheckedChangeListener(this)
            }
            get() = checkBox.isChecked


        init {
            itemView.setOnClickListener { checkBox.performClick() }
        }

        fun bind(filter:FilterItem,checked: Boolean){
            label.text = filter.content
            checkBox.contentDescription = filter.content
            isChecked = checked
            this.filter = filter
        }

        override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
            if(checked){
                when(filter.category){
                    FilterItem.FilterTagCategory.VISIBLE_LEVEL -> filters.enableUnreadOnly = checked
                    FilterItem.FilterTagCategory.LOCATION->filters.addLocationFilter(filter.content)
                    FilterItem.FilterTagCategory.TYPE->filters.addTypeFilter(filter.content)
                }
            }else{
                when(filter.category){
                    FilterItem.FilterTagCategory.VISIBLE_LEVEL -> filters.enableUnreadOnly = checked
                    FilterItem.FilterTagCategory.LOCATION->filters.removeLocationFilter(filter.content)
                    FilterItem.FilterTagCategory.TYPE->filters.removeTypeFilter(filter.content)
                }
            }
            filterChangedListener?.invoke(filters)
        }


    }
}