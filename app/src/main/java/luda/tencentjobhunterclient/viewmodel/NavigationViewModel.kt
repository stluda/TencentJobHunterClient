package luda.tencentjobhunterclient.viewmodel

import android.arch.lifecycle.ViewModel
import android.support.v4.app.FragmentManager
import luda.tencentjobhunterclient.fragment.BaseNavigationFragment
import luda.tencentjobhunterclient.fragment.JobQueryFragment
import luda.tencentjobhunterclient.fragment.MyAccountFragment
import luda.tencentjobhunterclient.fragment.TaskFragment
import luda.tencentjobhunterclient.util.INavigationState
import java.util.*

/**
 * Created by luda on 2018/6/18
 * QQ 340071887.
 */
class NavigationViewModel : ViewModel(),INavigationState {
    val currentFragmentTags = arrayOf("JobQueryFragment","TaskFragment","SettingFragment")
    private val lastFragmentTags = arrayOf("JobQueryFragment","TaskFragment","SettingFragment")
    private val fragmentTagStacksByGroup = Array<Stack<String>>(3){Stack<String>()}

    override val currentFragmentTag get() = currentFragmentTags[mCurrentGroupId]

    fun getCurrentFragment(fm:FragmentManager) : BaseNavigationFragment?
    {
        return fm.findFragmentByTag(currentFragmentTag) as BaseNavigationFragment
    }

    override var taskIdOfTaskDetailFragment: Int = 0

    val currentGroupId get() = mCurrentGroupId
    val lastGroupId get() = mLastGroupId

    private var mCurrentGroupId = 0
    private var mLastGroupId = 0

    val lastFragmentTag get() = lastFragmentTags[currentGroupId]

    init {
        for(i in 0..2)initTagStack(i)
    }

    fun addToBackStack(tag:String){
        fragmentTagStacksByGroup[currentGroupId].push(tag)
    }

    fun popTag() : String{
        return fragmentTagStacksByGroup[currentGroupId].pop()
    }

    fun isTop() : Boolean
    {
        return isTop(currentGroupId);
    }

    fun isTop(groupId:Int) : Boolean
    {
        return fragmentTagStacksByGroup[groupId].isEmpty();
    }

    fun initTagStackOfCurrentGroup(){
        initTagStack(currentGroupId)
    }

    private fun initTagStack(groupId: Int){
        fragmentTagStacksByGroup[groupId].clear()
    }

    fun setInfo(groupId:Int,navTag:String)
    {
        if(mCurrentGroupId == groupId && navTag == currentFragmentTags[groupId]) return

        mLastGroupId = currentGroupId
        if(groupId==currentGroupId)
        {
            lastFragmentTags[groupId] = currentFragmentTags[groupId]
        }

        mCurrentGroupId=groupId
        currentFragmentTags[currentGroupId] = navTag
    }
}