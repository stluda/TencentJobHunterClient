package luda.tencentjobhunterclient.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.dialog_fullscreen_input.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.util.KeyboardUtils

/**
 * Created by luda on 2018/10/16
 * QQ 340071887.
 */
class FullScreenInputDialog : DialogFragment() {

    var onConfirm : ((String,Int)->Unit)? = null
    var text = ""
    var index = 0

    var isShutdown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(onConfirm==null)dismiss()
        retainInstance = true
}

    private fun doSync(){
        if(dialog!=null){
            val edt = dialog.window.findViewById<EditText>(R.id.edt_input)
            onConfirm?.invoke(edt.text.toString(),edt.selectionEnd)
            onConfirm = null
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.dialog_fullscreen_input,container,false)
        if(onConfirm!=null){
            val btn = view.findViewById<Button>(R.id.btn_confirm)
            btn.setOnClickListener {
                KeyboardUtils.hideKeyboard(edt_input)
                doSync()
                dismiss()
            }
            val edt = view.findViewById<EditText>(R.id.edt_input)
            edt.addTextChangedListener(object:TextWatcher{
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    onConfirm?.invoke(edt.text.toString(),edt.selectionEnd)
                }
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
            edt.setText(text)
            if(index>=0)edt.setSelection(index)
        }
        return view
    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
        Handler().postDelayed({
            KeyboardUtils.showKeyboard(edt_input)
        },300)
    }


    override fun onSaveInstanceState(outState: Bundle?) {

        super.onSaveInstanceState(outState)
        doSync()
    }

    companion object {
        fun show(manager: FragmentManager,onConfirm:(String,Int)->Unit,text:String,index:Int){
            val dialog = FullScreenInputDialog()
            dialog.onConfirm = onConfirm
            dialog.text = text
            dialog.index = index
            dialog.show(manager,"FullScreenInputDialog")
        }

    }

}