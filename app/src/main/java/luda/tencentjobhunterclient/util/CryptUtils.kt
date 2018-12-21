package luda.tencentjobhunterclient.util



/**
 * Created by luda on 2018/3/18.
 * QQ：340071887
 * 加密工具类
 */

object CryptUtils
{
    //external fun stringFromJNI(): String
    //val test = stringFromJNI()
    // Used to load the 'native-lib' library on application startup.
    init {
        System.loadLibrary("crypt")
    }

    external fun encrypt(secretMessage: ByteArray): ByteArray
    external fun decrypt(encryptedMessage : ByteArray): ByteArray
}

    //由于SpongyCastle/BouncyCastle过于庞大，以及出于安全性的考虑(java中间码容易被反编译)
    //改用JNI的方式实现，利用openssl实现AES-128-GCM加解密算法
    /*
//import org.bouncycastle.crypto.engines.AESFastEngine
import TencentJobHunterMessage.Message
import com.google.protobuf.CodedOutputStream
import org.spongycastle.crypto.InvalidCipherTextException
import org.spongycastle.crypto.engines.AESLightEngine
import org.spongycastle.crypto.modes.GCMBlockCipher
import org.spongycastle.crypto.params.AEADParameters
import org.spongycastle.crypto.params.KeyParameter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom

    private const val kKeyBitSize = 128
    private const val kNonceBitSize = 96
    private const val kMacBitSize = 128
    private val random = SecureRandom()


    fun AES_128_GCM_EncryptWithSizeInfo(secretMessage : ByteArray, key:ByteArray, nonSecretPayload:ByteArray = ByteArray(0)) : ByteArray
    {
        var gcm:GCMBlockCipher

        if (key.size !== kKeyBitSize / 8)
            throw IllegalArgumentException(String.format("Key必须是%d字节!", kKeyBitSize));


        if (secretMessage.size == 0)
            throw IllegalArgumentException("需要提供待加密明文!");

        //Using random nonce large enough not to repeat
        var nonce = ByteArray(kNonceBitSize / 8)
        random.nextBytes(nonce);

        var cipher = GCMBlockCipher(AESLightEngine());

        var parameters = AEADParameters(KeyParameter(key), kMacBitSize, nonce, nonSecretPayload);
        cipher.init(true, parameters);


        //Generate Cipher Text With Auth Tag
        var cipherText = ByteArray(cipher.getOutputSize(secretMessage.size));
        var len = cipher.processBytes(secretMessage, 0, secretMessage.size, cipherText, 0);
        cipher.doFinal(cipherText, len);

        //OutputStreamWriter()

        ByteArrayOutputStream().use{ bos ->
            val pack_size = 2 + nonSecretPayload.size + nonce.size + cipherText.size

            bos.write(byteArrayOf((pack_size and 0xff).toByte(),(pack_size.shr(8)  and 0xff).toByte()))
            bos.write(nonSecretPayload)
            bos.write(nonce)
            bos.write(cipherText)
            return bos.toByteArray()
        }
    }

    fun AES_128_GCM_DecryptWithSizeInfo(encryptedMessage : ByteArray, start:Int , size:Int, key:ByteArray, nonSecretPayloadLength:Int = 0) : ByteArray {
        var gcm: GCMBlockCipher

        if (key.size !== kKeyBitSize / 8)
            throw IllegalArgumentException(String.format("Key必须是%d字节!", kKeyBitSize));


        if (encryptedMessage.size == 0)
            throw IllegalArgumentException("需要提供密文!");

        ByteArrayInputStream(encryptedMessage).use { ios ->
            ios.skip(start.toLong())

            //Grab Payload
            var nonSecretPayload = ByteArray(nonSecretPayloadLength)
            ios.read(nonSecretPayload)
            //Grab Nonce
            var nonce = ByteArray(kNonceBitSize / 8)
            ios.read(nonce)

            val cipher = GCMBlockCipher(AESLightEngine())
            val parameters = AEADParameters(KeyParameter(key), kMacBitSize, nonce, nonSecretPayload)
            cipher.init(false, parameters)

            //解密
            var cipherText = ByteArray(size - nonSecretPayloadLength - nonce.size)
            ios.read(cipherText)
            var plainText = ByteArray(cipher.getOutputSize(cipherText.size))

            try {
                var len = cipher.processBytes(cipherText, 0, cipherText.size, plainText, 0);
                cipher.doFinal(plainText, len);
            } catch (e: InvalidCipherTextException) {
                //解密失败返回null
                throw e
            }
            return plainText
        }
    }
    */
