package luda.tencentjobhunterclient.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_task.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.adapter.TaskAdapter
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.util.MyLinearLayoutManager
import luda.tencentjobhunterclient.util.MyRxLifeCycle
import luda.tencentjobhunterclient.util.DialogUtils
import luda.tencentjobhunterclient.util.mySubscribe
import luda.tencentjobhunterclient.viewmodel.ITaskFragmentViewModel
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel
import luda.tencentjobhunterclient.viewmodel.TaskViewModel
import java.util.concurrent.TimeUnit

/**
 * Created by luda on 2018/6/16
 * QQ 340071887.
 */
class TaskFragment : BaseNavigationFragment(){

    companion object {
        const val TAG = "TaskFragment"
    }

    override val navTag: String
        get() = TAG


    private lateinit var mJobQueryResultViewModel : JobQueryResultViewModel
    private lateinit var mTaskViewModel : ITaskFragmentViewModel
    private lateinit var mAdapter :TaskAdapter

    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun subscribeViewModelSubjects() {

        mTaskViewModel.dataChangedSubject
                .compose(MyRxLifeCycle<Boolean>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
            mAdapter.notifyDataSetChanged()
        }

        mTaskViewModel.queryResultAddedSubject
                .compose(MyRxLifeCycle<Int>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {addedCount->
                    mTaskViewModel.addedCountOfNewQueryResult += addedCount
                    DialogUtils.makeUnSwipeAbleSnackBar(coordinator_layout,"您的任务有新查询结果，合计${mTaskViewModel.addedCountOfNewQueryResult}项", Snackbar.LENGTH_INDEFINITE)
                            .setAction("知道了",{
                                //清零新查询结果计数
                                mTaskViewModel.addedCountOfNewQueryResult = 0
                            })
                            .show()
                    mAdapter.notifyDataSetChanged()
        }

        mTaskViewModel.removeTaskSubject
                .compose(MyRxLifeCycle<Int>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {_->
                    Toast.makeText(activity,"已成功删除",Toast.LENGTH_SHORT).show()
                    mAdapter.notifyDataSetChanged()
                }

        super.subscribeViewModelSubjects()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mJobQueryResultViewModel = ViewModelProviders.of(activity).get(JobQueryResultViewModel::class.java)
        mTaskViewModel = ViewModelProviders.of(activity).get(TaskViewModel::class.java)
        super.onActivityCreated(savedInstanceState)
        loadUI()


    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater!!.inflate(R.layout.frag_task, container, false)

    private fun loadUI(){
        toolbar.setTitle(R.string.menu_task)
        (activity as MainActivity).setToolBar(toolbar)
        setupRecyclerView()
        setHasOptionsMenu(true)

        rv_task.post {
            val params =  rv_task.layoutParams as CoordinatorLayout.LayoutParams
            params.setMargins(0,0,0,bottom_navigation.height)
            rv_task.layoutParams = params
        }

    }

    private fun setupRecyclerView()
    {
        rv_task.layoutManager = MyLinearLayoutManager(activity)
        mAdapter = TaskAdapter(mTaskViewModel)
        //添加加载更多的监听器
        rv_task.adapter = mAdapter

        mTaskViewModel.init()

//        mAdapter.onTaskItemClickedSubject
//                .compose(MyRxLifeCycle<Task>(this))
//                .subscribe {task->
//            mTaskViewModel.initDataIfNeeded(task).subscribe {_->
//
//            }
//        }

        mAdapter.onTaskItemClickedListener = { task, _ ->
            val bundle = Bundle()
            bundle.putInt(KeyConstants.TASK_ID,task.id)
            bundle.putString(KeyConstants.QUERY_EXPRESSION,task.queryExpression)
            val taskDetailFragment = TaskDetailFragment()
            taskDetailFragment.arguments= bundle
            toSubFragment(taskDetailFragment)
        }



        mAdapter.onTaskItemLongClickedListener = { task, view,point ->


            DialogUtils.showPopupWindow(activity,R.layout.popup_task,view,point,{layout,window->
                val tv =layout.findViewById<TextView>(R.id.tv_remove_task)
                tv.setOnClickListener {
                    mTaskViewModel.removeByTaskId(task.id).mySubscribe(this,true,{})
                    window.dismiss()
                }
            })

//            val pop = PopupMenu(activity,view)
//            pop.menuInflater.inflate(R.menu.simple_delete,pop.menu)
//            pop.setOnMenuItemClickListener {item->
//                when(item.itemId){
//                    R.id.item_delete->{
//                        mTaskViewModel.removeByTaskId(task.id).mySubscribe(this,true,{})
//                        true
//                    }
//                    else->throw NotImplementedError()
//                }
//            }
//            pop.show()
        }

        mAdapter.onAddTaskItemClickedListener={
                    if(mJobQueryResultViewModel.jobQueryResults.size==0){
                        Toast.makeText(activity,"查询任务是需要通过现有查询来建立\n你还没有查询，请先新建一个查询",Toast.LENGTH_SHORT).show()
                        navigate(0)
                    }
                    else{
                        toSubFragment(AddTaskFromQueryFragment())
                    }
                }
    }

    override val groupId get() = 1
}