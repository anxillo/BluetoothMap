package corsomobile.andreagiro.com.bluetoothmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.Locale;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private DevicesDBOpenHelper db;
    private String mac;
    private int zoom;
    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private IntentFilter filter = new IntentFilter(Statics.EVENT_ACTION_NEW_BLUETOOTH_DEVICE);
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(googleMap != null) {
                Toast.makeText(context, "Nuovi dati...", Toast.LENGTH_SHORT).show();
                refreshMarkers(googleMap);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        //prendo il mac adress dall intent
         mac = getIntent().getStringExtra("mac");
         zoom = 15;
        db = new DevicesDBOpenHelper(this);

        //zoom all'ultima posizione rilevata
        Cursor c = db.listAllLocations(mac);
        if(c.getCount() != 0) {
            c.moveToLast();
            LatLng lastPos = new LatLng(c.getDouble(c.getColumnIndex(DevicesDBOpenHelper.GEO_LAT)),
                    c.getDouble(c.getColumnIndex(DevicesDBOpenHelper.GEO_LONG)));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPos, zoom));
        }

        //leggo dal database e creo i marker
        refreshMarkers(googleMap);
    }


    @Override
    protected void onResume() {
        registerReceiver(receiver, filter);
        if(googleMap != null) {
            refreshMarkers(googleMap);
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }


    /**
     * GetDate: formatta il timestamp nel database in data leggibile
     * @param time timestamp
     * @return  data leggibile
     */
    private String getDate(long time) {
        time = time * 1000;
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("d MMM yyyy - HH:mm", cal).toString();
    }


    /**
     * refreshMarkers: fa un refresh dei markers
     * @param googleMap la mappa
     */
    private void refreshMarkers(GoogleMap googleMap) {
        googleMap.clear();
        db = new DevicesDBOpenHelper(this);
        Cursor c = db.listAllLocations(mac);
        if (c.getCount() != 0) {
            while (c.moveToNext()) {
                double latitude = c.getDouble(c.getColumnIndex(DevicesDBOpenHelper.GEO_LAT));
                double longitude = c.getDouble(c.getColumnIndex(DevicesDBOpenHelper.GEO_LONG));
                long time = c.getLong(c.getColumnIndex(DevicesDBOpenHelper.GEO_TIME));
                String toponym = c.getString(c.getColumnIndex(DevicesDBOpenHelper.GEO_TOPONYM));

                googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(c.getPosition() + ": " + toponym)
                                .snippet(getDate(time))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                );
            }
        }
    }
}
