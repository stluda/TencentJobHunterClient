package luda.tencentjobhunterclient.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.frag_add_job_query_simple.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.R.id.*
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.base.expression.JobQueryExpression
import luda.tencentjobhunterclient.ui.FullScreenInputDialog
import luda.tencentjobhunterclient.util.DialogUtils
import luda.tencentjobhunterclient.util.SettingHelper
import luda.tencentjobhunterclient.util.mySubscribe
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel

/**
 * Created by luda on 2018/12/12
 * QQ 340071887.
 */
class NewSimpleQueryFragment: BaseFragment() {

    lateinit var jobQueryResultViewModel : JobQueryResultViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.frag_add_job_query_simple,container,false)


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        jobQueryResultViewModel = ViewModelProviders.of(activity).get(JobQueryResultViewModel::class.java)

        //(parentFragment as JobQueryFragment).jobQueryResultViewModel.newJobQueryFragmentTag = tag
        btn_new_job_query.setOnClickListener{
            if(jobQueryResultViewModel.jobQueryResults.size>=3){
                //超过可允许查询的最大数量
                Toast.makeText(activity,R.string.new_job_query_limit_reached, Toast.LENGTH_LONG).show()
            }
            else
            {

            }
        }

        btn_expression_query.setOnClickListener {
            SettingHelper.queryMode = 2
            jobQueryResultViewModel.isExpressionMode = true
        }

        val v = jobQueryResultViewModel

        setEditTextListener(edt_keyword,{text->v.inputKeyword=text})
        setEditTextListener(edt_title,{text->v.inputTitle=text})
        setEditTextListener(edt_type,{text->v.inputType=text})
        setEditTextListener(edt_location,{text->v.inputLocation=text})
        setEditTextListener(edt_duties,{text->v.inputDuties=text})
        setEditTextListener(edt_requirements,{text->v.inputRequirements=text})

        edt_keyword.setText(v.inputKeyword)
        edt_title.setText(v.inputTitle)
        edt_type.setText(v.inputType)
        edt_location.setText(v.inputLocation)
        edt_duties.setText(v.inputDuties)
        edt_requirements.setText(v.inputRequirements)

        rb_advance.setOnCheckedChangeListener { _, flag ->
            val state1 = if(flag)View.VISIBLE else View.GONE
            val state2 = if(flag)View.GONE else View.VISIBLE

            tv_title.visibility = state1
            edt_title.visibility = state1
            tv_type.visibility = state1
            edt_type.visibility = state1
            tv_location.visibility = state1
            edt_location.visibility = state1
            tv_duties.visibility = state1
            edt_duties.visibility = state1
            tv_requirements.visibility = state1
            edt_requirements.visibility = state1

            tv_keyword.visibility = state2
            edt_keyword.visibility = state2

            v.isSimpleMode = !flag
            SettingHelper.queryMode = if(flag) 1 else 0
        }

        btn_new_job_query.setOnClickListener {
            if (jobQueryResultViewModel.jobQueryResults.size >= 3) {
                //超过可允许查询的最大数量
                Toast.makeText(activity, R.string.new_job_query_limit_reached, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val sb = StringBuilder()
            if (rb_advance.isChecked) {
                val build = { edt: EditText, tp: String ->
                    val tmp = edt.text.toString()
                    if (tmp != "") {
                        if (sb.isNotEmpty()) sb.append("&&")
                        sb.append("$tp{$tmp}")
                    }
                }
                build(edt_title, "T")
                build(edt_type, "TY")
                build(edt_location, "L")
                build(edt_duties, "D")
                build(edt_requirements, "R")
            } else {
                val tmp = edt_keyword.text.toString()
                if (tmp != "") {
                    sb.append("T{$tmp}||TY{$tmp}||L{$tmp}||D{$tmp}||R{$tmp}")
                }
            }

            val expStr = sb.toString()
            if (expStr == "") {
                Toast.makeText(activity, "关键字不得为空！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                JobQueryExpression.parse(expStr)
                jobQueryResultViewModel.query(expStr).mySubscribe(
                        this, true, {})
            }
            catch (e: Exception) {
                Toast.makeText(activity, "输入内容中含有非法字符，请检查\n", Toast.LENGTH_SHORT).show()
            }

        }

        rb_simple.isChecked = v.isSimpleMode
    }


    private fun setEditTextListener(edt:EditText,act:(String)->Unit)
    {
        edt.setOnTouchListener {  _, motionEvent ->
            if(motionEvent.action != MotionEvent.ACTION_UP){
                return@setOnTouchListener true
            }
            DialogUtils.showInputDialog("请输入内容",edt.text.toString(),"点击输入内容",false,activity,{content->
                edt.setText(content)
                act(content)
            })
            return@setOnTouchListener true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }

}