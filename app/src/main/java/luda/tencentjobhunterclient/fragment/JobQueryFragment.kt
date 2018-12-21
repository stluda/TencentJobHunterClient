package luda.tencentjobhunterclient.fragment

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers

import kotlinx.android.synthetic.main.frag_query.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.activity.MainActivity
import luda.tencentjobhunterclient.adapter.QueryPagerAdapter
import luda.tencentjobhunterclient.model.JobQueryResult
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.util.MyRxLifeCycle
import luda.tencentjobhunterclient.util.SettingHelper
import luda.tencentjobhunterclient.util.mySubscribe
import luda.tencentjobhunterclient.viewmodel.JobQueryResultViewModel


/**
 * Created by luda on 2018/4/1
 * QQ 340071887.
 */
class JobQueryFragment : BaseNavigationFragment() {

    companion object {
        const val TAG = "JobQueryFragment"
    }

    override val navTag: String
        get() = TAG

    private lateinit var mPagerAdapter: QueryPagerAdapter
    private val mTabNoList
        get() = jobQueryResultViewModel.numberList
    private var mLatestTabNo
        get() = jobQueryResultViewModel.latestNumber
        set(value) { jobQueryResultViewModel.latestNumber = value}

    //var jobQueryResultViewModel = MainFragmentInstanceState()
    private var mJobQueryResultViewModel : JobQueryResultViewModel? = null
    var jobQueryResultViewModel : JobQueryResultViewModel
        get() {
            if(mJobQueryResultViewModel==null){
                mJobQueryResultViewModel = ViewModelProviders.of(activity).get(JobQueryResultViewModel::class.java)
            }
            return mJobQueryResultViewModel!!
        }
    set(value) {
        mJobQueryResultViewModel = value
    }

    private val mQueryExpressionMap = HashMap<String,String>()




    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        return inflater!!.inflate(R.layout.frag_query, container, false)
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        //inflater?.inflate(R.menu.toolbar,menu)
        inflater?.inflate(R.menu.filter, menu);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_filter) {
            drawer_layout.openDrawer(GravityCompat.END);
            return true;
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        //jobQueryResultViewModel.isInitialized = false
        super.onDestroyView()
        MyApplication.refWatcher.watch(this)
    }


    override val groupId get() = 0

    override fun subscribeViewModelSubjects() {
        jobQueryResultViewModel.loadJobQueryResultFromLocalSubject
                .compose(MyRxLifeCycle<ArrayList<JobQueryResult>>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {list->//onNext
                    for (i in 0 until list.size)
                        jobQueryResultViewModel.numberList.add(jobQueryResultViewModel.latestNumber++)

                    loadUI()
                }

        jobQueryResultViewModel.addJobQueryResultSubject
                .compose(MyRxLifeCycle<JobQueryResult>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result->
                    if(mPagerAdapter.addJobQueryResultPager(result)) {
                        mTabNoList.add(mLatestTabNo++)
                        updateCustomTabs(mPagerAdapter.count-1)
                    }
                    else{
                        //超过可允许查询的最大数量
                        Toast.makeText(activity,R.string.new_job_query_limit_reached, Toast.LENGTH_LONG).show()
                    }
                }

        jobQueryResultViewModel.removeJobQueryResultSubject
                .compose(MyRxLifeCycle<Int>(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { index:Int->
                    mPagerAdapter.removeJobQueryResultPager(index)
                    mTabNoList.removeAt(index)
                    updateCustomTabs(Math.min(index,mPagerAdapter.count-1))
                }

        for(info in  jobQueryResultViewModel.jobQueryResultInfoList)
            mQueryExpressionMap[info.queryId] = info.queryExpression


        jobQueryResultViewModel.isExpressionMode = SettingHelper.queryMode == 2
        jobQueryResultViewModel.isSimpleMode = SettingHelper.queryMode != 1
        jobQueryResultViewModel.queryModeChangedSubject
                .compose(MyRxLifeCycle<Boolean>(this))
                .subscribe { _->
                    mPagerAdapter.notifyDataSetChanged()
                    updateCustomTabs(0)
                }

        super.subscribeViewModelSubjects()
    }



    fun init(){
        if(!jobQueryResultViewModel.checkIsSameAsServer()){
            if(jobQueryResultViewModel.jobQueryResults.size==0){
                cancelCacheMode()
                jobQueryResultViewModel.isInitialized = true
            }
            else{
                //数据不一致，弹框选择是否进入快照模式
                val normalDialog : AlertDialog.Builder =
                        AlertDialog.Builder(activity);

                normalDialog.setTitle("是否进入快照模式")
                        .setMessage("检测到服务器远程存储的查询数据和本地数据不一致，是否进入快照模式？\n\n"+
                                "快照模式只能浏览本地数据，无法新建查询或获取更多查询记录\n\n"+
                                "点击取消会删除本地数据，并从服务器同步最新的查询记录")
                        .setNegativeButton("取消", { _, _ ->
                            cancelCacheMode()
                            jobQueryResultViewModel.isInitialized = true
                        }).setPositiveButton("确定",{ _, _ ->
                            //快照模式
                            jobQueryResultViewModel.isCacheMode = true
                            jobQueryResultViewModel.loadFromLocal()
                            jobQueryResultViewModel.isInitialized = true
                        }).show();
            }
        }
        else{
            if(!jobQueryResultViewModel.isInitialized){
                jobQueryResultViewModel.loadFromLocal()
                jobQueryResultViewModel.isInitialized = true
            }
            else
            {
                loadUI()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        jobQueryResultViewModel = ViewModelProviders.of(activity).get(JobQueryResultViewModel::class.java)
        super.onActivityCreated(savedInstanceState)

        if(jobQueryResultViewModel.isInitialized){
            loadUI()
            return
        }

        init()

    }

    private fun loadUI(){
        toolbar.setTitle(R.string.menu_query)
        (activity as MainActivity).setToolBar(toolbar)
        setupViewPager()
        setHasOptionsMenu(true);


        if(!jobQueryResultViewModel.isCacheMode)
            updateCustomTabs(jobQueryResultViewModel.selectedQueryIndex)
    }


    fun cancelCacheMode()
    {
        val queryResultAlreadyReceived:ArrayList<JobQueryResult> = ArrayList<JobQueryResult>()
        jobQueryResultViewModel.loadJobQueryResultFromServerSubject
                .compose(MyRxLifeCycle<ArrayList<JobQueryResult>>(this))
                .subscribe( {list->//onNext
            for(i in 0 until list.size)
            {
                jobQueryResultViewModel.numberList.add(jobQueryResultViewModel.latestNumber++)
            }
            jobQueryResultViewModel.isCacheMode = false
            loadUI()
        },{ex->//onError
            //弹出对话框
            val normalDialog : AlertDialog.Builder =
                    AlertDialog.Builder(activity);
            //normalDialog.setIcon(R.drawable);
            normalDialog.setTitle("网络不给力哦")
                    .setMessage("从服务器获取数据失败，请稍后重试")
                    .setNegativeButton("重试", { _, _ ->
                        jobQueryResultViewModel.loadFromServer(queryResultAlreadyReceived)
                        jobQueryResultViewModel.isInitialized = true
                    }).show();
        })
        jobQueryResultViewModel.loadFromServer(queryResultAlreadyReceived)
    }



    //重新刷新tab菜单样式
    private fun updateCustomTabs(newPos:Int){
        if(mPagerAdapter.count>0)
        {
            var tabs=activity.findViewById<TabLayout>(R.id.main_tabs)
            for(i in 1 until mPagerAdapter.count){
                var tab = tabs.getTabAt(i)!!
                var view = tab.setCustomView(R.layout.closable_tab_item).customView!!
                view.findViewById<TextView>(R.id.tv_tabitem).text="查询" + mTabNoList[i-1]
                //关闭查询

                view.findViewById<ImageButton>(R.id.btn_close_tabitem).setOnClickListener{ view ->
                    jobQueryResultViewModel.removeByIndex(i-1)
                            .mySubscribe(this,false,{})
                }
            }
            tabs.getTabAt(newPos)!!.select()
        }
    }



    private fun setupViewPager(){
        //val pagerAdapter = QueryPagerAdapter(childFragmentManager, activity)
        //mTabFragmentList.add(NewExpressionQueryFragment())
        mPagerAdapter = QueryPagerAdapter(childFragmentManager, jobQueryResultViewModel)
        view_pager.adapter = mPagerAdapter
        main_tabs.setupWithViewPager(view_pager)
        main_tabs.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                jobQueryResultViewModel.selectedQueryIndex = main_tabs.selectedTabPosition
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }

    fun setPagerItem(item: Int) {
        view_pager.currentItem = item
    }


}