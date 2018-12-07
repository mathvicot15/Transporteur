package e.formation.transporteur;

import android.content.Intent;
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

                    Toast.makeText(getApplicationContext(), "Il faut s'arreter", Toast.LENGTH_LONG).show();

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("0769871037", null, "Le chauffeur s'est arrêté", null, null);
                }
            }

            @Override
            public void onFinish() {

                texte.setText("Done");
            }
        }.start();

    }

    public void cancelTimer(View view) {
        if(cTimer!=null)
            cTimer.cancel();
        TextView texte = (TextView) findViewById(R.id.txt_1);
        texte.setText("30");
    }
}
