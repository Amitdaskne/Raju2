#include <jni.h>
#include <string>
#include <android/log.h>
#include <curl/curl.h>
#include <openssl/evp.h>
#include <openssl/pem.h>
#include <openssl/rsa.h>
#include <openssl/err.h>
#include <openssl/md5.h>
#include <openssl/aes.h>
#include <json.hpp>
#include <obfuscate.h>
#include "oxorany.h"
#include <openssl/sha.h>
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
#define LOG_TAG "SignatureCheck"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

using json = nlohmann::ordered_json;

time_t rng = 0;
std::string Enc;
static char ZENINOP[64];
static std::string exdate = oxorany("NULL");

std::string g_Token, g_Auth;
bool bValid = false;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_bgmi_MAct_exdate(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(exdate.c_str());
}
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
extern "C" JNIEXPORT jstring JNICALL
Java_com_bgmi_MAct_ZENINOP(JNIEnv *env, jobject activityObject) {
    return env->NewStringUTF(ZENINOP);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_bgmi_LogAct_GetKey(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(oxorany("https://t.me/")); // Link Channel
}
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
extern "C"
JNIEXPORT jstring JNICALL
Java_com_bgmi_utils_Downtwo_Version(JNIEnv *env, jclass clazz) {
    const char *versionUrl = (oxorany("Put Your Git Version.txt Link Here"));
    return env->NewStringUTF(versionUrl);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_bgmi_utils_Downtwo_Link(JNIEnv *env, jclass clazz) {
    const char *downloadUrl = (oxorany("Put Your Lib File Link Here"));  // Note Change Your Lib Name To libbgmi.so And Zip It With The Name Of ZENIN.zip And Then Upload To Git.
    // If You Want To Change This Just Change It From BoxApplication.java
    return env->NewStringUTF(downloadUrl);
}
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
extern "C"
JNIEXPORT jstring JNICALL
Java_com_bgmi_BoxApplication_getSdkKey(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(oxorany("YVSYVWYFS6TC"));//sdk key 
}

static const char *EXPECTED_SIGNATURE = "77f05d53ce8bf1855caef38ce87f13a8bb2b1b2cdd2d48da9d3ba897eac4549e";
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_bgmi_LogAct_nativeVerifySignature(JNIEnv *env, jobject thiz, jobject context) {
    jclass contextClass = env->GetObjectClass(context);
    jmethodID midGetPM = env->GetMethodID(contextClass, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject pm = env->CallObjectMethod(context, midGetPM);
    jmethodID midGetPkg = env->GetMethodID(contextClass, "getPackageName", "()Ljava/lang/String;");
    jstring pkgName = (jstring) env->CallObjectMethod(context, midGetPkg);
    jclass pmClass = env->GetObjectClass(pm);
    jmethodID midGetInfo = env->GetMethodID(pmClass, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jobject pkgInfo = env->CallObjectMethod(pm, midGetInfo, pkgName, 64);
    jclass pkgInfoClass = env->GetObjectClass(pkgInfo);
    jfieldID fidSignatures = env->GetFieldID(pkgInfoClass, "signatures", "[Landroid/content/pm/Signature;");
    jobjectArray sigArray = (jobjectArray) env->GetObjectField(pkgInfo, fidSignatures);
    if (sigArray == nullptr) return JNI_FALSE;
    jsize sigCount = env->GetArrayLength(sigArray);
    for (jsize i = 0; i < sigCount; i++) {
        jobject sig = env->GetObjectArrayElement(sigArray, i);
        jclass sigClass = env->GetObjectClass(sig);
        jmethodID midToBytes = env->GetMethodID(sigClass, "toByteArray", "()[B");
        jbyteArray sigBytes = (jbyteArray) env->CallObjectMethod(sig, midToBytes);
        jsize len = env->GetArrayLength(sigBytes);
        jbyte *buf = env->GetByteArrayElements(sigBytes, nullptr);
        unsigned char hash[SHA256_DIGEST_LENGTH];
        SHA256((unsigned char *) buf, len, hash);
        env->ReleaseByteArrayElements(sigBytes, buf, 0);
        char hexHash[SHA256_DIGEST_LENGTH * 2 + 1];
        for (int j = 0; j < SHA256_DIGEST_LENGTH; j++) {
            sprintf(&hexHash[j * 2], "%02x", hash[j]);
        }
        hexHash[SHA256_DIGEST_LENGTH * 2] = '\0';
        if (strcasecmp(hexHash, EXPECTED_SIGNATURE) == 0) {
            return JNI_TRUE;
        } else {
            LOGE("Invalid signature: %s", hexHash);
        }
    }
    return JNI_FALSE;
}

const char *GetAndroidID(JNIEnv *env, jobject context) {
    jclass contextClass = env->FindClass("android/content/Context");
    jmethodID getContentResolverMethod = env->GetMethodID(contextClass, "getContentResolver", "()Landroid/content/ContentResolver;");
    jclass settingSecureClass = env->FindClass("android/provider/Settings$Secure");
    jmethodID getStringMethod = env->GetStaticMethodID(settingSecureClass, "getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");
    auto obj = env->CallObjectMethod(context, getContentResolverMethod);
    auto str = (jstring) env->CallStaticObjectMethod(settingSecureClass, getStringMethod, obj, env->NewStringUTF("android_id"));
    return env->GetStringUTFChars(str, 0);
}

const char *GetDeviceModel(JNIEnv *env) {
    jclass buildClass = env->FindClass("android/os/Build");
    jfieldID modelId = env->GetStaticFieldID(buildClass, "MODEL", "Ljava/lang/String;");
    auto str = (jstring) env->GetStaticObjectField(buildClass, modelId);
    return env->GetStringUTFChars(str, 0);
}

const char *GetDeviceBrand(JNIEnv *env) {
    jclass buildClass = env->FindClass("android/os/Build");
    jfieldID modelId = env->GetStaticFieldID(buildClass, "BRAND", "Ljava/lang/String;");
    auto str = (jstring) env->GetStaticObjectField(buildClass, modelId);
    return env->GetStringUTFChars(str, 0);
}

const char *GetPackageName(JNIEnv *env, jobject context) {
    jclass contextClass = env->FindClass("android/content/Context");
    jmethodID getPackageNameId = env->GetMethodID(contextClass, "getPackageName", "()Ljava/lang/String;");
    auto str = (jstring) env->CallObjectMethod(context, getPackageNameId);
    return env->GetStringUTFChars(str, 0);
}

const char *GetDeviceUniqueIdentifier(JNIEnv *env, const char *uuid) {
    jclass uuidClass = env->FindClass("java/util/UUID");
    auto len = strlen(uuid);
    jbyteArray myJByteArray = env->NewByteArray(len);
    env->SetByteArrayRegion(myJByteArray, 0, len, (jbyte *) uuid);
    jmethodID nameUUIDFromBytesMethod = env->GetStaticMethodID(uuidClass, "nameUUIDFromBytes", "([B)Ljava/util/UUID;");
    jmethodID toStringMethod = env->GetMethodID(uuidClass, "toString", "()Ljava/lang/String;");
    auto obj = env->CallStaticObjectMethod(uuidClass, nameUUIDFromBytesMethod, myJByteArray);
    auto str = (jstring) env->CallObjectMethod(obj, toStringMethod);
    return env->GetStringUTFChars(str, 0);
}

struct MemoryStruct {
    char *memory;
    size_t size;
};

static size_t WriteMemoryCallback(void *contents, size_t size, size_t nmemb, void *userp) {
    size_t realsize = size * nmemb;
    struct MemoryStruct *mem = (struct MemoryStruct *) userp;
    mem->memory = (char *) realloc(mem->memory, mem->size + realsize + 1);
    if (mem->memory == NULL) {
        return 0;
    }
    memcpy(&(mem->memory[mem->size]), contents, realsize);
    mem->size += realsize;
    mem->memory[mem->size] = 0;
    return realsize;
}

std::string CalcMD5(std::string s) {
    std::string result;
    unsigned char hash[MD5_DIGEST_LENGTH];
    char tmp[4];
    MD5_CTX md5;
    MD5_Init(&md5);
    MD5_Update(&md5, s.c_str(), s.length());
    MD5_Final(hash, &md5);
    for (unsigned char i : hash) {
        sprintf(tmp, "%02x", i);
        result += tmp;
    }
    return result;
}

std::string CalcSHA256(std::string s) {
    std::string result;
    unsigned char hash[SHA256_DIGEST_LENGTH];
    char tmp[4];
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, s.c_str(), s.length());
    SHA256_Final(hash, &sha256);
    for (unsigned char i : hash) {
        sprintf(tmp, "%02x", i);
        result += tmp;
    }
    return result;
}
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
        //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
extern "C"
JNIEXPORT jstring JNICALL
Java_com_bgmi_LogAct_Check(JNIEnv *env, jclass clazz, jobject mContext, jstring mUserKey) {
    auto user_key = env->GetStringUTFChars(mUserKey, 0);
    std::string hwid = user_key;
    hwid += GetAndroidID(env, mContext);
    hwid += GetDeviceModel(env);
    hwid += GetDeviceBrand(env);
    std::string UUID = GetDeviceUniqueIdentifier(env, hwid.c_str());
    std::string errMsg;
    struct MemoryStruct chunk{};
    chunk.memory = (char *) malloc(1);
    chunk.size = 0;
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
    CURL *curl;
    CURLcode res;
    curl = curl_easy_init();
    if (curl) {
        char lol[1000];
        sprintf(lol, oxorany("https://venomkey.com/connect")); // PUT YOUR PANEL LINK HERE, IF YOUR PANEL SERVER IS PUBG THEN IT WILL WORK IF YOUR PANEL SERVER IS BGMI THEN SEARG PUBG IN THIS FILE AND REPLACE WITH BGMI, THEN YOUR PANEL KEY WILL WORK 
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
        curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, "POST");
        curl_easy_setopt(curl, CURLOPT_URL, lol);
        curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1);
        curl_easy_setopt(curl, CURLOPT_DEFAULT_PROTOCOL, "https");
        struct curl_slist *headers = NULL;
        headers = curl_slist_append(headers, "Accept: application/json");
        headers = curl_slist_append(headers, "Content-Type: application/x-www-form-urlencoded");
        headers = curl_slist_append(headers, "Charset: UTF-8");
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        char data[4096];
        sprintf(data, "game=PUBG&user_key=%s&serial=%s", user_key, UUID.c_str());
        LOGD("Request Data: %s", data);
        curl_easy_setopt(curl, CURLOPT_POST, 1);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteMemoryCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *) &chunk);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYSTATUS, 0);
        curl_easy_setopt(curl, CURLOPT_USERAGENT, "");
        curl_easy_setopt(curl, CURLOPT_TIMEOUT, 30L);
        curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 10L);
        res = curl_easy_perform(curl);
        if (res == CURLE_OK) {
            LOGD("Server Response: %s", chunk.memory);
            try {
                json result = json::parse(chunk.memory);
                bool isSuccess = false;
                if (result.contains("status")) {
                    if (result["status"].is_boolean()) {
                        isSuccess = result["status"].get<bool>();
                    } else if (result["status"].is_string()) {
                        std::string sStatus = result["status"].get<std::string>();
                        if (sStatus == "true" || sStatus == "1") {
                            isSuccess = true;
                        }
                    } else if (result["status"].is_number()) {
                        isSuccess = (result["status"].get<int>() == 1);
                    }
                }
                LOGD("Status parsed: %s", isSuccess ? "true" : "false");
                if (isSuccess) {
                    if (result.contains("data") && !result["data"].is_null()) {
                        std::string token = "";
                        if (result["data"].contains("token")) {
                            if (result["data"]["token"].is_string()) {
                                token = result["data"]["token"].get<std::string>();
                            }
                        }
                        if (result["data"].contains("Enc")) {
                            if (result["data"]["Enc"].is_string()) {
                                Enc = result["data"]["Enc"].get<std::string>();
                            }
                        }
                        if (result["data"].contains("EXP")) {
                            if (result["data"]["EXP"].is_string()) {
                                exdate = result["data"]["EXP"].get<std::string>();
                            }
                        } else {
                            exdate = "Expired";
                        }
                        if (result["data"].contains("rng") && !result["data"]["rng"].is_null()) {
                            if (result["data"]["rng"].is_number()) {
                                rng = result["data"]["rng"].get<time_t>();
                            } else if (result["data"]["rng"].is_string()) {
                                std::string rngStr = result["data"]["rng"].get<std::string>();
                                rng = std::stol(rngStr);
                            }
                        } else {
                            rng = time(0);
                        }
                        LOGD("Token: %s", token.c_str());
                        LOGD("Enc: %s", Enc.c_str());
                        LOGD("EXP: %s", exdate.c_str());
                        LOGD("RNG: %ld, Current Time: %ld", rng, time(0));
                        time_t currentTime = time(0);
                        long timeDiff = labs(currentTime - rng);
                        if (timeDiff <= 60) {
                            std::string auth = "PUBG";
                            auth += "-";
                            auth += user_key;
                            auth += "-";
                            auth += UUID;
                            auth += "-";
                            std::string license = oxorany("Vm8Lk7Uj2JmsjCPVPVjrLa7zgfx3uz9E");
                            auth += license.c_str();
                            std::string outputAuth = CalcMD5(auth);
                            LOGD("Calculated Auth: %s", outputAuth.c_str());
                            LOGD("Server Token: %s", token.c_str());
                            g_Token = token;
                            g_Auth = outputAuth;
                            if (!token.empty() && token == outputAuth) {
                                bValid = true;
                                if (!Enc.empty() && Enc.length() < 64) {
                                    strcpy(ZENINOP, Enc.c_str());
                                }
                                LOGD("Login Success");
                            } else {
                                bValid = false;
                                errMsg = "Authentication failed (Token mismatch)";
                                LOGE("Token mismatch - Expected: %s, Got: %s", outputAuth.c_str(), token.c_str());
                            }
                        } else {
                            errMsg = "Time synchronization error. Please check device time.";
                            LOGE("RNG check failed - Time diff: %ld seconds", timeDiff);
                        }      //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
                    } else {
                        errMsg = "Server returned success but no data";
                        LOGE("No data in response");
                    }
                } else {
                    if (result.contains("reason") && !result["reason"].is_null()) {
                        if (result["reason"].is_string()) {
                            errMsg = result["reason"].get<std::string>();
                        } else {
                            errMsg = "Login failed (Invalid reason format)";
                        }
                    } else if (result.contains("message") && result["message"].is_string()) {
                        errMsg = result["message"].get<std::string>();
                    } else {
                        errMsg = "Login failed (No error message)";
                    }
                    LOGE("Login failed: %s", errMsg.c_str());
                }
            } catch (json::exception &e) {
                errMsg = std::string("JSON Parse Error: ") + e.what();
                LOGE("JSON Error: %s\nResponse was: %s", e.what(), chunk.memory);
            }
        } else {
            errMsg = std::string("Network Error: ") + curl_easy_strerror(res);
            LOGE("CURL Error: %s", curl_easy_strerror(res));
        }     //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
    } else {
        errMsg = "Failed to initialize network";
    }
    free(chunk.memory);
    env->ReleaseStringUTFChars(mUserKey, user_key);
    return bValid ? env->NewStringUTF("OK") : env->NewStringUTF(errMsg.c_str());
}
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1