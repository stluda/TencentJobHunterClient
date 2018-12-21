package luda.tencentjobhunterclient.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import luda.tencentjobhunterclient.constant.KeyConstants
import android.support.v4.view.PagerAdapter
import android.util.Log
import luda.tencentjobhunterclient.fragment.*
import luda.tencentjobhunterclient.model.JobQueryResult
import luda.tencentjobhunterclient.util.SettingHelper
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel


/**
 * Created by luda on 2018/4/1
 * QQ 340071887.
 */
class QueryPagerAdapter(
        private val fm:FragmentManager,
        private val mJobQueryResultViewModel: JobQueryResultViewModel) :
        FragmentStatePagerAdapter(fm) {//FragmentStatePagerAdapter FragmentPagerAdapter

    private val mFirstFragment : BaseFragment get() =
        if(mJobQueryResultViewModel.isCacheMode)
            CacheModeInfoFragment()
        else
            if(SettingHelper.queryMode==2)
                NewExpressionQueryFragment()
            else
                NewSimpleQueryFragment()
    private val mFragmentList: ArrayList<JobQueryResultTabFragment> = ArrayList<JobQueryResultTabFragment>()



    init {

        for(result in mJobQueryResultViewModel.jobQueryResults){
            val fragment = JobQueryResultTabFragment()
            //val queryNo = mJobQueryResultViewModel.getQueryNoByIndex(i)

            val bundle = Bundle()
            bundle.putInt(KeyConstants.QUERY_NO,result.queryNo)
            bundle.putString(KeyConstants.QUERY_ID,result.queryId);
            bundle.putString(KeyConstants.QUERY_EXPRESSION,result.queryExpression)
            fragment.arguments= bundle
            mFragmentList.add(fragment)
        }

    }

    override fun getItem(position: Int): Fragment =
        if(position==0)
            mFirstFragment
        else
            mFragmentList[position-1]


    override fun getCount(): Int = mFragmentList.size+1


    /*override fun getItemPosition(`object`: Any?): Int {
        return PagerAdapter.POSITION_NONE
    }*/

    override fun getPageTitle(position: Int): CharSequence =
            if(position==0)"新建查询"
            else "查询$position"

    //新增查询
    fun addJobQueryResultPager(result: JobQueryResult) : Boolean {
        if(mFragmentList.size>=3){
            return false
        }
        else{
            val fragment = JobQueryResultTabFragment()
            //mJobQueryResultViewModel.addJobQueryResult(result)

            val bundle = Bundle()
            bundle.putInt(KeyConstants.QUERY_NO,result.queryNo)
            bundle.putString(KeyConstants.QUERY_ID,result.queryId)
            bundle.putString(KeyConstants.QUERY_EXPRESSION,result.queryExpression)

            fragment.arguments= bundle
            mFragmentList.add(fragment)


            //mFragmentList.add(JobQueryResultTabFragment().init(list))
            //通知Adpater数据已刷新
            notifyDataSetChanged()
            return true
        }
    }

    override fun getItemPosition(`object`: Any?): Int {
        return PagerAdapter.POSITION_NONE
    }


    fun removeJobQueryResultPager(position: Int){
        try{
            mFragmentList.removeAt(position)
        }
        catch (e:Exception){
            Log.d("removePager",e.message)
        }

        //fm.fragments.remove(fragment)
        notifyDataSetChanged()
    }

}