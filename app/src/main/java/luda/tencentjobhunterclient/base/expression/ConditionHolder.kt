package luda.tencentjobhunterclient.base.expression

/**
 * Created by luda on 2018/8/19
 * QQ 340071887.
 */
class ConditionHolder(val type:Type,val content:String) {
    enum class Type{
        Contains,
        NotContains,
        AndContains,
        AndNotContains,
        OrContains,
        OrNotContains
    }

    val expression get() = when(type){
        Type.Contains -> content
        Type.NotContains -> "!$content"
        Type.AndContains -> "&&$content"
        Type.AndNotContains -> "&&!$content"
        Type.OrContains -> "||$content"
        Type.OrNotContains -> "||!$content"
    }
    val chsType = when(type){
        Type.Contains -> "包含"
        Type.NotContains -> "不包含"
        Type.AndContains -> "且包含"
        Type.AndNotContains -> "且不包含"
        Type.OrContains -> "或包含"
        Type.OrNotContains -> "或不包含"
    }
    val chsExpression  = chsType + content


    override fun toString() = expression

}