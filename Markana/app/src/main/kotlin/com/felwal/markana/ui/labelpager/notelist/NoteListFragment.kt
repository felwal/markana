package com.felwal.markana.ui.labelpager.notelist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.felwal.android.util.canScrollUp
import com.felwal.android.util.getColorByAttr
import com.felwal.android.util.getDrawableCompatWithTint
import com.felwal.android.util.getInteger
import com.felwal.android.util.isPortrait
import com.felwal.android.util.removeAll
import com.felwal.markana.R
import com.felwal.markana.app
import com.felwal.markana.data.Note
import com.felwal.markana.databinding.FragmentNotelistBinding
import com.felwal.markana.prefs
import com.felwal.markana.ui.labelpager.LabelPagerActivity
import com.felwal.markana.ui.labelpager.NoteListListAdapter
import com.felwal.markana.ui.notedetail.NoteDetailActivity
import com.felwal.markana.util.submitListKeepScroll

// args
private const val ARG_POSITION = "labelId"

class NoteListFragment :
    Fragment(),
    SwipeRefreshLayout.OnRefreshListener {

    // data
    private lateinit var model: NoteListViewModel
    private var position = 0

    // view
    lateinit var binding: FragmentNotelistBinding
    private lateinit var listAdapter: NoteListListAdapter

    private val c get() = requireContext()
    private val a get() = requireActivity() as LabelPagerActivity

    // lifecycle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotelistBinding.inflate(layoutInflater, container, false)

        initData()
        initRecycler()
        initRefreshLayout()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        model.loadNotes()
        onRefresh()
    }

    // data

    private fun initData() {
        arguments?.apply {
            position = getInt(ARG_POSITION, 0)
        }

        val container = requireActivity().app.appContainer
        model = container.noteListViewModels[position]

        model.notesData.observe(viewLifecycleOwner) { notes ->
            submitItems(notes)
        }
        model.notifyAdapterData.observe(viewLifecycleOwner) { notify ->
            if (notify) {
                listAdapter.notifyDataSetChanged()
                model.notifyAdapterData.postValue(false)
            }
        }
        model.notifyManagerData.observe(viewLifecycleOwner) { notify ->
            if (notify) {
                setManager()
                model.notifyManagerData.postValue(false)
            }
        }
    }

    // recycler

    private fun initRecycler() {
        // animate tb and fab on scroll
        binding.rv.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            // animate tb
            a.binding.ab.isActivated = binding.rv.canScrollUp()

            // show/hide fab
            a.binding.fam.updateVisibilityOnScroll(scrollY - oldScrollY)
        }

        // adapter
        listAdapter = NoteListListAdapter(
            onClick = {
                if (model.isSelectionMode) selectNote(it)
                else NoteDetailActivity.startActivity(c, it.uri, it.colorIndex, model.searchQueryOrNull)
            },
            onLongClick = {
                selectNote(it)
            }
        )
        binding.rv.adapter = listAdapter

        setManager()
    }

    private fun setManager() {
        // set manager
        val spanCount =
            if (!prefs.gridView) c.getInteger(R.integer.quantity_notelist_list_columns)
            else if (c.isPortrait) c.getInteger(R.integer.quantity_notelist_grid_columns_portrait)
            else c.getInteger(R.integer.quantity_notelist_grid_columns_landscape)

        val manager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        binding.rv.layoutManager = manager
    }

    private fun submitItems(items: List<Note>) {
        // when searching, keeping scroll state results in irregularities
        if (model.isSearching) listAdapter.submitList(items)
        else listAdapter.submitListKeepScroll(items, binding.rv.layoutManager)

        // toggle empty page
        if (items.isEmpty()) {
            binding.inEmpty.root.isGone = false

            // set search empty page
            if (model.isSearching) {
                binding.inEmpty.tvEmptyTitle.text = c.getString(R.string.tv_notelist_empty_search_title)
                binding.inEmpty.tvEmptyMessage.text = c.getString(R.string.tv_notelist_empty_search_message)
                binding.inEmpty.ivEmpty.setImageDrawable(
                    c.getDrawableCompatWithTint(R.drawable.ic_search_24, R.attr.colorAccent)
                )
            }
            // set new user empty page
            else {
                binding.inEmpty.tvEmptyTitle.text = c.getString(R.string.tv_notelist_empty_new_title)
                binding.inEmpty.tvEmptyMessage.text = c.getString(R.string.tv_notelist_empty_new_message)
                binding.inEmpty.ivEmpty.setImageDrawable(
                    c.getDrawableCompatWithTint(R.drawable.ic_note_24, R.attr.colorAccent)
                )
            }
        }
        else binding.inEmpty.root.isGone = true
    }

    // swiperefresh

    private fun initRefreshLayout() {
        binding.srl.setOnRefreshListener(this)
        binding.srl.setProgressBackgroundColorSchemeColor(c.getColorByAttr(R.attr.colorSurface))
        binding.srl.setColorSchemeColors(c.getColorByAttr(R.attr.colorControlActivated))

        model.srlEnabledData.observe(viewLifecycleOwner) { enabled ->
            binding.srl.isEnabled = enabled
        }
    }

    override fun onRefresh() {
        model.syncNotes {
            binding.srl.isRefreshing = false
        }
    }

    // selection

    private fun selectNote(note: Note) {
        // sync with data and adapter
        val index = model.toggleNoteSelection(note)
        listAdapter.notifyItemChanged(index)

        a.syncToolbar()
    }

    //

    companion object {
        fun newInstance(position: Int) = NoteListFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POSITION, position)
            }
        }
    }
}