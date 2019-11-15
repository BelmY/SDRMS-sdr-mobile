package space.sdrmaker.sdrmobile.ui

import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import space.sdrmaker.sdrmobile.R
import space.sdrmaker.sdrmobile.dsp.*
import space.sdrmaker.sdrmobile.dsp.taps.NOAA
import space.sdrmaker.sdrmobile.dsp.taps.SR8320_2000_2100_75t
import space.sdrmaker.sdrmobile.dsp.taps.SR832k_7k_9k_283t
import kotlin.concurrent.thread


class NOAAFragment : Fragment() {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var startButton: Button

    private val handler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_noaa, container, false)

        // setup UI
        startButton = root.findViewById(R.id.startButton)
        startButton.setOnClickListener {
            start()
        }
        tvOutput = root.findViewById<Button>(R.id.tvOutput)
        tvOutput.movementMethod = ScrollingMovementMethod()
        tvOutput.append("Ready...\n")

        return root
    }

    private fun start() {
        thread { this.transformThread() }
    }

//    private fun transformThread() {
//        printOnScreen("NOAA demodulation started.\n")
//        val readPath = "${context!!.getExternalFilesDir(null)}/noaa_iq_sample_832k.iq"
////        val writePath = "${context!!.getExternalFilesDir(null)}/noaa_sample_demoded.iq"
//        val writePath = "${context!!.getExternalFilesDir(null)}/noaa_iq_sample_832k_fm_demoded.iq"
//        val reader = FileReader(readPath)
//
////        val filter = ComplexFIRFilter(reader, NOAA)
//        val fmDemodulator = FMDemodulator(reader, 7500)
////        val hilbert = HilbertTransform(fmDemodulator)
////        val amDemodulator = AMDemodulator(hilbert, gain = 4f)
////        val decimator = Decimator(amDemodulator, 200, NOAA)
////        val filter2 = FIRFilter(amDemodulator, NOAA)
//
//        val writer = FileWriter()
////        writer.write(decimator, writePath)
//        writer.write(fmDemodulator, writePath)
////        val audioSink = AudioSink()
////        while (decimator.hasNext())
////            audioSink.write(amDemodulator.next())
////            audioSink.write(decimator.next())
////        writer.write(amDemodulator, writePath)
//        printOnScreen("NOAA demodulation finished.\n")
//    }

//    private fun transformThread() {
//        printOnScreen("NOAA demodulation started.\n")
//        val readPath = "${context!!.getExternalFilesDir(null)}/noaa_iq_sample_832k.iq"
////        val writePath = "${context!!.getExternalFilesDir(null)}/noaa_sample_demoded.iq"
//        val writePath = "${context!!.getExternalFilesDir(null)}/noaa_iq_sample_832k_decoded.iq"
//        val reader = FileReader(readPath)
//
////        val filter = ComplexFIRFilter(reader, NOAA)
//        val fmDemodulator = FMDemodulator(reader, 7500)
//        val hilbert = HilbertTransform(fmDemodulator)
//        val amDemodulator = AMDemodulator(hilbert, gain = 4f)
//        val decimator = Decimator(amDemodulator, 200, NOAA)
//        val normalizer = Normalizer(decimator, 0f, 1f)
//        val syncer = NOAALineSyncer(normalizer)
//        val image = NOAAImageSink(syncer)
////        val filter2 = FIRFilter(amDemodulator, NOAA)
//
//        val writer = FileWriter()
//        writer.write(arrayListOf(image.write()).iterator(), writePath)
//        printOnScreen("NOAA decoding finished.\n")
//    }

//    private fun transformThread() {
//        printOnScreen("NOAA demodulation started.\n")
//        val readPath = "${context!!.getExternalFilesDir(null)}/noaa_iq_from_python.iq"
//        val writePath = "${context!!.getExternalFilesDir(null)}/noaa_iq_from_python_decoded.iq"
//        val reader = FileReader(readPath)
//
//        val normalizer = Normalizer(reader, 0f, 1f)
//        val syncer = NOAALineSyncer(normalizer)
//        val image = NOAAImageSink(syncer)
////        val filter2 = FIRFilter(amDemodulator, NOAA)
//
//        val writer = FileWriter()
//        writer.write(arrayListOf(image.write()).iterator(), writePath)
//        printOnScreen("NOAA decoding finished.\n")
//    }

    // PREDECIMATION
//    private fun transformThread() {
//        printOnScreen("NOAA demodulation started.\n")
////        val readPath = "${context!!.getExternalFilesDir(null)}/gqrx_20190824_152507_137100000_mono48k-norm-gnuradio.raw"
////        val writePath = "${context!!.getExternalFilesDir(null)}/gqrx_20190824_152507_137100000_mono48k-norm-decoded-bigblock.iq"
//        val readPath = "${context!!.getExternalFilesDir(null)}/gqrx_20190824_152507_137100000_mono832k-norm.raw"
//        val writePath = "${context!!.getExternalFilesDir(null)}/gqrx_20190824_152507_137100000_mono832k-norm-decoded-1.iq"
////        val readPath = "${context!!.getExternalFilesDir(null)}/SDRSharp_20180101_122108Z_137100000Hz_IQ_187500SR.iq"
////        val writePath = "${context!!.getExternalFilesDir(null)}/SDRSharp_20180101_122108Z_137100000Hz_IQ_187500SR-decoded-properfilter-bigblock.raw"
//
//        val reader = FileReader(readPath)
////        val decimator = Decimator(reader, 100, NOAA)
////        val filter = FIRFilter(reader, NOAA)
////        val fmDemodulator = FMDemodulator(reader, 7500)
//        val decimator1 = FIRFilter(reader, SR832k_7k_9k_283t, 100)  // FIXME: SR / 2
//        val hilbert = HilbertTransform(decimator1)
//        val amDemodulator = AMDemodulator(hilbert)
////        val interpolator = Interpolator(amDemodulator, 13, NOAA)
//        val decimator2 = FIRFilter(amDemodulator, SR8320_2000_2100_75t, 2)
//        val normalizer = Normalizer(decimator2, 0f, 1f)
//        val syncer = NOAALineSyncer(normalizer)
//        val image = NOAAImageSink(syncer, verbose = true)
//
//        val writer = FileWriter()
//        writer.write(arrayListOf(image.write()).iterator(), writePath)
//        printOnScreen("NOAA decoding finished.\n")
//    }

    private fun transformThread() {
        printOnScreen("NOAA demodulation started.\n")
//        val readPath = "${context!!.getExternalFilesDir(null)}/gqrx_20190824_152507_137100000_mono48k-norm-gnuradio.raw"
//        val writePath = "${context!!.getExternalFilesDir(null)}/gqrx_20190824_152507_137100000_mono48k-norm-decoded-bigblock.iq"
        val readPath = "${context!!.getExternalFilesDir(null)}/gqrx_20190824_152507_137100000_mono832k-norm.raw"
        val writePath = "${context!!.getExternalFilesDir(null)}/gqrx_20190824_152507_137100000_mono832k-norm-decoded.iq"
//        val readPath = "${context!!.getExternalFilesDir(null)}/SDRSharp_20180101_122108Z_137100000Hz_IQ_187500SR.iq"
//        val writePath = "${context!!.getExternalFilesDir(null)}/SDRSharp_20180101_122108Z_137100000Hz_IQ_187500SR-decoded-properfilter-bigblock.raw"

        val reader = FileReader(readPath)
        val decimator1 = FIRFilter(reader, SR832k_7k_9k_283t, 200)
        val hilbert = HilbertTransform(decimator1)
        val amDemodulator = AMDemodulator(hilbert)
//        val interpolator = Interpolator(amDemodulator, 13, NOAA)
//        val decimator1 = FIRFilter(amDemodulator, SR832k_7k_9k_283t, 100)
//        val decimator2 = FIRFilter(amDemodulator, SR8320_2000_2100_75t, 2)
        val normalizer = Normalizer(amDemodulator, 0f, 1f)
        val syncer = NOAALineSyncer(normalizer)
        val image = NOAAImageSink(syncer, verbose = true)

        val writer = FileWriter()
        writer.write(arrayListOf(image.write()).iterator(), writePath)
        printOnScreen("NOAA decoding finished.\n")
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}
