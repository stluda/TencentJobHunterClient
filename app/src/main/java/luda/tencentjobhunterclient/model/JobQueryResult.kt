package luda.tencentjobhunterclient.model

import TencentJobHunterMessage.Message
import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import luda.tencentjobhunterclient.exception.GetResponseException
import luda.tencentjobhunterclient.listener.IRealmTaskWorker
import luda.tencentjobhunterclient.model.realm.RJob
import luda.tencentjobhunterclient.model.realm.RJobQueryResult
import luda.tencentjobhunterclient.util.LoginHelper
import luda.tencentjobhunterclient.util.MessageAgent
import luda.tencentjobhunterclient.util.RequestHelper

data class JobQueryResult(val queryNo:Int,val queryId:String,
                          override val queryExpression:String,val startIndex:Int,val maxLength:Int) : ArrayList<Job>(),IQueryResultSource {

    override val canLoadMore get() = maxLength>size
    override val totalCount: Int get() = maxLength

    override fun indexOf(element: Job): Int {

        withIndex().forEach {wJob->
            if(wJob.value.id==element.id)return wJob.index
        }
        return -1
    }

    override val viewSize: Int
        get() = size

    override fun getItemAt(index: Int): Job = this[index]

    override fun clone(): Any {
        val cloneResult = JobQueryResult(queryNo,queryId,queryExpression,startIndex,maxLength)
        cloneResult.addAll(this)
        return cloneResult
    }


    companion object {

        val invalid = JobQueryResult(0,"","",0,0)

        private fun fromRealm(rResult:RJobQueryResult) : JobQueryResult
        {
            val result = JobQueryResult(rResult.queryNo,rResult.queryId,rResult.queryExpression,rResult.startIndex,rResult.maxLength)
            for(rJob in rResult.datas)
            {
                result.add(Job.createInstance(rJob.id,rJob.title,rJob.location,rJob.type,rJob.date,rJob.hiringNumber,rJob.duties,rJob.requirements,rJob.hasBeenSeen))
            }
            return result
        }

        fun fromQuery(queryExpression: String,queryNo:Int,
                      realmTaskWorker: IRealmTaskWorker): Single<JobQueryResult>
                = RequestHelper
                .getJobQueryResult(queryNo,"",0,queryExpression)
                .doOnSuccess { result ->
                    //因为result里的数据后面可能发生变化，所以先创建个副本
                    val resultCopy = result.clone() as JobQueryResult
                    realmTaskWorker.enqueueTask { realm ->
                        try {
                            val rResult = realm.createObject<RJobQueryResult>(resultCopy.queryId)
                            rResult.queryNo = resultCopy.queryNo
                            rResult.startIndex = resultCopy.startIndex
                            rResult.maxLength = resultCopy.maxLength
                            rResult.queryExpression = resultCopy.queryExpression
                            resultCopy.appendToRealm(rResult, realm)
                            val profile = RealmHelper.getProfile(realm)!!
                            profile.queryResults.add(rResult)
                            Log.d("dataChange", String.format("[add_sql]queryNo=%d,queryId=%s,index=%d", result.queryNo, result.queryId, profile.queryResults.size - 1))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

        fun fromLocal() : Single<ArrayList<JobQueryResult>> = Single.create{emitter ->
            var realm = RealmHelper.getInstance()
            try {

                val profile = RealmHelper.getProfile(realm)!!

                val results = ArrayList<JobQueryResult>()
                for(rResult in profile.queryResults)
                {
                    val result = JobQueryResult.fromRealm(rResult)
                    results.add(result)
                }
                emitter.onSuccess(results)
                //emitter.onComplete()

            }
            catch (e:Exception)
            {
                emitter.onError(e)
            }
            finally {
                realm.close()
            }
        }

        fun fromServer(queryResultAlreadyReceived:ArrayList<JobQueryResult>,jobQueryResultInfoListFromServer:List<JobQueryResultInfo>,realmTaskWorker: IRealmTaskWorker): Single<ArrayList<JobQueryResult>>
                = Single.create{emitter ->

            val queryExpressionMap = HashMap<String,String>()
            for(info in jobQueryResultInfoListFromServer)
                queryExpressionMap[info.queryId] = info.queryExpression


            //根据概要信息获取各查询结果的详情
            val requestList = ArrayList<Message.Request>()
            for(i in 0 until jobQueryResultInfoListFromServer.size){
                val resultInfo = jobQueryResultInfoListFromServer[i]

                //如果已经有了某个查询结果，则跳过
                if(queryResultAlreadyReceived.any {jobQueryResult: JobQueryResult ->
                            jobQueryResult.queryId == resultInfo.queryId }){
                    continue
                }

                val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
                requestBuilder.session = LoginHelper.session
                requestBuilder.type = TencentJobHunterMessage.Message.Type.ADD_JOB_QUERY
                requestBuilder.requestTime = System.currentTimeMillis()

                val queryOptionBuilder = TencentJobHunterMessage.Message.JobQueryOption.newBuilder()
                queryOptionBuilder.queryNo = resultInfo.queryNo
                queryOptionBuilder.queryId = resultInfo.queryId
                queryOptionBuilder.queryExpression = ""
                queryOptionBuilder.resultType = Message.JobDataType.EXCLUDE_CONTENT
                queryOptionBuilder.startIndex = 0
                requestBuilder.jobQueryOption = queryOptionBuilder.build()

                requestList.add(requestBuilder.build())
            }

            MessageAgent.getResponseRxMulti(requestList,LoginHelper.connectionOption)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { resultList->

                //当所有请求都成功才进行下一步
                if(resultList.all{result-> result.state==MessageAgent.RequestResultState.SUCCESS
                                && result.response!!.errorCode==Message.ErrorCode.SUCCESS}){
                    for(result in resultList){
                        val resultProtoBuf = result.response!!.jobQueryResult
                        val result = convertFromProtoBufList(resultProtoBuf.listList,resultProtoBuf.queryNo,
                                resultProtoBuf.queryId,queryExpressionMap[resultProtoBuf.queryId]?:"",resultProtoBuf.startIndex,resultProtoBuf.maxLength)
                        queryResultAlreadyReceived.add(result)
                    }

                    //按照queryNo排序
                    queryResultAlreadyReceived.sortBy {jobQueryResult: JobQueryResult ->jobQueryResult.queryNo}

                    emitter.onSuccess(queryResultAlreadyReceived)

                    //更新本地数据库
                    realmTaskWorker.enqueueTask { realm: Realm ->
                        val profile = RealmHelper.getProfile(realm)!!
                        profile.queryResults.clear()
                        realm.where<RJobQueryResult>().findAll().deleteAllFromRealm()

                        for (result in queryResultAlreadyReceived){
                            val rResult = realm.createObject<RJobQueryResult>(result.queryId)
                            rResult.queryNo = result.queryNo
                            rResult.startIndex = result.startIndex
                            rResult.maxLength = result.maxLength
                            rResult.queryExpression = result.queryExpression
                            result.appendToRealm(rResult,realm)

                            profile.queryResults.add(rResult)
                        }
                    }
                }
                else{
                    //请求里出现错误时的实现
                    lateinit var e : GetResponseException
                    for(result in resultList){
                        if(result.state==MessageAgent.RequestResultState.SUCCESS
                                && result.response!!.errorCode==Message.ErrorCode.SUCCESS){
                            val resultProtoBuf = result.response.jobQueryResult
                            val result = convertFromProtoBufList(resultProtoBuf.listList,resultProtoBuf.queryNo,
                                    resultProtoBuf.queryId,queryExpressionMap[resultProtoBuf.queryId]?:"",resultProtoBuf.startIndex,resultProtoBuf.maxLength)
                            queryResultAlreadyReceived.add(result)
                        }
                        else{
                            e = GetResponseException(result.response!!.errorCode,result.state)
                        }
                    }
                    emitter.onError(e)
                }
            }

        }

        fun remove(index:Int,queryNo: Int,queryId: String,realmTaskWorker: IRealmTaskWorker) : Single<Int>
                = RequestHelper.removeJobQueryResult(index,queryId)
                .doOnSuccess {
                    //删除realm中的持久化数据
                    realmTaskWorker.enqueueTask { realm: Realm ->
                        val profile = RealmHelper.getProfile(realm)!!
                        profile.queryResults.where().equalTo("queryNo",queryNo).findAll().deleteAllFromRealm()
                        Log.d("dataChange",String.format("[delete_sql]queryNo=%d,index=%d",queryNo,index))
                    }
                }

        fun updateLocalJobQueryResult(result: JobQueryResult,realmTaskWorker: IRealmTaskWorker)
        {
            realmTaskWorker.enqueueTask {realm->
                val profile = RealmHelper.getProfile(realm)!!
                val rResult = profile.queryResults.where().equalTo("queryId",result.queryId).findFirst()!!
                result.appendToRealm(rResult,realm)
            }
        }

        fun fromLoadMore(queryNo:Int,queryId: String,startIndex: Int) : Single<JobQueryResult>
                = RequestHelper.getJobQueryResult(queryNo,queryId,startIndex,"")

        fun convertFromProtoBufList(protoBufList:List<TencentJobHunterMessage.Message.Job>, queryNo:Int, queryId:String, queryExpression: String, startIndex:Int, maxLength:Int):JobQueryResult{
            val list = JobQueryResult(queryNo,queryId,queryExpression,startIndex,maxLength)
            for(item in protoBufList){
                val location = JobRelatedInfo.getLocationText(item.location) ?: item.location.toString()
                val type = JobRelatedInfo.getTypeText(item.type)?: item.type.toString()
                list.add(Job.createInstance(item.id,item.title,location,type,item.date,item.hiringNumber))
            }
            return list
        }


        fun checkIsSameAsServer(resultInfoList: List<JobQueryResultInfo>) : Boolean{
            RealmHelper.getInstance().use {realm->
                val profile = RealmHelper.getProfile(realm)!!

                //大小不一致直接返回false
                if(resultInfoList.size != profile.queryResults.size)return false

                //对比服务器的queryId和本地的是否一致
                val sortedRQueryResult = profile.queryResults.sort("queryNo")
                for(i in 0 until resultInfoList.size){
                    Log.d("checkIsSameAsServer","checkQueryId:"+resultInfoList[i].queryId+","+sortedRQueryResult[i]!!.queryId)
                    if(resultInfoList[i].queryId!=sortedRQueryResult[i]!!.queryId)
                    {
                        return false
                    }
                }
                return true
            }
        }
    }

    fun merge(result:JobQueryResult) : Boolean{
        //只有当要合并的结果起始索引能够完全对上号时才进行合并操作
        if(queryId==result.queryId && result.startIndex == size && result.maxLength==maxLength){
            addAll(result)
            return true
        }
        return false
    }



    fun appendToRealm(rResult:RJobQueryResult, realm: Realm)
    {
        for(jobItem in this)
        {
            var rJobItem = realm.where<RJob>().equalTo("id",jobItem.id).findFirst()
            if(rJobItem==null){
                rJobItem = realm.createObject<RJob>(jobItem.id)
                rJobItem.title = jobItem.title
                rJobItem.location = jobItem.location
                rJobItem.type = jobItem.type
                rJobItem.date = jobItem.date
                rJobItem.hiringNumber = jobItem.hiringNumber
                rJobItem.requirements = jobItem.requirements
                rJobItem.duties = jobItem.duties

            }
            //rJobItem.id = jobItem.id
            rResult.datas.add(rJobItem)
        }
    }
}