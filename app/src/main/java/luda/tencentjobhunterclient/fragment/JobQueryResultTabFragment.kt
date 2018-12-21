package luda.tencentjobhunterclient.fragment

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.frag_job_query_result.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.R.string.query
import luda.tencentjobhunterclient.adapter.QueryResultAdapter
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.model.JobQueryResult
import luda.tencentjobhunterclient.model.QueryResultFilterHolder
import luda.tencentjobhunterclient.util.*
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel


/**
 * Created by luda on 2018/4/5
 * QQ 340071887.
 */
class JobQueryResultTabFragment : BaseFragment() {

    private lateinit var mJobQueryResultViewModel : JobQueryResultViewModel

    private val queryNo:Int get() = arguments.getInt(KeyConstants.QUERY_NO)
    private val queryId:String get() = arguments.getString(KeyConstants.QUERY_ID)
    private val queryExpression:String get() = arguments.getString(KeyConstants.QUERY_EXPRESSION)
    private lateinit var mQueryResult : JobQueryResult
    private lateinit var mAdapter: QueryResultAdapter

    override fun onStop() {
        super.onStop()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }

    override fun subscribeViewModelSubjects() {
        mJobQueryResultViewModel.getLoadMoreJobQueryResultSubject(queryId)
                .compose(MyRxLifeCycle<JobQueryResult>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {result->
                    //通知Adapter数据源已追加新结果
                    mAdapter.notifyJobQueryResultInserted(result)
                }

        mJobQueryResultViewModel.filterChangedSubject
                .compose(MyRxLifeCycle<QueryResultFilterHolder>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {filters->
                    mAdapter.setFilter(filters)
                }


        super.subscribeViewModelSubjects()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mJobQueryResultViewModel = ViewModelProviders.of(activity).get(JobQueryResultViewModel::class.java)
        super.onActivityCreated(savedInstanceState)


        val qno = queryNo
        var result = mJobQueryResultViewModel.getJobQueryResult(qno)
        if(result!=null){
            rv_job_query_result.layoutManager = MyLinearLayoutManager(activity)
            mQueryResult = result
            mAdapter = QueryResultAdapter(result)
            //添加加载更多的监听器
            rv_job_query_result.adapter = mAdapter

            //给列表的item添加点击事件监听器
            mAdapter.onItemClickedListener = { position: Int ->
                        mJobQueryResultViewModel.getJobDetail(mQueryResult[position].id,queryId)
                                .mySubscribe(this,true,{job->
                                    val jobDetailFragment = JobDetailFragment()
                                    val bundle = Bundle()
                                    bundle.putInt(KeyConstants.JOB_ID,job.id)
                                    bundle.putInt(KeyConstants.GROUP_ID,0)
                                    jobDetailFragment.arguments = bundle
                                    job.addToHistory()

                                    mAdapter.notifyItemChanged(job)

                                    toSubFragment(jobDetailFragment)
                                })
                    }

            mAdapter.onAddTaskButtonClickListener = {_->
                val bundle = Bundle()
                bundle.putInt(KeyConstants.QUERY_NO,queryNo)
                val fragment = AddTaskFragment()
                fragment.arguments = bundle
                toSubFragment(fragment)
            }

            rv_job_query_result.addOnScrollListener(object:RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if(mAdapter.hasFilter)return

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

            setOnAddTaskButtonClick()

        }
        //(parentFragment as JobQueryFragment).jobQueryResultViewModel.setJobQueryResultFragmentTag(tag,qno)
    }

    private fun setOnAddTaskButtonClick()
    {
        //添加“创建任务”按钮的事件监听器
//        mAdapter.setOnAddTaskButtonClickListener {queryResult: IQueryResultSource ->
//            RequestHelper.newJobQueryTask(LoginHelper.connectionOption,LoginHelper.session,(queryResult as JobQueryResult).queryId,{ result ->
//                if(result.state== MessageAgent.RequestResultState.SUCCESS){
//
//                }
//            })
//        }
    }


    private fun doLoadMore(){

        if(mQueryResult.canLoadMore){

            //快照模式下禁止加载更多数据
            if(mJobQueryResultViewModel.isCacheMode){
                mAdapter.notifyStateCacheMode()
                return
            }

            //标记为读取中
            mAdapter.notifyStateLoading()

            mJobQueryResultViewModel.loadMore(queryNo,queryId,{
                rv_job_query_result.post {
                    mAdapter.notifyStateLoadError()
                }
            })

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.frag_job_query_result,container,false)
        return view
    }

}