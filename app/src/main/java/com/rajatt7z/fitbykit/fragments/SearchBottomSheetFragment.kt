package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.rajatt7z.fitbykit.R

class SearchBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_bottom_sheet_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchEdit = view.findViewById<TextInputEditText>(R.id.bsSearchEdit)
        val btnSearch = view.findViewById<MaterialButton>(R.id.btnSearch)
        val chipGroup = view.findViewById<ChipGroup>(R.id.filterChipGroup)

        searchEdit.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch(searchEdit.text.toString().trim())
                true
            } else {
                false
            }
        }

        btnSearch.setOnClickListener {
            performSearch(searchEdit.text.toString().trim())
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChipId = checkedIds.first()
                var query = ""
                when (checkedChipId) {
                    R.id.chipChicken -> query = "Chicken"
                    R.id.chipBeef -> query = "Beef"
                    R.id.chipVegetarian -> query = "Vegetarian"
                    R.id.chipDessert -> query = "Dessert"
                    R.id.chipPork -> query = "Pork"
                }
                
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            setFragmentResult("searchRequestKey", bundleOf("query" to query))
            dismiss()
        }
    }
    
    companion object {
        const val TAG = "SearchBottomSheetFragment"
    }
}
