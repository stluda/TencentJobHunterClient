package luda.tencentjobhunterclient.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ConditionVariable
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.util.SparseIntArray
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.realm.kotlin.where
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.constant.ActionConstants
import luda.tencentjobhunterclient.fragment.TaskDetailFragment
import luda.tencentjobhunterclient.fragment.TaskFragment
import luda.tencentjobhunterclient.model.ITaskRepository
import luda.tencentjobhunterclient.model.Task
import luda.tencentjobhunterclient.model.realm.RProfile
import luda.tencentjobhunterclient.util.INavigationState
import luda.tencentjobhunterclient.util.RequestHelper
import luda.tencentjobhunterclient.util.SettingHelper
import java.util.HashMap

/**
 * Created by luda on 2018/7/2
 * QQ 340071887.
 */
class TaskNotificationService : Service() {

    companion object {
        const val TAG = "TaskNotificationService"
    }

    private val mutex1 = Unit
    private val mutex2 = Unit


    var mIsWorking = false


    val taskQueryResultChangedSubject = PublishSubject.create<Boolean>()
    var taskRepository : ITaskRepository? = null
    var navigationManager : INavigationState? = null

    private val currentUIState : String
        get() =synchronized(mutex2){
            if(MyApplication.isForeground&&navigationManager!=null)navigationManager!!.currentFragmentTag
            else "Background"
        }





    private val mBinder = Binder()

    private val mTaskIdList = ArrayList<Int>()
    private val mQueryResultCountOfTask = SparseIntArray()
    private val mQueryResultChangedTaskIdList = ArrayList<Int>()

    private val mWorkThreadSleepingCondition: ConditionVariable = ConditionVariable()
    private var mWorkerThread : Thread? = null

    private var mTaskQueryResultChangedTime : Long = 0

    private var notificationClickReceiver : NotificationClickReceiver? = null

    //private val mCurrentFragmentTag : String get() =

    inner class Binder : android.os.Binder(){
        val service get() = this@TaskNotificationService
    }

    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"服务执行onCreate")
        init()
        doWork()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
        Log.d(TAG,"服务执行onStartCommand")
    }

    fun clearExternalObjects(){
        synchronized(mutex2){
            taskRepository = null
            navigationManager = null
        }
    }


    fun notifyHasTask(taskQueryResultChangedTime:Long){
        synchronized(mutex1){
            mTaskQueryResultChangedTime = taskQueryResultChangedTime
        }
        doWork()
    }



    private fun init(){
        notificationClickReceiver = NotificationClickReceiver()
        val intentFilter = IntentFilter()
        intentFilter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
        intentFilter.addAction("luda.tjh.click_notification")
        registerReceiver(notificationClickReceiver, intentFilter)

        RealmHelper.getInstance().use {realm->
            val profile = RealmHelper.getProfile(realm)!!
            mTaskQueryResultChangedTime = profile.taskQueryResultChangedTime
        }
        val taskList = Task.fromLocal().blockingGet()
        for(task in taskList){
            mTaskIdList.add(task.id)
            mQueryResultCountOfTask.put(task.id,task.queryResultTotalCount)

        }

        if(SettingHelper.enableNoticeForeground&&SettingHelper.enableNotice){
            startForeground(1,makeForegroundNotification())
        }
    }


    fun makeForegroundNotification() : Notification{
        val intent = Intent(this,MainActivity.javaClass)
        return NotificationCompat.Builder(this,"TaskNotificationService")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                //.setPriority(NotificationManager.IMPORTANCE_DEFAULT)  //通知的优先级
                .setCategory(Notification.CATEGORY_MESSAGE)  //通知的类型
                .setContentTitle("服务正在运行")
                .setContentText("腾讯职位检索系统服务正在运行，当您的任务有新结果时将会第一时间通知")
                .setContentIntent(PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT))
                //.setFullScreenIntent(PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT), true)  //不设置此项不会悬挂,false 不会出现悬挂
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build()
    }



    fun synchronizedDo(func:(TaskNotificationService)->Unit){
        synchronized(mutex2){
            func(this)
        }
    }


    @Synchronized
    private fun doWork(){
        //防止重复启动
        if(mIsWorking) return
        mIsWorking = true
        mWorkerThread = Thread{

            Log.d(TAG,"服务线程启动")

            while (mIsWorking){
                try {
                    var shouldNotice = false
                    if(!MyApplication.isNetworkEnabled){
                        Thread.sleep(1000)
                        continue
                    }
                    val newTime = RequestHelper.getTaskQueryResultChangedTime().blockingGet()
                    synchronized(mutex1){
                        if(mTaskQueryResultChangedTime==0L)
                        {
                            mTaskQueryResultChangedTime = newTime
                        }
                        //当获取到的更新时间发生改变时，说明任务中的查询结果发生改变
                        if(newTime>mTaskQueryResultChangedTime){
                            shouldNotice = true
                            mTaskQueryResultChangedTime = newTime
                        }
                    }

                    if(shouldNotice){ //任务列表发生变化，需要更新
                        updateTaskQueryResultInfo()
                    }

                    if(!mIsWorking) break
                    mWorkThreadSleepingCondition.block(30000)

                }
                catch (e:Exception){
                    Log.d(TAG,"服务在获取服务器任务信息时出错")
                    e.printStackTrace()
                    mWorkThreadSleepingCondition.block(10000)
                }

                //Log.d(TAG,"DOING WORK")
            }
            Log.d(TAG,"线程退出")
            mWorkerThread = null
        }
        mWorkerThread!!.start()

    }

    override fun onDestroy() {
        Log.d(TAG,"调用onDestroy，等待工作线程结束")
        mIsWorking = false
        while(mWorkerThread!=null){
            mWorkThreadSleepingCondition.open()
            Thread.sleep(50)
        }
        if(notificationClickReceiver!=null)unregisterReceiver(notificationClickReceiver!!)
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if(!SettingHelper.enableNotice){
            //如果不启用通知的话，活动结束(解除绑定)后服务没必要再继续存活，关闭
            stopSelf()
        }
        return super.onUnbind(intent)
    }


    private fun updateTaskQueryResultInfo(){
        Task.getList(MyApplication.realmTaskWorker)
                .onErrorReturn{ex->
                    Log.d("error",ex.message)
                    //init()
                    ArrayList<Task>()
                }
                .subscribe {taskList->
                    mTaskIdList.clear()
                    mQueryResultChangedTaskIdList.clear()
                    var addedCount = 0 //新增查询结果的个数
                    for(t in taskList) {
                        mTaskIdList.add(t.id)
                        val oldCount = mQueryResultCountOfTask[t.id,-1]
                        if(oldCount>=0){
                            if(oldCount < t.queryResultTotalCount){
                                mQueryResultChangedTaskIdList.add(t.id)
                                addedCount += t.queryResultTotalCount - oldCount
                            }
                        }
                        else{
                            if(t.queryResultTotalCount>0){
                                mQueryResultChangedTaskIdList.add(t.id)
                                addedCount += t.queryResultTotalCount
                            }

                        }

                        mQueryResultCountOfTask.put(t.id,t.queryResultTotalCount)
                    }
                    if(addedCount>0) onTaskQueryResultChanged(addedCount)//在任务的查询结果发生改变后调用，发送通知等等
                }
    }

    private fun makeNotification(count:Int){
        if(!SettingHelper.enableNotice)return //通知被禁用，直接返回

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent("luda.tjh.click_notification")
        val notification = NotificationCompat.Builder(this,"NewTaskQueryResult")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                //.setPriority(NotificationManager.IMPORTANCE_DEFAULT)  //通知的优先级
                .setCategory(Notification.CATEGORY_MESSAGE)  //通知的类型
                .setContentTitle("您的任务有新的查询结果")
                .setAutoCancel(true)
                .setContentText("共新增${count}项，点击立刻查看")
                .setFullScreenIntent(PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT), true)  //不设置此项不会悬挂,false 不会出现悬挂
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build()

        notificationManager.notify(1,notification)
    }

    inner class NotificationClickReceiver :BroadcastReceiver() {
        override fun onReceive(context:Context, intent:Intent) {
            if(SettingHelper.enableNoticeForeground&&SettingHelper.enableNotice){
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(1,makeForegroundNotification())
            }

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(ActionConstants.TO_TASK_FRAGMENT,true)
            context.startActivity(intent)
        }
    }


    private fun onTaskQueryResultChanged(count:Int){
        //taskRepository?.updateQueryResultCount(t.id,t.queryResultTotalCount)
        synchronized(mutex2){
            val taskRepo=taskRepository
            if(taskRepo!=null){
                for(id in mQueryResultChangedTaskIdList)
                    taskRepo.updateQueryResultCount(id,mQueryResultCountOfTask[id]!!)
                taskRepo.updateTotalQueryResultAddedCount(count)
            }

            //通过判断前台UI当前所处的界面，来选择不同的通知方式
            when(currentUIState){
                TaskFragment.TAG->{
                    //处于任务列表界面时，不通知（但更新数据会使TaskFragment自动弹出Snackbar提示）
                    Log.d(TAG,"Notice："+currentUIState)
                }
                TaskDetailFragment.TAG->{
                    //处于任务详情列表界面时，如果刚好只有正在阅览的界面的查询数据发生变化，那么不通知，否则发出通知
                    Log.d(TAG,"Notice："+currentUIState)
                    if(mQueryResultChangedTaskIdList.size>1 || mQueryResultChangedTaskIdList.size==1&&
                            mQueryResultChangedTaskIdList[0] != navigationManager!!.taskIdOfTaskDetailFragment){
                        makeNotification(count)
                    }
                }
                "Background"->{//处于后台状态
                    Log.d(TAG,"Notice：Background")
                    makeNotification(count)
                }
                else->{
                    Log.d(TAG,"Notice："+currentUIState)
                    makeNotification(count)
                }
            }

            return
        }
    }



}