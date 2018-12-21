package luda.tencentjobhunterclient.model.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by luda on 2018/4/30
 * QQ 340071887.
 */
open class RProfile(
        @PrimaryKey var id : Long = 0,
        var username:String = String(),
        var password:String = String(),
        var session:String = String(),
        var queryResults: RealmList<RJobQueryResult> = RealmList(),
        var tasks: RealmList<RTask> = RealmList(),
        var taskQueryResultChangedTime : Long = 0,
        var myFavoriteJobs : RealmList<RJob> = RealmList(),
        var historyOfJobViewed : RealmList<RJob> = RealmList()
        ) : RealmObject(){
}