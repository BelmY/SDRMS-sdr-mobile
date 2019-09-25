package space.sdrmaker.sdrmobile.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import space.sdrmaker.sdrmobile.R

private val TAB_TITLES = arrayOf(
    R.string.tab_1_title,
    R.string.tab_2_title,
    R.string.tab_3_title,
    R.string.tab_4_title,
    R.string.tab_5_title,
    R.string.tab_6_title
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return appropriate Fragment (defined as a static inner class below).
        return when(position) {
            0 -> RXFragment()
            1 -> ReadFileFragment()
            2 -> ResamplingFragment()
            3 -> FMDemodFragment()
            4 -> AudioSinkFragment()
            5 -> FMRcvFragment()
            else -> RXFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 6
    }
}