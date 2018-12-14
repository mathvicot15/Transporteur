package e.formation.transporteur;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Config extends AppCompatActivity {

    public static String telSiege = "+33769871037";
    public static String telConducteur1 = "+33769871037";
    public static String telConducteur2 = "+33769871037";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        //Initialisation des numéros dans les champs de saisie
        TextView txtTelSiege = (TextView)findViewById(R.id.telSiege);
        TextView txtTelConducteur1 = (TextView)findViewById(R.id.telConducteur1);
        TextView txtTelConducteur2 = (TextView)findViewById(R.id.telConducteur2);

        txtTelSiege.setText(telSiege, TextView.BufferType.EDITABLE);
        txtTelConducteur1.setText(telConducteur1, TextView.BufferType.EDITABLE);
        txtTelConducteur2.setText(telConducteur2, TextView.BufferType.EDITABLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.conduire :
                Intent intent1 = new Intent(this,Conduire.class);
                this.startActivity(intent1);
                return true;
            case R.id.carte :
                Intent intent2 = new Intent(this, Carte.class);
                this.startActivity(intent2);
                return true;
            case R.id.config :
                Intent intent3 = new Intent(this, Config.class);
                this.startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void enregistreTels(View view){

        //Selection des zones de saisie
        TextView txtTelSiege = (TextView)findViewById(R.id.telSiege);
        TextView txtTelConducteur1 = (TextView)findViewById(R.id.telConducteur1);
        TextView txtTelConducteur2 = (TextView)findViewById(R.id.telConducteur2);

        //Selection du message d'erreur
        TextView txtMsgErreur = (TextView)findViewById(R.id.msgErreur);
        txtMsgErreur.setVisibility(View.GONE);

        //Récuperation des données dans les champs et traitement pour le format +33
        telSiege = txtTelSiege.getText().toString();
        String telSiegeFormat = telSiege.substring(0, 3);
        telConducteur1 = txtTelConducteur1.getText().toString();
        String telConducteur1Format = telSiege.substring(0, 3);
        telConducteur2 = txtTelConducteur2.getText().toString();
        String telConducteur2Format = telSiege.substring(0, 3);

        //Vérifie si les numéros saisis sont des numéros de telephone de france métropolitaine
        if(telSiege.length() != 10 && telSiege.length() != 12 ||
                telConducteur1.length() != 10 && telConducteur1.length() != 12 ||
                telConducteur2.length() != 10 && telConducteur2.length() != 12){
            txtMsgErreur.setVisibility(View.VISIBLE);
            Toast.makeText(this,"Erreur de saisie",Toast.LENGTH_LONG).show();
        }
        else{
            //Conversion des numéros en format international s'il ne le sont pas
            if(telSiegeFormat != "+33"){
                telSiege = telSiege.substring(1,9);
                telSiege = "+33"+telSiege;
            }
            if(telConducteur1Format != "+33"){
                telConducteur1 = telConducteur1.substring(1,9);
                telConducteur1 = "+33"+telConducteur1;
            }
            if(telConducteur2Format != "+33"){
                telConducteur2 = telConducteur2.substring(1,9);
                telConducteur2 = "+33"+telConducteur2;
            }

            //Enregistrement dans les shared preferencies
            SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor myEditor = myPreferences.edit();
            myEditor.putString("telSiege", telSiege);
            myEditor.putString("telConducteur1", telConducteur1);
            myEditor.putString("telConducteur2", telConducteur2);
            myEditor.commit();

            Toast.makeText(this,"Numéros enregistrés",Toast.LENGTH_LONG).show();
        }
    }
}
