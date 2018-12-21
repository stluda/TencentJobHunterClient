package luda.tencentjobhunterclient.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.TextView
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.util.Weak
import android.graphics.drawable.ColorDrawable
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager


/**
 * Created by luda on 2018/8/12
 * QQ 340071887.
 */
class LoadingDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.LoadingDialogStyle);
    }

    fun myDismiss()
    {
        mIsShutdown = true
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_loading, null)
        builder.setView(view)
        val msg = view.findViewById<TextView>(R.id.tv_loading)
        msg.text = "加载中"

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val window = dialog.window
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        ownerActivity = context as FragmentActivity
        instance = this
    }

    companion object {
        var ownerActivity by Weak<FragmentActivity>()
        var instance by Weak<LoadingDialog>()
        var mIsShutdown = true
        val isShutdown get() =mIsShutdown

        fun show(manager:FragmentManager){
            LoadingDialog().show(manager,"LoadingDialog")
            mIsShutdown = false
        }

    }

}