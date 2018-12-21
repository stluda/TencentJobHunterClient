package luda.tencentjobhunterclient.fragment

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.act_main.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.viewmodel.NavigationViewModel


/**
 * Created by luda on 2018/6/16
 * QQ 340071887.
 */
abstract class BaseNavigationFragment : BaseFragment() {

    private lateinit var mNavigationViewModel : NavigationViewModel

    abstract val navTag : String

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mNavigationViewModel =  ViewModelProviders.of(activity).get(NavigationViewModel::class.java)
        mNavigationViewModel.setInfo(groupId,navTag)
        setBottomNavigation()
        (activity as MainActivity).setThemeByType(groupId)
        activity.nav_view.setCheckedItem(when(groupId){
            0->R.id.navigation_job_query
            1->R.id.navigation_task
            else->R.id.navigation_setting
        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    abstract val groupId : Int


    companion object {

        internal fun getGroupIdByItemId(itemId:Int) : Int = when(itemId) {
            R.id.query_nav_item->0
            R.id.task_nav_item->1
            R.id.myacct_nav_item->2
            else->throw NotImplementedError()
        }
        internal fun getItemIdByGroupId(groupId:Int) : Int = when(groupId) {
            0->R.id.query_nav_item
            1->R.id.task_nav_item
            2->R.id.myacct_nav_item
            else->throw NotImplementedError()
        }


    }

    private fun getNavFragment(index:Int) : BaseNavigationFragment  {
        val fragment = activity.supportFragmentManager.findFragmentByTag(mNavigationViewModel.currentFragmentTags[index]) as BaseNavigationFragment?
        if(fragment==null){
            Log.d(navTag,"getNavFragment() fragment=null")
        }
        return fragment ?: when(index){
            0->JobQueryFragment()
            1->TaskFragment()
            else->MyAccountFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        val navigation = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        navigation.selectedItemId = getItemIdByGroupId(groupId)
        //mNavigationViewModel.setInfo(groupId,navTag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    internal fun setTaskIdOfTaskDetailFragment(id:Int){
        mNavigationViewModel.taskIdOfTaskDetailFragment = id
    }

//    fun jumpToFragment(fragment:BaseNavigationFragment){
//        activity.supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.container,fragment,fragment.navTag)
//                .addToBackStack(navTag)
//                . commit()
//    }

    fun navigate(toGroupId: Int) : BaseNavigationFragment{

        if(toGroupId == groupId)
        {
            return this
        }

        (activity as MainActivity).setThemeByType(toGroupId)


        val fragment = getNavFragment(toGroupId)
        activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container,fragment , mNavigationViewModel.currentFragmentTags[toGroupId])
                .addToBackStack(mNavigationViewModel.currentFragmentTags[groupId])
                .commit()
        return fragment
    }

    private fun setBottomNavigation()
    {
        //mNavigationViewModel.currentGroupId = groupId
        val navigation = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        navigation.setOnNavigationItemSelectedListener {item->
            val toGroupId = getGroupIdByItemId(item.itemId)
            navigate(toGroupId)
            return@setOnNavigationItemSelectedListener true
        }
    }

}