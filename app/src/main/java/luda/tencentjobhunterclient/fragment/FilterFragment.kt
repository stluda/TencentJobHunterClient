package luda.tencentjobhunterclient.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.filter_drawer.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.adapter.QueryResultFilterAdapter
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel

/**
 * Created by luda on 2018/6/25
 * QQ 340071887.
 */
class FilterFragment : BaseFragment() {

    private lateinit var mAdapter : QueryResultFilterAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.filter_drawer,container,false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        filters.layoutManager = LinearLayoutManager(activity)
        mAdapter = QueryResultFilterAdapter()

        filters.adapter = mAdapter

        val jobQueryResultViewModel = ViewModelProviders.of(activity).get(JobQueryResultViewModel::class.java)

        mAdapter.filterChangedListener = {filters->
            jobQueryResultViewModel.filterChangedSubject.onNext(filters)
        }
    }


}