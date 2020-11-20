package org.iesch.alberto.firebasedam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnRegistro, btnLogin, btnGoogle, btnFacebook;

    //1- Nos declaramos el objeto FirebaseAnalytics en la parte superior
    private FirebaseAnalytics mFirebaseAnalytics;
    // 2 - FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        comprobarSiLogin();
    }

    private void comprobarSiLogin() {
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        String _email = preferences.getString("email","");
        String _provider = preferences.getString("provider","");
        if (_email != ""){
            irAHome(_email,_provider);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        carpinteria();
        //Analytics
        analizando();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        //HACEMOS CLICK EN REGISTRAR
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarse();
            }
        });

        //HACEMOS CLICK EN LOGIN
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loguearme();
            }
        });

    }

    private void loguearme() {
        final String _email = etEmail.getText().toString();
        final String _password = etPassword.getText().toString();
        //compruebo que email y pass no estén vacios
        if (!_email.isEmpty() && !_password.isEmpty() ) {
            mAuth.signInWithEmailAndPassword(_email, _password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "Usuario y contraseña CORRECTOS");
                                FirebaseUser user = mAuth.getCurrentUser();
                                //Grabamos el email con el que nos hemos logueado satisfactoriamente
                                SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
                                SharedPreferences.Editor Obj_Editor = preferences.edit();
                                Obj_Editor.putString("email",_email);
                                Obj_Editor.putString("password",_password);
                                Obj_Editor.putString("provider","USUARIO/CONTRASEÑA");
                                Obj_Editor.commit();
                                irAHome(_email, "USUARIO/CONTRASEÑA");
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Usuario y /o contraseña incorrectos.",
                                        Toast.LENGTH_LONG).show();
                                //updateUI(null);
                            }

                        }
                    });
        }
    }

    private void irAHome(String email, String provider) {
        Intent i = new Intent(LoginActivity.this,MainActivity.class);
        i.putExtra("email", email);
        i.putExtra("provider", provider);
        startActivity(i);
    }

    private void registrarse() {

        String _email = etEmail.getText().toString();
        String _password = etPassword.getText().toString();
        if (!_email.isEmpty() && !_password.isEmpty() ) {

            mAuth.createUserWithEmailAndPassword(_email, _password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "El usuario ha sido creado correctamente");
                                Toast.makeText(LoginActivity.this, "El usuario SE HA REGISTRADO CORRECTAMENTE",
                                        Toast.LENGTH_LONG).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Autenticacion FALLIDA",
                                        Toast.LENGTH_LONG).show();
                                //updateUI(null);
                            }

                        }
                    });
        }


    }

    private void carpinteria() {
        etEmail = findViewById(R.id.emailEditText);
        etPassword = findViewById(R.id.passwordEditText);
        btnLogin = findViewById(R.id.loginbutton);
        btnRegistro = findViewById(R.id.registerButton);
        btnGoogle = findViewById(R.id.buttonGoogle);
        btnFacebook = findViewById(R.id.buttonFacebook);
    }

    private void analizando() {
        // Obtenemos la instancia de FirebaseAnalytics.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString("Inicio","La aplicacion se ha cargado correctamente por Alberto R.");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }


}