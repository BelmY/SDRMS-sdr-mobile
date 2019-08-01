#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_space_sdrmaker_sdrmobile_benchmarks_MainActivity_ndkConvolution(
        JNIEnv *env,
jobject /* this */) {
std::string bm_result = "NDK convolution result";
return env->NewStringUTF(bm_result.c_str());
}
