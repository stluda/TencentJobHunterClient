package luda.tencentjobhunterclient.fragment

import android.app.AlertDialog
import android.app.ProgressDialog.show
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.frag_add_job_query.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.R.id.tbx_query_expression
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.base.expression.JobQueryExpression
import luda.tencentjobhunterclient.ui.FullScreenInputDialog
import luda.tencentjobhunterclient.util.DialogUtils
import luda.tencentjobhunterclient.util.SettingHelper
import luda.tencentjobhunterclient.util.mySubscribe
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel


/**
 * Created by luda on 2018/4/4
 * QQ 340071887.
 */
class NewExpressionQueryFragment : BaseFragment() {

    lateinit var jobQueryResultViewModel : JobQueryResultViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
        = inflater?.inflate(R.layout.frag_add_job_query,container,false)




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
                val expStr = tbx_query_expression.text.toString()
                try{
                    val expression = JobQueryExpression.parse(expStr)

                    val normalDialog : AlertDialog.Builder =
                            AlertDialog.Builder(activity);

                    normalDialog.setTitle("是否进行查询？")
                            .setMessage("将使用以下表达式进行查询：\n"+
                                    "$expStr\n\n"+
                                    "意为：\n"+
                                    "${expression.description}\n\n"+
                                    "确认无误吗？"
                            )
                            .setNegativeButton("取消", { _, _ ->})
                            .setPositiveButton("确定",{ _, _ ->
                                jobQueryResultViewModel.query(expStr).mySubscribe(
                                        this,true,{})
                            }).show();
                }
                catch (e:Exception){
                    Toast.makeText(activity,"表达式不合法，请检查",Toast.LENGTH_SHORT).show()
                }

            }
        }

        btn_make_expression.setOnClickListener {
            val dialog = QueryExpressionWizardDialogFragment()
            dialog.onConfirmClickedListener={expression->
                tbx_query_expression.setText(expression)
            }
            dialog.show(fragmentManager,QueryExpressionWizardDialogFragment.TAG)
        }

        tbx_query_expression.setText(jobQueryResultViewModel.inputExpression)

        tbx_query_expression.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                jobQueryResultViewModel.inputExpression = p0.toString()
            }
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        setButtonAppend(btn_make_left_brace,"{")
        setButtonAppend(btn_make_right_brace,"}")
        setButtonAppend(btn_make_left_parentheses,"(")
        setButtonAppend(btn_make_right_parentheses,")")
        setButtonAppend(btn_make_not,"!")
        setButtonAppend(btn_make_and,"&&")
        setButtonAppend(btn_make_or,"||")

        setButtonAppend2(btn_make_title,"T")
        setButtonAppend2(btn_make_type,"TY")
        setButtonAppend2(btn_make_location,"L")
        setButtonAppend2(btn_make_requirements,"R")
        setButtonAppend2(btn_make_duties,"D")
        setButtonAppend2(btn_make_hiring_number,"H",true)

        btn_input_text.setOnClickListener {
            DialogUtils.showInputDialog("请输入内容","","请输入内容",false,activity,{content->
                insertText(content)
            })
        }

        btn_backspace.setOnClickListener {
            removeOneChar()
        }

        setBackspace()

        btn_keyboard.setOnClickListener {
            FullScreenInputDialog.show(activity.supportFragmentManager,{text,index->
                tbx_query_expression.setText(text)
                if(index>=0)tbx_query_expression.setSelection(index)
            },tbx_query_expression.text.toString(),tbx_query_expression.selectionEnd)
        }

        btn_simple_query.setOnClickListener {
            SettingHelper.queryMode = if(jobQueryResultViewModel.isSimpleMode) 0 else 1
            jobQueryResultViewModel.isExpressionMode = false
        }
    }

    private fun removeOneChar(){
        var start = tbx_query_expression.selectionStart
        var end = tbx_query_expression.selectionEnd
        if(start>=0&&end>start){
            tbx_query_expression.text = tbx_query_expression.text.delete(start,end)
            tbx_query_expression.setSelection(start)
        }
        else if(end>0){
            tbx_query_expression.text = tbx_query_expression.text.delete(end-1,end)
            tbx_query_expression.setSelection(end-1)
        }
    }

    private fun setBackspace(){
        var downTime = 0L
        var isTouching = false
        btn_backspace.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN->{
                    downTime = System.currentTimeMillis()
                    isTouching = true
                    Thread{
                        while (isTouching){
                            if(System.currentTimeMillis()-downTime>500){
                                activity.runOnUiThread {
                                    removeOneChar()
                                }
                            }
                            Thread.sleep(50)
                        }
                    }.start()
                }
                MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{
                    isTouching = false
                }
            }
            false
        }
    }

    private fun setButtonAppend(view: View, content:String){
        view.setOnClickListener {
            insertText(content)
        }
    }
    private fun insertText(content:String){
        var index = tbx_query_expression.selectionEnd
        if(index<0)index = tbx_query_expression.text.count() - 1
        tbx_query_expression.text.insert(index,content)
    }
    private fun setButtonAppend2(view: View, prefix:String,isNumeric:Boolean = false){
        view.setOnClickListener {
            DialogUtils.showInputDialog("请输入内容","","请输入内容",isNumeric,activity,{content->
                tbx_query_expression.append("$prefix{$content}")
                tbx_query_expression.setSelection(tbx_query_expression.text.count()-1)
            })

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }

}