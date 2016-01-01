package corsomobile.andreagiro.com.bluetoothmap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by andrea on 31.12.15.
 */
public class DevicesCursorAdapter extends CursorAdapter {

    public DevicesCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.list_element, null);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ((TextView) view.findViewById(R.id.lw_tv_device)).setText(
                cursor.getString(cursor.getColumnIndex(DevicesDBOpenHelper.DEVICES_NAME)));

        ((TextView) view.findViewById(R.id.lw_tv_mac)).setText(
                cursor.getString(cursor.getColumnIndex(DevicesDBOpenHelper.DEVICES_MAC)));

        final RelativeLayout rw = (RelativeLayout) view.findViewById(R.id.cursor_layout);
        rw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent().setClass(context, MapActivity.class);
                String clickedMac = ((TextView) rw.findViewById(R.id.lw_tv_mac)).getText().toString();
                intent.putExtra("mac", clickedMac);
                context.startActivity(intent);

            }
        });

    }
}
