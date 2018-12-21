package luda.tencentjobhunterclient.model

/**
 * Created by luda on 2018/7/1
 * QQ 340071887.
 */
interface IJobRepository {
    fun save(job:Job)
    fun get(id:Int) : Job?

    val myFavoriteJobs : List<Job>
    fun addToFavorite(job:Job)
    fun deleteFromFavorite(job: Job)

    val historyOfJobViewed : List<Job>
    fun addToHistory(job:Job)
    fun deleteFromHistory(job: Job)
}