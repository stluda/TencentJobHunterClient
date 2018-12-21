package luda.tencentjobhunterclient.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.ui.LoadingDialog

/**
 * Created by luda on 2018/4/14
 * QQ 340071887.
 */
open class BaseFragment : Fragment() {

    protected var isViewModelSubjectsSubscribed = false
    private var mLoadingDialog : LoadingDialog? = null

    protected open fun subscribeViewModelSubjects(){
        isViewModelSubjectsSubscribed = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(!isViewModelSubjectsSubscribed){
            subscribeViewModelSubjects()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    fun toSubFragment(fragment: BaseNavigationFragment){
        (activity as MainActivity).toSubFragment(fragment)
    }

    fun toBackFragment(){
        (activity as MainActivity).toBackFragment()
    }

    fun toTopFragment(){
        (activity as MainActivity).toTopFragment()
    }
}