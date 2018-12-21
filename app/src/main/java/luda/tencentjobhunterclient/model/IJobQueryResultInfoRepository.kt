package luda.tencentjobhunterclient.model

/**
 * Created by luda on 2018/7/8
 * QQ 340071887.
 */
interface IJobQueryResultInfoRepository {
    val jobQueryResultInfoList : List<JobQueryResultInfo>

    fun setJobQueryResultInfoList(jobQueryResultInfoList : List<JobQueryResultInfo>)
}