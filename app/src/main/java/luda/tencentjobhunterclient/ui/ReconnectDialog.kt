package luda.tencentjobhunterclient.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import luda.tencentjobhunterclient.constant.KeyConstants
import luda.tencentjobhunterclient.util.Weak

/**
 * Created by luda on 2018/9/24
 * QQ 340071887.
 */
class ReconnectDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cancelable = arguments.getBoolean(KeyConstants.CANCELABLE)

        var normalDialog : AlertDialog.Builder =
                AlertDialog.Builder(activity)
        normalDialog = normalDialog.setTitle("加载失败")
                .setMessage(if(cancelable)"网络不给力哦，是否重试？" else "网络不给力哦，请重试")
                .setPositiveButton("重试",{ _, _ ->
                    reconnectFunc?.invoke()
                })
        if(cancelable)normalDialog.setNegativeButton("取消", { _, _ ->
            instance?.dismiss()
        })
        val dialog = normalDialog.create()
        if(!cancelable){
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
        }
        return dialog
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        ownerActivity = context as FragmentActivity
        instance = this
    }

    companion object {
        fun show(manager:FragmentManager,cancelable:Boolean,reconnectFunc:()->Unit) {
            this.reconnectFunc  = reconnectFunc

            val dialog = ReconnectDialog()
            val args = Bundle()
            args.putBoolean(KeyConstants.CANCELABLE,cancelable)
            dialog.arguments = args
            dialog.show(manager,"ReconnectDialog")
        }

        private var reconnectFunc : (()->Unit)? = null
        var ownerActivity by Weak<FragmentActivity>()
        var instance by Weak<ReconnectDialog>()

    }
}