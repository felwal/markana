package me.felwal.markana.ui.labelpager

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import me.felwal.markana.ui.labelpager.notelist.NoteListFragment

class LabelPagerStateAdapter(
    private val labelCount: Int,
    fragment: FragmentActivity
) : FragmentStateAdapter(fragment) {

    val fragments = MutableList<NoteListFragment?>(itemCount) { null }

    //

    override fun getItemCount() = labelCount

    override fun createFragment(position: Int) = NoteListFragment.newInstance(position).also {
        fragments[position] = it
    }
}