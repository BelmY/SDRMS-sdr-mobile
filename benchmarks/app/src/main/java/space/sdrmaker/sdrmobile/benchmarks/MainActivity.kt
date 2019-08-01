package space.sdrmaker.sdrmobile.benchmarks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        java_convolution_button.setOnClickListener(View.OnClickListener {
            this.onJavaConvolutionClick()
        })

        ndk_convolution_button.setOnClickListener(View.OnClickListener {
            this.onNDKConvolutionClick()
        })
    }

    private fun onJavaConvolutionClick() {
        println("Java convolution result")
    }

    private fun onNDKConvolutionClick() {
        val bmResult = ndkConvolution()
        println(bmResult)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun ndkConvolution(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
