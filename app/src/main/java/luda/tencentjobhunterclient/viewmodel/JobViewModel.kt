package luda.tencentjobhunterclient.viewmodel

import android.arch.lifecycle.ViewModel
import luda.tencentjobhunterclient.model.IJobRepository
import luda.tencentjobhunterclient.model.Job
import luda.tencentjobhunterclient.state.ExtraInfo

/**
 * Created by luda on 2018/7/1
 * QQ 340071887.
 */
class JobViewModel : ViewModel() , IJobRepository {
    private val mJobMap = HashMap<Int,Job>()

    override val myFavoriteJobs = ArrayList<Job>()
    override val historyOfJobViewed = ArrayList<Job>()

    val myFavoriteJobsClone : ArrayList<Job> get() {
        val list = ArrayList<Job>()
        list.addAll(myFavoriteJobs)
        return list
    }

    override fun addToFavorite(job: Job) {
        if(!myFavoriteJobs.contains(job))myFavoriteJobs.add(job)
    }

    override fun deleteFromFavorite(job: Job) {
        myFavoriteJobs.remove(job)
    }

    override fun addToHistory(job: Job) {
        historyOfJobViewed.remove(job)
        historyOfJobViewed.add(job)
    }

    override fun deleteFromHistory(job: Job) {
        historyOfJobViewed.remove(job)
    }


    override fun get(id: Int): Job? {
        return mJobMap[id]
    }

    override fun save(job: Job) {
        mJobMap[job.id] = job
    }

}