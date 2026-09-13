package com.example.nintec;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    EditText cajau;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        cajau = this.findViewById(R.id.CAJAU);


        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }
    public void Jhosmar(View view) {

        Intent obj = new Intent(this, PaginaDos.class);
        String usuario = cajau.getText().toString();
        obj.putExtra("Usuario", usuario);
        this.startActivity(obj);
        this.finish();
    }
    //Creamos una clase publica
    public void JhosmarTres(View view) {
    //creamos un intent y le ponemos un nombre para poder llamar a la pagina siguiente
        Intent obj2 = new Intent(this, PantallaTres.class);
    //Inicializamos la pagina seleccionada con la variable
        this.startActivity(obj2);
    //Finalizamos el proceso
        this.finish();
    }
}