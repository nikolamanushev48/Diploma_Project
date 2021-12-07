package com.example.google_maps_try

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginorig)
        println("LOGIN!!")

        val mAuthStateListener: AuthStateListener
        val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance();
        val emailId: EditText = findViewById(R.id.editText);
        val password: EditText = findViewById(R.id.editText2);
        val btnSignIn: Button = findViewById(R.id.button2);
        val tvSignUp: TextView = findViewById(R.id.textView);

        tvSignUp.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
        }


        mAuthStateListener = AuthStateListener {
            val mFirebaseUser: FirebaseUser? = mFirebaseAuth.getCurrentUser()
            if (mFirebaseUser != null) {
                Toast.makeText(this, "You are logged in", Toast.LENGTH_SHORT).show()
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
            } else {
                Toast.makeText(this, "Please Login", Toast.LENGTH_SHORT).show()
            }
        }

        btnSignIn.setOnClickListener(){

                val email: String = emailId.getText().toString()
                val pwd: String = password.getText().toString()
                if (email.isEmpty()) {
                    emailId.setError("Please enter email id")
                    emailId.requestFocus()
                } else if (pwd.isEmpty()) {
                    password.setError("Please enter your password")
                    password.requestFocus()
                } else if (email.isEmpty() && pwd.isEmpty()) {
                    Toast.makeText(this, "Fields Are Empty!", Toast.LENGTH_SHORT)
                        .show()
                } else if (!(email.isEmpty() && pwd.isEmpty())) {
                    mFirebaseAuth.signInWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(this,
                            OnCompleteListener<AuthResult?> { task ->
                                if (!task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Login Error, Please Login Again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val intToHome = Intent(this, MainActivity::class.java)
                                    startActivity(intToHome)
                                }
                            })
                } else {
                    Toast.makeText(this, "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}