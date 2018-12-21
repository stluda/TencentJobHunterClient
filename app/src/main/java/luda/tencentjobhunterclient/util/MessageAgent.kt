package luda.tencentjobhunterclient.util

import TencentJobHunterMessage.Message
import android.os.AsyncTask
import android.os.Build.VERSION_CODES.O
import android.os.ConditionVariable
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.exception.GetResponseException
import luda.tencentjobhunterclient.model.JobQueryResult
import luda.tencentjobhunterclient.option.ConnectionOption
import luda.tencentjobhunterclient.util.MessageUtils.encrpytRequestBytes
import java.net.*
import java.nio.ByteBuffer
import java.text.ParseException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by luda on 2018/3/23
 * QQ 340071887.
 */


object MessageAgent {
    enum class RequestResultState
    {
        SUCCESS,
        TIMEOUT,
        PARSE_FAILED,
        ERROR,
        INTERRUPTED
    }


    private val mExecutorService : ExecutorService = Executors.newCachedThreadPool();

    private val mScheduledPoolExecutor = ScheduledThreadPoolExecutor(2)

    //服务器地址


    data class RequestResult(val state : RequestResultState,val ex:Exception?, val response:Message.Response?){}

    private data class Ref<T>(var value: T)

    private data class Sender(
            val run : ()->Unit,
            val quit : (RequestResultState, ex:Exception?, response:Message.Response?)->Unit
    )


    //返回一个Sender，执行run()后正式开始工作
    //自带定时器重发，返回值为onQuit高阶函数，在外部调用onQuit后停止方法内部的定时器
    private fun createSender(connectionOption: ConnectionOption, request : Message.Request, socket: DatagramSocket, cond : ConditionVariable, rResult: Ref<RequestResult?>)
            :  Sender{


        val serverAddress = InetSocketAddress(connectionOption.serverHost, connectionOption.serverPort)
        val receiveTimeout = connectionOption.receiveTimeout
        val totalReceiveTimeout  = 1000 + receiveTimeout * (1 + connectionOption.maxReceiveRetryTimes)


        val beginTime = request.requestTime
        val currentMessage : ByteArray  = encrpytRequestBytes(request)
        val sendPack = DatagramPacket(currentMessage, currentMessage.size, serverAddress)

        //设置超时时间三秒
        socket.soTimeout = 3000

        //makeHole(socket)

        //重发计时器
        val resendTimer = Timer()
        //是否已经退出（结果：完成/超时/其他异常）
        var isQuit = false

        //标记为任务已结束，停止定时器并保存结果
        val onQuit =  { state : RequestResultState,ex:Exception?, response:Message.Response? ->
            isQuit = true
            resendTimer.cancel()
            //currentMessage = null
            rResult.value = RequestResult(state,ex,response)
            socket.close()
            cond.open()//通知条件变量任务已结束
        }

        fun remainTime() = totalReceiveTimeout -  (System.currentTimeMillis() - beginTime)


        val run :()->Unit = {

            mScheduledPoolExecutor.scheduleAtFixedRate({
                try {
                    if (isQuit || remainTime() <= 10) return@scheduleAtFixedRate
                    if(MyApplication.isNetworkEnabled)socket.send(sendPack)//发送
                }
                catch (ex:Exception){

                }

            },0, receiveTimeout,TimeUnit.MILLISECONDS)

//            resendTimer.schedule(object:TimerTask(){
//                override fun run() {
//
//                }
//
//            },0, receiveTimeout)
            //socket.send(sendPack)//发送
        }

        return Sender(run,onQuit);
    }


    //封包解析类
    private class PackParser(val data: ByteArray){
        val magic = ByteBuffer.wrap(data,0,2).short.toInt()
        val type = data[2].toInt()
        val requestTime = ByteBuffer.wrap(data, 3,8).long
        val size = ByteBuffer.wrap(data, 11,2).short.toInt()

        val packCount : Int
        val packIndex : Int
        val packSize : Int

        init {
            when(type){
                0->{
                    packCount=1
                    packIndex=0
                    packSize=size
                }
                1->{
                    packCount = data[13].toInt()
                    packIndex = data[14].toInt()
                    packSize = ByteBuffer.wrap(data, 15,2).short.toInt()
                }
                else->{
                    throw ParseException("解析封包时出错，type类型不正确",0)
                }
            }
        }
    }


    //处理单个封包
    private fun dealWithSinglePackage(packInfo:PackParser,recvPack:DatagramPacket,sender:Sender){
        //解密并解析响应数据
        try {
            val response = MessageUtils.decryptResponseBytes(recvPack.data,13,packInfo.size-13)
            //用requestTime判断该response是否和该request成对，如果不是想要的，则继续接收
            sender.quit(RequestResultState.SUCCESS,null,response)
        }
        catch(ex:Exception)
        {
            sender.quit(RequestResultState.PARSE_FAILED,ex,null)
        }
    }

    //处理多个封包并将其拼凑成完整的response
    //返回true表示已经处理完毕，否则表示尚未处理完成
    private fun dealWithMultiPackage(packInfo:PackParser,recvPack:DatagramPacket,sender:Sender,
                                     packReceivedFlag:BooleanArray,buff:ByteArray) : Boolean{
        //Log.d("Message","收到包[{$requestTime}],$packIndex,$type,${recvPack.length},$packSize")
        val dataSize = packInfo.size - packInfo.packCount*17
        //Log.d("Message","收到包[{$requestTime}],$packIndex")
        val offset = (1024-17)*packInfo.packIndex
        System.arraycopy(recvPack.data,17,buff,offset,packInfo.packSize-17)
        packReceivedFlag[packInfo.packIndex] = true

        var allReceived = true
        for(i in 0 until packInfo.packCount){
            if(!packReceivedFlag[i]){
                allReceived = false
                break
            }
        }

        if(allReceived){
            //解密并解析响应数据
            try {
                val response = MessageUtils.decryptResponseBytes(buff,0,dataSize)
                //用requestTime判断该response是否和该request成对，如果不是想要的，则继续接收
                sender.quit(RequestResultState.SUCCESS,null,response)
                return true
            }
            catch(ex:Exception)
            {
                sender.quit(RequestResultState.PARSE_FAILED,ex,null)
                return true
            }
        }else{
            return false
        }
    }




    fun getResponseRx(request : Message.Request, option: ConnectionOption) : Single<Message.Response>
            = Single.create<Message.Response> {emitter->
        try {
            val request = request.toBuilder().setRequestTime(System.currentTimeMillis()).build()
            val cond = ConditionVariable()
            var rResult = Ref<RequestResult?>(null)


            val socket = DatagramSocket()
            val sender = createSender(option,request,socket,cond,rResult)
            val beginTime = request.requestTime

            val totalReceiveTimeout = 1000 + option.receiveTimeout * (1 + option.maxReceiveRetryTimes)
            fun remainTime() = totalReceiveTimeout -  (System.currentTimeMillis() - beginTime)

            //另开线程进行接收任务
            mExecutorService.execute {

                val buff = ByteArray(65536);
                val packReceivedFlag = BooleanArray(64,{false});
                var ix=0;
                while (remainTime()>10)
                {
                    ix++
                    Log.d("msg","第{$ix}轮接收");
                    try{
                        val recvPack = DatagramPacket(ByteArray(1024),1024)
                        //仅当收到的数据确实来自于服务器，且包头的长度数据和实际收到的数据长度一致时，才进行下一步，不然忽略，继续接收
                        socket.receive(recvPack)
                        val data = recvPack.data

                        val packInfo = PackParser(data)

                        Log.d("Message","收到包:${recvPack.length}，magic=$packInfo.magic,requestTime=$packInfo.requestTime")

                        //val size = ((data[1].toInt() and 0xff) shl 8) or (data[0].toInt() and 0xff)
                        if(packInfo.magic==1937 && beginTime==packInfo.requestTime)
                        {
                            if(packInfo.type==0 && packInfo.size==recvPack.length)
                            {
                                dealWithSinglePackage(packInfo,recvPack,sender)
                                return@execute
                            }
                            else if(packInfo.type==1 && packInfo.packSize==recvPack.length){
                                if(dealWithMultiPackage(packInfo,recvPack,sender,packReceivedFlag,buff))return@execute
                            }
                        }
                    }
                    catch(e1: SocketTimeoutException){//超时

                    }
                    catch(ex:Exception){
                        sender.quit(RequestResultState.ERROR,ex,null)
                        return@execute
                    }
                    Log.d("msg","剩余时间:${remainTime()}")
                }
                sender.quit(RequestResultState.TIMEOUT,null,null)//超时quit
            }
            sender.run()
            cond.block()//等待任务结束

            val value = rResult.value!!
            if(value.state== MessageAgent.RequestResultState.SUCCESS){
                val response = value.response!!
                if(response.errorCode==Message.ErrorCode.SUCCESS)
                    emitter.onSuccess(response)
                else
                    emitter.onError(GetResponseException(response.errorCode,RequestResultState.SUCCESS))
            }
            else
            {
                emitter.onError(GetResponseException(Message.ErrorCode.INVALID,value.state))
            }
        }
        catch (ex:Exception){
            emitter.onError(ex)
        }
    }.subscribeOn(Schedulers.io())


    fun getResponseRxMulti(requestList : ArrayList<Message.Request>, option: ConnectionOption) : Single<ArrayList<MessageAgent.RequestResult>>
            = Single.create<ArrayList<MessageAgent.RequestResult>> {emitter->

        try {
            val cond = ConditionVariable()
            val rResultList = ArrayList<Ref<RequestResult?>>()


            var doInterrupt = false

            for(request in requestList){
                var rResult = Ref<RequestResult?>(null)
                rResultList.add(rResult)
                //val resendCond = ConditionVariable()
                val socket = DatagramSocket()
                val sender = createSender(option,request,socket,cond,rResult)
                val beginTime = request.requestTime

                val totalReceiveTimeout = 1000 + option.receiveTimeout * (1 + option.maxReceiveRetryTimes)
                fun remainTime() = totalReceiveTimeout -  (System.currentTimeMillis() - beginTime)


                //另开线程进行接收任务
                mExecutorService.execute {

                    val buff = ByteArray(65536);
                    val packReceivedFlag = BooleanArray(64,{false});
                    var ix=0;
                    while (remainTime()>10)
                    {
                        //被打断
                        if(doInterrupt){
                            sender.quit(RequestResultState.INTERRUPTED,null,null)
                            return@execute
                        }

                        ix++
                        Log.d("msg","第{$ix}轮接收");
                        try{
                            val recvPack = DatagramPacket(ByteArray(1024),1024)
                            //仅当收到的数据确实来自于服务器，且包头的长度数据和实际收到的数据长度一致时，才进行下一步，不然忽略，继续接收
                            socket.receive(recvPack)

                            //被打断
                            if(doInterrupt){
                                sender.quit(RequestResultState.INTERRUPTED,null,null)
                                return@execute
                            }

                            val data = recvPack.data

                            val packInfo = PackParser(data)

                            Log.d("Message","收到包:${recvPack.length}，magic=$packInfo.magic,requestTime=$packInfo.requestTime")

                            //val size = ((data[1].toInt() and 0xff) shl 8) or (data[0].toInt() and 0xff)
                            if(packInfo.magic==1937 && beginTime==packInfo.requestTime)
                            {
                                if(packInfo.type==0 && packInfo.size==recvPack.length)
                                {
                                    dealWithSinglePackage(packInfo,recvPack,sender)
                                    return@execute
                                }
                                else if(packInfo.type==1 && packInfo.packSize==recvPack.length){
                                    if(dealWithMultiPackage(packInfo,recvPack,sender,packReceivedFlag,buff))return@execute
                                }
                            }
                        }
                        catch(e1: SocketTimeoutException){//超时

                        }
                        catch(ex:Exception){
                            sender.quit(RequestResultState.ERROR,ex,null)
                            return@execute
                        }
                        Log.d("msg","剩余时间:${remainTime()}")
                    }
                    sender.quit(RequestResultState.TIMEOUT,null,null)//超时quit
                }
                sender.run()
            }


            val resultList = ArrayList<RequestResult>()

            while(true){
                cond.block(500)//每500毫秒，或执行sender.quit()方法后取消阻塞，检查是否所有接收任务都已完成

                var errorOccured = false
                var allDone = true
                for(rResult in rResultList){
                    if(rResult.value!=null){
                        if(rResult.value!!.state != RequestResultState.SUCCESS){
                            //resultList.add(rResult.value!!)
                            errorOccured = true
                        }
                    }
                    else{
                        //只要有一个rResult的value为空（尚未执行sender.quit()方法），则继续等待
                        allDone = false
                    }
                }

                //有异常发生，或所有任务都已完成
                if(errorOccured){
                    //等待300毫秒，给receiver一个机会去接受数据
                    Thread.sleep(300)
                    for(rResult in rResultList)
                        if(rResult.value!=null)resultList.add(rResult.value!!)
                    //打断仍在进行的接收/重发任务
                    doInterrupt = true
                    break
                }
                else if(allDone){
                    for(rResult in rResultList)resultList.add(rResult.value!!)
                    break
                }
            }

            emitter.onSuccess(resultList)
        }
        catch (ex:Exception){
            emitter.onError(ex)
        }

    }.subscribeOn(Schedulers.io())


}

