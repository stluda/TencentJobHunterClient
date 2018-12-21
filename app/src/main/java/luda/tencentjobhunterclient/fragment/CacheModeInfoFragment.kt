package luda.tencentjobhunterclient.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frag_cache_mode_info.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.application.MyApplication

/**
 * Created by luda on 2018/5/4
 * QQ 340071887.
 */
class CacheModeInfoFragment : BaseFragment()  {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.frag_cache_mode_info,container,false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btn_cancelCacheMode.setOnClickListener {

            val normalDialog : AlertDialog.Builder =
                    AlertDialog.Builder(activity);
            //normalDialog.setIcon(R.drawable);
            normalDialog.setTitle("是否解除快照模式")
                    .setMessage("解除快照模式会删除本地快照，并从服务器同步最新的查询记录，是否继续？")
                    .setNegativeButton("取消", { _, _ ->
                    }).setPositiveButton("确定",{ _, _ ->
                        (parentFragment as JobQueryFragment).cancelCacheMode()
                    }).show();
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }

}