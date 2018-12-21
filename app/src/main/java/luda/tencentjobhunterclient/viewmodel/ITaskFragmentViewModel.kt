package luda.tencentjobhunterclient.viewmodel

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import luda.tencentjobhunterclient.model.Task

/**
 * Created by luda on 2018/7/22
 * QQ 340071887.
 */
interface ITaskFragmentViewModel {
    val dataChangedSubject : PublishSubject<Boolean>
    val queryResultAddedSubject : PublishSubject<Int>
    val removeTaskSubject : PublishSubject<Int>
    var addedCountOfNewQueryResult : Int

    val taskCount : Int
    fun getTaskAt(position:Int) : Task
    fun removeByTaskId(taskId:Int) : Single<Int>
    fun init()

    //fun initDataIfNeeded(task:Task) : Single<Boolean>
}