package luda.tencentjobhunterclient.fragment

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Typeface
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.frag_expression_wizard_content1.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.base.expression.ConditionHolder
import luda.tencentjobhunterclient.base.expression.ConditionStack
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.util.DialogUtils
import luda.tencentjobhunterclient.util.MyRxLifeCycle
import luda.tencentjobhunterclient.viewmodel.QueryExpressionWizardViewModel

/**
 * Created by luda on 2018/8/18
 * QQ 340071887.
 */
class QueryExpressionWizardPagerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater?.inflate(R.layout.frag_expression_wizard_content1,container,false)

    lateinit var type : String
    val pageIndex get() = QueryExpressionWizardViewModel.expressionCategories.indexOf(type)
    private var mIsViewModelSubjectsSubscribed = false
    private lateinit var mViewModel : QueryExpressionWizardViewModel
    private lateinit var mConditionStack : ConditionStack
    private lateinit var mConditionRedoStack : ConditionStack


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init(){

        type = arguments.getString(KeyConstants.EXP_ENUM)
        when(type){
            KeyConstants.EXP_TITLE->{
                tv_field.text="标题满足："
            }
            KeyConstants.EXP_LOCATION->{
                tv_field.text="地点满足："
            }
            KeyConstants.EXP_TYPE->{
                tv_field.text="类别满足："
            }
            KeyConstants.EXP_REQUIREMENTS->{
                tv_field.text="岗位要求满足："
            }
            KeyConstants.EXP_DUTIES->{
                tv_field.text="岗位职责满足："
            }
        }

        mViewModel = ViewModelProviders.of(parentFragment).get(QueryExpressionWizardViewModel::class.java)
        mConditionStack = mViewModel.conditionStackMap[type]!!
        mConditionRedoStack = mViewModel.conditionRedoStackMap[type]!!
        spin_cond_type.setSelection(when(mViewModel.expressionPrefixMap[type]!!){
            QueryExpressionWizardViewModel.ExpressionPrefix.And->0
            QueryExpressionWizardViewModel.ExpressionPrefix.Or->1
        })

        setUI()
        setListeners()
        subscribeViewModelSubjects()
    }

    private fun setListeners(){
        setInputDialog(btn_exp_contains,"包含",ConditionHolder.Type.Contains)
        setInputDialog(btn_exp_not_contains,"不包含",ConditionHolder.Type.NotContains)
        setInputDialog(btn_exp_and_contains,"且包含",ConditionHolder.Type.AndContains)
        setInputDialog(btn_exp_and_not_contains,"且不包含",ConditionHolder.Type.AndNotContains)
        setInputDialog(btn_exp_or_contains,"或包含",ConditionHolder.Type.OrContains)
        setInputDialog(btn_exp_or_not_contains,"或不包含",ConditionHolder.Type.OrNotContains)

        btn_exp_undo.setOnClickListener {
            mConditionStack.transferOne(mConditionRedoStack)
        }
        btn_exp_redo.setOnClickListener {
            mConditionRedoStack.transferOne(mConditionStack)
        }
        btn_exp_reset.setOnClickListener {
            mConditionStack.transferAll(mConditionRedoStack)
        }

        spin_cond_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                mViewModel.expressionPrefixMap[type] =
                        if(position==0)
                            QueryExpressionWizardViewModel.ExpressionPrefix.And
                        else
                            QueryExpressionWizardViewModel.ExpressionPrefix.Or
                mViewModel.expressionChangedSubject.onNext(true)
            }
        }

        mConditionStack.setOnDataChangeListener{
            mViewModel.expressionChangedSubject.onNext(true)
        }
    }

    private fun subscribeViewModelSubjects(){
        if(!mIsViewModelSubjectsSubscribed){
            mViewModel.expressionChangedSubject
                    .compose(MyRxLifeCycle<Boolean>(this))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        setUI()
                    }
            mIsViewModelSubjectsSubscribed = true
        }
    }

    private fun setInputDialog(button: Button,title:String,type:ConditionHolder.Type){
        button.setOnClickListener {
            DialogUtils.showInputDialog(title, "", "请输入条件",false, activity, { content ->
                if (content != "") {
                    mConditionRedoStack.clear()
                    mConditionStack.push(ConditionHolder(type, content))
                }
            })
        }
    }

    private fun setUI(){
        val builder = SpannableStringBuilder()
        for(conditionHolder in mConditionStack){
            val span = SpannableString(conditionHolder.chsType)
            span.setSpan(StyleSpan(Typeface.BOLD),0,span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(span).append(conditionHolder.content)
        }
        tv_condition.text = builder

        val set = ConstraintSet()
        set.clone(exp_wizard_page)
        set.clear(R.id.tv_field,ConstraintSet.START)
        spin_cond_type.visibility =
                if(mViewModel.firstIndexOfExpressionCategoriesEnabled < pageIndex) {
                    set.connect(R.id.tv_field,ConstraintSet.START,R.id.spin_cond_type,ConstraintSet.END)
                    set.applyTo(exp_wizard_page)
                    View.VISIBLE
                }
                else {
                    set.connect(R.id.tv_field,ConstraintSet.START,R.id.exp_wizard_page,ConstraintSet.START)
                    set.applyTo(exp_wizard_page)
                    View.INVISIBLE
                }

        if(!mConditionStack.empty()){
            container_add_condition1.visibility = View.INVISIBLE
            container_add_condition2.visibility = View.VISIBLE
            btn_exp_undo.isEnabled = true
        }
        else{
            container_add_condition1.visibility = View.VISIBLE
            container_add_condition2.visibility = View.INVISIBLE
            btn_exp_undo.isEnabled = false
        }

        btn_exp_redo.isEnabled = !mConditionRedoStack.empty()
        btn_exp_reset.isEnabled = !mConditionStack.empty()

    }

    companion object {
        enum class Type{
            Title,
            Location,
            Type,
            Requirements,
            Duties
        }

        fun getInstance(enum:Type) : QueryExpressionWizardPagerFragment{
            val bundle = Bundle()
            when(enum){
                Type.Title -> bundle.putString(KeyConstants.EXP_ENUM,KeyConstants.EXP_TITLE)
                Type.Location -> bundle.putString(KeyConstants.EXP_ENUM,KeyConstants.EXP_LOCATION)
                Type.Type -> bundle.putString(KeyConstants.EXP_ENUM,KeyConstants.EXP_TYPE)
                Type.Requirements -> bundle.putString(KeyConstants.EXP_ENUM,KeyConstants.EXP_REQUIREMENTS)
                Type.Duties -> bundle.putString(KeyConstants.EXP_ENUM,KeyConstants.EXP_DUTIES)
            }
            val ret = QueryExpressionWizardPagerFragment()
            ret.arguments = bundle
            return ret
        }
    }
}