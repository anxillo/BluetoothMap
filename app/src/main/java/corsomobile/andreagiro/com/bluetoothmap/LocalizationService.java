package corsomobile.andreagiro.com.bluetoothmap;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by andrea on 31.12.15.
 */
public class LocalizationService extends Service implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {


    /**
     * Variariabili e costanti
     */
    private static final int LOCALIZATION_SERVICE_SCAN_INTERVAL = 30000;
    private Handler handleJSON;

    DevicesDBOpenHelper db;
    Boolean localizationIsRunning;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationrequest;
    private BluetoothAdapter btAdapter  = BluetoothAdapter.getDefaultAdapter();
    private Location currentLocation;
    private String currentToponym;




    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                saveDataToDb(device);

            }
        }
    };


    @Override
    public void onCreate() {
        /**
         * Database
         */
        db = new DevicesDBOpenHelper(this);


        /**
         * Localization
         */

        // Creo istanza google api client
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Creo la location request e setto parametri
        mLocationrequest = new LocationRequest();
        mLocationrequest.setInterval(LOCALIZATION_SERVICE_SCAN_INTERVAL);
        mLocationrequest.setFastestInterval(LOCALIZATION_SERVICE_SCAN_INTERVAL);
        mLocationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        /**
         * bluetooth
         */
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        /**
         * handler JSON
         */
        handleJSON = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                //currentToponym = b.getString("toponym", "Toponym not set");
                currentToponym = "RUNNED";
            }
        };

        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            updateLocation(mLastLocation);
        }
        startUpdates();
        startBtScan();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location);
        startBtScan();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected() == false) {
            mGoogleApiClient.connect();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        stopUpdates();
        super.onDestroy();
    }

    void stopUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);


    }

    void startUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationrequest, this);
        
    }

    void startBtScan() {
        btAdapter.startDiscovery();
    }




    private void saveDataToDb(BluetoothDevice device) {
        String nome = device.getName();
        String mac = device.getAddress();
        Long time = System.currentTimeMillis() / 1000;


        // se non esiste creo una nuova entry nella tabella device
        if(!db.deviceExist(mac)) {
            db.insertDevice(nome, mac);
        }

        // inserisco il dato nella tabella geo
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            db.insertEntry(mac, latitude, longitude, currentToponym, time);
        }

        // broadcast event per l'aggiornamento delle interfacce
        Intent newLocIntent = new Intent(Statics.EVENT_ACTION_NEW_BLUETOOTH_DEVICE);
        sendBroadcast(newLocIntent);

    }

    private void updateLocation (Location location) {
        currentLocation = location;
        updateToponym(location.getLatitude(), location.getLongitude());
    }

    private void updateToponym (double latitude, double longitude) {
        String code = "lat=" + latitude + "&lng=" + longitude;
        Intent intent = new Intent(this, JSONToponymService.class);
        intent.putExtra("code", code );
        intent.putExtra("handler", new Messenger(handleJSON));
        startService(intent);
    }
}