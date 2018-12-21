package luda.tencentjobhunterclient.util

import TencentJobHunterMessage.Message
import com.google.protobuf.CodedOutputStream
import java.io.ByteArrayOutputStream

/**
 * Created by luda on 2018/3/30
 * QQ 340071887.
 * 报文加密解析类
 * 在CryptUtils的基础上，基于Protobuf对CryptUtils做进一步的封装
 */

object  MessageUtils
{
    fun decryptResponseBytes(encrypted_data: ByteArray, start:Int, size:Int): Message.Response {
        //val data = CryptUtils.AES_128_GCM_DecryptWithSizeInfo(encrypted_data, start,size, key)
        val data = CryptUtils.decrypt(encrypted_data.copyOfRange(start,start+size))
        //val data = CryptUtils.decrypt()
        return Message.Response.parseFrom(data)
    }

    fun encrpytRequestBytes(request: Message.Request): ByteArray{
        val buff = ByteArray(request.serializedSize)
        val stream = CodedOutputStream.newInstance(buff)
        request.writeTo(stream)
        val encryptResult = CryptUtils.encrypt(buff)
        val ret = ByteArray(encryptResult.size+2)

        ByteArrayOutputStream().use{ bos ->
            val packSize = 2 + encryptResult.size
            bos.write(byteArrayOf((packSize and 0xff).toByte(),(packSize.shr(8)  and 0xff).toByte()))
            bos.write(encryptResult)
            return bos.toByteArray()
        }
    }

    /*
    fun decryptResponseBytes(encrypted_data: ByteArray, start:Int, size:Int, key: ByteArray): Message.Response {
        val data = CryptUtils.AES_128_GCM_DecryptWithSizeInfo(encrypted_data, start,size, key)
        return Message.Response.parseFrom(data)
    }

    fun encrpytRequestBytes(request: Message.Request, aesKey:ByteArray): ByteArray{
        val buff = ByteArray(request.serializedSize)
        val stream = CodedOutputStream.newInstance(buff)
        request.writeTo(stream)
        return CryptUtils.AES_128_GCM_EncryptWithSizeInfo(buff, aesKey)
    }
    */
}