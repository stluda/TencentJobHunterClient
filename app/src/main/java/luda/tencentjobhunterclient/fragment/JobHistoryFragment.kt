package luda.tencentjobhunterclient.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frag_job_history.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.adapter.JobHistoryAdapter
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.model.Job
import luda.tencentjobhunterclient.util.MyLinearLayoutManager
import luda.tencentjobhunterclient.util.MyRxLifeCycle
import luda.tencentjobhunterclient.viewmodel.JobViewModel

/**
 * Created by luda on 2018/7/29
 * QQ 340071887.
 */
class JobHistoryFragment : BaseNavigationFragment() {
    companion object {
        const val TAG = "JobHistoryFragment"
    }

    override val navTag: String
        get() = TAG

    override val groupId: Int
        get() = 2

    private lateinit var mAdapter : JobHistoryAdapter
    private lateinit var mJobViewModel : JobViewModel

    override fun subscribeViewModelSubjects() {
        super.subscribeViewModelSubjects()
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater!!.inflate(R.layout.frag_job_history, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mJobViewModel = ViewModelProviders.of(activity).get(JobViewModel::class.java)
        super.onActivityCreated(savedInstanceState)
        loadUI()
    }

    private fun loadUI(){
        toolbar.setTitle(R.string.menu_job_history)
        (activity as MainActivity).setToolBar2(toolbar)
        setupRecyclerView()
        setHasOptionsMenu(true)
    }

    private fun setupRecyclerView()
    {
        rv_job_history.layoutManager = MyLinearLayoutManager(activity)
        mAdapter = JobHistoryAdapter(mJobViewModel.historyOfJobViewed)

        mAdapter.onItemClickListener = {job->
                    val jobDetailFragment = JobDetailFragment()
                    val bundle = Bundle()
                    bundle.putInt(KeyConstants.JOB_ID,job.id)
                    bundle.putInt(KeyConstants.GROUP_ID,groupId)
                    jobDetailFragment.arguments = bundle
                    toSubFragment(jobDetailFragment)
                }


        rv_job_history.adapter = mAdapter
    }


}