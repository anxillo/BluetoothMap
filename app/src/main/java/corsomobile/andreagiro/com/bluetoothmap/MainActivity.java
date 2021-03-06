package corsomobile.andreagiro.com.bluetoothmap;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int ENABLE_BT = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private  BluetoothAdapter btAdapter  = BluetoothAdapter.getDefaultAdapter();
    private DevicesDBOpenHelper devicesDBOpenHelper;
    private ListView listView;
    private IntentFilter filter = new IntentFilter(Statics.EVENT_ACTION_NEW_BLUETOOTH_DEVICE);

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String nome = intent.getStringExtra("nome");
            Toast.makeText(context, "Nuovi dati: " + nome, Toast.LENGTH_SHORT).show();
            refreshUi();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Controllo e start bluetooth
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT);
        } else {
            startBluetoothScanService();
        }

        //UI
        listView = (ListView) findViewById(R.id.listView);
        refreshUi();

    }


    public boolean onCreateOptionsMenu (Menu menu) {
        MenuItem viewUserInfo = menu.add(Menu.NONE, 1, Menu.NONE, "Informazioni utente");
        viewUserInfo.setIcon(R.drawable.ic_timer_auto_24dp).setShowAsAction(1);
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Intent intent = new Intent().setClass(this, UserDataActivity.class);
                startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startBluetoothScanService();
            }
        }

    }


    @Override
    protected void onResume() {
        registerReceiver(receiver, filter);
        super.onResume();
    }


    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }


    void startBluetoothScanService  () {
        Intent intent = new Intent(this, LocalizationService.class);
        startService(intent);
    }


    void refreshUi () {
        devicesDBOpenHelper = new DevicesDBOpenHelper(this);
        Cursor c = devicesDBOpenHelper.listAllDevices();
        if (c.getCount() != 0) {
            DevicesCursorAdapter adapter = new DevicesCursorAdapter(this, c, true);
            listView.setAdapter(adapter);


        }
    }
}
