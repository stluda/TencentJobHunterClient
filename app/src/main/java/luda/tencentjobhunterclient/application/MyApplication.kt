package luda.tencentjobhunterclient.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.squareup.leakcanary.LeakCanary
import io.realm.Realm
import com.squareup.leakcanary.RefWatcher
import io.realm.kotlin.where
import luda.tencentjobhunterclient.model.JobRelatedInfo
import luda.tencentjobhunterclient.model.realm.RTask
import luda.tencentjobhunterclient.model.realm.RTaskQueryResultItem
import luda.tencentjobhunterclient.util.SettingHelper
import luda.tencentjobhunterclient.util.LoginHelper
import luda.tencentjobhunterclient.worker.RealmTaskWorker
import android.telephony.TelephonyManager
import android.net.NetworkInfo
import android.content.Context.TELEPHONY_SERVICE
import android.net.ConnectivityManager
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager




/**
 * Created by luda on 2018/4/7
 * QQ 340071887.
 */
class MyApplication: Application() {

    private lateinit var mRefWatcher : RefWatcher
    private lateinit var mRealmTaskWorker : RealmTaskWorker


//    fun _clearInfo_for_test(){
//        RealmHelper.getInstance().use {realm->
//            realm.executeTransaction {realm->
//                realm.where<RTask>().findAll().forEach { rTask->
//                    rTask.queryResult.where().findAll().deleteAllFromRealm()
//                    if(rTask.id==1){
//                        rTask.queryResultTotalCount = 59
//                    }
//                }
//                realm.where<RTaskQueryResultItem>().findAll().deleteAllFromRealm()
//            }
//        }
//    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Realm.init(this)

        //_clearInfo_for_test()

        mRealmTaskWorker = RealmTaskWorker()
        mRealmTaskWorker.onCreate()


        val realm = RealmHelper.getInstance()

        SettingHelper.init(realm)
        //SettingHelper.serverIp =   "tjhserv.com" //"192.168.43.3"
        //SettingHelper.serverIp =   "192.168.43.3"

        LoginHelper.init(realm)
        JobRelatedInfo.init(realm)
        realm.close()
        //LeakCanary.install(this);


        mRefWatcher= setupLeakCanary()

        setActivityLifecycleCallbacks()

        //webkit调试realm数据库工具
        /*Stetho.initialize(//Stetho初始化
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build()
        );*/


    }

    private var mActivityCount : Int = 0


    //判断GPS是否打开
    val isGpsEnabled : Boolean get() {
        val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val accessibleProviders = lm.getProviders(true)
        return accessibleProviders != null && accessibleProviders.size > 0
    }

    //判断WIFI是否打开
    val isWifiEnabled: Boolean get()  {
        val mgrConn = this
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mgrTel = this
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return mgrConn.activeNetworkInfo != null && mgrConn
                .activeNetworkInfo.state == NetworkInfo.State.CONNECTED || mgrTel
                .networkType == TelephonyManager.NETWORK_TYPE_UMTS
    }

    val isNetworkEnabled get() = isGpsEnabled||isWifiEnabled

    private fun setActivityLifecycleCallbacks(){
        registerActivityLifecycleCallbacks(object:ActivityLifecycleCallbacks{

            override fun onActivityStarted(p0: Activity?) {
                mActivityCount++
            }

            override fun onActivityStopped(p0: Activity?) {
                mActivityCount--
            }

            override fun onActivityResumed(p0: Activity?) {}
            override fun onActivityPaused(p0: Activity?) { }
            override fun onActivityCreated(p0: Activity?, p1: Bundle?) {}
            override fun onActivityDestroyed(p0: Activity?) {}
            override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {}


        })
    }

    private fun setupLeakCanary(): RefWatcher {
        return if (LeakCanary.isInAnalyzerProcess(this)) {
            RefWatcher.DISABLED
        } else
            LeakCanary.install(this)
    }


    companion object {
        lateinit var instance: MyApplication
            private set
        val refWatcher get() = instance.mRefWatcher
        val realmTaskWorker get() = instance.mRealmTaskWorker
        val isForeground get() = instance.mActivityCount>0
        //判断GPS是否打开
        private val isGpsEnabled get() = instance.isGpsEnabled
        //判断WIFI是否打开
        private val isWifiEnabled get() = instance.isWifiEnabled
        val isNetworkEnabled get() = isGpsEnabled||isWifiEnabled
    }


    override fun onTerminate() {
        super.onTerminate()
        mRealmTaskWorker.onDestroy()
    }
}