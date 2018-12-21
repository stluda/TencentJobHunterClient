package luda.tencentjobhunterclient.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frag_task_add.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.model.JobQueryResult
import luda.tencentjobhunterclient.model.Task
import luda.tencentjobhunterclient.util.DialogUtils
import luda.tencentjobhunterclient.util.MyRxLifeCycle
import luda.tencentjobhunterclient.util.RequestHelper
import luda.tencentjobhunterclient.util.mySubscribe
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel
import luda.tencentjobhunterclient.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * Created by luda on 2018/7/31
 * QQ 340071887.
 */
class AddTaskFragment : BaseNavigationFragment() {
    companion object {
        const val TAG = "AddTaskFragment"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val timeFormat = SimpleDateFormat("HH:mm")
    }

    override val navTag: String
        get() = TAG

    override val groupId: Int
        get() = 1

    var queryNo : Int = -1
    lateinit var queryResult : JobQueryResult

    private lateinit var mTaskViewModel : TaskViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater!!.inflate(R.layout.frag_task_add, container, false)


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val jobQueryResultViewModel = ViewModelProviders.of(activity).get(JobQueryResultViewModel::class.java)
        mTaskViewModel = ViewModelProviders.of(activity).get(TaskViewModel::class.java)

        queryNo = arguments.getInt(KeyConstants.QUERY_NO,-1)

        val result = jobQueryResultViewModel.getJobQueryResult(queryNo)
        if(result==null){
            //如果在查询页删除了查询，则会导致找不到查询，这种情况直接返回任务fragment顶层
            //jumpToFragment(TaskFragment())
            toTopFragment()
        }
        else{
            queryResult = result
            loadUI()
        }
        //queryResult = jobQueryResultViewModel.getJobQueryResult(queryNo)

    }

    private fun loadUI(){
        toolbar.setTitle(R.string.add_task)
        (activity as MainActivity).setToolBar2(toolbar)
        setHasOptionsMenu(true)

        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.MONTH,2)

        tv_expression.text = queryResult.queryExpression

        edt_expire_time_date.setText(dateFormat.format(calendar.time))
        edt_expire_time_time.setText(timeFormat.format(calendar.time))

        val selectedCalendar = calendar.clone() as Calendar

        edt_expire_time_date.setOnTouchListener { view, motionEvent ->
            if(motionEvent.action != MotionEvent.ACTION_UP){
                return@setOnTouchListener true
            }
            val datePickerDialog = DatePickerDialog(activity,{_, year, month, day ->
                selectedCalendar.set(year,month,day)
                edt_expire_time_date.setText(dateFormat.format(selectedCalendar.time))
                //val selectedDate = Date

            },selectedCalendar.get(Calendar.YEAR),selectedCalendar.get(Calendar.MONTH),selectedCalendar.get(Calendar.DAY_OF_MONTH))
            val picker = datePickerDialog.datePicker
            picker.maxDate = calendar.timeInMillis

            val cal = calendar.clone() as Calendar
            cal.add(Calendar.MONTH,-2)
            cal.add(Calendar.DATE,1)
            picker.minDate = cal.timeInMillis
            datePickerDialog.show()

            return@setOnTouchListener true
        }

        edt_expire_time_time.setOnTouchListener { _, motionEvent ->
            if(motionEvent.action != MotionEvent.ACTION_UP){
                return@setOnTouchListener true
            }
            val timePickerDialog = TimePickerDialog(activity,{_, hour, minute ->
                selectedCalendar.set(Calendar.HOUR_OF_DAY,hour)
                selectedCalendar.set(Calendar.MINUTE, minute)
                edt_expire_time_time.setText(timeFormat.format(selectedCalendar.time))
            },selectedCalendar.get(Calendar.HOUR_OF_DAY),selectedCalendar.get(Calendar.MINUTE),true)
            timePickerDialog.show()
            return@setOnTouchListener true
        }

        edt_task_name.setOnTouchListener { _, motionEvent ->
            if(motionEvent.action != MotionEvent.ACTION_UP){
                return@setOnTouchListener true
            }
            DialogUtils.showInputDialog("任务名称",edt_task_name.text.toString(),"点击输入任务名称",false,activity,{name->
                edt_task_name.setText(name)
            })
            return@setOnTouchListener true
        }

        btn_add_task.setOnClickListener {
            mTaskViewModel.createTask(edt_task_name.text.toString(),queryResult.queryId,selectedCalendar)
                    .mySubscribe(this,true, {_->
                        //jumpToFragment(TaskFragment())
                        toTopFragment()
                    })
        }

    }
}