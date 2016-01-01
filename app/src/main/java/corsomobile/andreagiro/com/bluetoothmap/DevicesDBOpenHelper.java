package corsomobile.andreagiro.com.bluetoothmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DevicesDBOpenHelper extends SQLiteOpenHelper {

    /**
     * Variabili statiche
     */
    public static final String DATABASE_NAME = "devicadatabase1.db";
    public static final int DATABASE_VERSION = 1;

    // Tabella devices
    public static final String DEVICES_TABLE_NAME = "devices";
    public static final String DEVICES_ID_NUM = "_id";
    public static final String DEVICES_NAME = "nome";
    public static final String DEVICES_MAC = "mac";

    //Tabella riscontri
    public static final String GEO_TABLE_NAME = "geo";
    public static final String GEO_NUM_ID = "_id";
    public static final String GEO_DEV_MAC = "device_mac";
    public static final String GEO_LAT = "latitude";
    public static final String GEO_LONG = "longitude";
    public static final String GEO_TOPONYM = "toponymName";
    public static final String GEO_TIME = "time";


    /**
     * Queries
     */
    private static final String DEVICES_TABLE_CREATE =
            "CREATE TABLE " + DEVICES_TABLE_NAME + " (" + DEVICES_ID_NUM + " integer primary key autoincrement," +
                                  DEVICES_NAME + " TEXT," +
                                  DEVICES_MAC + " TEXT);";

    private static final String GEO_TABLE_CREATE =
            "CREATE TABLE " + GEO_TABLE_NAME + " (" + GEO_NUM_ID + " integer primary key autoincrement," +
                              GEO_DEV_MAC + " TEXT," +
                              GEO_LAT + " double," +
                              GEO_LONG + " double," +
                              GEO_TOPONYM + " TEXT," +
                              GEO_TIME + " integer);";

    private static final String LIST_DEVICES_QUERY =
            "select " + DEVICES_ID_NUM + ", " +
                    DEVICES_NAME + ", " +
                    DEVICES_MAC +
                    " from " + DEVICES_TABLE_NAME +
                    " order by " + DEVICES_ID_NUM + " DESC;";

    private static final String CHECK_DEVICE =
            "select " + DEVICES_MAC +
                    " from " + DEVICES_TABLE_NAME +
                    " where " + DEVICES_MAC +
                    " = ?";

    private static final String RETURN_MAC =
            "select " + DEVICES_MAC +
                    " from " + DEVICES_TABLE_NAME +
                    " where " + DEVICES_ID_NUM +
                    " = ? ";


    private static final String LIST_GEO_QUERY =
            "select " +
                    GEO_LAT + ", " +
                    GEO_LONG + ", " +
                    GEO_TOPONYM + ", " +
                    GEO_TIME +
                    " from " +
                    GEO_TABLE_NAME +
                    " where " + GEO_DEV_MAC +
                    " = ? order by " + GEO_TIME + " ASC";


    public DevicesDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DEVICES_TABLE_CREATE);
        db.execSQL(GEO_TABLE_CREATE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: inserire codice per upgrade database
    }

    /**
     * deviceExist
     * @param mac mac address o altro identificativo unico
     * @return true se gia nel database, false altrimenti
     */
    public boolean deviceExist (String mac) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(CHECK_DEVICE, new String[] {mac});
        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }
    }


    /*public String  getMac (int ID) {
        String ID_string = Integer.toString(ID);
        String mac = "";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(RETURN_MAC, new String[] {ID_string});
        while (cursor.moveToNext()) {
            mac = cursor.getString(cursor.getColumnIndex(DEVICES_MAC));
        }
        return mac;
    } */


    /**
     * insertDevice: inserisce un nuovo device nel database
     * @param nome nome del device
     * @param mac mac address o altro identificativo unico
     */
    public void insertDevice (String nome, String mac) {
        SQLiteDatabase db_w = getWritableDatabase();
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DEVICES_NAME, nome);
        dataToInsert.put(DEVICES_MAC, mac);
        db_w.insert(DEVICES_TABLE_NAME, null, dataToInsert);
        db_w.close();
    }

    /**
     * insertEntry: inserisce una nuova entry nella tabella geo
     * @param mac mac address
     * @param latitudine latitudine
     * @param longitudine longitudine
     * @param tempo timestamp
     */
    public void insertEntry (String mac, double latitudine, double longitudine, String toponym, long tempo ) {
        SQLiteDatabase db_w = getWritableDatabase();
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(GEO_DEV_MAC, mac);
        dataToInsert.put(GEO_LAT, latitudine);
        dataToInsert.put(GEO_LONG, longitudine);
        dataToInsert.put(GEO_TOPONYM, toponym);
        dataToInsert.put(GEO_TIME, tempo);
        db_w.insert(GEO_TABLE_NAME, null, dataToInsert);
        db_w.close();
    }


    /**
     * Cursore per listare tutti i device
     * @return il cursore
     */
    public Cursor listAllDevices () {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(LIST_DEVICES_QUERY, null);
    }


    /**
     * Cursore per listare tutte le entry di un singolo device
     * @param mac mac address
     * @return il cursore
     */
    public Cursor listAllLocations(String mac) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(LIST_GEO_QUERY, new String[] {mac});
    } 


}
