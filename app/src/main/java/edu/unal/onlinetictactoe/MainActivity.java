package edu.unal.onlinetictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout player1Layout, player2Layout;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9;
    private TextView player1TV, player2TV;
    private final List<int[]> combinationsList = new ArrayList<>();
    private final List<String> doneBoxes = new ArrayList<>();
    private String playerUniqueId = "0";

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tictactoeonlinetest-fa7be-default-rtdb.firebaseio.com/");

    private boolean opponentFound = false;
    private String opponentUniqueId = "0";

    private String status = "matching";
    private String playerTurn = "";

    private String connectionId = "";

    ValueEventListener turnsEventListener, wonEvenetListener;

    private final String[] boxesSelectedBy = {"","","","","","","","",""};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player1Layout = findViewById(R.id.player1Layout);
        player2Layout = findViewById(R.id.player2Layout);






        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);

        player1TV = findViewById(R.id.player1TV);
        player2TV = findViewById(R.id.player2TV);

        //obteniendo el PlayerName de la clase PlayerName.class
        final String getPlayerName = getIntent().getStringExtra("playerName");
        combinationsList.add(new int[]{0,1,2});
        combinationsList.add(new int[]{3,4,5});
        combinationsList.add(new int[]{6,7,8});
        combinationsList.add(new int[]{0,3,6});
        combinationsList.add(new int[]{1,4,7});
        combinationsList.add(new int[]{2,5,8});
        combinationsList.add(new int[]{2,4,6});
        combinationsList.add(new int[]{0,4,8});

        // mostrar dialogo mientras se busca oponente
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Waiting for Opponent");
        progressDialog.show();

        //generar unique id del jugador.
        playerUniqueId = String.valueOf(System.currentTimeMillis());

        //configurar nombre de jugador en el textview
        player1TV.setText(getPlayerName);

        databaseReference.child("connections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // verificar si hay oponente o no, parabuscar por uno
                if(!opponentFound){
                    //comprobar si hay otros en la base de datos firebase
                    if(snapshot.hasChildren()){
                        //verificar todas las conexiones si otros usuarios estan esperando a jugar
                        for(DataSnapshot connections : snapshot.getChildren()){
                            //obteniendo id unico
                            String conId = connections.getKey();
                            //2 jugadores se requieren para el juego.
                            //si playercount es 1 significa que hay otro jugador esperando
                            //si no significa que esta conexion esta completa con dos jugadores
                            int getPlayerCount = (int)connections.getChildrenCount();

                            //despues de creado una nueva conexion se espera al otro a que entre
                            if(status.equals("waiting")){
                                //si es 2 significa que otro jugador se unió
                                if(getPlayerCount==2){

                                    playerTurn = playerUniqueId;
                                    applyPlayerTurn(playerTurn);


                                    // verdadero cuando hay un oponente encontrado para jugar
                                    boolean playerFound = false;
                                    //obtener jugadores en conexión
                                    for(DataSnapshot players : connections.getChildren()){
                                        String getPlayerUniqueId = players.getKey();
                                        //comprobar si el jugador coincide con quien creo la conexión (este usuario)
                                        if(getPlayerUniqueId.equals(playerUniqueId)){
                                            playerFound = true;
                                        }
                                        else if(playerFound){
                                            String getOpponentPlayerName = players.child("player_name").getValue(String.class);
                                            opponentUniqueId = players.getKey();

                                            //establecer el nombre del oponente en el textview
                                            player2TV.setText(getOpponentPlayerName);
                                            //asignar id de conexión
                                            connectionId = conId;
                                            opponentFound = true;

                                            //agregando listeners de ganador y de turnos
                                            databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                            databaseReference.child("won").child(connectionId).addValueEventListener(wonEvenetListener);

                                            //ocultar progreso dialogo si se muestra
                                            if(progressDialog.isShowing()){
                                                progressDialog.dismiss();
                                            }

                                            //una vez la conexión se ha hecho, remover el listener de conexión
                                            databaseReference.child("connections").removeEventListener(this);
                                        }
                                    }
                                }
                            }
                            //en caso el usuario no ha creado la conexión por que otros salones estan abiertos paraunirse.
                            else{
                                //verificar si la conexión tiene 1 jugador y necesita uno mas
                                if(getPlayerCount == 1){
                                    //añadir jugador a la conexión
                                    connections.child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);
                                    //obteniendo ambos jugadores
                                    for(DataSnapshot players : connections.getChildren()){
                                        String getOpponentName = players.child("player_name").getValue(String.class);
                                        opponentUniqueId = players.getKey();

                                        //primer turno sera de quien creo el room
                                        playerTurn = opponentUniqueId;
                                        applyPlayerTurn(playerTurn);

                                        //mostrar oponente o jugador en el textview
                                        player2TV.setText(getOpponentName);

                                        //asignar Id de Conexión
                                        connectionId = conId;
                                        opponentFound = true;

                                        //agregando listeners de ganador y de turnos
                                        databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                        databaseReference.child("won").child(connectionId).addValueEventListener(wonEvenetListener);


                                        //ocultar progreso dialogo si se muestra
                                        if(progressDialog.isShowing()){
                                            progressDialog.dismiss();
                                        }

                                        //una vez la conexión se ha hecho, remover el listener de conexión
                                        databaseReference.child("connections").removeEventListener(this);

                                        break;
                                    }
                                }
                            }
                        }
                        //revisar si el oponente no fue encontrado y usuario no esta esperando entonces crear una nueva conexión
                        if(!opponentFound && !status.equals("waiting")){

                            //geneerar un unico id para la conexión
                            String connectionUniqueId = String.valueOf(System.currentTimeMillis());
                            //añadir el primer jugar a la conexion y esperar qel otro para completar la conexion y jugar
                            snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);
                            status = "waiting";
                        }
                    }
                    //si no hya conexion disponible en la base de datos crear nueva conexxion
                    // es como crear una sala y esperar otros jugadores.
                    else{
                        //geneerar un unico id para la conexión
                        String connectionUniqueId = String.valueOf(System.currentTimeMillis());
                        //añadir el primer jugar a la conexion y esperar qel otro para completar la conexion y jugar
                        snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                        status = "waiting";

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        turnsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //tomar todos los turnos de la conexión
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.getChildrenCount() == 2){
                        //obteniendo la posicion de la caja seleccionada por el usuario.
                        final int getBoxPosition = Integer.parseInt(dataSnapshot.child("box_position").getValue(String.class));
                        //obtener el Id del jugador que selecciono la caja.
                        final String getPlayerId = dataSnapshot.child("player_id").getValue(String.class);
                        //verificar si el usuario no ha seleccionado la caja antes
                        if(!doneBoxes.contains(String.valueOf(getBoxPosition))){
                            doneBoxes.add(String.valueOf(getBoxPosition));
                            if(getBoxPosition == 1){
                                selectBox(image1,getBoxPosition,getPlayerId);

                            }
                            else if(getBoxPosition == 2){
                                selectBox(image2,getBoxPosition,getPlayerId);

                            }
                            else if(getBoxPosition == 3){
                                selectBox(image3,getBoxPosition,getPlayerId);

                            }
                            else if(getBoxPosition == 4){
                                selectBox(image4,getBoxPosition,getPlayerId);

                            }
                            else if(getBoxPosition == 5){
                                selectBox(image5,getBoxPosition,getPlayerId);

                            }
                            else if(getBoxPosition == 6){
                                selectBox(image6,getBoxPosition,getPlayerId);

                            }
                            else if(getBoxPosition == 7){
                                selectBox(image7,getBoxPosition,getPlayerId);

                            }
                            else if(getBoxPosition == 8){
                                selectBox(image8,getBoxPosition,getPlayerId);

                            }
                            else if(getBoxPosition == 9){
                                selectBox(image9,getBoxPosition,getPlayerId);

                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        wonEvenetListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // verificar si un usuario ha ganado la partida
                if(snapshot.hasChild("player_id")){
                    String getWinPlayerId = snapshot.child("player_id").getValue(String.class);
                    final WinDialog winDialog;
                    if(getWinPlayerId.equals(playerUniqueId)){
                        // mostrar dialogo de ganador
                        winDialog = new WinDialog( MainActivity.this, "You won the game");

                    }
                    else{
                        // mostrar dialogo de ganador
                        winDialog = new WinDialog( MainActivity.this, "Opponent won the game");
                    }
                    winDialog.setCancelable(false);
                    winDialog.show();

                    // remover listeners de la database

                    databaseReference.child("turns").child(connectionId).removeEventListener(turnsEventListener);
                    databaseReference.child("won").child(connectionId).removeEventListener(wonEvenetListener);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the box is not selected before
                if(!doneBoxes.contains("1") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_img);
                    // enviar la posicion de la caja y el id del usuario
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("1");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // cambiar de turno
                    playerTurn = opponentUniqueId;

                }
            }
        });
        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the box is not selected before
                if(!doneBoxes.contains("2") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_img);
                    // enviar la posicion de la caja y el id del usuario
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("2");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // cambiar de turno
                    playerTurn = opponentUniqueId;

                }
            }
        });
        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the box is not selected before
                if(!doneBoxes.contains("3") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_img);
                    // enviar la posicion de la caja y el id del usuario
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("3");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // cambiar de turno
                    playerTurn = opponentUniqueId;

                }
            }
        });
        image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the box is not selected before
                if(!doneBoxes.contains("4") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_img);
                    // enviar la posicion de la caja y el id del usuario
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("4");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // cambiar de turno
                    playerTurn = opponentUniqueId;

                }
            }
        });
        image5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the box is not selected before
                if(!doneBoxes.contains("5") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_img);
                    // enviar la posicion de la caja y el id del usuario
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("5");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // cambiar de turno
                    playerTurn = opponentUniqueId;

                }
            }
        });
        image6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the box is not selected before
                if(!doneBoxes.contains("6") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_img);
                    // enviar la posicion de la caja y el id del usuario
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("6");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // cambiar de turno
                    playerTurn = opponentUniqueId;

                }
            }
        });
        image7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the box is not selected before
                if(!doneBoxes.contains("7") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_img);
                    // enviar la posicion de la caja y el id del usuario
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() - 1)).child("box_position").setValue("7");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() - 1)).child("player_id").setValue(playerUniqueId);
                    // cambiar de turno
                    playerTurn = opponentUniqueId;

                }
            }
        });
        image8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the box is not selected before
                if(!doneBoxes.contains("8") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_img);
                    // enviar la posicion de la caja y el id del usuario
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() - 1)).child("box_position").setValue("8");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() - 1)).child("player_id").setValue(playerUniqueId);
                    // cambiar de turno
                    playerTurn = opponentUniqueId;

                }
            }
        });
        image9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the box is not selected before
                if(!doneBoxes.contains("9") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_img);
                    // enviar la posicion de la caja y el id del usuario
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() - 1)).child("box_position").setValue("9");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() - 1)).child("player_id").setValue(playerUniqueId);
                    // cambiar de turno
                    playerTurn = opponentUniqueId;

                }
            }
        });

    }

    private void applyPlayerTurn(String playerUniqueId2){
        if(playerUniqueId2.equals(playerUniqueId)){
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        }
        else{
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        }
    }

    private void selectBox(ImageView imageView, int selectedBoxPosition, String selectedByPlayer){
        boxesSelectedBy[selectedBoxPosition - 1] = selectedByPlayer;
        if(selectedByPlayer.equals(playerUniqueId)){
            imageView.setImageResource(R.drawable.x_img);
            playerTurn = opponentUniqueId;
        }
        else{
            imageView.setImageResource(R.drawable.o_img);
            playerTurn = playerUniqueId;
        }

        applyPlayerTurn(playerTurn);
        //verificar si el jugador gano la partida
        if(checkPlayerWin(selectedByPlayer)){

            //enviar el jugador ganador el unico id a la base de datos, de manera que el oponente pueda ser notificado
            databaseReference.child("won").child(connectionId).child("player_id").setValue(selectedByPlayer);

        }

        if(doneBoxes.size() == 9){
            final WinDialog winDialog = new WinDialog(MainActivity.this, "It is a tie!");
            winDialog.setCancelable(false);
            winDialog.show();


        }

    }

    private boolean checkPlayerWin(String playerId){
        boolean isPlayerWon = false;

        //compara los turnos de los jugadores en cada combinación de ganar
        for(int i=0; i<combinationsList.size(); i++){
            final int[] combination = combinationsList.get(i);
            // verificar los 3 ultimos turnos del usuario
            if(boxesSelectedBy[combination[0]].equals(playerId) &&
                boxesSelectedBy[combination[1]].equals(playerId) &&
                boxesSelectedBy[combination[2]].equals(playerId)){
                    isPlayerWon = true;
            }
        }

        return isPlayerWon;
    }
}