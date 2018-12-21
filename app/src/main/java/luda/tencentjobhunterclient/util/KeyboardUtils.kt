package luda.tencentjobhunterclient.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import luda.tencentjobhunterclient.application.MyApplication

/**
 * Created by luda on 2018/10/27
 * QQ 340071887.
 */
object KeyboardUtils {
    fun showKeyboard(editText: EditText) {
        //其中editText为dialog中的输入框的 EditText
        if(editText!=null){
            //设置可获得焦点
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            //请求获得焦点
            editText.requestFocus();
            //调用系统输入法
            val inputManager = MyApplication.instance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(editText, 0);
        }
    }

    fun hideKeyboard(view: View) {
        val inputManager =  MyApplication.instance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, 0);
    }
}