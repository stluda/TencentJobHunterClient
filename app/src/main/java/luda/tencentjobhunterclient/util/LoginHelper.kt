package luda.tencentjobhunterclient.util

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import luda.tencentjobhunterclient.application.MyApplication.Companion.realmTaskWorker
import luda.tencentjobhunterclient.exception.GetResponseException
import luda.tencentjobhunterclient.model.IJobQueryResultInfoRepository
import luda.tencentjobhunterclient.model.JobQueryResultInfo
import luda.tencentjobhunterclient.model.JobRelatedInfo
import luda.tencentjobhunterclient.model.realm.RJob
import luda.tencentjobhunterclient.model.realm.RProfile
import luda.tencentjobhunterclient.option.ConnectionOption

/**
 * Created by luda on 2018/7/8
 * QQ 340071887.
 */
object LoginHelper {
    private var mSession : String = ""
    val session get() = mSession

    private var mUsername : String = ""
    private var mPassword : String = ""

    private lateinit var mConnectionOption : ConnectionOption
    val connectionOption get() = mConnectionOption

    val isLoggedIn : Boolean
        get() = mSession!=""

    fun init(realm:Realm){
        mConnectionOption = ConnectionOption(SettingHelper.serverIp,SettingHelper.serverPort)

        var profile = realm.where<RProfile>().equalTo("id",0L).findFirst()
        if(profile!=null){
            mSession = profile.session
            mUsername = profile.username
            mPassword = profile.password
        }
    }

    fun logoff()
    {
        mSession = ""
        mUsername = ""
        mPassword = ""
        realmTaskWorker.enqueueTask { realm ->
            try {
                realm.where<RProfile>().equalTo("id", 0L).findAll().deleteAllFromRealm()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //val loginSubject = PublishSubject.create<LoginResult>()

    val isLoggedInChangedSubject = PublishSubject.create<Boolean>()

    fun login(jobQueryResultInfoRepo : IJobQueryResultInfoRepository? = null,connectionOption: ConnectionOption = mConnectionOption)
            =login("","", mSession,connectionOption,jobQueryResultInfoRepo)

    fun login(username:String,password:String,connectionOption: ConnectionOption = mConnectionOption,jobQueryResultInfoRepo : IJobQueryResultInfoRepository? = null)
            =login(username,password, "",connectionOption,jobQueryResultInfoRepo)

    fun register(username: String,password: String,email:String,connectionOption: ConnectionOption = mConnectionOption): Single<Boolean>
            = RequestHelper.register(username,password, email,connectionOption)
            .map {response->
                SettingHelper.serverIp = connectionOption.serverHost
                SettingHelper.serverPort = connectionOption.serverPort
                mConnectionOption = connectionOption

                mSession = response.session

                realmTaskWorker.enqueueTask { realm ->
                    try {
                        realm.where<RProfile>().equalTo("id", 0L).findAll().deleteAllFromRealm()
                        val profile = realm.createObject<RProfile>(0L)
                        profile.username = username
                        mUsername = username
                        profile.password = password
                        mPassword = password
                        profile.session = mSession
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                JobRelatedInfo.loadFromProtoBuf(response.jobRelatedInfo)
                true
            }


    private fun login(username:String,password:String,session:String,connectionOption: ConnectionOption = mConnectionOption,
              jobQueryResultInfoRepo : IJobQueryResultInfoRepository? = null) : Single<Boolean>
        = RequestHelper.login(username,password, session,connectionOption)
            .map {response->
                SettingHelper.serverIp = connectionOption.serverHost
                SettingHelper.serverPort = connectionOption.serverPort
                mConnectionOption = connectionOption

                mSession = response.session

                realmTaskWorker.enqueueTask { realm ->
                    try {
                        var profile = realm.where<RProfile>().equalTo("id", 0L).findFirst()
                        if(profile==null) {
                            profile = realm.createObject<RProfile>(0L)
                            profile.username = username
                            mUsername = username
                            profile.password = password
                            mPassword = password
                        }
                        profile.session = mSession
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                JobRelatedInfo.loadFromProtoBuf(response.jobRelatedInfo)
                jobQueryResultInfoRepo?.setJobQueryResultInfoList(JobQueryResultInfo.fromProtoBufList(response.jobQueryResultInfo))
                true
            }

}