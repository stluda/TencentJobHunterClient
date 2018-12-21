package luda.tencentjobhunterclient.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frag_myfavorite_job.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.adapter.JobHistoryAdapter
import luda.tencentjobhunterclient.adapter.MyFavoriteJobAdapter
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.util.MyLinearLayoutManager
import luda.tencentjobhunterclient.viewmodel.JobViewModel

/**
 * Created by luda on 2018/10/27
 * QQ 340071887.
 */
class MyFavoriteJobsFragment: BaseNavigationFragment() {
    companion object {
        const val TAG = "MyFavoriteJobsFragment"
    }

    override val navTag: String
        get() = TAG

    override val groupId: Int
        get() = 2

    private lateinit var mAdapter : MyFavoriteJobAdapter
    private lateinit var mJobViewModel : JobViewModel

    override fun subscribeViewModelSubjects() {
        super.subscribeViewModelSubjects()
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater!!.inflate(R.layout.frag_myfavorite_job, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mJobViewModel = ViewModelProviders.of(activity).get(JobViewModel::class.java)
        super.onActivityCreated(savedInstanceState)
        loadUI()
    }

    private fun loadUI(){
        toolbar.setTitle(R.string.menu_my_favorite)
        (activity as MainActivity).setToolBar2(toolbar)
        setupRecyclerView()
        setHasOptionsMenu(true)
    }

    private fun setupRecyclerView()
    {
        rv_my_favorite_job.layoutManager = MyLinearLayoutManager(activity)
        mAdapter = MyFavoriteJobAdapter(mJobViewModel.myFavoriteJobsClone)

        mAdapter.onItemClickListener = {job->
            val jobDetailFragment = JobDetailFragment()
            val bundle = Bundle()
            bundle.putInt(KeyConstants.JOB_ID,job.id)
            bundle.putInt(KeyConstants.GROUP_ID,groupId)
            jobDetailFragment.arguments = bundle
            toSubFragment(jobDetailFragment)
        }


        rv_my_favorite_job.adapter = mAdapter
    }


}