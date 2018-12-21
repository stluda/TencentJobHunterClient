package luda.tencentjobhunterclient.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import luda.tencentjobhunterclient.fragment.QueryExpressionWizardPagerFragment
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel

/**
 * Created by luda on 2018/8/18
 * QQ 340071887.
 */
class QueryExpressionWizardPagerAdapter(private val fm: FragmentManager) :FragmentStatePagerAdapter(fm)  {

    override fun getCount(): Int = 5

    override fun getItem(position: Int): Fragment
        = when(position){
            0->QueryExpressionWizardPagerFragment.getInstance(QueryExpressionWizardPagerFragment.Companion.Type.Title)
            1->QueryExpressionWizardPagerFragment.getInstance(QueryExpressionWizardPagerFragment.Companion.Type.Type)
            2->QueryExpressionWizardPagerFragment.getInstance(QueryExpressionWizardPagerFragment.Companion.Type.Location)
            3->QueryExpressionWizardPagerFragment.getInstance(QueryExpressionWizardPagerFragment.Companion.Type.Requirements)
            4->QueryExpressionWizardPagerFragment.getInstance(QueryExpressionWizardPagerFragment.Companion.Type.Duties)
            else->throw NotImplementedError()
        }

}