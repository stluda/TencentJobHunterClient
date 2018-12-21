package luda.tencentjobhunterclient.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import luda.tencentjobhunterclient.R
import android.view.Gravity
import android.view.View
import android.widget.ToggleButton
import java.util.ArrayList


class CollapsableLayout(context: Context, attrs:AttributeSet) : LinearLayout(context,attrs) {
    init{
        val container = LinearLayout(context)
        val containerParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        containerParams.gravity = Gravity.CENTER
        containerParams.height=0
        container.layoutParams =containerParams
        addView(container)

        //LayoutInflater.from(context).inflate(R.layout.collapsable_layout,this);
        val toggle = ToggleButton(context)
        toggle.textOff=""
        toggle.textOn=""
        toggle.text=""
        //设置图片宽高
        val toggleParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        toggleParams.gravity = Gravity.CENTER
        toggle.layoutParams =toggleParams
        toggle.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp)

        addView(toggle)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val childList = ArrayList<View>()
        for(i in 2 until childCount){
            childList.add(getChildAt(i))
        }

        val innerContainer = getChildAt(0) as LinearLayout
        for(child in childList){
            removeView(child)
            innerContainer.addView(child)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val toggle = getChildAt(1) as ToggleButton
        val innerContainer = getChildAt(0) as LinearLayout


        toggle.setOnCheckedChangeListener { compoundButton, isChecked ->
            val containerParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            containerParams.gravity = Gravity.CENTER
            if(isChecked){
                toggle.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            }
            else{
                containerParams.height=0
                toggle.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }
            innerContainer.layoutParams =containerParams
        }
    }

}