package luda.tencentjobhunterclient.model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by luda on 2018/7/6
 * QQ 340071887.
 */
open class RSetting(@PrimaryKey var id:Int=0,
                    var serverIp : String = String(),
                    var serverPort : Int = 0,
                    var enableNotice : Boolean = false,
                    var enableNoticeForeground : Boolean = false,
                    var queryMode : Int = 0) : RealmObject(){
}