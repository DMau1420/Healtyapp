package com.mau.healtyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private lateinit var registrar : TextView
    private lateinit var email : EditText
    private lateinit var password : EditText
    private lateinit var verpass : ImageView
    private lateinit var entrar : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email = findViewById(R.id.Email)
        password = findViewById(R.id.Password)

        registrar = findViewById(R.id.Registrarse)
        registrar.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }//fin del boton registrar


        verpass = findViewById(R.id.verpas)
        verpass.setOnClickListener {
            if (password.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT) {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            password.setSelection(password.text.length)
        }//fin de ocultar contraseña

        entrar = findViewById(R.id.Entrar)

        entrar.setOnClickListener {
            val email = email.text.toString().trim()
            val password = password.text.toString().trim()

            if (validateInputs(email, password)) {
                verificar(email, password) { success, uid, tipo ->
                    if (success && uid != null && tipo != null) {
                        if (tipo == "paciente") {
                            val intent = Intent(this, reloj::class.java).apply {
                            putExtra("uid",uid)
                            }
                            startActivity(intent)
                            finish()
                        }
                        else {
                            val intent = Intent(this, Admin_pacientes::class.java).apply {
                                putExtra("uid",uid)
                            }
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun validateInputs(eemail: String, ppassword: String): Boolean {
        var isValid = true

        if (eemail.isEmpty()) {
            email.error = "Ingrese su correo"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(eemail).matches()) {
            email.error = "Correo no válido"
            isValid = false
        }
        if (ppassword.isEmpty()) {
            password.error = "Ingrese su contraseña"
            isValid = false
        } else if (ppassword.length < 6) {
            password.error = "Mínimo 6 caracteres"
            isValid = false
        }

        return isValid
    }//fin del metodo validar inputs


    private fun verificar(
        email: String,
        password: String,
        callback: (Boolean, String?, String?) -> Unit  // (success, uid, tipo)
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://162.243.81.73/login.php?correo=$email&contra=$password")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                val correo = jsonObject.getString("correo")
                val contrasena = jsonObject.getString("contra")
                val uid = jsonObject.getString("uid")
                val tipo = jsonObject.getString("tipo")

                runOnUiThread {
                    callback(email == correo && password == contrasena, uid, tipo)
                }
            }
            catch (e: Exception) {
                Log.e("LoginError", "Error de conexión", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    callback(false, null, null)
                }
            }
        }
    }//fin del metodo verificar

}