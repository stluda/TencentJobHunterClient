package luda.tencentjobhunterclient.base

/**
 * Created by luda on 2018/7/15
 * QQ 340071887.
 */
//表示一个区间
data class Interval(val start:Int,
                    val count:Int) {
    companion object {
        val invalid = Interval(-1,-1)
    }
}