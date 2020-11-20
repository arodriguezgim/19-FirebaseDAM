package org.iesch.alberto.firebasedam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    TextView tvemail, tvprovider;
    Button btnlogOut;

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
        btnlogOut = findViewById(R.id.logOutButton);

    }
}