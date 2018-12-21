package luda.tencentjobhunterclient.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.frag_job_detail.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
//import luda.tencentjobhunterclient.R.id.iv_job_detail_back
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.model.Job
import luda.tencentjobhunterclient.util.mySubscribe

/**
 * Created by luda on 2018/4/21
 * QQ 340071887.
 */
class JobDetailFragment : BaseNavigationFragment() {
    companion object {
        const val TAG = "JobDetailFragment"
    }

    override val navTag: String
        get() = TAG + groupId


    override fun onCreate(savedInstanceState: Bundle?) {
        mGroupId = arguments.getInt(KeyConstants.GROUP_ID,0)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.frag_job_detail,container,false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as MainActivity).setToolBar2(toolbar)

        val jobId = arguments.getInt(KeyConstants.JOB_ID)
        val job = Job.getInstance(jobId)!!
        tv_job_detail_title_content.text = job.title
        tv_job_detail_location_content.text = job.location
        tv_job_detail_type_content.text = job.type
        tv_job_detail_hiring_number_content.text = "${job.hiringNumber}人"
        tv_job_detail_duties_content.text = job.duties
        tv_job_detail_requirements_content.text = job.requirements

        if(job.duties==""&&MyApplication.isNetworkEnabled){
            Job.fromServer(jobId,MyApplication.realmTaskWorker)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{ job->
                tv_job_detail_duties_content.text = job.duties
                tv_job_detail_requirements_content.text = job.requirements
            }
        }

        bookmark.isActivated = job.isBookmarked
        bookmark.setOnClickListener {
            bookmark.isActivated = !bookmark.isActivated
            if(bookmark.isActivated){
                job.addToMyFavorites()
            }
            else{
                job.deleteFromMyFavorites()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }

    override val groupId: Int  get() = mGroupId
    private var mGroupId = 0

}