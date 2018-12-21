package luda.tencentjobhunterclient.listener

import io.realm.Realm

/**
 * Created by luda on 2018/5/29
 * QQ 340071887.
 */
interface IRealmTaskWorker {
    fun enqueueTask(task:(realm: Realm)->Unit)
}