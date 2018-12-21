package luda.tencentjobhunterclient.activity


import TencentJobHunterMessage.Message
import android.app.Application
import android.app.Fragment
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import kotlinx.android.synthetic.main.act_main.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel
import android.view.WindowManager
import android.os.Build
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.constant.ActionConstants
import luda.tencentjobhunterclient.exception.GetResponseException
import luda.tencentjobhunterclient.fragment.*
import luda.tencentjobhunterclient.model.Job
import luda.tencentjobhunterclient.service.TaskNotificationService
import luda.tencentjobhunterclient.ui.LoadingDialog
import luda.tencentjobhunterclient.util.FragmentUtils
import luda.tencentjobhunterclient.util.LoginHelper
import luda.tencentjobhunterclient.util.mySubscribe
import luda.tencentjobhunterclient.viewmodel.JobViewModel
import luda.tencentjobhunterclient.viewmodel.NavigationViewModel
import luda.tencentjobhunterclient.viewmodel.TaskViewModel


class MainActivity : AppCompatActivity(),ServiceConnection{

    companion object {
        const val TAG = "MainActivity"
    }

    private var mDrawerToggle: ActionBarDrawerToggle?
            = null
    //private var mNavigationview: android.support.design.widget.NavigationView? = null
    //private var mDrawerlayout: android.support.v4.widget.DrawerLayout? = null
    private var mCurrentMenuItem: Int = 0
    //var currentFragment : BaseNavigationFragment? = null

    private lateinit var mNavigationViewModel: NavigationViewModel
    private lateinit var mQueryResultViewModel: JobQueryResultViewModel
    val navigationViewModel get() = mNavigationViewModel
    val queryResultViewModel get() = mQueryResultViewModel
    val currentFragmentTag : String get() = mNavigationViewModel.currentFragmentTag
    val currentGroupId : Int get() = mNavigationViewModel.currentGroupId


    private lateinit var mTaskNotificationServiceIntent : Intent
    private lateinit var mTaskNotificationService : TaskNotificationService
    private var mLoadingDialog : LoadingDialog? = null

    private var mInited = false

    override fun onDestroy() {
        Log.d(TAG,"onDestroy()")
        super.onDestroy()
        unbindService(this)
    }

    private fun toTaskFragment(){
        intent.removeExtra(ActionConstants.TO_TASK_FRAGMENT)
        setThemeByType(1)
        val fragment = supportFragmentManager.findFragmentByTag(TaskFragment.TAG) ?: TaskFragment()
        if(currentFragmentTag!=TaskFragment.TAG){
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container,fragment, TaskFragment.TAG)
                    .addToBackStack(currentFragmentTag)
                    .commit()
            //currentFragment = fragment as TaskFragment
        }
    }

    fun toSubFragment(fragment:BaseNavigationFragment){
        val tag =
                if(fragment is JobDetailFragment)
                    JobDetailFragment.TAG + navigationViewModel.currentGroupId
                else
                    fragment.navTag

        navigationViewModel.addToBackStack(navigationViewModel.currentFragmentTag)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container,fragment,tag)
                .addToBackStack(null)//navigationViewModel.currentFragmentTag
                .commit()
    }

    fun toBackFragment(){
        FragmentUtils.toBackStack(supportFragmentManager,this)
    }

    fun toTopFragment(){
        FragmentUtils.toTop(supportFragmentManager,this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModel()
        setThemeByType(navigationViewModel.currentGroupId)

        setTitle(when(navigationViewModel.currentGroupId){
            1-> R.string.menu_task
            2-> R.string.menu_my_account
            else -> R.string.menu_query
        })

        Log.d(TAG,"onCreate()")





        setContentView(R.layout.act_main)


        //初始化View
        //this.mNavigationview = findViewById(R.id.nav_view) as NavigationView

        //setToolBar(toolbar)

        //设置菜单点击监听
        nav_view.setNavigationItemSelectedListener(object:OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                val id = item.getItemId()
                if (id == mCurrentMenuItem) {
                    //点击了当前的Tab则直接关闭
                    drawer_layout.closeDrawers()
                    return false
                }

                val fragment = navigationViewModel.getCurrentFragment(supportFragmentManager)
                if(fragment!=null){
                    when(id){
                        R.id.navigation_job_query->fragment.navigate(0)
                        R.id.navigation_task->fragment.navigate(1)
                        R.id.navigation_setting->fragment.navigate(2)
                    }
                    mCurrentMenuItem = id
                    item.isChecked = true
                    //关闭侧滑菜单
                }


                drawer_layout.closeDrawers()
                return true
            }
        })

        startTaskNotificationService()
        bindTaskNotificationService()


        autoLogin()

        if(!mInited){

        }


        mCurrentMenuItem = R.id.navigation_job_query

        mInited = true
    }


    fun autoLogin()
    {
        LoginHelper.login(mQueryResultViewModel).mySubscribe(this,false, { _->
            loadFragment()
        },{ex->
            if(ex is GetResponseException)
            {
                when(ex.errorCode)
                {
                    Message.ErrorCode.SESSION_INVALID_ID->{
                        Toast.makeText(this,"会话超时，请重新登录！", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this,LoginActivity::class.java))
                        finish()
                    }
                }
            }
            else
            {
                if(MyApplication.isNetworkEnabled){
                    autoLogin()
                }
                else{
                    setNetworkErrorView {
                        autoLogin()
                    }
                }
            }

        })
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG,"onNewIntent()")
        super.onNewIntent(intent)
        if(intent?.hasExtra(ActionConstants.TO_TASK_FRAGMENT)==true){
            toTaskFragment()
        }
    }

    private fun startTaskNotificationService(){
        mTaskNotificationServiceIntent=Intent(this,TaskNotificationService::class.java)
        startService(mTaskNotificationServiceIntent)
    }

    private fun bindTaskNotificationService(){
        bindService(mTaskNotificationServiceIntent,this, Context.BIND_AUTO_CREATE)
    }

    override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
        mTaskNotificationService = (binder as TaskNotificationService.Binder).service
        mTaskNotificationService.synchronizedDo { service->
            service.taskRepository = ViewModelProviders.of(this).get(TaskViewModel::class.java)
            service.navigationManager = mNavigationViewModel
        }

    }
    override fun onServiceDisconnected(p0: ComponentName?) {
        mTaskNotificationService.clearExternalObjects()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        setThemeByType(navigationViewModel.currentGroupId)
    }




    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)
        if(fragment is BaseNavigationFragment)
            setThemeByType(navigationViewModel.currentGroupId)
    }

    fun setThemeByType(type:Int)
    {
        val statusBarColor = when(type){
            0->R.color.query_status
            1->R.color.task_status
            2->R.color.setting_status
            else->throw NotImplementedError()
        }
        val theme = when(type){
            0->R.style.QueryTheme
            1->R.style.TaskTheme
            2->R.style.SettingTheme
            else->throw NotImplementedError()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this,statusBarColor)
        }
        setTheme(theme);
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        setThemeByType(navigationViewModel.currentGroupId)
        super.onConfigurationChanged(newConfig)
    }

    private fun initViewModel()
    {
        val jobViewModel = ViewModelProviders.of(this).get(JobViewModel::class.java)
        Job.setRepository(jobViewModel)
        Job.loadHistoryFromLocal()
        Job.loadMyFavoritesFromLocal()
        val taskViewModel =  ViewModelProviders.of(this).get(TaskViewModel::class.java)
        mQueryResultViewModel = ViewModelProviders.of(this).get(JobQueryResultViewModel::class.java)
        mNavigationViewModel = ViewModelProviders.of(this).get(NavigationViewModel::class.java)
    }


    var mNetworkErrorView : View? = null
    private fun setNetworkErrorView(retryFunc:()->Unit){
        mNetworkErrorView = View.inflate(this,R.layout.panel_network_error,null)
        val button = mNetworkErrorView!!.findViewById<Button>(R.id.btn_retry)
        button.setOnClickListener {
            if(MyApplication.isNetworkEnabled){
                container.removeView(mNetworkErrorView)
                mNetworkErrorView = null
                retryFunc()
            }
            else{
                Toast.makeText(this,"请打开网络后再继续",Toast.LENGTH_SHORT).show()
            }
        }
        container.addView(mNetworkErrorView)
    }


    private fun loadFragment(){
        if(intent?.hasExtra(ActionConstants.TO_TASK_FRAGMENT)==true){
            toTaskFragment()
            return
        }

        FragmentUtils.reload(supportFragmentManager,this)
    }


    override fun onBackPressed() {
        if(navigationViewModel.isTop())
        {
            moveTaskToBack(true);
        }
        else
        {
            toBackFragment();
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
    }


    fun setToolBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar);
        //getSupportActionBar()?.setDisplayShowTitleEnabled(false);
        //val drawerToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        //drawer_layout.setDrawerListener(drawerToggle)
        //drawerToggle.syncState()
        //setSupportActionBar(toolBar);
    }

    fun setToolBar2(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //actionBar.setDisplayShowTitleEnabled(false)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                toBackFragment()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
