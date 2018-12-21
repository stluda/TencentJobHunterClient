import io.realm.*
import io.realm.kotlin.where

import luda.tencentjobhunterclient.model.realm.*
import java.util.*


object RealmHelper {

    private fun updateFrom4To9(realm:DynamicRealm,currentVersion:Long) : Long{
        var currentVersion = currentVersion
        if(currentVersion<4L){
            val objectSchema = realm.schema.create(RTask::class.java.simpleName)
            objectSchema.addField("id",Int::class.java,FieldAttribute.INDEXED,FieldAttribute.PRIMARY_KEY)
            objectSchema.addField("name",String::class.java,FieldAttribute.REQUIRED)
            objectSchema.addField("queryExpression", String::class.java,FieldAttribute.REQUIRED)
            objectSchema.addField("expireTime",Date::class.java,FieldAttribute.REQUIRED)
            objectSchema.addField("queryResultTotalCount",Int::class.java)
            objectSchema.addRealmListField("queryResult",realm.schema.get("RJob"))
            realm.schema.get(RProfile::class.java.simpleName)!!.addRealmListField("tasks",objectSchema)
            currentVersion=4L
        }
        if(currentVersion<=4L){
            realm.schema.get(RJob::class.java.simpleName)!!.addField("hasBeenSeen",Boolean::class.java)
            currentVersion=5L
        }

        if(currentVersion<=5L){
            realm.schema.get(RProfile::class.java.simpleName)!!.addField("taskQueryResultChangedTime",Long::class.java)
            currentVersion=6L
        }

        if(currentVersion<=6L){
            val objectSchema = realm.schema.create(RSetting::class.java.simpleName)
            objectSchema.addField("id",Int::class.java,FieldAttribute.INDEXED,FieldAttribute.PRIMARY_KEY)
            objectSchema.addField("serverIp",String::class.java,FieldAttribute.REQUIRED)
            objectSchema.addField("serverPort",Int::class.java)
            objectSchema.addField("fetchTaskInfo",Boolean::class.java)
            currentVersion=7L
        }


        if(currentVersion<=7L){
            var objectSchema = realm.schema.create(RJobType::class.java.simpleName)
            objectSchema.addField("id",Int::class.java,FieldAttribute.INDEXED,FieldAttribute.PRIMARY_KEY)
            objectSchema.addField("text",String::class.java,FieldAttribute.REQUIRED)

            objectSchema = realm.schema.create(RJobLocation::class.java.simpleName)
            objectSchema.addField("id",Int::class.java,FieldAttribute.INDEXED,FieldAttribute.PRIMARY_KEY)
            objectSchema.addField("text",String::class.java,FieldAttribute.REQUIRED)
            currentVersion=8L
        }


        if(currentVersion<=8L){
            realm.schema.get(RProfile::class.java.simpleName)!!.renameField("hashOfTasks","taskQueryResultChangedTime")
            currentVersion=9L
        }

        return currentVersion
    }

    fun getInstance(): Realm {//
        val config = RealmConfiguration.Builder()
                .name("default1.realm")
                .schemaVersion(18)
                .migration { realm, oldVersion, newVersion ->
                    var currentVersion = updateFrom4To9(realm,oldVersion)

                    if(currentVersion<=9L){
                        val taskQueryResultItemSchema = realm.schema.create(RTaskQueryResultItem::class.java.simpleName)
                        taskQueryResultItemSchema.addField("index",Int::class.java,FieldAttribute.INDEXED,FieldAttribute.PRIMARY_KEY)
                        taskQueryResultItemSchema.addRealmObjectField("job",realm.schema.get(RJob::class.java.simpleName)!!)

                        val taskSchema = realm.schema.get(RTask::class.java.simpleName)!!
                        taskSchema.removeField("queryResult")
                        taskSchema.addRealmListField("queryResult",taskQueryResultItemSchema)

                        currentVersion=10L
                    }

                    if(currentVersion<=10L){
                        realm.where(RTaskQueryResultItem::class.java.simpleName).findAll().deleteAllFromRealm()
                        val taskQueryResultItemSchema = realm.schema.get(RTaskQueryResultItem::class.java.simpleName)!!
                        taskQueryResultItemSchema.removePrimaryKey()
                        taskQueryResultItemSchema.addIndex("index")
                        taskQueryResultItemSchema.addField("taskId",Int::class.java,FieldAttribute.INDEXED)
                        currentVersion=11L
                    }

                    if(currentVersion<=11L){
                        realm.schema.get(RSetting::class.java.simpleName)!!.renameField("fetchTaskInfo","enableNotice")
                        currentVersion=12
                    }
                    if(currentVersion<=12L){
                        realm.schema.rename("RConfig","RSetting")
                        currentVersion=14
                    }
                    if(currentVersion<=14L){
                        val profileSchema = realm.schema.get(RProfile::class.java.simpleName)!!
                        profileSchema.addRealmListField("myFavoriteJobs",realm.schema.get("RJob"))
                        profileSchema.addRealmListField("historyOfJobViewed",realm.schema.get("RJob"))
                        currentVersion=15
                    }
                    if(currentVersion<=15L){
                        val settingSchema = realm.schema.get(RSetting::class.java.simpleName)!!
                        settingSchema.addField("enableNoticeForeground",Boolean::class.java)
                        currentVersion=16
                    }

                    if(currentVersion<=16L){
                        val jobSchema = realm.schema.get(RJob::class.java.simpleName)!!
                        jobSchema.addField("isBookmarked",Boolean::class.java)
                        currentVersion=17
                    }

                    if(currentVersion<=17L){
                        val settingSchema = realm.schema.get(RSetting::class.java.simpleName)!!
                        settingSchema.addField("queryMode",Int::class.java)
                        currentVersion=18
                    }

                }.build()

        Realm.setDefaultConfiguration(config)

        return Realm.getDefaultInstance()
    }

    fun getProfile(realm:Realm) : RProfile?{
        return realm.where<RProfile>().equalTo("id", 0L).findFirst()
    }

}