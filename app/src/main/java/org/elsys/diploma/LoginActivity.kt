package org.elsys.diploma

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val emailId: EditText = findViewById(R.id.editText)
        val password: EditText = findViewById(R.id.editText2)
        val btnSignIn: Button = findViewById(R.id.button2)
        val tvSignUp: TextView = findViewById(R.id.textView)

        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            finish()
            startActivity(intent)
        }


        btnSignIn.setOnClickListener {

            val email: String = emailId.text.toString()
            val pwd: String = password.text.toString()

            val emailPattern = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,12}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{1,8}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{1,6}" +
                        ")+"
            )

            if (email.isEmpty()) {
                emailId.error = "Please enter email id"
                emailId.requestFocus()
            } else if (pwd.isEmpty()) {
                password.error = "Please enter your password"
                password.requestFocus()
            } else if (!emailPattern.matcher(email).matches()) {
                emailId.error = "Please enter valid email"
                emailId.requestFocus()
            } else if (!(email.isEmpty() && pwd.isEmpty()) && emailPattern.matcher(email)
                    .matches()
            ) {
                (application as MyApplication).apiService.login(email, pwd) {
                    if (!it) {
                        Toast.makeText(
                            this,
                            "Login Error, Please Login Again",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Error Occurred!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if ((application as MyApplication).apiService.currentUser() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }


}