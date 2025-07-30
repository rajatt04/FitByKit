package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.databinding.ActivityAboutUserBinding
import com.rajatt7z.fitbykit.navigation.FitByKitNav
import java.io.ByteArrayOutputStream

class AboutUser : AppCompatActivity() {

    private lateinit var binding: ActivityAboutUserBinding
    private var byteArray: ByteArray ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.questionAboutUser.setOnClickListener{
            MaterialAlertDialogBuilder(this)
                .setTitle("About You")
                .setMessage("This details will be used to create your profile and to personalize your experience like generating diet plans and calculating BMI")
                .setPositiveButton("Yes", null)
                .show()
        }

        binding.userImg.setOnClickListener{
            val  i = Intent(Intent.ACTION_GET_CONTENT)
            i.setType("image/*")
            ImageUploading.launch(i)
        }

        val genderOptions = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, genderOptions)
        binding.genderDropdown.setAdapter(genderAdapter)

        val weights = (30..200).map { "$it kg" }
        val weightAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, weights)
        binding.weightDropdown.setAdapter(weightAdapter)

        val heights = (100..220).map { "$it cm" }
        val heightAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, heights)
        binding.heightDropdown.setAdapter(heightAdapter)

        binding.nextBtn.setOnClickListener {

            val rawName = binding.userNameEnter.text.toString()
            val cleanName = rawName.cleanInvisibleV2()

            if(cleanName.isEmpty() || cleanName.length < 3) {
                Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val gender = binding.genderDropdown.text.toString().trim()
            val weight = binding.weightDropdown.text.toString().replace(" kg", "").trim()
            val height = binding.heightDropdown.text.toString().replace(" cm", "").trim()

            if (byteArray == null) {
                Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imgString = Base64.encodeToString(byteArray,Base64.DEFAULT)

            if (gender.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty()) {
                val sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)
                sharedPref.edit {
                    putString("userImg", imgString)
                    putString("userName", cleanName)
                    putString("userGender", gender)
                    putString("userWeight", weight)
                    putString("userHeight", height)
                    putBoolean("isUserSetupDone", true)
                    apply()
                }

                val intent = Intent(this, FitByKitNav::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private val ImageUploading = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

            rc -> if(rc.resultCode == RESULT_OK){
        val uri = rc.data!!.data
        try {
            val inputStream = contentResolver.openInputStream(uri!!)
            if(inputStream!=null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
                byteArray = byteArrayOutputStream.toByteArray()
                if(byteArray!=null) {
                    if (byteArray!!.size / 1024 < 2048) {
                        binding.userImg.setImageBitmap(bitmap)
                        inputStream.close()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please choose image below 2MB",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }else{
                Toast.makeText(this, "Input Stream Null", Toast.LENGTH_SHORT).show()
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
    }

    override fun onStart() {
        super.onStart()
        Toast.makeText(this, "Click To Add Image", Toast.LENGTH_SHORT).show()
    }
}

fun String.cleanInvisibleV2(trimResult: Boolean = true): String {
    val sb = StringBuilder(this.length)
    var lastCharWasWhitespace = false

    for (char in this) {
        if (Character.isISOControl(char) || char == '\u200B' || char == '\u200D' || char == '\u2060') {
            continue
        }

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

    if (!trimResult) {
        return sb.toString()
    }

    if (sb.isNotEmpty() && sb[0] == ' ') {
        sb.deleteCharAt(0)
    }

    if (sb.isNotEmpty() && sb[sb.length - 1] == ' ') {
        sb.deleteCharAt(sb.length - 1)
    }
    return sb.toString()
}
