package space.sdrmaker.sdrmobile.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_noaadecode.view.*
import space.sdrmaker.sdrmobile.R
import space.sdrmaker.sdrmobile.dsp.demodulators.AMDemodulator
import space.sdrmaker.sdrmobile.dsp.file.FileReader
import space.sdrmaker.sdrmobile.dsp.filters.FIRFilter
import space.sdrmaker.sdrmobile.dsp.image.NOAAImageSink
import space.sdrmaker.sdrmobile.dsp.image.NOAALineSyncer
import space.sdrmaker.sdrmobile.dsp.math.HilbertTransform
import space.sdrmaker.sdrmobile.dsp.math.Normalizer
import space.sdrmaker.sdrmobile.dsp.taps.SR8320_2000_2100_75t
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.round


class NOAADecodeFragment : Fragment() {

    private lateinit var root: View
    private lateinit var decodeButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressLabel: TextView
    private lateinit var fileTable: TableLayout
    private lateinit var files: List<String>
    private lateinit var checkBoxes: ArrayList<CheckBox>
    private lateinit var labels: ArrayList<TextView>
    private var decoded = 0
    private var toDecode = 0
    private val iqExtension = "iq"
    private val imgExtension = "png"
    private val handler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_noaadecode, container, false)

        // setup UI
        progressLabel = root.progressLabel
        progressBar = root.progressBar
        decodeButton = root.decodeButton
        decodeButton.setOnClickListener {
            start()
        }
        fileTable = root.fileTable

        refreshFileList()
        updateProgress()

        return root
    }

    private fun refreshFileList() {
        files = listFiles()
        buildFileTable()
    }

    private fun start() {
        decoded = 0
        toDecode = checkBoxes.filter { it.isChecked }.size
        if(toDecode == 0) return
        decodeButton.isEnabled = false
        updateProgress()
        checkBoxes.forEachIndexed { index, checkBox ->
            thread { if(checkBox.isChecked) this.decode(labels[index].text.toString()) }
        }
    }

    private fun listFiles(): List<String> {
        val dir = "${context!!.getExternalFilesDir(null)}"
        val iqFiles = File(dir).listFiles()!!.filter { it.extension == iqExtension }.map {
            it.name.slice(0 until it.name.length - iqExtension.length - 1)
        }.toSet()
        val imgFiles = File(dir).listFiles()!!.filter { it.extension == imgExtension }.map {
            it.name.slice(0 until it.name.length - imgExtension.length - 1)
        }.toSet()

        val undecodedFiles = iqFiles - imgFiles
        decodeButton.isEnabled = undecodedFiles.isNotEmpty()
        return undecodedFiles.toList()
    }

    private fun buildFileTable() {
        fileTable.removeAllViews()
        checkBoxes = ArrayList()
        labels = ArrayList()

        files.forEach {
            val row = TableRow(context)
            val checkBox = CheckBox(context)
            checkBoxes.add(checkBox)
            val label = TextView(context)
            label.text = it
            labels.add(label)
            row.addView(checkBox)
            row.addView(label)
            fileTable.addView(row)
        }
    }

    private fun updateProgress() {
        progressLabel.text = "$decoded / $toDecode"
        progressBar.progress = round(decoded.toFloat() * 100 / toDecode).toInt()
    }

    private fun decode(fileName: String) {
        val readPath = "${context!!.getExternalFilesDir(null)}/${fileName}.${iqExtension}"
        val writePath = "${context!!.getExternalFilesDir(null)}/${fileName}.${imgExtension}"

        val reader = FileReader(readPath, blockSize = 16 * 1000)
        val hilbert = HilbertTransform(reader)
        val amDemodulator = AMDemodulator(hilbert)
        val decimator = FIRFilter(amDemodulator, SR8320_2000_2100_75t, 2)
        val normalizer = Normalizer(decimator, 0f, 1f)
        val syncer = NOAALineSyncer(normalizer)
        val image = NOAAImageSink(syncer, writePath, verbose = true)
        image.write()

        decoded++
        updateProgress()
        if (decoded == toDecode) {
            handler.post { refreshFileList() }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            refreshFileList()
            toDecode = checkBoxes.filter { it.isChecked }.size
            updateProgress()
        }
    }

}
