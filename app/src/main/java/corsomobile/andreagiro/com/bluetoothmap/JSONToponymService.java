package corsomobile.andreagiro.com.bluetoothmap;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class JSONToponymService extends IntentService {

    private static final String TOPONYM_JSON_URL = "http://api.geonames.org/findNearbyJSON?username=supsi&";

    public JSONToponymService() {
        super("JSONToponymService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("JSON", "started");
        Bundle b = intent.getExtras();
        String code = b.getString("code");
        Messenger messenger = (Messenger)b.get("handler");
        String myUrl = TOPONYM_JSON_URL + code;
        Log.d("JSON url",myUrl );
        HttpURLConnection connection = null;
        InputStream is = null;
        String toponym = "";

        try {
            URL url = new URL(myUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return;
            }

            is = connection.getInputStream();

            JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("geonames")) {
                    reader.beginArray();
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String objName = reader.nextName();
                        if (objName.equals("toponymName")) {
                            toponym = reader.nextString();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                    reader.endArray();
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("toponym", toponym);
                    message.setData(bundle);
                    try {
                        if (messenger != null) {
                            messenger.send(message);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            }
            reader.endObject();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
