package org.elsys.diploma


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        val signIn: TextView = findViewById(R.id.textView)
        val emailId: EditText = findViewById(R.id.editText)
        val password: EditText = findViewById(R.id.editText2)
        val btnSignUp: Button = findViewById(R.id.button2)
        val confirmPassword: EditText = findViewById(R.id.confirmPassword)

        signIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }

        btnSignUp.setOnClickListener {

            val emailPattern = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,12}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{1,8}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{1,6}" +
                        ")+"
            )

            val email = emailId.text.toString()
            val pwd: String = password.text.toString()
            val pwdConfirm: String = confirmPassword.text.toString()

            if (email.isEmpty()) {
                emailId.error = "Please enter email id"
                emailId.requestFocus()
            } else if (pwd.isEmpty()) {
                password.error = "Please enter your password"
                password.requestFocus()
            } else if (!emailPattern.matcher(email).matches()) {
                emailId.error = "Please enter valid email"
                emailId.requestFocus()
            } else if (pwd != pwdConfirm) {
                confirmPassword.error = "Must match password"
                confirmPassword.requestFocus()
            } else if (!(email.isEmpty() && pwd.isEmpty())) {
                (application as MyApplication).apiService.register(email, pwd) {
                    if (!it) {
                        Toast.makeText(
                            this,
                            "SignUp Unsuccessful, Please Try Again",
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


}