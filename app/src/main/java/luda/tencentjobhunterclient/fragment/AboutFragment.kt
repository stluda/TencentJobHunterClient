package luda.tencentjobhunterclient.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frag_about.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.application.MyApplication
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

/**
 * Created by luda on 2018/12/15
 * QQ 340071887.
 */
class AboutFragment : BaseNavigationFragment() {
    companion object {
        val TAG = "AboutFragment"
    }

    override val groupId: Int
        get() = 2

    override val navTag: String
        get() = TAG


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.frag_about,container,false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).setToolBar2(toolbar)
        toolbar.setTitle(R.string.my_account_about)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val aboutPage = AboutPage(activity)
                .isRTL(false)
                .setDescription("腾讯职位查询系统-客户端")
                .addItem(Element().setTitle("Version 0.1"))
                .addGroup("项目说明")
                .addWebsite("https://stluda.github.io","https://stluda.github.io")
                .addGroup("联系作者")
                .addEmail("lu.nosora@gmail.com","lu.nosora@gmail.com")
                .addItem(Element().setTitle("QQ：340071887"))
                .create()

        container_about.addView(aboutPage)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }

}