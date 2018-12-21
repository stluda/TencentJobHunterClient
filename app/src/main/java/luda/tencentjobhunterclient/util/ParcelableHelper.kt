package luda.tencentjobhunterclient.util

/**
 * Created by luda on 2018/5/1
 * QQ 340071887.
 */
import android.os.Parcel
import android.os.Parcelable

object ParcelableHelper {

    fun <T : Parcelable> copy(input: Parcelable, classLoader: ClassLoader = input.javaClass.classLoader): T {
        var parcel: Parcel? = null
        try {
            parcel = Parcel.obtain()
            parcel!!.writeParcelable(input, 0)
            parcel.setDataPosition(0)
            return parcel.readParcelable(classLoader)
        } finally {
            if (parcel != null) {
                parcel.recycle()
            }
        }
    }
}