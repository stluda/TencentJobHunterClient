package luda.tencentjobhunterclient.viewmodel

import android.arch.lifecycle.ViewModel
import android.util.Log
import android.widget.Toast
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.frag_task_add.*
import luda.tencentjobhunterclient.R.id.edt_task_name
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.base.Interval
import luda.tencentjobhunterclient.model.ITaskRepository
import luda.tencentjobhunterclient.model.Task
import luda.tencentjobhunterclient.util.RequestHelper
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by luda on 2018/6/18
 * QQ 340071887.
 */
class TaskViewModel: ViewModel(),
        ITaskRepository ,
        ITaskFragmentViewModel,
        ITaskDetailFragmentViewModel{
    private val mTaskList : ArrayList<Task> = ArrayList<Task>()
    private val mTaskQueryResultInsertedSubjects : ArrayList<PublishSubject<Interval>> = ArrayList<PublishSubject<Interval>>()
    private val mTaskDict = HashMap<Int,Task>()

    override val dataChangedSubject = PublishSubject.create<Boolean>()
    private var mIsInitialized = false

    override var addedCountOfNewQueryResult = 0

    private val dictOfHasNewQueryResultSubject = HashMap<Int,PublishSubject<Int>>()
    override fun hasNewQueryResultSubject(taskId:Int) : PublishSubject<Int>{
        return if(!dictOfHasNewQueryResultSubject.containsKey(taskId)){
            val subject = PublishSubject.create<Int>()
            dictOfHasNewQueryResultSubject[taskId] = subject
            subject
        }else{
            dictOfHasNewQueryResultSubject[taskId]!!
        }
    }

    override val queryResultAddedSubject = PublishSubject.create<Int>()

    override val removeTaskSubject = PublishSubject.create<Int>()

    init {
        removeTaskSubject.subscribe {id->
            val task = getTaskById(id)
            val index = mTaskList.indexOf(task)

            dictOfHasNewQueryResultSubject.remove(id)
            mTaskQueryResultInsertedSubjects.removeAt(index)
            mTaskList.remove(task)
            mTaskDict.remove(id)
        }

    }

    override fun updateQueryResultCount(id: Int, count: Int) {
        val task = getTaskById(id)
        if(task!=null&&count>task.queryResultTotalCount){
            hasNewQueryResultSubject(id).onNext(count-task.queryResultTotalCount)
            task.queryResultTotalCount = count
            loadMore(task)
        }
    }

    override fun updateTotalQueryResultAddedCount(count: Int) {
        queryResultAddedSubject.onNext(count)
    }

    override val taskCount get() = mTaskList.size
    override fun getTaskAt(position:Int) = mTaskList[position]

    override fun getTaskById(id:Int) = mTaskDict[id]
            //mTaskList.find { task -> id==task.id }



    override fun getTaskQueryResultInsertedSubject(task:Task) : PublishSubject<Interval>?
    {
        val index = mTaskList.indexOf(task)
        return if(index<0||index>=mTaskQueryResultInsertedSubjects.size)
            null
        else
            mTaskQueryResultInsertedSubjects[index]
    }

//    override fun initDataIfNeeded(task:Task) : Single<Boolean>
//    {
//        return if(task.canLoadMore&&task.viewSize==0) Task.loadMoreTaskQueryResult(task,MyApplication.realmTaskWorker)
//                .onErrorReturn {ex->
//                    ex.printStackTrace()
//
//                    Interval(0,0)
//                }.map {true} else Single.just(true)
//    }


    override fun loadMore(task:Task)
    {
        if(task.canLoadMore) Task.loadMoreTaskQueryResult(task,MyApplication.realmTaskWorker)
                .onErrorReturn {ex->
                    ex.printStackTrace()
                    Thread.sleep(500)
                    loadMore(task)
                     Interval(0,0)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { interval->
                    getTaskQueryResultInsertedSubject(task)?.onNext(Interval(task.queryResultTotalCount-(interval.start+interval.count),interval.count))
                }
    }

    override fun init()
    {
        if(mIsInitialized) return
        loadData()
        mIsInitialized = true
    }

    override fun removeByTaskId(taskId:Int) : Single<Int>
    {
        return Task.remove(taskId).doOnSuccess {id->
            removeTaskSubject.onNext(id)
        }
    }

    fun createTask(taskName:String,queryId: String,expireTime:Calendar) : Single<Task>
        = Task.add(taskName,queryId,expireTime,MyApplication.realmTaskWorker)
                .doOnSuccess{task->
                    addTask(task)
                    dataChangedSubject.onNext(true)
                }

    private fun addTask(task:Task){
        val t = mTaskDict[task.id]
        if(t!=null){
            //如果本地数据库已存在该任务，则只更新查询结果个数
            t.queryResultTotalCount = task.queryResultTotalCount
        }
        else{
            //否则加入到列表当中
            mTaskList.add(task)
            mTaskDict[task.id]=task
        }
        mTaskQueryResultInsertedSubjects.add(PublishSubject.create())
    }

    private fun loadData()
    {
        Task.fromLocal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { taskList->
                    mTaskList.clear()
                    mTaskList.addAll(taskList)
                    for(task in taskList)mTaskDict[task.id]=task


                    Task.getList(MyApplication.realmTaskWorker)
                            .observeOn(AndroidSchedulers.mainThread())
                            .onErrorReturn {ex->
                                ArrayList<Task>()
                            }
                            .subscribe {taskList->
                                for(t in taskList){
                                    addTask(t)
                                }
                                dataChangedSubject.onNext(true)
                            }

                }


    }
}