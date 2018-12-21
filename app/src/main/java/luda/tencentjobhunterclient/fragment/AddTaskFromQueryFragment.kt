package luda.tencentjobhunterclient.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.frag_select_query.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.adapter.QueryAdapter
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.model.JobQueryResult
import luda.tencentjobhunterclient.util.MyLinearLayoutManager
import luda.tencentjobhunterclient.util.MyRxLifeCycle
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel

/**
 * Created by luda on 2018/7/29
 * QQ 340071887.
 */
class AddTaskFromQueryFragment : BaseNavigationFragment() {
    companion object {
        const val TAG = "AddTaskFromQueryFragment"
    }
    override val groupId: Int
        get() = 1
    override val navTag: String
        get() = TAG

    private lateinit var mAdapter :QueryAdapter

    private lateinit var mJobQueryResultViewModel : JobQueryResultViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater!!.inflate(R.layout.frag_select_query, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mJobQueryResultViewModel = ViewModelProviders.of(activity).get(JobQueryResultViewModel::class.java)
        super.onActivityCreated(savedInstanceState)
        loadUI()
    }

    private fun loadUI(){
        toolbar.setTitle(R.string.add_task_select_query)
        (activity as MainActivity).setToolBar2(toolbar)
        setupRecyclerView()
        setHasOptionsMenu(true)
    }


    private fun setupRecyclerView()
    {
        rv_select_query.layoutManager = MyLinearLayoutManager(activity)
        mAdapter = QueryAdapter(mJobQueryResultViewModel)
        //添加加载更多的监听器
        rv_select_query.adapter = mAdapter
        mAdapter.onItemClickListener= {query->
                    val bundle = Bundle()
                    bundle.putInt(KeyConstants.QUERY_NO,query.queryNo)
                    val fragment = AddTaskFragment()
                    fragment.arguments = bundle
                    toSubFragment(fragment)
                    //jumpToFragment(fragment)
                }
    }

}