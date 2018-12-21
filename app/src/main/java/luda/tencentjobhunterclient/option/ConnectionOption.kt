package luda.tencentjobhunterclient.option

import java.net.InetSocketAddress

data class ConnectionOption(
        var serverHost : String,
        var serverPort : Int,
        var receiveTimeout : Long = 3000,
        var maxReceiveRetryTimes : Long = 3){}