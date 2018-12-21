package luda.tencentjobhunterclient.viewmodel

import io.reactivex.subjects.PublishSubject
import luda.tencentjobhunterclient.base.Interval
import luda.tencentjobhunterclient.model.Task

/**
 * Created by luda on 2018/7/22
 * QQ 340071887.
 */
interface ITaskDetailFragmentViewModel {
    fun getTaskById(taskId:Int) : Task?
    fun getTaskQueryResultInsertedSubject(task:Task) : PublishSubject<Interval>?
    fun hasNewQueryResultSubject(taskId:Int) : PublishSubject<Int>
    fun loadMore(task:Task)
}