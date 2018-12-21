package luda.tencentjobhunterclient.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by luda on 2018/7/8
 * QQ 340071887.
 */
open class RJobType(@PrimaryKey var id:Int=0,
                var text:String = String()) : RealmObject(){
}