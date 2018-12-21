package luda.tencentjobhunterclient.base.expression

/**
 * Created by luda on 2018/9/1
 * QQ 340071887.
 */

abstract class Condition{
    abstract fun isMatch(text:String) : Boolean
    abstract val description : String
    companion object {

        private data class TokenHolder(var value:String="",var offset:Int=0)

        fun parse(text:String) : Condition{
            val token = TokenHolder()
            getToken(text,token)
            return evalOrCondition(text,token)
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
                '!','(',')'->{
                    token.offset += 1
                    token.value = "$ch"
                    return
                }
            }

            val start = token.offset
            loop@ while (++token.offset < max) {
                ch = text[token.offset]
                when (ch) {
                    '!', '(', ')' -> break@loop
                    '&', '|' -> if (token.offset + 1 < max && text[token.offset + 1] == ch) {
                        break@loop
                    }
                    else if(ch.isWhitespace())break@loop
                }
            }
            token.value = text.substring(start, token.offset)
        }

        private fun evalAtom(text:String,token: TokenHolder) : Condition{
            when(token.value){
                "&&","||","!","(",")",""->throw Exception("条件格式有误！")
                else -> {
                    val ret = AtomCondition(token.value)
                    getToken(text,token)
                    return ret
                }
            }
        }

        private fun evalBraceCondition(text:String,token: TokenHolder) =
            if(token.value=="("){
                getToken(text,token)
                val cond = BraceCondition(evalOrCondition(text,token))
                if(token.value!=")")throw Exception("条件格式有误！")
                getToken(text,token)
                cond
            }
            else{
                evalAtom(text,token)
            }

        private fun evalNotCondition(text:String,token: TokenHolder) : Condition{
            if (token.value == "!") {
                getToken(text, token)
                return NotCondition(evalBraceCondition(text, token))
            } else {
                return evalBraceCondition(text, token)
            }
        }
        private fun evalAndCondition(text:String,token: TokenHolder) : Condition{
            var result = evalNotCondition(text, token)
            while (token.value == "&&") {
                getToken(text, token)
                result = AndCondition(result, evalNotCondition(text, token))
            }
            return result
        }
        private fun evalOrCondition(text:String,token: TokenHolder) : Condition{
            var result = evalAndCondition(text, token)
            while (token.value == "||") {
                getToken(text, token)
                result = OrCondition(result, evalAndCondition(text, token))
            }
            return result
        }

    }
}

class AtomCondition(val content:String) : Condition(){
    override fun isMatch(text: String) = content.contains(text)
    override fun toString() = content
    override val description: String
        get() = "包含'$content'"
}

class BraceCondition(val condition : Condition) : Condition(){
    override fun isMatch(text: String) = condition.isMatch(text)
    override fun toString() = "(${condition.toString()})"
    override val description: String
        get() = "(${condition.description})"
}

class NotCondition(val condition: Condition) : Condition() {
    override fun isMatch(text: String) = !condition.isMatch(text)
    override fun toString() = "!${condition.toString()}"
    override val description: String
        get() =
            if(condition is AtomCondition)"不${condition.description}"
            else "不满足[${condition.description}]"
}

class OrCondition(val condition1: Condition,val condition2: Condition) : Condition() {
    override fun isMatch(text: String) = condition1.isMatch(text) || condition2.isMatch(text)
    override fun toString() = "${condition1.toString()}||${condition2.toString()}"
    override val description: String
        get() = "${condition1.description}或${condition2.description}"
}

class AndCondition(val condition1: Condition,val condition2: Condition) : Condition() {
    override fun isMatch(text: String) = condition1.isMatch(text) && condition2.isMatch(text)
    override fun toString() = "${condition1.toString()}&&${condition2.toString()}"
    override val description: String
        get() = "${condition1.description}且${condition2.description}"
}
