package e.formation.transporteur;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsListener extends BroadcastReceiver {

    private final String ACTION_RECEIVE_SMS  = "android.provider.Telephony.SMS_RECEIVED";
    private String latitude;
    private String longitude;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(ACTION_RECEIVE_SMS))
        {
            Bundle bundle = intent.getExtras();
            if (bundle != null)
            {
                Object[] pdus = (Object[]) bundle.get("pdus");

                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++)  {  messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);  }  if (messages.length > -1)
                {
                    final String messageBody = messages[0].getMessageBody();
                    final String phoneNumber = messages[0].getDisplayOriginatingAddress();

                    String[] type = messageBody.split(":");

                    if(phoneNumber.equals(Config.telConducteur1) && type[0].equals("Coordonnées")){
                        String[] latLng = type[1].split(",");
                        latitude = latLng[0];
                        longitude = latLng[1];

                        Intent carte = new Intent(context, Carte.class);
                        carte.putExtra("latitude", this.latitude);
                        carte.putExtra("longitude", this.longitude);
                        context.startActivity(carte);
                    }
                }
            }
        }
    }
}
