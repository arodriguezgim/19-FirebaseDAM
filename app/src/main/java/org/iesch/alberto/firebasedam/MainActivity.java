package org.iesch.alberto.firebasedam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView tvemail, tvprovider;
    EditText etDireccion, etTelefono;
    Button btnlogOut, btnError, btnGuardar, btnRecuperar,btnEliminar;
    // Remote Config
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    // Firebase Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        carpinteria();
        //Escribimos los nombres en sus campos
        Bundle datos = this.getIntent().getExtras();
        String email = datos.getString("email");
        String provider = datos.getString("provider");
        tvemail.setText(email);
        tvprovider.setText(provider);

        //Remote Config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        btnError.setVisibility(View.INVISIBLE);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            //Log.d("TAG", "Config params updated: " + updated);
                            Toast.makeText(MainActivity.this, "Configuracion Remota obtenida correctamente",
                                    Toast.LENGTH_SHORT).show();
                            boolean mostrarBoton = mFirebaseRemoteConfig.getBoolean("mostrar_boton_error");
                            String textoBoton = mFirebaseRemoteConfig.getString("texto_boton_error");
                            if ( mostrarBoton ){
                                btnError.setVisibility(View.VISIBLE);
                                btnError.setText(textoBoton);
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        //displayWelcomeMessage();
                    }
                });
        //Firebase Firestore
        db = FirebaseFirestore.getInstance();


        //CLICK EN RECUPERAR
        btnRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("usuarios")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        etDireccion.setText(document.getString("direccion"));
                                        etTelefono.setText(document.getString("telefono"));
                                    }
                                } else {
                                    Log.w("TAG", "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });


        //CLICK EN GUARDAR
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("email", email);
                usuario.put("provider", provider);
                usuario.put("direccion", etDireccion.getText().toString());
                usuario.put("telefono", etTelefono.getText().toString());

                db.collection("usuarios").document(email).set(usuario);
            }
        });
        //CLICK EN ELIMINAR
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //HACEMOS CLICK EN EL BOTON LOGOUT
        btnlogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                //Eliminamosla cuenta conla que nos hemos logueado
                SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
                SharedPreferences.Editor Obj_Editor = preferences.edit();
                Obj_Editor.putString("email","");
                Obj_Editor.putString("password","");
                Obj_Editor.putString("provider","");
                Obj_Editor.commit();
                onBackPressed();
            }
        });
    }

    private void carpinteria() {
        tvemail = findViewById(R.id.tvEmail);
        tvprovider = findViewById(R.id.tvProveedor);
        etDireccion = findViewById(R.id.tvDireccion);
        etTelefono = findViewById(R.id.tvTelefono);
        btnlogOut = findViewById(R.id.logOutButton);
        btnError = findViewById(R.id.errorButton);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnRecuperar = findViewById(R.id.btnRecuperar);
        btnEliminar = findViewById(R.id.btnBorrar);

    }
}