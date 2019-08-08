#include <jni.h>
#include <string>
#include <chrono>
#include <iostream>
#include <vector>
#include <sstream>

#include <fftw3.h>

using namespace std;
using namespace std::chrono;

class FIR {
    vector<float> coefs;
    int length;
    vector<float> delayLine;
    int count = 0;

public:
    FIR(vector<float> coefs) {
        this->coefs = coefs;
        this->length = coefs.size();
        this->delayLine = vector<float>(this->length);
    }

    float getOutputSample(float inputSample) {
        delayLine[count] = inputSample;
        float result = 0.0F;
        int index = count;
        for (int i = 0; i < length; i++) {
            result += coefs[i] * delayLine[index--];
            if (index < 0) index = length - 1;
        }
        if (++count >= length) count = 0;
        return result;
    }

};

extern "C" JNIEXPORT jlong JNICALL
Java_space_sdrmaker_sdrmobile_benchmarks_MainActivity_ndkConvolutionBenchmark(
        JNIEnv *env,
        jobject /* this */,
        jint filterLength,
        jint dataLength) {

    // init FIR filter & data
    vector<float> coefs(filterLength);
    for (int i = 0; i < filterLength; i++) {
        coefs[i] = static_cast<float> (rand()) / static_cast <float> (RAND_MAX);
    }

    vector<float> data(dataLength);
    for (int i = 0; i < dataLength; i++) {
        data[i] = static_cast<float> (rand()) / static_cast <float> (RAND_MAX);
    }

    FIR *filter = new FIR(coefs);

    // time it
    long int start = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    for (int i = 0; i < dataLength; i++) {
        filter->getOutputSample(data[i]);
    }
    long int end = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();

    return end - start;
}

extern "C" JNIEXPORT jlong JNICALL
Java_space_sdrmaker_sdrmobile_benchmarks_MainActivity_ndkFFTBenchmark(
        JNIEnv *env,
        jobject /* this */,
        jint fftWidth,
        jint dataLength) {

    fftw_complex *in = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * dataLength);

    // initialize input with random data
    long int initStart = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    for (int i = 0; i < dataLength; i++) {
        in[i][0] = static_cast<float> (rand()) / static_cast <float> (RAND_MAX);
        in[i][1] = static_cast<float> (rand()) / static_cast <float> (RAND_MAX);
    }
    long int initEnd = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();

    // run FFT & time it
    long int start = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    fftw_plan plan = fftw_plan_dft_1d(fftWidth, in, in, FFTW_FORWARD, FFTW_ESTIMATE);
    for (int i = 0; i <= dataLength / fftWidth; i++) {
        fftw_execute_dft(plan, &in[i * fftWidth], &in[i * fftWidth]);
    }
    long int end = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();

    // free up resources
    fftw_destroy_plan(plan);
    fftw_free(in);

    return end - start;
}
