package luda.tencentjobhunterclient.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import kotlinx.android.synthetic.main.frag_my_account.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.LoginActivity
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.util.LoginHelper
import luda.tencentjobhunterclient.util.SettingHelper

/**
 * Created by luda on 2018/7/24
 * QQ 340071887.
 */
class MyAccountFragment : BaseNavigationFragment() {
    companion object {
        val TAG = "MyAccountFragment"
    }

    override val groupId: Int
        get() = 2
    override val navTag: String
        get() = "MyAccountFragment"


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.frag_my_account,container,false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //(activity as MainActivity).setToolBar(toolbar)
        toolbar.setTitle(R.string.menu_my_account)

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSettingsSwitch(R.id.setting_enable_notification_container,
                R.id.settings_enable_notification_label,
                R.id.settings_enable_notification_switch,
                SettingHelper.enableNotice,
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    SettingHelper.enableNotice = isChecked
                })

        setupSettingsSwitch(R.id.setting_enable_notification_foreground_container,
                R.id.settings_enable_notification_foreground_label,
                R.id.settings_enable_notification_foreground_switch,
                SettingHelper.enableNoticeForeground,
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    SettingHelper.enableNoticeForeground = isChecked
                })

        btn_history_of_job_viewed.setOnClickListener {
            //jumpToFragment(JobHistoryFragment())
            toSubFragment(JobHistoryFragment())
        }
        btn_favorites_job.setOnClickListener{
            toSubFragment(MyFavoriteJobsFragment())
        }

        btn_logoff.setOnClickListener{
            LoginHelper.logoff()
            val intent = Intent(activity,LoginActivity::class.java)
            startActivity(intent)
            activity.finish()
        }

        btn_about.setOnClickListener {
            toSubFragment(AboutFragment())
        }
    }

    private fun setupSettingsSwitch(containerId: Int, labelId: Int, switchId: Int, checked: Boolean,
                                    checkedChangeListener: CompoundButton.OnCheckedChangeListener) {
        val container = view!!.findViewById(containerId) as ViewGroup
        val switchLabel = (container.findViewById<View>(labelId) as TextView).text.toString()
        val switchView = container.findViewById<View>(switchId) as Switch
        switchView.contentDescription = switchLabel
        switchView.isChecked = checked
        container.setOnClickListener { switchView.performClick() }
        switchView.setOnCheckedChangeListener(checkedChangeListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }
}