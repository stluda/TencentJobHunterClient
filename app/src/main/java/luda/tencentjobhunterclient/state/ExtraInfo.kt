package luda.tencentjobhunterclient.state

import android.os.Parcel
import android.os.Parcelable
import luda.tencentjobhunterclient.model.JobQueryResultInfo
/**
 * Created by luda on 2018/6/9
 * QQ 340071887.
 */

//登录时服务器会返回一些后续要用到的数据，将它们集中保存
//继承Parcelable类使之可以通过intent传递
class ExtraInfo  : Parcelable {
    lateinit var jobTypeMap : HashMap<Int,String>
    lateinit var jobLocationMap : HashMap<Int,String>

    //第一次登陆时，服务器返回的储存于用户的查询结果的概要（不返回详细信息），只用于对比和本地数据是否一致
    lateinit var jobQueryResultInfoListFromServer : List<JobQueryResultInfo>

    constructor(parcel: Parcel){
        jobTypeMap = parcel.readHashMap(HashMap::class.java.classLoader) as HashMap<Int,String>
        jobLocationMap = parcel.readHashMap(HashMap::class.java.classLoader) as HashMap<Int,String>

        jobQueryResultInfoListFromServer=ArrayList<JobQueryResultInfo>()
        parcel.readList(jobQueryResultInfoListFromServer, JobQueryResultInfo.javaClass.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flag: Int) {
        parcel.writeMap(jobTypeMap)
        parcel.writeMap(jobLocationMap)

        parcel.writeList(jobQueryResultInfoListFromServer)

    }

    constructor(){}

    companion object CREATOR : Parcelable.Creator<ExtraInfo> {
        override fun createFromParcel(parcel: Parcel): ExtraInfo {
            return ExtraInfo(parcel)
        }

        override fun newArray(size: Int): Array<ExtraInfo?> {
            return arrayOfNulls(size)
        }
    }


    override fun describeContents(): Int = 0
}