package luda.tencentjobhunterclient.model.realm

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import luda.tencentjobhunterclient.model.JobQueryResult
import java.util.*

/**
 * Created by luda on 2018/6/20
 * QQ 340071887.
 */
open class RTask(@PrimaryKey var id:Int = 0,
                 var name : String = String(),
                 var queryExpression : String = String(),
                 var expireTime : Date = Date(),
                 var queryResultTotalCount : Int = 0,
                 var queryResult: RealmList<RTaskQueryResultItem> = RealmList()) : RealmObject(){

    fun fillQueryResult(queryResult: JobQueryResult,startIndex:Int, realm: Realm)
    {
        for((i,jobItem) in queryResult.withIndex())
        {
            var resultItem = this.queryResult.where().equalTo("index",startIndex+i).findFirst()
            if(resultItem==null){
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

                resultItem = realm.createObject<RTaskQueryResultItem>()
                resultItem.index = startIndex+i
                resultItem.taskId = id
                resultItem.job = rJobItem

                this.queryResult.add(resultItem)
            }
        }
    }
}

/*
fun RTask.fillQueryResult(queryResult: JobQueryResult, realm: Realm)
{
    for(jobItem in queryResult)
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
        this.queryResult.add(rJobItem)
    }
}
        */