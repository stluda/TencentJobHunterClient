package luda.tencentjobhunterclient.base.expression

import java.util.*

/**
 * Created by luda on 2018/8/19
 * QQ 340071887.
 */
class ConditionStack : Stack<ConditionHolder>() {
    val condition : String get() {
        val builder = StringBuilder()
        for(conditionHolder in this){
            builder.append(when(conditionHolder.type){
                ConditionHolder.Type.Contains -> ""
                ConditionHolder.Type.NotContains -> "!"
                ConditionHolder.Type.AndContains -> "&&"
                ConditionHolder.Type.AndNotContains -> "&&!"
                ConditionHolder.Type.OrContains -> "||"
                ConditionHolder.Type.OrNotContains -> "||!"
            }).append(conditionHolder.content)
        }
        return builder.toString()
    }

    private var mOnDataChangedListener : (()->Unit)? = null

    fun setOnDataChangeListener(listener:()->Unit){
        mOnDataChangedListener = listener
    }

    override fun push(item: ConditionHolder?): ConditionHolder {
        val ret = super.push(item)
        mOnDataChangedListener?.invoke()
        return ret
    }

    override fun pop(): ConditionHolder {
        val ret = super.pop()
        if(ret!=null) mOnDataChangedListener?.invoke()
        return ret
    }

    fun transferOne(stack: ConditionStack){
        stack.silentPush(silentPop())
        mOnDataChangedListener?.invoke()
        stack.mOnDataChangedListener?.invoke()
    }

    fun transferAll(stack: ConditionStack){
        while (!empty()){
            stack.silentPush(silentPop())
        }
        mOnDataChangedListener?.invoke()
        stack.mOnDataChangedListener?.invoke()
    }

    private fun silentPop(): ConditionHolder {
        return super.pop()
    }

    private fun silentPush(item: ConditionHolder?): ConditionHolder {
        return super.push(item)
    }

}