package com.sakeena.easemovieapp

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private val CAMERA_REQ = 100
    private val GALLERY_REQ = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImage = findViewById(R.id.profileImage)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        btnSave = findViewById(R.id.btnSave)

        val cameraBtn = findViewById<ImageView>(R.id.btnCamera)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val imgPrefs = getSharedPreferences("profile", MODE_PRIVATE)
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Load data from Firebase or SharedPreferences
        val initialName = currentUser?.displayName ?: prefs.getString("name", "") ?: ""
        val initialEmail = currentUser?.email ?: prefs.getString("email", "") ?: ""
        
        etName.setText(initialName)
        etEmail.setText(initialEmail)
        etPhone.setText(prefs.getString("phone", ""))

        // Email should not be editable as requested
        etEmail.isEnabled = false
        etEmail.alpha = 0.7f // Visual feedback that it's disabled

        val savedImage = currentUser?.photoUrl?.toString() ?: imgPrefs.getString("image", null)
        if (!savedImage.isNullOrEmpty()) {
            try {
                // Using Glide or direct loading? The previous code used profileImage.setImageURI(uri)
                // Let's stick to the consistent URI loading
                val uri = Uri.parse(savedImage)
                profileImage.setImageURI(uri)
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.ic_person_24)
            }
        }

        // 🔥 Image click → dialog
        cameraBtn.setOnClickListener {
            showImageDialog()
        }

        // 🔙 Back
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        profileImage.setOnClickListener {
            val uri = getImageUri()
            if (!uri.isNullOrEmpty()) {
                val intent = Intent(this, FullScreenImageActivity::class.java)
                intent.putExtra("imageUri", uri)
                startActivity(intent)
            }
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )

        // 💾 Save
        btnSave.setOnClickListener {

            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Enter name"
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                etPhone.error = "Enter phone"
                return@setOnClickListener
            }

            // Update Firebase Display Name if user is logged in
            currentUser?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates)
            }

            prefs.edit()
                .putString("name", name)
                .putString("phone", phone)
                .apply()

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // 🔥 Dialog
    private fun showImageDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_image_options, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val viewPhoto = dialogView.findViewById<LinearLayout>(R.id.viewPhoto)
        val takePhoto = dialogView.findViewById<LinearLayout>(R.id.takePhoto)
        val uploadPhoto = dialogView.findViewById<LinearLayout>(R.id.uploadPhoto)
        val removePhoto = dialogView.findViewById<LinearLayout>(R.id.removePhoto)

        viewPhoto.setOnClickListener {

            val uri = getImageUri()

            if (!uri.isNullOrEmpty()) {
                val intent = Intent(this, FullScreenImageActivity::class.java)
                intent.putExtra("imageUri", uri)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No profile image found", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        takePhoto.setOnClickListener {
            checkCameraPermission()
            dialog.dismiss()
        }

        uploadPhoto.setOnClickListener {
            checkGalleryPermission() // ✅ FIXED
            dialog.dismiss()
        }

        removePhoto.setOnClickListener {
            profileImage.setImageResource(R.drawable.ic_person_24)
            saveImage("")
            dialog.dismiss()
        }
    }

    // ✅ Save URI
    private fun saveImage(uri: String) {
        val prefs = getSharedPreferences("profile", MODE_PRIVATE)
        prefs.edit().putString("image", uri).apply()
    }

    private fun getImageUri(): String? {
        val pref = getSharedPreferences("profile", MODE_PRIVATE)
        return pref.getString("image", null)
    }

    // 📸 Camera
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQ)
    }

    // 📂 Gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQ)
    }

    // 🔐 Camera Permission
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQ)
        } else {
            openCamera()
        }
    }

    // 🔐 Gallery Permission
    private fun checkGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), GALLERY_REQ)
        } else {
            openGallery()
        }
    }

    // 🔁 Permission Result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                CAMERA_REQ -> openCamera()
                GALLERY_REQ -> openGallery()
            }
        } else {
            Toast.makeText(this, "Permission Required!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }

    // 🔥 Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            if (requestCode == CAMERA_REQ) {
                val photo = data?.extras?.get("data") as Bitmap
                profileImage.setImageBitmap(photo)

                val uri = saveBitmapToStorage(photo)
                startCrop(uri)
                saveImage(uri.toString())

            }
            else if (requestCode == GALLERY_REQ) {
                val uri = data?.data ?: return
                startCrop(uri) // 🔥 crop open hoga

                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                profileImage.setImageBitmap(bitmap)

                val savedUri = saveBitmapToStorage(bitmap)
                saveImage(savedUri.toString())
            }
            if (requestCode == com.yalantis.ucrop.UCrop.REQUEST_CROP && resultCode == RESULT_OK) {

                val resultUri = com.yalantis.ucrop.UCrop.getOutput(data!!) ?: return

                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)
                profileImage.setImageBitmap(bitmap)

                val savedUri = saveBitmapToStorage(bitmap)
                saveImage(savedUri.toString())
                uploadImageToFirebase(savedUri)
            }
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")

        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show()

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build()

                    user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Profile photo updated in Firebase", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 💾 Save Bitmap
    private fun saveBitmapToStorage(bitmap: Bitmap): Uri {
        val filename = "profile_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProfileImages")
                }

                val resolver = contentResolver
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                fos = imageUri?.let { resolver.openOutputStream(it) }

            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "ProfileImages")

                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, filename)
                fos = FileOutputStream(file)
                imageUri = Uri.fromFile(file)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return imageUri!!
    }

    private fun startCrop(uri: Uri) {

        val destinationUri = Uri.fromFile(
            java.io.File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        )

        val options = com.yalantis.ucrop.UCrop.Options()
        options.setCircleDimmedLayer(true) // 🔥 Instagram circle crop
        options.setHideBottomControls(false)

        com.yalantis.ucrop.UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f) // square
            .withOptions(options)
            .start(this)
    }
}