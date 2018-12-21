package luda.tencentjobhunterclient.util

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.SwipeDismissBehavior
import android.widget.EditText
import luda.tencentjobhunterclient.R
import java.security.AccessController.getContext
import android.widget.TextView
import android.graphics.Color.parseColor
import android.graphics.Point
import android.support.v4.widget.PopupWindowCompat.showAsDropDown
import android.util.DisplayMetrics
import android.opengl.ETC1.getWidth
import android.opengl.ETC1.getHeight
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import luda.tencentjobhunterclient.util.KeyboardUtils.showKeyboard


/**
 * Created by luda on 2018/7/22
 * QQ 340071887.
 */
object DialogUtils {
    //创建一个不可滑动移除的SnackBar
    fun makeUnSwipeAbleSnackBar(view: View,text:CharSequence,duration:Int) : Snackbar{
        val snackBar = Snackbar.make(view,text,duration)
        val layout = snackBar.view as Snackbar.SnackbarLayout
        layout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val lp = layout.layoutParams
                if (lp is CoordinatorLayout.LayoutParams) {
                    lp.behavior = object: SwipeDismissBehavior<Snackbar.SnackbarLayout>(){
                        override fun canSwipeDismissView(view: View): Boolean {
                            return false
                        }
                    }
                    layout.layoutParams = lp
                }
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        return snackBar
    }

    fun showInputDialog(title:String,text:String,hint:String = "请输入",isNumeric:Boolean,context:Context,onConfirmListener: (value: String)->Unit){
        val inputView = LayoutInflater.from(context).inflate(R.layout.dialog_simple_input, null)
        val editText = inputView.findViewById<EditText>(R.id.edt_input)
        editText.hint = hint
        editText.setText(text)
        if(isNumeric)editText.inputType = InputType.TYPE_CLASS_NUMBER
        val dialog = AlertDialog.Builder(context)
                .setTitle(title)
                .setView(inputView)
                .setPositiveButton("确定",{_, _ ->
                    KeyboardUtils.hideKeyboard(editText)
                    onConfirmListener(editText.text.toString().trim())
                })
                .setNegativeButton("取消",{_,_->
                    KeyboardUtils.hideKeyboard(editText)
                })
                .create()
        dialog.show()
        Handler().postDelayed({
            showKeyboard(editText)
        },300)
    }

    fun showPopupWindow(context: Context,layoutID:Int,currentItem:View,point: Point,func:(layout:View,window:PopupWindow)->Unit) {
        // popupWindow中的布局
        val view = LayoutInflater.from(context).inflate(layoutID, null)
        val popupWindow = PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        // 只有绘制完后才能获取到正确的popupWindow的宽高
        popupWindow.contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.border_shadow))
        popupWindow.isTouchable = true
        popupWindow.isOutsideTouchable =true

        // 窗口的宽高
        val metric = DisplayMetrics()
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getMetrics(metric)

        val size = Point()
        manager.defaultDisplay.getSize(size)

        // 获取当前item在window中的坐标
        val curPositionInWindow = IntArray(2)
        currentItem.getLocationOnScreen(curPositionInWindow)

        val itemHeight = currentItem.height
        val popupHeight = view.measuredHeight // metric.heightPixels// view.measuredHeight
        val itemWidth = currentItem.width
        val popupWidth= view.measuredWidth
        val windowHeight = metric.heightPixels
        val showX = point.x
        val showY = point.y - itemHeight

        val xOff = if(showX<itemWidth/2) showX+10 else showX - popupWidth-10
        val yOff =if(showY + curPositionInWindow[1]<windowHeight/2) showY+10 else showY - popupHeight

        popupWindow.showAsDropDown(currentItem, xOff,yOff)
        // 对popupWindow的dismiss监听，关闭时将被选中item的颜色恢复
        popupWindow.setOnDismissListener {
            currentItem.setBackgroundColor(Color.parseColor("#ffffff"))
        }

        func(view,popupWindow)
    }

}