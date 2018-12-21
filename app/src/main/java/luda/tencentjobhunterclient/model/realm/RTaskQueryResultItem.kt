package luda.tencentjobhunterclient.model.realm

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

/**
 * Created by luda on 2018/7/14
 * QQ 340071887.
 */
open class RTaskQueryResultItem(@Index var index:Int=0,
                                @Index var taskId:Int=0,
                                var job:RJob? = null
): RealmObject(){
}