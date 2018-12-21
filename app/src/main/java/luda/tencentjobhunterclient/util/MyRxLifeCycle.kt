package luda.tencentjobhunterclient.util

/**
 * Created by luda on 2018/6/9
 * QQ 340071887.
 */

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Looper
import android.support.annotation.MainThread
import android.util.Log

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject


class MyRxLifeCycle<T:Any> constructor(private val mLifecycleOwner: LifecycleOwner) : ObservableTransformer<T, T>, LifecycleObserver {

    private val mSubject = PublishSubject.create<T>()

    private var mDisposable: Disposable? = null

    private lateinit var mData: T

    private var mActive: Boolean = false

    private var mVersion = -1

    private var mLastVersion = -1

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        if (mLifecycleOwner.lifecycle.currentState != Lifecycle.State.DESTROYED) {
            mLifecycleOwner.lifecycle.addObserver(this)
            mDisposable = upstream.subscribe({ it ->
                ++mVersion
                mData = it
                considerNotify()
            }, { it ->
                mSubject.onError(it)
            }) {
                mSubject.onComplete()
            }

            return mSubject.doOnDispose{
                mDisposable?.dispose()
            }
        } else {
            return Observable.empty()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    private fun onStateChange() {
        if (this.mLifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            if (mDisposable?.isDisposed==false) {
                mDisposable!!.dispose()
            }
            mLifecycleOwner.lifecycle.removeObserver(this)
        } else {
            this.activeStateChanged(MyRxLifeCycle.isActiveState(mLifecycleOwner.lifecycle.currentState))
        }
    }

    private fun activeStateChanged(newActive: Boolean) {
        if (newActive != mActive) {
            mActive = newActive
            considerNotify()
        }
    }

    private fun considerNotify() {
        if (mActive) {
            if (isActiveState(mLifecycleOwner.lifecycle.currentState)) {
                if (mLastVersion < mVersion) {
                    mLastVersion = mVersion
                    if (mDisposable?.isDisposed==false) {
                        mSubject.onNext(mData)
                    }
                }
            }
        }
    }


    companion object {
        fun isActiveState(state: Lifecycle.State): Boolean {
            return state.isAtLeast(Lifecycle.State.STARTED)
        }
    }

}