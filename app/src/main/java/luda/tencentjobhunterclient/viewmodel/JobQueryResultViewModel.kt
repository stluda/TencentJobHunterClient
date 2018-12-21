package luda.tencentjobhunterclient.viewmodel

import android.arch.lifecycle.ViewModel
import android.util.Log
import android.widget.Toast
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.model.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by luda on 2018/5/31
 * QQ 340071887.
 */
class JobQueryResultViewModel : ViewModel(),IJobQueryResultInfoRepository,IQuerySource {
    val jobQueryResults = ArrayList<JobQueryResult>()
    private val dictOfJobQueryResultLoadMoreSubject = HashMap<String,PublishSubject<JobQueryResult>>()
    private val jobQueryNoMap = IntArray(3){-1} //查询编号对应的jobQueryResults的索引

    val numberList = ArrayList<Int>()
    var latestNumber = 1
    var selectedQueryIndex = 0
    var isCacheMode = false

    val queryModeChangedSubject = PublishSubject.create<Boolean>()
    private var mIsExpressionMode = true
    var isExpressionMode : Boolean
    set(value) {
        mIsExpressionMode = value
        queryModeChangedSubject.onNext(value)
    }
    get()
    {
        return mIsExpressionMode
    }
    var isSimpleMode : Boolean = true

    var isInitialized = false


    var inputKeyword : String = ""

    var inputTitle : String = ""
    var inputType : String = ""
    var inputLocation : String = ""
    var inputDuties : String = ""
    var inputRequirements : String = ""

    var inputExpression : String = ""


    override val count: Int
        get() = jobQueryResults.size
    override fun getItemAt(index: Int): JobQueryResult = jobQueryResults[index]
    override fun indexOf(queryResult: JobQueryResult): Int = jobQueryResults.indexOf(queryResult)
    override fun getNumberAt(index: Int): Int = numberList[index]

    override val jobQueryResultInfoList = ArrayList<JobQueryResultInfo>()

    override fun setJobQueryResultInfoList(list: List<JobQueryResultInfo>) {
        jobQueryResultInfoList.clear()
        jobQueryResultInfoList.addAll(list)
    }

    val addJobQueryResultSubject = PublishSubject.create<JobQueryResult>()
    private val loadMoreJobQueryResultPrepareSubject = PublishSubject.create<JobQueryResult>()

    val filterChangedSubject = PublishSubject.create<QueryResultFilterHolder>()

    fun getLoadMoreJobQueryResultSubject(queryId:String) : PublishSubject<JobQueryResult>
    {
        if(!dictOfJobQueryResultLoadMoreSubject.containsKey(queryId))
        {
            val subject = PublishSubject.create<JobQueryResult>()
            dictOfJobQueryResultLoadMoreSubject[queryId] = subject
            return subject
        }
        else
        {
            return dictOfJobQueryResultLoadMoreSubject[queryId]!!
        }
    }

    val loadJobQueryResultFromLocalSubject = PublishSubject.create<ArrayList<JobQueryResult>>()
    val loadJobQueryResultFromServerSubject = PublishSubject.create<ArrayList<JobQueryResult>>()
    val removeJobQueryResultSubject = PublishSubject.create<Int>()



    init {

        addJobQueryResultSubject.subscribe{result->
            addJobQueryResult(result)
            Log.d("dataChange",String.format("[add]queryNo=%d,queryId=%s,index=%d",result.queryNo,result.queryId,jobQueryResults.size-1))
        }
        loadJobQueryResultFromLocalSubject.subscribe { results->
            for(result in results) addJobQueryResult(result)
        }
        loadJobQueryResultFromServerSubject.subscribe { results->
            for(result in results) addJobQueryResult(result)
        }
        removeJobQueryResultSubject.subscribe { index->
            val queryResult = jobQueryResults[index]
            Log.d("dataChange",String.format("[delete]queryNo=%d,queryId=%s,index=%d",queryResult.queryNo,queryResult.queryId,index))
            removeJobQueryResult(queryResult.queryNo)
            dictOfJobQueryResultLoadMoreSubject.remove(queryResult.queryId)
        }
        loadMoreJobQueryResultPrepareSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {result->
            val theResult = getJobQueryResult(result.queryNo)
            if(theResult!=null)
            {
                if(theResult.merge(result))
                {
                    //成功合并后，才通知外部UI进行更新
                    getLoadMoreJobQueryResultSubject(result.queryId).onNext(result)
                    //本地数据库更新
                    JobQueryResult.updateLocalJobQueryResult(result,MyApplication.realmTaskWorker)
                }
            }
        }
    }

    fun getJobDetail(id:Int,queryId: String) : Single<Job>
    {
        return Job.fromServer(id,MyApplication.realmTaskWorker)
    }


    //private var tmp : Observable<JobQueryResult> = Observable.empty()
    fun query(queryExpression: String) : Single<JobQueryResult>
    {
        return JobQueryResult.fromQuery(queryExpression,availableQueryNo,MyApplication.realmTaskWorker)
                .doOnSuccess{
                    result->addJobQueryResultSubject.onNext(result)
                }

    }

    fun removeByIndex(index: Int) : Single<Int>
    {
        val queryResult = jobQueryResults[index]
        return JobQueryResult.remove(index,queryResult.queryNo,queryResult.queryId,MyApplication.realmTaskWorker)
                .doOnSuccess{index -> removeJobQueryResultSubject.onNext(index)}
    }

    fun loadFromLocal()
    {
        reset1()
        JobQueryResult.fromLocal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{resultList -> loadJobQueryResultFromLocalSubject.onNext(resultList)}
    }

    fun loadFromServer(queryResultAlreadyReceived:ArrayList<JobQueryResult>)
    {
        reset1()
        JobQueryResult.fromServer(queryResultAlreadyReceived,jobQueryResultInfoList,MyApplication.realmTaskWorker)
                .subscribe{result->loadJobQueryResultFromServerSubject.onNext(result)}
    }

    fun loadMore(queryNo: Int,queryId:String,onError:()->Unit)
    {
        val theResult = getJobQueryResult(queryNo)
        if(theResult!=null)
        {
            JobQueryResult.fromLoadMore(queryNo,queryId,theResult.size)
                    .onErrorReturn {_->
                        onError()
                        JobQueryResult.invalid
                    }
                    .subscribe {result->loadMoreJobQueryResultPrepareSubject.onNext(result)}
        }
    }


    //检查本地数据库中的查询结果记录和服务器的是否一致
    fun checkIsSameAsServer() = JobQueryResult.checkIsSameAsServer(jobQueryResultInfoList)

    //var newJobQueryFragmentTag = ""
    //val jobQueryResultFragmentTags = ArrayList<String>()

    val availableQueryNo:Int get() {
        for(i in 0 until jobQueryNoMap.size){
            if(jobQueryNoMap[i]<0) return i
        }
        return -1
    }

    private fun reset1(){
        jobQueryResults.clear()
        jobQueryNoMap.fill(-1)
        numberList.clear()
        latestNumber = 1
        selectedQueryIndex = 0
    }

    fun reset(){
        jobQueryResults.clear()
        jobQueryNoMap.fill(-1)
        numberList.clear()
        latestNumber = 1
        selectedQueryIndex = 0
        isCacheMode = false
    }

    fun getQueryNoByIndex(index:Int):Int{
        return jobQueryResults[index].queryNo
    }

    private fun addJobQueryResult(result : JobQueryResult) {
        //val queryNo = availableQueryNo
        if(result.queryNo>=0){
            jobQueryResults.add(result)
            //jobQueryResultFragmentTags.add("")
            jobQueryNoMap[result.queryNo] = jobQueryResults.size - 1
            Log.d("addJobQueryResult","jobQueryNoMap:"+jobQueryNoMap.joinToString(",","(",")"))
        }
    }
    private fun removeJobQueryResult(queryNo:Int){
        try {
            val index = jobQueryNoMap[queryNo]
            if(index>=0){
                for(qno in 0 until jobQueryNoMap.size){
                    var i = jobQueryNoMap[qno]
                    if(index<i) jobQueryNoMap[qno]=i-1 //删除位置的索引如果在前面，则所有的queryNo对应的显示位置都要向前挪移1个位置
                }
                jobQueryNoMap[queryNo] = -1
                jobQueryResults.removeAt(index)
                //jobQueryResultFragmentTags.removeAt(index)
            }
            Log.d("removeJobQueryResult","jobQueryNoMap:"+jobQueryNoMap.joinToString(",","(",")"))
        }
        catch (e:Exception){
            Log.d("removeJobQueryResult","queryNo="+queryNo)
        }
    }

    fun setJobQueryResultFragmentTag(tag : String, queryNo: Int){
        val index = jobQueryNoMap[queryNo]
        //jobQueryResultFragmentTags[index] = tag
    }



    fun getJobQueryResult(queryNo : Int) : JobQueryResult?{
        val index = jobQueryNoMap[queryNo]
        return if(index<0) null else jobQueryResults[index]
    }

    fun getJobQueryResultAt(position: Int) = jobQueryResults[position]

}