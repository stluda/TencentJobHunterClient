package luda.tencentjobhunterclient.model

/**
 * Created by luda on 2018/7/29
 * QQ 340071887.
 */
interface IQuerySource {
    val count : Int
    fun getItemAt(index:Int) : JobQueryResult
    fun getNumberAt(index: Int) : Int
    fun indexOf(queryResult : JobQueryResult) : Int
}