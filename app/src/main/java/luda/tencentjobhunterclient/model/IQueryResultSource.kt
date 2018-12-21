package luda.tencentjobhunterclient.model

import io.realm.annotations.Index

/**
 * Created by luda on 2018/6/20
 * QQ 340071887.
 */
interface IQueryResultSource {
    val totalCount : Int
    val queryExpression : String
    val canLoadMore : Boolean

    val viewSize : Int
    fun getItemAt(index:Int) : Job
    fun indexOf(job:Job) : Int

    fun filter(holder:QueryResultFilterHolder, start: Int, count:Int) : ArrayList<Int>{
        val result = ArrayList<Int>()

        for (i in 0 until count){
            if(holder.satisfy(this,i+start))result.add(i+start)
        }
        return result
    }
}