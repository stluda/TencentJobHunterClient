package luda.tencentjobhunterclient.model.realm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where

/**
 * Created by luda on 2018/4/30
 * QQ 340071887.
 */
open class RJob(@PrimaryKey var id:Int=0,
                var title:String = String(),
                var location:String = String(),
                var type:String = String(),
                var date:String = String(),
                var hiringNumber:Int = 0,
                var requirements:String = String(),
                var duties:String = String(),
                var hasBeenSeen:Boolean = false,
                var isBookmarked:Boolean = false) : RealmObject(){
    companion object {
        fun get(id:Int,realm:Realm) : RJob?
            = realm.where<RJob>().equalTo("id", id).findFirst()
    }
}