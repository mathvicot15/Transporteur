package e.formation.transporteur;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Conduire extends AppCompatActivity {

    public static String telSiege = "+33769871037";
    public static String telConducteur1 = "+33769871037";
    public static String telConducteur2 = "+33769871037";

    public static Double latitude;
    public static Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conduire);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    CountDownTimer cTimer = null;


    public void startTimer(View view) {

        if(cTimer!=null) {
            cTimer.cancel();
        }

        cTimer = new CountDownTimer(30000, 1000) {

            TextView texte = (TextView) findViewById(R.id.txt_1);

            @Override
            public void onTick(long millisUntilFinished) {

                long valeur = millisUntilFinished/1000;
                texte.setText(String.valueOf(millisUntilFinished / 1000));

                if(valeur == 20){

                    cTimer.cancel();
                    TextView texte = (TextView) findViewById(R.id.txt_1);
                    texte.setText("Arret du camion");

                    //Message au conducteur
                    Toast.makeText(getApplicationContext(), "Il faut s'arreter", Toast.LENGTH_LONG).show();

                    SmsManager smsManager = SmsManager.getDefault();

                    //Message au siège
                    smsManager.sendTextMessage(telSiege, null, "Le chauffeur s'est arrêté", null, null);
                    //Message au conducteur2
                    envoiCoordonnees();
                }
            }

            @Override
            public void onFinish() {

                texte.setText("Done");
            }
        }.start();

    }

    @SuppressWarnings("MissingPermission")
    public void envoiCoordonnees(){

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location == null){
            Toast.makeText(this,"Localisation introuvable",Toast.LENGTH_LONG).show();
        }
        else{
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            SmsManager smsManager = SmsManager.getDefault();

            smsManager.sendTextMessage(telConducteur2, null, "Coordonnées:"+String.valueOf(latitude)+","+String.valueOf(longitude), null, null);

            Toast.makeText(getApplicationContext(), "Coordonnées envoyées !", Toast.LENGTH_LONG).show();
        }
    }

    public void cancelTimer(View view) {
        if(cTimer!=null)
            cTimer.cancel();
        TextView texte = (TextView) findViewById(R.id.txt_1);
        texte.setText("30");
    }
}
