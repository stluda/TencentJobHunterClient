package luda.tencentjobhunterclient.model

import io.reactivex.Single
import io.realm.kotlin.where
import luda.tencentjobhunterclient.application.MyApplication.Companion.realmTaskWorker
import luda.tencentjobhunterclient.listener.IRealmTaskWorker
import luda.tencentjobhunterclient.model.realm.RJob
import luda.tencentjobhunterclient.model.realm.RProfile
import luda.tencentjobhunterclient.util.RequestHelper
import luda.tencentjobhunterclient.util.Weak

/**
 * Created by luda on 2018/4/5
 * QQ 340071887.
 */
data class Job private constructor(val id:Int,
                                   val title:String ,
                                   val location:String = String(),
                                   val type:String = String(),
                                   val date:String = String(),
                                   val hiringNumber:Int = 0,
                                   var requirements:String = String(),
                                   var duties:String = String(),
                                   var hasBeenSeen:Boolean = false,
                                   var isBookmarked : Boolean = false){


    fun addToMyFavorites(){
        isBookmarked = true
        sRepo?.addToFavorite(this)
        realmTaskWorker.enqueueTask { realm ->
            try {
                val profile = RealmHelper.getProfile(realm)!!
                val rJob = RJob.get(id,realm)
                if(rJob!=null&&!profile.myFavoriteJobs.contains(rJob)){
                    rJob.isBookmarked = true
                    profile.myFavoriteJobs.add(rJob)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFromMyFavorites(){
        isBookmarked = false
        sRepo?.deleteFromFavorite(this)
        realmTaskWorker.enqueueTask { realm ->
            try {
                val rJob = RJob.get(id,realm)
                if(rJob!=null){
                    rJob.isBookmarked = false
                    val profile = RealmHelper.getProfile(realm)!!
                    profile.myFavoriteJobs.remove(rJob)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addToHistory(){
        sRepo?.addToHistory(this)
        realmTaskWorker.enqueueTask { realm ->
            try {
                val rJob = RJob.get(id,realm)
                if(rJob!=null){
                    val profile = RealmHelper.getProfile(realm)!!
                    profile.historyOfJobViewed.remove(rJob)
                    profile.historyOfJobViewed.add(rJob)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFromHistory(){
        sRepo?.deleteFromHistory(this)
        realmTaskWorker.enqueueTask { realm ->
            try {
                val rJob = RJob.get(id,realm)
                if(rJob!=null){
                    val profile = RealmHelper.getProfile(realm)!!
                    profile.historyOfJobViewed.remove(rJob)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    companion object{

        private var sRepo by Weak<IJobRepository>()

        fun loadHistoryFromLocal(){
            try{
                RealmHelper.getInstance().use {realm->
                    val profile = RealmHelper.getProfile(realm)!!
                    for(rJob in profile.historyOfJobViewed){
                        val job = createInstance(rJob)
                        sRepo?.addToHistory(job)
                    }
                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

        fun loadMyFavoritesFromLocal(){
            try{
                RealmHelper.getInstance().use {realm->
                    val profile = RealmHelper.getProfile(realm)!!
                    for(rJob in profile.myFavoriteJobs){
                        val job = createInstance(rJob)
                        sRepo?.addToFavorite(job)
                    }
                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

        fun createInstance(rJob:RJob) =createInstance(
                rJob.id,rJob.title,rJob.location,rJob.type,rJob.date,rJob.hiringNumber,
                rJob.duties,rJob.requirements,rJob.hasBeenSeen,rJob.isBookmarked)


        fun createInstance(id:Int,
                              title:String ,
                              location:String = String(),
                              type:String = String(),
                              date:String = String(),
                              hiringNumber:Int = 0,
                              duties:String = String(),
                              requirements:String = String(),
                              hasBeenSeen:Boolean = false,
                              isBookmarked: Boolean = false) : Job{
            var job = getInstance(id)
            if(job==null){
                job = Job(id,title,location,type,date, hiringNumber,  duties,requirements, hasBeenSeen,isBookmarked)
                sRepo?.save(job)
            }
            else{
                if(requirements!=""){
                    job.requirements=requirements
                    job.duties=duties
                }
            }
            return job
        }

        fun getInstance(id:Int) : Job? = sRepo?.get(id)





        fun setRepository(jobRepo:IJobRepository){
            sRepo=jobRepo
        }

        fun fromProtoBuf(protoJob:TencentJobHunterMessage.Message.Job) : Job{
            val location = JobRelatedInfo.getLocationText(protoJob.location) ?: protoJob.location.toString()
            val type = JobRelatedInfo.getTypeText(protoJob.type) ?: protoJob.type.toString()
            return Job.createInstance(protoJob.id,protoJob.title,location,
                    type,protoJob.date,protoJob.hiringNumber,protoJob.duties,protoJob.requirements
            )
        }

        fun fromServer(id:Int,realmTaskWorker: IRealmTaskWorker) : Single<Job>
                = RequestHelper.getJobDetail(id)
                .doOnSuccess {job->
                    job.hasBeenSeen = true
                    realmTaskWorker.enqueueTask { realm ->
                        try {
                            val rJob = realm.where<RJob>().equalTo("id", job.id).findFirst()!!
                            rJob.duties = job.duties
                            rJob.requirements = job.requirements
                            rJob.hasBeenSeen = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }


    }

}