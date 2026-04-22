package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityAboutUserBinding
import com.rajatt7z.fitbykit.navigation.FitByKitNav
import java.io.ByteArrayOutputStream

class AboutUser : AppCompatActivity() {

    private lateinit var binding: ActivityAboutUserBinding
    private var byteArray: ByteArray? = null

    // ── Photo picker (uses Android 13+ PhotoPicker API when available) ────────────
    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            handleSelectedImage(uri)
        }
    }

    // ── Legacy image picker for Android 12 and below ─────────────────────────────
    @SuppressLint("SuspiciousIndentation")
    private val legacyImagePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { rc ->
        if (rc.resultCode == RESULT_OK) {
            rc.data?.data?.let { uri -> handleSelectedImage(uri) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdowns()
        setupClickListeners()
    }

    private fun setupDropdowns() {
        val genderOptions = listOf("Male", "Female", "Other")
        binding.genderDropdown.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, genderOptions)
        )

        val weights = (30..200).map { "$it kg" }
        binding.weightDropdown.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, weights)
        )

        val heights = (100..220).map { "$it cm" }
        binding.heightDropdown.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, heights)
        )
    }

    private fun setupClickListeners() {
        binding.questionAboutUser.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("About You")
                .setMessage(
                    "These details are used to personalise your experience " +
                            "— generating diet plans and calculating your BMI."
                )
                .setPositiveButton("Got it", null)
                .show()
        }

        binding.userImg.setOnClickListener { launchImagePicker() }

        binding.nextBtn.setOnClickListener { onNextClicked() }
    }

    private fun launchImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: use the privacy-preserving PhotoPicker
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            // Android 12 and below: fall back to file chooser
            legacyImagePicker.launch(
                Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            )
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: run {
                Toast.makeText(this, "Could not read image", Toast.LENGTH_SHORT).show()
                return
            }

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, bos)
            val compressed = bos.toByteArray()

            if (compressed.size / 1024 >= 2048) {
                Toast.makeText(this, "Please choose an image smaller than 2 MB", Toast.LENGTH_SHORT).show()
                return
            }

            byteArray = compressed
            binding.userImg.setImageBitmap(bitmap)
        } catch (_: Exception) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onNextClicked() {
        val rawName = binding.userNameEnter.text.toString()
        val cleanName = rawName.cleanInvisibleV2()

        if (cleanName.length < 3) {
            Toast.makeText(this, "Please enter a valid name (at least 3 characters)", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = binding.genderDropdown.text.toString().trim()
        val weight = binding.weightDropdown.text.toString().replace(" kg", "").trim()
        val height = binding.heightDropdown.text.toString().replace(" cm", "").trim()

        if (gender.isEmpty() || weight.isEmpty() || height.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Use the selected image or fall back to the default avatar
        val finalBytes = byteArray ?: run {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.def)
            ByteArrayOutputStream().also { bos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            }.toByteArray()
        }

        getSharedPreferences("userPref", MODE_PRIVATE).edit {
            putString("userImg", Base64.encodeToString(finalBytes, Base64.DEFAULT))
            putString("userName", cleanName)
            putString("userGender", gender)
            putString("userWeight", weight)
            putString("userHeight", height)
            putBoolean("isUserSetupDone", true)
        }

        startActivity(Intent(this, FitByKitNav::class.java))
        finish()
    }
}

/**
 * Strips invisible/control characters and collapses duplicate whitespace.
 * Used to sanitise user-entered names before saving to SharedPreferences.
 */
fun String.cleanInvisibleV2(trimResult: Boolean = true): String {
    val sb = StringBuilder(this.length)
    var lastCharWasWhitespace = false

    for (char in this) {
        if (Character.isISOControl(char) ||
            char == '\u200B' || char == '\u200D' || char == '\u2060'
        ) continue

        if (Character.isWhitespace(char)) {
            if (!lastCharWasWhitespace) {
                sb.append(' ')
                lastCharWasWhitespace = true
            }
        } else {
            sb.append(char)
            lastCharWasWhitespace = false
        }
    }

    return if (trimResult) sb.toString().trim() else sb.toString()
}
