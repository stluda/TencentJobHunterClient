package luda.tencentjobhunterclient.model

import android.util.Log
import io.reactivex.Single
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import luda.tencentjobhunterclient.application.MyApplication.Companion.realmTaskWorker
import luda.tencentjobhunterclient.base.Interval
import luda.tencentjobhunterclient.listener.IRealmTaskWorker
import luda.tencentjobhunterclient.model.realm.*
import luda.tencentjobhunterclient.util.RequestHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by luda on 2018/6/18
 * QQ 340071887.
 */
class Task(val id:Int,
                val name:String ,
                override val queryExpression:String,
                val expireTime:Date,
                queryResultTotalCount:Int) : IQueryResultSource{
    //任务里的查询结果和普通的查询结果有点不一样，普通查询结果是正序排列
    //而任务里的查询结果要把最新结果放在最上面，因此是倒序排列

    private val mJobQueryResult = ArrayList<Job?>()

    private var mQueryResultTotalCount : Int = 0;
    var queryResultTotalCount:Int
        get() = mQueryResultTotalCount
        set(value) {
            val start = mJobQueryResult.size
            if(value>start)mJobQueryResult.addAll(Array<Job?>(value-start,{null}))
            computeViewSize()
            mQueryResultTotalCount = value
        }

    init {
        this.queryResultTotalCount = queryResultTotalCount
    }



    override val totalCount: Int get() = queryResultTotalCount

    //未读查询结果数目
    val countOfHasNotBeenSeenResult : Int get() = mJobQueryResult.sumBy {job: Job? ->
        if(job==null||!job.hasBeenSeen) 1 else 0
    }

    override val canLoadMore: Boolean
        get() = viewSize < queryResultTotalCount

    //可见数目
    private var mViewSize = 0
    override val viewSize: Int
        get() = synchronized(this){mViewSize}


    val loadMoreInterval : Interval
        get()= synchronized(this) {
            val start = mQueryResultTotalCount - mViewSize -1
            val end = if(start-9<0) 0 else start-9
            for(i in start downTo end){
                if(mJobQueryResult[i]!=null){
                    return@synchronized Interval(i + 1 , start - i)
                }
            }
            return@synchronized Interval(end,start - end + 1)
        }


    private fun reverseIndex(index: Int) = mJobQueryResult.size-index-1

    //Task中的查询结果是倒序排列的，所以展示出来的item是相反排序
    //override fun getItemAt(index: Int): Job = mJobQueryResult[reverseIndex(index)]!!

    override fun getItemAt(index: Int): Job
    {
        val i=reverseIndex(index)
        if(i<0||i>=mJobQueryResult.size){
            Log.d("xxx","xxx");
        }
        if(mJobQueryResult[i]==null){
            Log.d("xxx","xxx");
        }
        return mJobQueryResult[i]!!
    }

    override fun indexOf(job: Job): Int {
        val index = mJobQueryResult.indexOf(job)
        return if(index>0) reverseIndex(index) else -1
    }

    private fun computeViewSize(){
        //从末尾开始计数直到找到的第一个空值为止，得到的计数即为可展现的结果个数
        synchronized(this){
            val length = mJobQueryResult.size
            mViewSize = length
            for(i in 0 until length){
                if(mJobQueryResult[length-i-1]==null){
                    mViewSize = i
                    break
                }
            }
        }
    }

    fun fillQueryResult(jobQueryResult: JobQueryResult,startIndex:Int)
    {
        val size = jobQueryResult.size
        for(i in 0 until size){
            mJobQueryResult[startIndex+i] = jobQueryResult[i]
        }
        computeViewSize()
    }



    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val invalid = Task(0,"","",Date(),0)

        private fun fromRealm(rTask: RTask) : Task
        {
            val task = Task(rTask.id,rTask.name,rTask.queryExpression,rTask.expireTime,rTask.queryResultTotalCount)
            for(rItem in rTask.queryResult)
            {
                val rJob = rItem.job!!
                task.mJobQueryResult[rItem.index]=Job.createInstance(rJob.id,rJob.title,rJob.location,rJob.type,rJob.date,rJob.hiringNumber,rJob.duties,rJob.requirements)
            }
            task.computeViewSize()
            return task
        }

        fun fromLocal() : Single<ArrayList<Task>> = Single.create { emitter ->
            var realm = RealmHelper.getInstance()
            try {
                val profile = RealmHelper.getProfile(realm)!!

                val taskList = ArrayList<Task>()

                for(rTask in profile.tasks){
                    taskList.add(fromRealm(rTask))
                }
                emitter.onSuccess(taskList)
            }
            catch (e:Exception)
            {
                emitter.onError(e)
            }
            finally {
                realm.close()
            }
        }

        fun add(taskName:String,queryId: String,expireTime:Calendar,realmTaskWorker: IRealmTaskWorker)
            :Single<Task> = RequestHelper.addTask(taskName,queryId,expireTime)
                .doOnSuccess {task->
                    realmTaskWorker.enqueueTask { realm ->
                        val profile = RealmHelper.getProfile(realm)!!
                        val rTasks = profile.tasks

                        if(rTasks.find { rTask -> rTask.id == task.id }==null){
                            val rTask = realm.where<RTask>().equalTo("id",task.id).findFirst()?:realm.createObject<RTask>(task.id)
                            rTask.name = task.name
                            rTask.queryExpression = task.queryExpression
                            rTask.expireTime = task.expireTime
                            rTask.queryResultTotalCount = task.queryResultTotalCount
                            rTasks.add(rTask)
                        }

                    }
                }

        fun remove(id:Int) : Single<Int> = RequestHelper.removeTask(id)
                .doOnSuccess {
                    //删除realm中的持久化数据
                    realmTaskWorker.enqueueTask { realm: Realm ->
                        val profile = RealmHelper.getProfile(realm)!!
                        profile.tasks.where().equalTo("id",id).findAll().deleteAllFromRealm()
                        realm.where<RTask>().equalTo("id",id).findAll().deleteAllFromRealm()
                    }
                }

        fun getList(realmTaskWorker: IRealmTaskWorker) : Single<List<Task>>
                = RequestHelper.getTaskListOverview()
                .doOnSuccess {//更新本地数据库，同步数据
                    taskList ->
                    val taskIdListFromServer = taskList.map { task -> task.id }
                    realmTaskWorker.enqueueTask { realm ->
                        //查询任务
                        val profile = RealmHelper.getProfile(realm)!!

                        //对比服务器的queryId和本地的是否一致
                        val rTasks = profile.tasks

                        //删除本地存在或服务器不存在的任务
                        rTasks.removeAll { rTask -> !taskIdListFromServer.contains(rTask.id) }

                        //更新本地已有任务的查询个数，增加本地没有的任务
                        taskList.forEach { task ->
                            var rTask = rTasks.find { rTask -> rTask.id == task.id }
                            if (rTask != null) {
                                rTask.queryResultTotalCount = task.queryResultTotalCount
                            } else {
                                rTask = realm.where<RTask>().equalTo("id",task.id).findFirst()?:realm.createObject<RTask>(task.id)
                                rTask.name = task.name
                                rTask.queryExpression = task.queryExpression
                                rTask.expireTime = task.expireTime
                                rTask.queryResultTotalCount = task.queryResultTotalCount
                                rTasks.add(rTask)
                            }
                        }
                    }
                }




        fun loadMoreTaskQueryResult(task:Task,realmTaskWorker: IRealmTaskWorker) : Single<Interval>
        {
            val interval = task.loadMoreInterval

            if(interval.start+interval.count>task.queryResultTotalCount){
                Log.d("task","ERROR,loadMoreInteval=${interval.start},${interval.count}")
            }

            Log.d("task","loadMoreInteval=${interval.start},${interval.count}")
            return RequestHelper.getTaskQueryResult(task.id,interval.start,interval.count)
                    .map {result: JobQueryResult ->
                        task.fillQueryResult(result,interval.start)
                        realmTaskWorker.enqueueTask { realm->
                            val profile = RealmHelper.getProfile(realm)!!
                            val rTask = profile.tasks.where().equalTo("id",task.id).findFirst()!!
                            rTask.fillQueryResult(result,interval.start,realm)
                        }
                        return@map Interval(interval.start,result.size)
                    }
        }


        fun convertFromProtoBufList(protoBufList:List<TencentJobHunterMessage.Message.Task>) : List<Task>
        {
            val list = ArrayList<Task>()
            for(pTask in protoBufList)
            {
                list.add(Task(pTask.id,pTask.taskname,pTask.queryExpression,  dateFormat.parse(pTask.expireTime),pTask.queryResultCount));
            }
            return list
        }



    }
}