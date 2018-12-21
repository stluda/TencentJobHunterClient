package luda.tencentjobhunterclient.base.expression

/**
 * Created by luda on 2018/9/2
 * QQ 340071887.
 */
abstract class JobQueryExpression {
    abstract val description : String
    companion object {
        private data class TokenHolder(var value:String="",var offset:Int=0)

        fun parse(text:String) : JobQueryExpression{
            val token = TokenHolder()
            getToken(text,token)
            return evalOrQueryExpression(text,token)
        }

        private fun getToken(text:String,token:TokenHolder){
            val max = text.length
            if(max==token.offset){
                token.value = ""
                return
            }

            while (token.offset < max && text[token.offset].isWhitespace())token.offset++
            if (max == token.offset){
                token.value = ""
                return
            }

            var ch = text[token.offset]
            when(ch){
                '&','|' -> {
                    if (token.offset + 1 < max&&text[token.offset + 1] == ch)
                    {
                        token.offset += 2
                        token.value = "$ch$ch"
                        return
                    }
                }
                '(',')','{','}'->{
                    token.offset += 1
                    token.value = "$ch"
                    return
                }
            }

            val start = token.offset
            loop@ while (++token.offset < max) {
                ch = text[token.offset]
                when (ch) {
                    '!', '(', ')','{','}'-> break@loop
                    '&', '|' -> if (token.offset + 1 < max && text[token.offset + 1] == ch) {
                        break@loop
                    }
                    else if(ch.isWhitespace())break@loop
                }
            }
            token.value = text.substring(start, token.offset)
        }

        private fun evalAtom(text:String,token:TokenHolder) : AtomJobQueryExpression{
            val type = when(token.value.toUpperCase()){
                "T","标题" -> "T"
                "TY","类别" -> "TY"
                "L","地点" -> "L"
                "H","人数" -> "H"
                "R","要求" -> "R"
                "D","职责" -> "D"
                else -> throw Exception("表达式格式有误！")
            }
            getToken(text,token)
            if (token.value != "{")throw Exception("表达式格式有误！")
            val end = text.indexOf('}',token.offset)
            if(end<0)throw Exception("表达式格式有误！")

            val innerText = text.substring(token.offset,end)
            val innerCondition = Condition.parse(innerText)

            token.offset = end+1
            getToken(text,token)

            return AtomJobQueryExpression(type,innerCondition)
        }

        private fun evalBraceExpression(text:String,token:TokenHolder) : JobQueryExpression{
            lateinit var ret : JobQueryExpression
            if(token.value == "("){
                getToken(text,token)
                ret = BraceJobQueryExpression(evalOrQueryExpression(text,token))
                if(token.value!=")")throw Exception("表达式格式有误！")
                getToken(text,token)
            }
            else{
                ret = evalAtom(text,token)
            }
            return ret
        }

        private fun evalAndQueryExpression(text:String,token:TokenHolder) : JobQueryExpression{
            var result = evalBraceExpression(text,token)
            while (token.value == "&&"){
                getToken(text,token)
                result = AndJobQueryExpression(result, evalBraceExpression(text,token))
            }
            return result
        }

        private fun evalOrQueryExpression(text:String,token:TokenHolder) : JobQueryExpression{
            var result = evalAndQueryExpression(text,token)
            while (token.value == "||"){
                getToken(text,token)
                result = OrJobQueryExpression(result, evalAndQueryExpression(text,token))
            }
            return result
        }


    }
}

class AtomJobQueryExpression(type:String,val innerCondition:Condition) : JobQueryExpression(){
    val type = type.toUpperCase()
    override val description: String
        get() = when(type){
            "T","标题" -> "标题"
            "TY","类别" -> "类别"
            "L","地点" -> "地点"
            "H","人数" -> "招聘人数"
            "R","要求" -> "要求"
            "D","职责" -> "职责"
            else -> throw NotImplementedError()
        } + "满足{${innerCondition.description}}"
    override fun toString() = "$type{${innerCondition.toString()}}"
}

class BraceJobQueryExpression(val innerExpression:JobQueryExpression) : JobQueryExpression(){
    override fun toString() = "(${innerExpression.toString()})"
    override val description: String
        get() = "(${innerExpression.description})"
}

class AndJobQueryExpression(val innerExpression1:JobQueryExpression,val innerExpression2:JobQueryExpression) : JobQueryExpression(){
    override fun toString() = "${innerExpression1.toString()}&&${innerExpression2.toString()}"
    override val description: String
        get() = "${innerExpression1.description}且${innerExpression2.description}"
}

class OrJobQueryExpression(val innerExpression1:JobQueryExpression,val innerExpression2:JobQueryExpression) : JobQueryExpression(){
    override fun toString() = "${innerExpression1.toString()}||${innerExpression2.toString()}"
    override val description: String
        get() = "${innerExpression1.description}或${innerExpression2.description}"
}