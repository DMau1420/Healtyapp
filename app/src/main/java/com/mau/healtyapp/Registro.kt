package com.mau.healtyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Registro : AppCompatActivity() {
    private lateinit var registrar : TextView
    private lateinit var tipousuario : Spinner
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confpass: EditText
    private lateinit var usname: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        //iniciospiner
        tipousuario = findViewById(R.id.spinnerTipoUsuario)

        val opciones = arrayOf("<Seleccionar>","Paciente", "Cuidador", "Independiente")
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_item, opciones) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0 // Desactiva la opción "<Seleccionar>"
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                if (position == 0) {
                    textView.setTextColor(android.graphics.Color.GRAY)
                } else {
                    textView.setTextColor(android.graphics.Color.BLACK)
                }
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.spinner_item)
        tipousuario.adapter = adapter
        //fin spinner

        email = findViewById(R.id.Email)
        password = findViewById(R.id.Password)
        confpass = findViewById(R.id.confPassword)
        usname  = findViewById(R.id.username)

        registrar = findViewById(R.id.Entrar)
        registrar.setOnClickListener {
            val e = email.text.toString().trim()
            val n = usname.text.toString().trim()
            val p = password.text.toString().trim()
            val cp = confpass.text.toString().trim()
            val t = tipousuario.selectedItem.toString().trim()

            if (validateInputs(e,n,p,cp,t)) {
                val u = generarUID6()

                registrarUser(e, n, p, u, t)//agregar nombre de usuario
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }


    }
    private fun validateInputs(eemail: String, name : String, ppassword: String, confirmPassword: String, tipous: String): Boolean {
        var isValid = true
        if (tipous == "<Seleccionar>"){
            Toast.makeText(this, "Seleccione un tipo de usuario", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (name.isEmpty()){
            usname.error = "Ingrese su nombre"
            isValid = false
        }

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

        if (confirmPassword.isEmpty()) {
            confpass.error = "Confirme su contraseña"
            isValid = false
        } else if (ppassword != confirmPassword) {
            confpass.error = "Las contraseñas no coinciden"
            isValid = false
        }
        return isValid
    }//fin validar inputs

    fun generarUID6(): String {
        val caracteres = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { caracteres.random() }
            .joinToString("")
    }//creacion del UID (user id)

    private fun registrarUser (correo: String, name: String, password: String, uid : String, tipo: String){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://162.243.81.73/registro.php")
                val params = "uid=$uid&username=$name&correo=$correo&contra=$password&tipo=$tipo"

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    outputStream.write(params.toByteArray())

                    val response = inputStream.bufferedReader().use { it.readText() }

                    runOnUiThread {
                        Toast.makeText(this@Registro, response, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@Registro, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}