#include <jni.h>
#include <string>
#include "openssl/crypto.h"
#include "openssl/aes.h"
#include "openssl/evp.h"
#include "openssl/rand.h"


void aes_init()
{
    static bool inited = false;
    if (!inited)
    {
        //EVP_CIPHER_CTX e_ctx, d_ctx;

        //initialize openssl ciphers
        OpenSSL_add_all_ciphers();
        //OPENSSL_init_crypto(OPENSSL_INIT_ADD_ALL_CIPHERS, NULL);

        //initialize random number generator (for IVs)
        int rv = RAND_load_file("/dev/urandom", 32);

        inited = true;
    }
}

int AES_128_GCM_Decrypt(const char* ciphertext, std::size_t cipher_size, const char* key, char* output_buffer)
{
    aes_init();
    unsigned char tag[16];
    unsigned char iv[12];

    memcpy(iv, ciphertext, 12);
    memcpy(tag, ciphertext + cipher_size - 16, 16);

    int actual_size = 0, final_size = 0;
    EVP_CIPHER_CTX *d_ctx = EVP_CIPHER_CTX_new();
    EVP_DecryptInit(d_ctx, EVP_aes_128_gcm(), (const unsigned char*)key, iv);
    EVP_DecryptUpdate(d_ctx, (unsigned char*)&output_buffer[0], &actual_size, (const unsigned char*)&ciphertext[12], cipher_size - 28);
    EVP_CIPHER_CTX_ctrl(d_ctx, EVP_CTRL_GCM_SET_TAG, 16, tag);
    EVP_DecryptFinal(d_ctx, (unsigned char*)&output_buffer[actual_size], &final_size);
    EVP_CIPHER_CTX_free(d_ctx);

    return actual_size + final_size;
}

int AES_128_GCM_Encrypt(const char* data, std::size_t data_size, const char* key, char* output_ciphertext)
{
    aes_init();

    unsigned char tag[16];
    unsigned char iv[12];

    RAND_bytes(iv, sizeof(iv));
    memcpy(output_ciphertext, iv, 12);

    int actual_size = 0, final_size = 0;
    EVP_CIPHER_CTX* e_ctx = EVP_CIPHER_CTX_new();

    EVP_EncryptInit(e_ctx, EVP_aes_128_gcm(), (const unsigned char*)key, iv);
    EVP_EncryptUpdate(e_ctx, (unsigned char*)&output_ciphertext[12], &actual_size, (const unsigned char*)data, data_size);
    EVP_EncryptFinal(e_ctx, (unsigned char*)&output_ciphertext[28 + actual_size], &final_size);
    EVP_CIPHER_CTX_ctrl(e_ctx, EVP_CTRL_GCM_GET_TAG, 16, tag);

    memcpy(output_ciphertext + actual_size + final_size + 12, tag, 16);

    EVP_CIPHER_CTX_free(e_ctx);

    return actual_size + final_size+28;
}



extern "C"
JNIEXPORT jstring

JNICALL
Java_luda_tencentjobhunterclient_util_CryptUtils_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
    //return env->NewStringUTF(OpenSSL_version(OPENSSL_VERSION));
}

extern "C"
JNIEXPORT jbyteArray
JNICALL
Java_luda_tencentjobhunterclient_util_CryptUtils_encrypt(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray secretMessage) {
    jsize size = env->GetArrayLength(secretMessage);
    jbyte* data = env->GetByteArrayElements(secretMessage,0);

    const char* aes_key = "a1Dx7hi631jsbbGz";

    if(size>996)
    {
        jbyteArray ret = env->NewByteArray(0);
        return ret;
    }
    char buff[1024];

    int result_size = AES_128_GCM_Encrypt((const char*)data,size,aes_key,buff);

    jbyteArray ret = env->NewByteArray(result_size);
    env->SetByteArrayRegion(ret, 0, result_size,  (jbyte*)buff);
    return ret;
}


extern "C"
JNIEXPORT jbyteArray
JNICALL
Java_luda_tencentjobhunterclient_util_CryptUtils_decrypt(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray ciphertext) {
    jsize size = env->GetArrayLength(ciphertext);
    jbyte* data = env->GetByteArrayElements(ciphertext,0);

    if(size<28||size>65000)
    {
        jbyteArray ret = env->NewByteArray(0);
        return ret;
    }

    const char* aes_key = "a1Dx7hi631jsbbGz";

    char buff[65536];

    int result_size = AES_128_GCM_Decrypt((const char*)data,size,aes_key,buff);

    jbyteArray ret = env->NewByteArray(result_size);
    env->SetByteArrayRegion(ret, 0, result_size,  (jbyte*)buff);
    return ret;
}