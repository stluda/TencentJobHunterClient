package luda.tencentjobhunterclient.fragment


import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager
import android.view.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.dialog_expression_wizard.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.adapter.QueryExpressionWizardPagerAdapter
import luda.tencentjobhunterclient.util.MyRxLifeCycle
import luda.tencentjobhunterclient.viewmodel.QueryExpressionWizardViewModel
import kotlin.math.exp


/**
 * Created by luda on 2018/8/18
 * QQ 340071887.
 */
class QueryExpressionWizardDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "QueryExpressionWizardDialogFragment"
    }

    private var mIsViewModelSubjectsSubscribed = false

    private lateinit var mViewModel : QueryExpressionWizardViewModel

    var onConfirmClickedListener : ((String)->Unit)? = null

    private lateinit var mPagerAdapter : QueryExpressionWizardPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_expression_wizard,container)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setupViewPager()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(QueryExpressionWizardViewModel::class.java)
        subscribeViewModelSubjects()
        setUI()
        setUI2()
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

    private fun setUI(){
        val expression = mViewModel.expression
        tv_expression_preview.text = expression
        btn_confirm.isEnabled = expression!=""
    }

    private fun setUI2(){
        btn_next.isEnabled = pager_wizard.currentItem < mPagerAdapter.count - 1
        btn_prev.isEnabled = pager_wizard.currentItem > 0
    }

    override fun onStart() {
        super.onStart()
        setSize()
    }

    private fun setListeners(){
        btn_confirm.setOnClickListener {
            onConfirmClickedListener?.invoke(mViewModel.expression)
            dismiss()
        }
        btn_cancel.setOnClickListener { dismiss() }
        pager_wizard.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) { }
            override fun onPageSelected(position: Int) {setUI2()}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        })

        btn_next.setOnClickListener {
            pager_wizard.setCurrentItem(pager_wizard.currentItem+1,true)
        }
        btn_prev.setOnClickListener {
            pager_wizard.setCurrentItem(pager_wizard.currentItem-1,true)
        }
    }

    private fun setupViewPager(){
        mPagerAdapter = QueryExpressionWizardPagerAdapter(childFragmentManager)
        pager_wizard.adapter = mPagerAdapter
    }

    private fun setSize(){
        val lp = dialog.window.attributes
        lp.gravity = Gravity.CENTER

        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size =  Point()
        wm.defaultDisplay.getSize(size)

        lp.width = size.x * 1
        lp.height = size.y * 1

        dialog.onWindowAttributesChanged(lp)
        dialog.window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
    }

}