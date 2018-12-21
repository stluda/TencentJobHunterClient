package luda.tencentjobhunterclient.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.frag_task_query_result.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.adapter.QueryResultAdapter
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.base.Interval
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.model.Job
import luda.tencentjobhunterclient.model.Task
import luda.tencentjobhunterclient.util.MyLinearLayoutManager
import luda.tencentjobhunterclient.util.MyRxLifeCycle
import luda.tencentjobhunterclient.util.mySubscribe
import luda.tencentjobhunterclient.viewmodel.ITaskDetailFragmentViewModel
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel
import luda.tencentjobhunterclient.viewmodel.TaskViewModel

/**
 * Created by luda on 2018/6/18
 * QQ 340071887.
 */
class TaskDetailFragment  : BaseNavigationFragment(){

    companion object {
        const val TAG = "TaskDetailFragment"
    }

    override val navTag: String
        get() = TAG


    private lateinit var mJobQueryResultViewModel : JobQueryResultViewModel
    private lateinit var mTaskViewModel : ITaskDetailFragmentViewModel
    private lateinit var mAdapter : QueryResultAdapter
    private lateinit var mTask : Task

    private val taskId:Int get() = arguments.getInt(KeyConstants.TASK_ID)
    private val queryExpression:String get() = arguments.getString(KeyConstants.QUERY_EXPRESSION)

    private var doRefresh = false


    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mJobQueryResultViewModel = ViewModelProviders.of(activity).get(JobQueryResultViewModel::class.java)
        mTaskViewModel = ViewModelProviders.of(activity).get(TaskViewModel::class.java)
        mTask = mTaskViewModel.getTaskById(taskId)!!
        super.onActivityCreated(savedInstanceState)

        setTaskIdOfTaskDetailFragment(taskId)
        loadUI()
        if(mTask.viewSize<6)
        {
            doRefresh = true
            doLoadMore()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater!!.inflate(R.layout.frag_task_query_result, container, false)

    //val viewForSnackBar = rv_job_query_result

    private fun loadUI(){
        toolbar.setTitle(R.string.menu_task)
        (activity as MainActivity).setToolBar2(toolbar)
        setupRecyclerView()
        setHasOptionsMenu(true)
    }

    override fun subscribeViewModelSubjects() {
        mTaskViewModel.getTaskQueryResultInsertedSubject(mTask)!!
                .compose(MyRxLifeCycle<Interval>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {interval->
                    mAdapter.notifyJobQueryResultInserted(interval.start,interval.count)
                }

        mTaskViewModel.hasNewQueryResultSubject(mTask.id)
                .compose(MyRxLifeCycle<Int>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {_->
                    Snackbar.make(coordinator_layout,"有新的查询结果，该任务目前总共有${mTask.countOfHasNotBeenSeenResult}个未读项", Snackbar.LENGTH_INDEFINITE)
                            .setAction("知道了",{

                            }).show()
                }
        super.subscribeViewModelSubjects()
    }


    private fun setupRecyclerView()
    {
        rv_job_query_result.layoutManager = MyLinearLayoutManager(activity)
        mAdapter = QueryResultAdapter(mTask,true)
        //添加加载更多的监听器
        rv_job_query_result.adapter = mAdapter

        mAdapter.onItemClickedListener = { pos->

            Job.fromServer(mTask.getItemAt(pos).id,MyApplication.realmTaskWorker)
                    .mySubscribe(this,true,{job->
                        val jobDetailFragment = JobDetailFragment()
                        val bundle = Bundle()
                        bundle.putInt(KeyConstants.JOB_ID,job.id)
                        bundle.putInt(KeyConstants.GROUP_ID,groupId)
                        jobDetailFragment.arguments = bundle
                        job.addToHistory()

                        mAdapter.notifyItemChanged(job)

                        toSubFragment(jobDetailFragment)
                    })
        }


        rv_job_query_result.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                var lm = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = recyclerView.adapter.itemCount
                val lastVisibleItemPosition = lm.findLastVisibleItemPosition()
                val visibleItemCount = recyclerView.childCount
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItemPosition == totalItemCount - 1
                        && visibleItemCount > 0) {
                    //加载更多
                    doLoadMore()
                }
            }
        })
    }


    private fun doLoadMore(){

        if(mTask.canLoadMore){
            //标记为读取中
            mAdapter.notifyStateLoading();
            mTaskViewModel.loadMore(mTask)
        }
    }



    override val groupId get() = 1
}