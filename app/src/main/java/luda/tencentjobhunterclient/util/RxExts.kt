package luda.tencentjobhunterclient.util

import android.arch.lifecycle.LifecycleOwner
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import luda.tencentjobhunterclient.exception.GetResponseException
import luda.tencentjobhunterclient.ui.LoadingDialog
import luda.tencentjobhunterclient.ui.ReconnectDialog

/**
 * Created by luda on 2018/8/12
 * QQ 340071887.
 */

fun <T:Any> Single<T>.mySubscribe(lifecycleOwner: LifecycleOwner, cancelable:Boolean, onNext:(T)->Unit,onError:((Throwable)->Unit)? = null) : Disposable?
{
    return mySubscribe(this,lifecycleOwner,cancelable,onNext,onError)
}

fun <T:Any> Observable<T>.mySubscribe(lifecycleOwner: LifecycleOwner, cancelable:Boolean, onNext:(T)->Unit,onError:((Throwable)->Unit)? = null) : Disposable?
{
    return mySubscribe(this,lifecycleOwner,cancelable,onNext,onError)
}

private fun <T:Any> mySubscribe(observable: Any, lifecycleOwner: LifecycleOwner, cancelable:Boolean, onNext:(T)->Unit,onError:((Throwable)->Unit)? ) : Disposable?
{
    try
    {
        //if(!LoadingDialog.isShutdown)return null
        var context by Weak<FragmentActivity>()
        context = if(lifecycleOwner is Fragment) lifecycleOwner.activity else lifecycleOwner as FragmentActivity

        if(context==null)return null

        LoadingDialog.show(context.supportFragmentManager)

        val callOnTimeout = {
            val dialog = LoadingDialog.instance
            if(dialog!=null)
            {
                dialog.myDismiss()
                ReconnectDialog.show(LoadingDialog.ownerActivity!!.supportFragmentManager,true,{
                    mySubscribe(observable,ReconnectDialog.ownerActivity!!,cancelable,onNext,onError)
                })
            }

        }

        return if(observable is Observable<*>){
            (observable as Observable<T>)
                    .compose(MyRxLifeCycle<T>(lifecycleOwner))
                    .doOnNext {
                        LoadingDialog.instance?.dismiss()
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onNext,{ ex ->
                        if(ex is GetResponseException && ex.state!=MessageAgent.RequestResultState.SUCCESS){
                            callOnTimeout()
                        }
                        else{
                            if(onError==null){
                                callOnTimeout()
                            }
                            else
                            {
                                LoadingDialog.instance?.dismiss()
                                onError(ex)
                            }
                        }
                    })
        }
        else if(observable is Single<*>){
            (observable as Single<T>)
                    .doOnSuccess {
                        LoadingDialog.instance?.dismiss()
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onNext,{ ex->
                        if(ex is GetResponseException && ex.state!=MessageAgent.RequestResultState.SUCCESS){
                            callOnTimeout()
                        }
                        else{
                            if(onError==null){
                                callOnTimeout()
                            }
                            else
                            {
                                LoadingDialog.instance?.dismiss()
                                onError(ex)
                            }
                        }
                    })
        }
        else throw NotImplementedError()
    }
    catch (e:Exception)
    {
        return null
    }
}