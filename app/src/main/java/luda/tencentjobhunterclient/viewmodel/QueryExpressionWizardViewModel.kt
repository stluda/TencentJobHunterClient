package luda.tencentjobhunterclient.viewmodel

import android.arch.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject
import luda.tencentjobhunterclient.base.expression.ConditionStack
import luda.tencentjobhunterclient.constant.KeyConstants
import kotlin.collections.HashMap

/**
 * Created by luda on 2018/8/19
 * QQ 340071887.
 */
class QueryExpressionWizardViewModel: ViewModel() {
    companion object {
        val expressionCategories = arrayOf(KeyConstants.EXP_TITLE,KeyConstants.EXP_TYPE,KeyConstants.EXP_LOCATION,
                KeyConstants.EXP_REQUIREMENTS,KeyConstants.EXP_DUTIES)
    }
    enum class ExpressionPrefix{
        And,
        Or
    }
    val conditionStackMap = HashMap<String,ConditionStack>()
    val conditionRedoStackMap = HashMap<String,ConditionStack>()
    val expressionPrefixMap = HashMap<String,ExpressionPrefix>()
    val expressionChangedSubject = PublishSubject.create<Boolean>()


    init {
        for(key in expressionCategories){
            conditionStackMap[key] = ConditionStack()
            conditionRedoStackMap[key] = ConditionStack()
            expressionPrefixMap[key] = ExpressionPrefix.And
        }

    }

    val firstIndexOfExpressionCategoriesEnabled : Int get() {
        for(i in 0 until expressionCategories.size){
            if(!conditionStackMap[expressionCategories[i]]!!.empty())return i
        }
        return Int.MAX_VALUE
    }

    val expression : String get() {
        val firstIndex = firstIndexOfExpressionCategoriesEnabled
        if(firstIndex==Int.MAX_VALUE)
            return ""
        else{
            val builder = StringBuilder()
            for(i in 0 until expressionCategories.size){
                val key = expressionCategories[i]
                val stack = conditionStackMap[key]!!
                if(stack.empty())continue

                if(i!=firstIndex) builder.append(when(expressionPrefixMap[key]!!){
                    ExpressionPrefix.And -> "&&"
                    ExpressionPrefix.Or -> "||"
                })
                builder.append(when(key){
                    KeyConstants.EXP_TITLE -> "T{"
                    KeyConstants.EXP_TYPE -> "TY{"
                    KeyConstants.EXP_LOCATION -> "L{"
                    KeyConstants.EXP_REQUIREMENTS -> "R{"
                    KeyConstants.EXP_DUTIES -> "D{"
                    else -> throw NotImplementedError()
                })
                builder.append(stack.condition).append("}")
            }

            return builder.toString()
        }
    }


}