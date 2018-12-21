package luda.tencentjobhunterclient.model.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by luda on 2018/4/30
 * QQ 340071887.
 */
open class RJobQueryResult(@PrimaryKey var queryId:String = String(),
                           var queryNo:Int = 0,
                           var queryExpression : String = String(),
                           var startIndex:Int = 0, var maxLength:Int = 0,
                           var datas: RealmList<RJob> = RealmList()) : RealmObject(){
}