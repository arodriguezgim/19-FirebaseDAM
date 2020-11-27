package org.iesch.alberto.firebasedam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1234;
    EditText etEmail, etPassword;
    Button btnRegistro, btnLogin, btnGoogle, btnFacebook;

    //1- Nos declaramos el objeto FirebaseAnalytics en la parte superior
    private FirebaseAnalytics mFirebaseAnalytics;
    // 2 - FirebaseAuth
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClent;
    // Remote Config
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

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

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        carpinteria();
        //Analytics
        analizando();
        // Mensajes Push
        notification();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Configuramos Google Sign In
        createRequest();

        // Remote Config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        //Si la App no se puede conectar a internet añado la configuracion a mano
        mFirebaseRemoteConfig.setDefaultsAsync(Map.of("mostrar_boton_error" , false, "texto_boton_error", "Boton ERROR"));


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

        //HACEMOS CLICK EN GOOGLE
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    private void notification() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        Log.i("FIREBASE", "El token de mi movil es: "+token);
                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        //Log.d("TAG", msg);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Nos creaos un GoogleSignIn Client con las opciones especificas en gso
        mGoogleSignInClent = GoogleSignIn.getClient(this,gso);
    }
    private void signIn(){
        Intent i = mGoogleSignInClent.getSignInIntent();
        startActivityForResult(i, RC_SIGN_IN);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // El login en Google es correcto, ahora nos logueamos en Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this,"El Login en Google ha fallado", Toast.LENGTH_LONG).show();
            }
        }


    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            irAHome(user.getEmail(),"GOOGLE");
                        } else {
                            Toast.makeText(LoginActivity.this, "FALLO FALLO", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }
}