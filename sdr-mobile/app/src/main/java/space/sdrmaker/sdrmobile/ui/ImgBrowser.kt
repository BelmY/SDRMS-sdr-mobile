package space.sdrmaker.sdrmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_noaadecode.view.fileTable
import space.sdrmaker.sdrmobile.R
import java.io.File
import android.content.Intent
import android.os.StrictMode
import androidx.core.content.FileProvider
import space.sdrmaker.sdrmobile.BuildConfig


class ImgBrowser : Fragment(), TabLayout.OnTabSelectedListener {

    private lateinit var root: View
    private lateinit var fileTable: TableLayout
    private lateinit var files: List<String>
    private lateinit var labels: ArrayList<TextView>
    private val imgExtension = "jpeg"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_imgbrowser, container, false)

        // setup UI
        fileTable = root.fileTable
        refreshFileList()

        try {
            val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
            m.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return root
    }

    private fun refreshFileList() {
        files = listFiles()
        buildFileTable()
    }

    private fun listFiles(): List<String> {
        val dir = "${context!!.getExternalFilesDir(null)}"
        return File(dir).listFiles().filter { it.extension == imgExtension }.map { it.name }
    }

    private fun buildFileTable() {
        fileTable.removeAllViews()
        labels = ArrayList()

        files.forEach {
            val row = TableRow(context)
            val label = TextView(context)
            label.text = it
            label.isClickable = true
            label.setOnClickListener { view -> displayImage((view as TextView).text.toString()) }
            labels.add(label)
            row.addView(label)
            fileTable.addView(row)
        }
    }

    private fun displayImage(fileName: String) {
        val filePath = "${context!!.getExternalFilesDir(null)}/${fileName}"
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val fileUri = FileProvider.getUriForFile(context!!, "${BuildConfig.APPLICATION_ID}.myfileprovider", File(filePath))
        intent.setDataAndType(fileUri, "image/*")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        startActivity(intent)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        refreshFileList()
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        onTabSelected(tab)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}
}
