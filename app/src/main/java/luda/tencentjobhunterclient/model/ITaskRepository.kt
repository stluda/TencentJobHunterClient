package luda.tencentjobhunterclient.model

/**
 * Created by luda on 2018/7/14
 * QQ 340071887.
 */
interface ITaskRepository {
    fun updateQueryResultCount(id:Int,count:Int)
    fun updateTotalQueryResultAddedCount(count:Int)
}