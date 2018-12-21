package luda.tencentjobhunterclient.util

import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.model.realm.RSetting

/**
 * Created by luda on 2018/7/7
 * QQ 340071887.
 */
object SettingHelper {
    private var inited = false

    private lateinit var mServerIp : String
    var serverIp : String
        get() {
            return mServerIp
        }
        set(value) {
            mServerIp = value
            setConfigValue{config -> config.serverIp = value }
        }

    private var mServerPort : Int = 0
    var serverPort : Int
        get() {
            return mServerPort
        }
        set(value) {
            mServerPort = value
            setConfigValue{config -> config.serverPort = value }
        }

    private var mEnableNotice : Boolean = false
    var enableNotice : Boolean
        get() {
            return mEnableNotice
        }
        set(value) {
            mEnableNotice = value
            setConfigValue{config -> config.enableNotice = value }
        }

    private var mEnableNoticeForeground : Boolean = false
    var enableNoticeForeground : Boolean
        get() {
            return mEnableNoticeForeground
        }
        set(value) {
            mEnableNoticeForeground = value
            setConfigValue{config -> config.enableNoticeForeground = value }
        }

    private var mQueryMode : Int = 0
    var queryMode : Int
        get() {
            return mQueryMode
        }
        set(value) {
            mQueryMode = value
            setConfigValue{config -> config.queryMode = value }
        }

    private fun setConfigValue(func:(config:RSetting) -> Unit){
        MyApplication.realmTaskWorker.enqueueTask {realm ->
            val config = realm.where<RSetting>().equalTo("id", 0L).findFirst()
            func(config!!)
        }
    }

    fun init(realm:Realm){
        var config = realm.where<RSetting>().equalTo("id", 0L).findFirst()
        if(config==null){
            MyApplication.realmTaskWorker.enqueueTask {realm ->
                realm.createObject<RSetting>(0)
            }
            initDefault()
        }
        else{
            mServerIp = config.serverIp
            mServerPort = config.serverPort
            mEnableNotice = config.enableNotice
            mEnableNoticeForeground = config.enableNoticeForeground
            mQueryMode = config.queryMode
        }
        inited = true
    }

    private fun initDefault(){
        mServerIp = "tjhserv.com" //"192.168.43.3"
        mServerPort = 22343
        mEnableNotice = true
        mEnableNoticeForeground = false
        mQueryMode = 0

        setConfigValue{config ->
            config.serverIp = mServerIp
            config.serverPort = mServerPort
            config.enableNotice = mEnableNotice
            config.enableNoticeForeground = mEnableNoticeForeground
            config.queryMode = mQueryMode
        }
    }

}