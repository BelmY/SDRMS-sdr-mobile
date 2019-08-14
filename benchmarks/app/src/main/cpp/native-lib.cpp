#include <jni.h>
#include <string>
#include <chrono>
#include <iostream>
#include <vector>
#include <sstream>
#include <math.h>
#include <cmath>

#include <fftw3.h>
#include <complex>

using namespace std;
using namespace std::chrono;

class FIRFloat {
    float *coefs;
    int length;
    float *delayLine;
    int count = 0;

public:
    FIRFloat(float *coefs, int length) {
        this->coefs = coefs;
        this->length = length;
        this->delayLine = new float[length];
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

class FIRShort {
    short *coefs;
    int length;
    short *delayLine;
    int count = 0;

public:
    FIRShort(short *coefs, int length) {
        this->coefs = coefs;
        this->length = length;
        this->delayLine = new short[length];
    }

    short getOutputSample(short inputSample) {
        delayLine[count] = inputSample;
        short result = 0;
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
Java_space_sdrmaker_sdrmobile_benchmarks_NativeUtils_ndkFloatConvolutionBenchmark(
        JNIEnv *env,
        jclass,
        jint filterLength,
        jint dataLength) {

    // init FIRFloat filter & data
    float coefs[filterLength];
    for (int i = 0; i < filterLength; i++) {
        coefs[i] = static_cast<float> (rand()) / static_cast <float> (RAND_MAX);
    }

    float data[dataLength];
    for (int i = 0; i < dataLength; i++) {
        data[i] = static_cast<float> (rand()) / static_cast <float> (RAND_MAX);
    }

    FIRFloat *filter = new FIRFloat(coefs, filterLength);

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
Java_space_sdrmaker_sdrmobile_benchmarks_NativeUtils_ndkShortConvolutionBenchmark(
        JNIEnv *env,
        jclass,
        jint filterLength,
        jint dataLength) {

    // init FIR filter & data
    short coefs[filterLength];
    for (int i = 0; i < filterLength; i++) {
        coefs[i] = static_cast<short> (rand());
    }

    short data[dataLength];
    for (int i = 0; i < dataLength; i++) {
        data[i] = static_cast<short> (rand());
    }

    FIRShort *filter = new FIRShort(coefs, filterLength);

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
Java_space_sdrmaker_sdrmobile_benchmarks_NativeUtils_ndkComplexFFTBenchmark(
        JNIEnv *env,
        jclass,
        jint fftWidth,
        jint dataLength) {

    fftw_complex *in = (fftw_complex *) fftw_malloc(sizeof(fftw_complex) * dataLength);

    // initialize input with random data
    for (int i = 0; i < dataLength; i++) {
        in[i][0] = static_cast<double> (rand()) / static_cast <double> (RAND_MAX);
        in[i][1] = static_cast<double> (rand()) / static_cast <double> (RAND_MAX);
    }

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

extern "C" JNIEXPORT jlong JNICALL
Java_space_sdrmaker_sdrmobile_benchmarks_NativeUtils_ndkRealFFTBenchmark(
        JNIEnv *env,
        jclass,
        jint fftWidth,
        jint dataLength) {

    double *in = (double *) fftw_malloc(sizeof(double) * dataLength);
    fftw_complex *out = (fftw_complex *) fftw_malloc(sizeof(fftw_complex) * (floor(fftWidth / 2) + 1));

    // initialize input with random data
    for (int i = 0; i < dataLength; i++) {
        in[i] = static_cast<double> (rand()) / static_cast <double> (RAND_MAX);
    }

    // run FFT & time it
    long int start = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    fftw_plan plan = fftw_plan_dft_r2c_1d(fftWidth, in, out, FFTW_ESTIMATE);
    for (int i = 0; i <= dataLength / fftWidth; i++) {
        fftw_execute_dft_r2c(plan, &in[i * fftWidth], out);
    }
    long int end = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();

    // free up resources
    fftw_destroy_plan(plan);
    fftw_free(in);

    return end - start;
}

const float dacRange = pow(2, 13) - 1;

extern "C" JNIEXPORT jlong JNICALL
Java_space_sdrmaker_sdrmobile_benchmarks_NativeUtils_ndkShortFloatConversionBenchmark(
        JNIEnv *env,
        jclass,
        jint conversionsToPerform) {

    // time empty for loop
    long int start = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    for (int i = 0; i < conversionsToPerform; i++) {
        // ¯\_(ツ)_/¯
    }
    long int end = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    long forLoopTime = end - start;

    // benchmark short -> float conversions
    short shortData[conversionsToPerform];
    for (int i = 0; i < conversionsToPerform; i++) {
        shortData[i] = static_cast<short> (rand());
    }
    start = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    for (int i = 0; i < conversionsToPerform; i++) {
        float floatVal = (float) shortData[i] / dacRange;
    }
    end = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    long shortFloatTime = end - start - forLoopTime;

    return shortFloatTime;
}

extern "C" JNIEXPORT jlong JNICALL
Java_space_sdrmaker_sdrmobile_benchmarks_NativeUtils_ndkFloatShortConversionBenchmark(
        JNIEnv *env,
        jclass,
        jint conversionsToPerform) {

    // time empty for loop
    long int start = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    for (int i = 0; i < conversionsToPerform; i++) {
        // ¯\_(ツ)_/¯
    }
    long int end = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    long forLoopTime = end - start;

    // benchmark float -> short conversions
    float floatData[conversionsToPerform];
    for (int i = 0; i < conversionsToPerform; i++) {
        floatData[i] = static_cast<float> (rand()) / static_cast <float> (RAND_MAX);
    }
    start = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    for (int i = 0; i < conversionsToPerform; i++) {
        float scaled = floatData[i] * dacRange;
        short shortVal;
        if (scaled > SHRT_MAX)
            shortVal = SHRT_MAX;
        else if (scaled < SHRT_MIN)
            shortVal = SHRT_MIN;
        else
            shortVal = (short) scaled;

    }
    end = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    long floatShortTime = end - start - forLoopTime;

    return floatShortTime;
}

extern "C" JNIEXPORT jlong JNICALL
Java_space_sdrmaker_sdrmobile_benchmarks_NativeUtils_ndkShortComplexConversionBenchmark(
        JNIEnv *env,
        jclass,
        jint conversionsToPerform) {

    // time empty for loop
    long int start = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    for (int i = 0; i < conversionsToPerform; i++) {
        // ¯\_(ツ)_/¯
    }
    long int end = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    long forLoopTime = end - start;

    // benchmark short -> complex conversions
    vector<short> shortComplexData(conversionsToPerform * 2);
    for (int i = 0; i < conversionsToPerform * 2 - 1; i++) {
        shortComplexData[i] = static_cast<short> (rand());
    }
    start = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    for (int i = 0; i < conversionsToPerform * 2 - 1; i += 2) {
        complex<float> complexVal = complex<float>(
                (float) shortComplexData[i] / dacRange,
                (float) shortComplexData[i + 1] / dacRange);

    }
    end = duration_cast<milliseconds>(
            system_clock::now().time_since_epoch()
    ).count();
    long shortComplexTime = end - start - forLoopTime;

    return shortComplexTime;
}