package luda.tencentjobhunterclient.util

import android.support.v4.app.FragmentManager
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.fragment.*

/**
 * Created by luda on 2018/10/5
 * QQ 340071887.
 */
object FragmentUtils {
    fun getFragmentByTag(tag:String,extra:Int=0) : BaseNavigationFragment = when(tag){
        JobQueryFragment.TAG->JobQueryFragment()
        TaskFragment.TAG->TaskFragment()
        TaskDetailFragment.TAG->TaskDetailFragment()
        AddTaskFragment.TAG->AddTaskFragment()
        AddTaskFromQueryFragment.TAG->TaskFragment()
        MyAccountFragment.TAG->MyAccountFragment()
        JobHistoryFragment.TAG->JobHistoryFragment()
        JobDetailFragment.TAG->JobDetailFragment()
        else->throw NotImplementedError()
    }

    fun reload(fm:FragmentManager,act:MainActivity){
        val nav = act.navigationViewModel
        var fragment = fm.findFragmentByTag(nav.currentFragmentTag) as BaseNavigationFragment?
        if(fragment==null){
            fragment = when(nav.currentGroupId){
                1->TaskFragment()
                2->MyAccountFragment()
                else->JobQueryFragment()
            }
        }

        fm.beginTransaction()
                .replace(R.id.container,fragment,fragment.navTag)
                .commit()
    }

    fun toTop(fm:FragmentManager,act:MainActivity){
        val nav = act.navigationViewModel
        var fragment = fm.findFragmentByTag(when(nav.currentGroupId){
            1->TaskFragment.TAG
            2->MyAccountFragment.TAG
            else->JobQueryFragment.TAG
        }) as BaseNavigationFragment?
        if(fragment==null){
            fragment = when(nav.currentGroupId){
                1->TaskFragment()
                2->MyAccountFragment()
                else->JobQueryFragment()
            }
            nav.initTagStackOfCurrentGroup()
        }

        fm.beginTransaction()
                .replace(R.id.container,fragment,fragment.navTag)
                . commit()
    }

    fun toBackStack(fm:FragmentManager,act:MainActivity){
        val nav = act.navigationViewModel
        val backTag = nav.popTag()
        var fragment = fm.findFragmentByTag(backTag) as BaseNavigationFragment?
        act.setThemeByType(nav.currentGroupId)
        if(fragment==null){
            fragment = when(nav.currentGroupId){
                1->TaskFragment()
                2->MyAccountFragment()
                else->JobQueryFragment()
            }
            nav.initTagStackOfCurrentGroup()
        }
        fm.beginTransaction()
                .replace(R.id.container,fragment,backTag)
                . commit()
        //if(nav.lastGroupId==nav.currentGroupId&&nav.currentFragmentTag!=nav.lastFragmentTag)
        //    fm.popBackStack()

    }


}
