package edu.unal.onlinetictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);

        final EditText playerNameEt = findViewById(R.id.playerNameEt);
        final AppCompatButton startGameBtn = findViewById(R.id.startGameBtn);

        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //obteniendo el nombre del jugador del EditText
                final String getPlayerName = playerNameEt.getText().toString();

                //verificando que se haya introducido un nombre
                if(getPlayerName.isEmpty()){
                    Toast.makeText(PlayerName.this, "Please enter player name", Toast.LENGTH_SHORT).show();
                }
                else{
                    //creando intento de abrir mainactivity
                    Intent intent = new Intent(PlayerName.this, MainActivity.class);

                    //añaniendo el nombre de jugador con intent
                    intent.putExtra("playerName", getPlayerName);

                    //abriendo main activity
                    startActivity(intent);

                    //destruyendo this activity
                    finish();
                }
            }
        });
    }
}