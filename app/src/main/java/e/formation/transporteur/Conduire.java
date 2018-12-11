package e.formation.transporteur;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

    private Double latitude;
    private Double longitude;
    public int tempsConduite = 10000;

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "Notification")
            .setSmallIcon(R.drawable.warning)
            .setContentTitle("Temps de conduite écoulé")
            .setContentText("Il faut s'arreter")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conduire);
        TextView texte = (TextView) findViewById(R.id.txt_1);
        texte.setText(String.valueOf(tempsConduite/1000));
        createNotificationChannel();
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

        TextView texte = (TextView) findViewById(R.id.txt_1);
        cTimer = new CountDownTimer(tempsConduite, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                long valeur = millisUntilFinished/1000;
                texte.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {

                TextView texte = (TextView) findViewById(R.id.txt_1);
                texte.setText("Appuyez sur 'Arret' pour demander un conducteur");

                //Message au conducteur
                Toast.makeText(getApplicationContext(), "Il faut s'arreter", Toast.LENGTH_LONG).show();
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(1, mBuilder.build());

                SmsManager smsManager = SmsManager.getDefault();

                //Message au siège
                smsManager.sendTextMessage(telSiege, null, "Le chauffeur doit s'arreter", null, null);
            }
        }.start();

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification";
            String description = "Channel général";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Notification", name, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
        texte.setText(String.valueOf(tempsConduite/1000));

        //Message au siège
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(telSiege, null, "Le chauffeur s'est arrêté", null, null);

        //Message au conducteur2
        envoiCoordonnees();
    }
}
