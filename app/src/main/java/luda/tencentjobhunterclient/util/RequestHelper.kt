package luda.tencentjobhunterclient.util

import TencentJobHunterMessage.Message
import luda.tencentjobhunterclient.model.JobQueryResult
import java.text.SimpleDateFormat
import java.util.*
import io.reactivex.Single
import luda.tencentjobhunterclient.model.Job
import luda.tencentjobhunterclient.model.Task
import luda.tencentjobhunterclient.option.ConnectionOption
import luda.tencentjobhunterclient.util.LoginHelper.session

/**
 * Created by luda on 2018/5/12
 * QQ 340071887.
 */
//封装所有要用到的请求
object RequestHelper {

    fun removeJobQueryResult(index:Int,queryId: String) : Single<Int>
    {
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.session = LoginHelper.session
        requestBuilder.type = TencentJobHunterMessage.Message.Type.DELETE_JOB_QUERY
        requestBuilder.requestTime = System.currentTimeMillis()

        val queryOptionBuilder = TencentJobHunterMessage.Message.JobQueryOption.newBuilder()
        queryOptionBuilder.queryId = queryId
        requestBuilder.jobQueryOption = queryOptionBuilder.build()

        return MessageAgent.getResponseRx(requestBuilder.build(),LoginHelper.connectionOption)
                .map {return@map index }
    }

    fun removeTask(id:Int) : Single<Int>
    {
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.session = LoginHelper.session
        requestBuilder.type = TencentJobHunterMessage.Message.Type.DELETE_TASK
        requestBuilder.requestTime = System.currentTimeMillis()
        requestBuilder.id = id

        return MessageAgent.getResponseRx(requestBuilder.build(),LoginHelper.connectionOption)
                .map {return@map id }
    }

    fun addTask(taskName:String,queryId: String,expireTime:Calendar) : Single<Task>{
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.session = LoginHelper.session
        requestBuilder.type = TencentJobHunterMessage.Message.Type.ADD_TASK
        requestBuilder.requestTime = System.currentTimeMillis()

        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MONTH,2)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")

        val taskOptionBuilder = TencentJobHunterMessage.Message.AddTaskOption.newBuilder()
        taskOptionBuilder.expireTime = sdf.format(expireTime.time)
        taskOptionBuilder.queryId = queryId
        taskOptionBuilder.taskName = taskName
        //requestBuilder.AddTaskOption
        requestBuilder.jobQueryTaskOption = taskOptionBuilder.build()

        return MessageAgent.getResponseRx(requestBuilder.build(),LoginHelper.connectionOption)
                .map {response->
                    return@map Task.convertFromProtoBufList(response.taskListList)[0]
                }
    }

    fun register(username:String, password:String, email:String,connectionOption: ConnectionOption=LoginHelper.connectionOption) : Single<Message.Response>
    {
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.username = username
        requestBuilder.password = password
        requestBuilder.email = email
        requestBuilder.type = TencentJobHunterMessage.Message.Type.REGISTER
        requestBuilder.requestTime = System.currentTimeMillis()
        return MessageAgent.getResponseRx(requestBuilder.build(),connectionOption)
    }


    fun login(username:String, password:String, session:String="",connectionOption: ConnectionOption=LoginHelper.connectionOption) : Single<Message.Response>
    {
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.username = username
        requestBuilder.password = password
        requestBuilder.session = session
        requestBuilder.type = TencentJobHunterMessage.Message.Type.LOGIN
        requestBuilder.requestTime = System.currentTimeMillis()
        return MessageAgent.getResponseRx(requestBuilder.build(),connectionOption)
    }

    fun getJobQueryResult(queryNo:Int, queryId:String, startIndex:Int, queryExpression:String) : Single<JobQueryResult>
    {
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.session = LoginHelper.session
        requestBuilder.type = TencentJobHunterMessage.Message.Type.ADD_JOB_QUERY
        requestBuilder.requestTime = System.currentTimeMillis()

        val queryOptionBuilder = TencentJobHunterMessage.Message.JobQueryOption.newBuilder()
        queryOptionBuilder.queryNo = queryNo
        queryOptionBuilder.queryId = queryId
        queryOptionBuilder.queryExpression = queryExpression
        queryOptionBuilder.resultType = Message.JobDataType.EXCLUDE_CONTENT
        queryOptionBuilder.startIndex = startIndex
        requestBuilder.jobQueryOption = queryOptionBuilder.build()

        return MessageAgent.getResponseRx(requestBuilder.build(),LoginHelper.connectionOption)
                .map {response->
                    val resultProtoBuf = response.jobQueryResult
                    return@map JobQueryResult.convertFromProtoBufList(resultProtoBuf.listList,resultProtoBuf.queryNo,resultProtoBuf.queryId,queryExpression,resultProtoBuf.startIndex,resultProtoBuf.maxLength)
                }
    }

    //获取任务列表（不包含查询结果）
   fun getTaskListOverview() : Single<List<Task>>
    {
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.session = LoginHelper.session
        requestBuilder.type = TencentJobHunterMessage.Message.Type.GET_TASK_LIST
        requestBuilder.requestTime = System.currentTimeMillis()
        return MessageAgent.getResponseRx(requestBuilder.build(), LoginHelper.connectionOption)
                .map {response->Task.convertFromProtoBufList(response.taskListList)
                }
    }

    fun getJobDetail(jobId : Int) : Single<Job>
    {
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.session = LoginHelper.session
        requestBuilder.type = TencentJobHunterMessage.Message.Type.GET_JOB_DETAIL
        requestBuilder.requestTime = System.currentTimeMillis()
        requestBuilder.id = jobId
        return MessageAgent.getResponseRx(requestBuilder.build(), LoginHelper.connectionOption)
                .map {response->Job.fromProtoBuf(response.jobDetail)}
    }

    //获取任务的查询结果
    fun getTaskQueryResult(taskId:Int,startIndex:Int,count:Int) : Single<JobQueryResult>
    {
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.session = LoginHelper.session
        requestBuilder.type = TencentJobHunterMessage.Message.Type.GET_TASK_QUERY_RESULT
        requestBuilder.requestTime = System.currentTimeMillis()

        val optionBuilder = Message.GetTaskQueryResultOption.newBuilder()
        optionBuilder.taskId=taskId
        optionBuilder.startIndex = startIndex
        optionBuilder.count = count
        requestBuilder.getTaskDetailOption = optionBuilder.build()

        return MessageAgent.getResponseRx(requestBuilder.build(), LoginHelper.connectionOption)
                .map {response->
                    val result = response.jobQueryResult
                    return@map JobQueryResult.convertFromProtoBufList(result.listList,-1,"","",
                            result.startIndex,result.maxLength)
                }
    }

    fun getTaskQueryResultChangedTime() : Single<Long>
    {
        val requestBuilder = TencentJobHunterMessage.Message.Request.newBuilder()
        requestBuilder.session = LoginHelper.session
        requestBuilder.type = TencentJobHunterMessage.Message.Type.GET_TASK_QUERY_RESULT_CHANGED_TIME
        requestBuilder.requestTime = System.currentTimeMillis()

        return MessageAgent.getResponseRx(requestBuilder.build(), LoginHelper.connectionOption)
                .map {
                    response-> response.taskQueryResultChangedTime
                }
    }

}