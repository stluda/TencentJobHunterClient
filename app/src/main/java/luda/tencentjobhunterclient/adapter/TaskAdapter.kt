package luda.tencentjobhunterclient.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.model.Task
import luda.tencentjobhunterclient.viewmodel.ITaskFragmentViewModel
import java.text.SimpleDateFormat
import android.view.MotionEvent
import android.R.attr.startY
import android.R.attr.startX
import android.graphics.Point


/**
 * Created by luda on 2018/6/16
 * QQ 340071887.
 */
class TaskAdapter(private val mModel: ITaskFragmentViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val dateFormat = SimpleDateFormat("YYYY-MM-dd HH:mm")
        private const val ITEM_TYPE_DATA = 0
        private const val ITEM_TYPE_FOOTER = 1
    }

    //val onTaskItemClickedSubject = PublishSubject.create<Task>()
    //val onTaskItemLongClickedSubject = PublishSubject.create<Pair<Task,View>>()
    //val onAddTaskItemClickedSubject = PublishSubject.create<Boolean>()
    var onAddTaskItemClickedListener : (()->Unit)? = null
    var onTaskItemClickedListener : ((Task, View)->Unit)? = null
    var onTaskItemLongClickedListener : ((Task, View, Point)->Unit)? = null

    override fun getItemCount(): Int = mModel.taskCount + 1

    override fun getItemViewType(position: Int) =
            if(position==itemCount-1) ITEM_TYPE_FOOTER else ITEM_TYPE_DATA

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(getItemViewType(position)) {
            ITEM_TYPE_DATA -> {
                val task = mModel.getTaskAt(position)
                val viewHolder = holder as TaskDataViewHolder
                viewHolder.nameView.text = task.name
                viewHolder.expressionView.text = task.queryExpression
                viewHolder.newTaskCountView.text = String.format("%d个新查询结果",task.queryResultTotalCount)
                viewHolder.expireTimeView.text = String.format("截止时间：%s",dateFormat.format(task.expireTime))
                viewHolder.itemView.tag = task
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var x=0
        var y=0
        when(viewType){
            ITEM_TYPE_DATA->{
                val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_task,parent,false)
                view.setOnClickListener{v: View ->
                    //onTaskItemClickedSubject.onNext(v.tag as Task)
                    onTaskItemClickedListener?.invoke(v.tag as Task,v)
                }
                view.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE -> {
                            x = event.x.toInt()
                            y = event.y.toInt()
                        }
                    }
                    return@setOnTouchListener false
                }
                view.setOnLongClickListener {v: View ->
                    //onTaskItemLongClickedSubject.onNext(Pair(v.tag as Task,v))
                    onTaskItemLongClickedListener?.invoke(v.tag as Task,v,Point(x,y))
                    true
                }
                return TaskDataViewHolder(view)
            }
            ITEM_TYPE_FOOTER->{
                val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_task_add,parent,false)
                val holder = AddTaskViewHolder(view)
                holder.addTaskView.setOnClickListener{
                    onAddTaskItemClickedListener?.invoke()
                    //onAddTaskItemClickedSubject.onNext(true)
                }
                return holder
            }
            else->throw NotImplementedError()
        }
    }


    private class AddTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addTaskView : ImageView = itemView.findViewById(R.id.btn_add_task)
    }

    private class TaskDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mNameView : TextView
        private val mExpressionView : TextView
        private val mNewTaskCountView : TextView
        private val mExpireTimeView : TextView

        val nameView get() = mNameView
        val expressionView get() = mExpressionView
        val newTaskCountView get() = mNewTaskCountView
        val expireTimeView get() = mExpireTimeView

        init {
            mNameView = itemView.findViewById(R.id.task_item_name)
            mExpressionView = itemView.findViewById(R.id.task_item_expression)
            mNewTaskCountView = itemView.findViewById(R.id.task_item_new_task_count)
            mExpireTimeView = itemView.findViewById(R.id.task_item_expire_time)
        }
    }
}