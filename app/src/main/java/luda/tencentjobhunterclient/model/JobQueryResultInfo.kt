package luda.tencentjobhunterclient.model

import TencentJobHunterMessage.Message
import android.os.Parcel
import android.os.Parcelable

data class JobQueryResultInfo(val queryNo:Int,val queryId:String,val queryExpression:String) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(queryNo)
        parcel.writeString(queryId)
        parcel.writeString(queryExpression)
    }

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString())
    {}

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JobQueryResultInfo> {
        override fun createFromParcel(parcel: Parcel): JobQueryResultInfo {
            return JobQueryResultInfo(parcel)
        }

        override fun newArray(size: Int): Array<JobQueryResultInfo?> {
            return arrayOfNulls(size)
        }

        fun fromProtoBufList(proto: Message.JobQueryResultInfo) : List<JobQueryResultInfo>{
            val result = ArrayList<JobQueryResultInfo>()
            for(i in 0 until proto.queryIdCount){
                result.add(JobQueryResultInfo(proto.getQueryNo(i),proto.getQueryId(i),proto.getQueryExpression(i)))
            }
            return result
        }
    }
}