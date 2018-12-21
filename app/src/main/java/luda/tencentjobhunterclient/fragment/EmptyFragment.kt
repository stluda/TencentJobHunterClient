package luda.tencentjobhunterclient.fragment

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.empty.*
import luda.tencentjobhunterclient.R
import java.util.concurrent.TimeUnit

/**
 * Created by luda on 2018/7/15
 * QQ 340071887.
 */
class EmptyFragment1 : BaseFragment() {
     val mGroupId: Int
        get() = 0
     val navTag: String
        get() = "EmptyFragment1"


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater!!.inflate(R.layout.empty, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Observable.just(true)
                .delay(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Snackbar.make(coordinator_layout,"暂无新查询结果", Snackbar.LENGTH_INDEFINITE)
                            .setAction("知道了",{})
                            .apply {
                                view.layoutParams = (view.layoutParams as CoordinatorLayout.LayoutParams).apply {bottomMargin=60}
                            }
                            .show()
                }
    }

}

class EmptyFragment2 : BaseNavigationFragment() {
    override val groupId: Int
        get() = 1
    override val navTag: String
        get() = "EmptyFragment2"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater!!.inflate(R.layout.empty, container, false)
}