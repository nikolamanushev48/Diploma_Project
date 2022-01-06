package com.example.google_maps_try

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
//import kotlinx.android.synthetic.main.activity_loginorig.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registerorig)
        println("REGISTER!!")

        val tvSignIn: TextView = findViewById(R.id.textView)
        val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val emailId: EditText = findViewById(R.id.editText)
        val password:EditText = findViewById<EditText>(R.id.editText2)
        val btnSignUp: Button = findViewById(R.id.button2)

        tvSignIn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
        }

        btnSignUp.setOnClickListener() {

                val email = emailId.text.toString()
                val pwd: String = password.text.toString()
                if (email.isEmpty()) {
                    emailId.error = "Please enter email id"
                    emailId.requestFocus()
                } else if (pwd.isEmpty()) {
                    password.error = "Please enter your password"
                    password.requestFocus()
                } else if (email.isEmpty() && pwd.isEmpty()) {
                    Toast.makeText(this, "Fields Are Empty!", Toast.LENGTH_SHORT).show()
                } else if (!(email.isEmpty() && pwd.isEmpty())) {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "SignUp Unsuccessful, Please Try Again",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                        }
                } else {
                    Toast.makeText(this, "Error Occurred!", Toast.LENGTH_SHORT).show()
                }

        }
    }


}