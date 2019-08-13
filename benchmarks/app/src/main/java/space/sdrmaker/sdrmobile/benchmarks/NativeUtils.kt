package space.sdrmaker.sdrmobile.benchmarks

class NativeUtils {

    companion object {
        @JvmStatic
        external fun ndkFloatConvolutionBenchmark(filterLength: Int, dataLength: Int): Long
        @JvmStatic
        external fun ndkShortConvolutionBenchmark(filterLength: Int, dataLength: Int): Long
        @JvmStatic
        external fun ndkComplexFFTBenchmark(fftWidth: Int, dataLength: Int): Long
        @JvmStatic
        external fun ndkRealFFTBenchmark(fftWidth: Int, dataLength: Int): Long
        @JvmStatic
        external fun ndkShortFloatConversionBenchmark(conversionsToPerform: Int): Long
        @JvmStatic
        external fun ndkFloatShortConversionBenchmark(conversionsToPerform: Int): Long
        @JvmStatic
        external fun ndkShortComplexConversionBenchmark(conversionsToPerform: Int): Long

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}

