package org.elsys.diploma

import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.google_maps_try.R
import com.google.firebase.auth.FirebaseAuth


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registerorig)


        val signIn: TextView = findViewById(R.id.textView)
        val emailId: EditText = findViewById(R.id.editText)
        val password:EditText = findViewById(R.id.editText2)
        val btnSignUp: Button = findViewById(R.id.button2)
        val confirmPassword : EditText = findViewById(R.id.confirmPassword)

        signIn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
        }

        btnSignUp.setOnClickListener {

                val email = emailId.text.toString()
                val pwd: String = password.text.toString()
                val pwdConfirm : String = confirmPassword.text.toString()

                if (email.isEmpty()) {
                    emailId.error = "Please enter email id"
                    emailId.requestFocus()
                } else if (pwd.isEmpty()) {
                    password.error = "Please enter your password"
                    password.requestFocus()
                } else if (email.isEmpty() && pwd.isEmpty()) {
                    Toast.makeText(this, "Fields Are Empty!", Toast.LENGTH_SHORT).show()
                }else if(pwd != pwdConfirm) {
                    Toast.makeText(this, "The password fields must be the same!", Toast.LENGTH_SHORT).show()
                }else if (!(email.isEmpty() && pwd.isEmpty())) {
                    (application as MyApplication).apiService.register(email,pwd){
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