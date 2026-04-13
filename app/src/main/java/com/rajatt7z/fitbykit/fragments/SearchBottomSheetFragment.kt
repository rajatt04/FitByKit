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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.rajatt7z.fitbykit.R

class SearchBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_bottom_sheet_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchEdit      = view.findViewById<TextInputEditText>(R.id.bsSearchEdit)
        val btnSearch       = view.findViewById<MaterialButton>(R.id.btnSearch)
        val countryChipGroup = view.findViewById<ChipGroup>(R.id.countryChipGroup)
        val letterChipGroup  = view.findViewById<ChipGroup>(R.id.letterChipGroup)

        // ── Keyboard search action ──────────────────────────────────────────
        searchEdit.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                performNameSearch(searchEdit.text.toString().trim())
                true
            } else false
        }

        // ── Search button ───────────────────────────────────────────────────
        btnSearch.setOnClickListener {
            performNameSearch(searchEdit.text.toString().trim())
        }

        // ── Country chips ───────────────────────────────────────────────────
        val countryMap = mapOf(
            R.id.chipAmerican   to "American",
            R.id.chipBritish    to "British",
            R.id.chipCanadian   to "Canadian",
            R.id.chipChinese    to "Chinese",
            R.id.chipCroatian   to "Croatian",
            R.id.chipDutch      to "Dutch",
            R.id.chipEgyptian   to "Egyptian",
            R.id.chipFilipino   to "Filipino",
            R.id.chipFrench     to "French",
            R.id.chipGreek      to "Greek",
            R.id.chipIndian     to "Indian",
            R.id.chipIrish      to "Irish",
            R.id.chipItalian    to "Italian",
            R.id.chipJamaican   to "Jamaican",
            R.id.chipJapanese   to "Japanese",
            R.id.chipKenyan     to "Kenyan",
            R.id.chipMalaysian  to "Malaysian",
            R.id.chipMexican    to "Mexican",
            R.id.chipMoroccan   to "Moroccan",
            R.id.chipPolish     to "Polish",
            R.id.chipPortuguese to "Portuguese",
            R.id.chipRussian    to "Russian",
            R.id.chipSpanish    to "Spanish",
            R.id.chipThai       to "Thai",
            R.id.chipTunisian   to "Tunisian",
            R.id.chipTurkish    to "Turkish",
            R.id.chipVietnamese to "Vietnamese"
        )

        countryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val area = countryMap[checkedIds.firstOrNull()] ?: return@setOnCheckedStateChangeListener
            setFragmentResult(
                RESULT_KEY,
                bundleOf("type" to "country", "area" to area)
            )
            dismiss()
        }

        // ── A-Z Letter chips ────────────────────────────────────────────────
        letterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chipId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(chipId) ?: return@setOnCheckedStateChangeListener
            val letter = chip.text.firstOrNull() ?: return@setOnCheckedStateChangeListener
            setFragmentResult(
                RESULT_KEY,
                bundleOf("type" to "letter", "letter" to letter.toString())
            )
            dismiss()
        }
    }

    private fun performNameSearch(query: String) {
        if (query.isNotEmpty()) {
            setFragmentResult(RESULT_KEY, bundleOf("type" to "search", "query" to query))
            dismiss()
        }
    }

    companion object {
        const val TAG = "SearchBottomSheetFragment"
        const val RESULT_KEY = "searchRequestKey"
    }
}
