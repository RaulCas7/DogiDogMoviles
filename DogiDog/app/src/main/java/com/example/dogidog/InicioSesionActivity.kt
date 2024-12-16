package com.example.dogidog

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import com.example.dogidog.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.logging.Logger
import kotlin.math.log

enum class ProviderType {
    GOOGLE
}
private val GOOGLE_SIGN_IN = 100
class InicioSesionActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")


        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email",email)
        prefs.putString("provider",provider)
        prefs.apply()

        binding.btnInicioGoogle.setOnClickListener{
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("107911147190948412698").requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this,googleConf)
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        binding.btnInicioSesiN.setOnClickListener{
            val intent = Intent(this,PantallaPrincipalActivity::class.java)
            startActivity(intent)
        }

        binding.btnMostrarContra.setOnClickListener {
            if (binding.edtContra.transformationMethod == PasswordTransformationMethod.getInstance()) {
                // Si la contraseña está oculta, mostrarla
                binding.edtContra.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.btnMostrarContra.setImageResource(R.drawable.visibility_24)
            } else {
                // Si la contraseña está visible, ocultarla
                binding.edtContra.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.btnMostrarContra.setImageResource(R.drawable.visibility_off_24)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            Logger("Email",account.email)
        }
    }
}