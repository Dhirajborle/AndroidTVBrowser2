package com.example.tvbrowser

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddBookmarkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bookmark)

        val titleEditText = findViewById<EditText>(R.id.title_edit_text)
        val urlEditText = findViewById<EditText>(R.id.url_edit_text)
        val saveButton = findViewById<Button>(R.id.save_button)

        // BACK BUTTON
        val backButton = findViewById<Button>(R.id.back_button)

        backButton.setOnClickListener {
            finish()
        }

        // SAVE BOOKMARK
        saveButton.setOnClickListener {

            val title = titleEditText.text.toString().trim()
            var url = urlEditText.text.toString().trim()

            if (title.isEmpty() || url.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please fill in both fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (!url.startsWith("http://") &&
                !url.startsWith("https://")) {

                url = "https://$url"
            }

            BookmarkStore.add(
                this,
                Bookmark(title, url)
            )

            Toast.makeText(
                this,
                "Bookmark saved",
                Toast.LENGTH_SHORT
            ).show()

            // Return to MainActivity
            finish()
        }
    }

    // Android TV remote BACK button
    override fun onBackPressed() {
        finish()
    }
}