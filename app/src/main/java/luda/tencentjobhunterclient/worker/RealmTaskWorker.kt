package luda.tencentjobhunterclient.worker

import android.os.ConditionVariable
import android.util.Log
import io.realm.Realm
import luda.tencentjobhunterclient.listener.IRealmTaskWorker
import java.util.concurrent.ConcurrentLinkedQueue



/**
 * Created by luda on 2018/5/31
 * QQ 340071887.
 */

class RealmTaskWorker : IRealmTaskWorker {

    companion object {
        private val TAG = "RealmTaskWorker"
    }

    var mIsWorkerEnabled = false
    private val mHasTaskCondition: ConditionVariable = ConditionVariable()
    private lateinit var mWorkerRealm: Realm
    private var mWorkerThread : Thread? = null

    private val mTaskQueue = ConcurrentLinkedQueue<(realm:Realm) -> Unit>()


    //专门开启一个线程，用来处理I/O等耗时操作，这里采用阻塞队列的方式，确保执行顺序正确
    private fun backgroundWorker()
    {
        if(mWorkerThread!=null) return //防止工作者线程重复开启
        //mHasTaskCondition
        mWorkerThread = Thread{
            mWorkerRealm = RealmHelper.getInstance()
            mIsWorkerEnabled = true
            val beginTime = System.currentTimeMillis()
            //val workerTag = "worker[$beginTime]"
            Log.d("tag","worker线程开始，$beginTime")
            while (mIsWorkerEnabled)
            {
                try {
                    //等待任务
                    mHasTaskCondition.block()

                    while(mTaskQueue.isNotEmpty())
                    {
                        val task = mTaskQueue.poll()
                        mWorkerRealm.executeTransaction {realm->task(realm)}
                    }

                }
                catch (e:Exception)
                {
                    Log.d("tag","work线程异常")
                    e.printStackTrace()
                }
            }
            Log.d("tag","worker线程结束")
            mWorkerRealm.close()
            mWorkerThread = null
        }
        mWorkerThread!!.start()
    }

    //给工作者线程添加一个任务
    override fun enqueueTask(task:(realm: Realm)->Unit)
    {
        mTaskQueue.add(task)
        mHasTaskCondition.open()//通知队列可用
    }

    fun onCreate() {
        backgroundWorker()
    }

    fun onDestroy() {
        mIsWorkerEnabled = false
        mHasTaskCondition.open()
    }

}