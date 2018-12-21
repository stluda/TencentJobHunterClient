package luda.tencentjobhunterclient.exception

import TencentJobHunterMessage.Message
import luda.tencentjobhunterclient.util.MessageAgent

/**
 * Created by luda on 2018/6/10
 * QQ 340071887.
 */
class GetResponseException(val errorCode: Message.ErrorCode, val state: MessageAgent.RequestResultState) : Exception(getMessage(errorCode,state)) {
    companion object
    {
        private fun getMessage(errorCode: Message.ErrorCode,state: MessageAgent.RequestResultState) : String
        {
            return state.toString() + errorCode.toString()
        }
    }
}