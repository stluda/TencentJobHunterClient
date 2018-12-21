package luda.tencentjobhunterclient.model

import TencentJobHunterMessage.Message
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.model.realm.RJobLocation
import luda.tencentjobhunterclient.model.realm.RJobType
import luda.tencentjobhunterclient.model.realm.RProfile

/**
 * Created by luda on 2018/7/8
 * QQ 340071887.
 */
object JobRelatedInfo {

    private val typeMap = HashMap<Int,String>()
    private val locationMap = HashMap<Int,String>()

    fun init(realm:Realm){
        realm.where<RJobType>().findAll().forEach {item->
            typeMap[item.id]=item.text
        }
        realm.where<RJobLocation>().findAll().forEach {item->
            locationMap[item.id]=item.text
        }
    }

    fun getTypeText(id:Int) = typeMap[id]
    fun getLocationText(id:Int) = locationMap[id]

    fun loadFromProtoBuf(info: Message.JobRelatedInfo){
        //构建职位的地点，类别[数字-名称]映射表
        for(i in 0 until info.typeIdCount){
            typeMap[info.getTypeId(i)]=info.getTypeName(i)
        }
        for(i in 0 until info.locationIdCount){
            locationMap[info.getLocationId(i)]=info.getLocationName(i)
        }

        MyApplication.realmTaskWorker.enqueueTask { realm ->
            realm.where<RJobType>().findAll().deleteAllFromRealm()
            realm.where<RJobLocation>().findAll().deleteAllFromRealm()

            for(i in 0 until info.typeIdCount){
                realm.createObject<RJobType>(info.getTypeId(i)).text=info.getTypeName(i)
            }
            for(i in 0 until info.locationIdCount){
                realm.createObject<RJobLocation>(info.getLocationId(i)).text=info.getLocationName(i)
            }
        }
    }

}