package corsomobile.andreagiro.com.bluetoothmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UserDataActivity extends AppCompatActivity {

    private static String PREFERENCE_FILE = "corsomobile.andreagiro.com.bluetoothmap.PREFERENCE_FILE";

    private Button b_salva;
    private Button b_cancella;
    private Button b_esci;

    private EditText et_nome;
    private EditText et_cognome;
    private EditText et_email;

    private TextView tw_UID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        /**
         *  UI
         */
        et_nome = (EditText) findViewById(R.id.et_nome);
        et_cognome = (EditText) findViewById(R.id.et_cognome);
        et_email = (EditText) findViewById(R.id.et_email);

        b_esci = (Button) findViewById(R.id.b_esci);
        b_cancella = (Button) findViewById(R.id.b_cancella);
        b_salva = (Button) findViewById(R.id.b_salva);

        tw_UID = (TextView) findViewById(R.id.tw_UID);

        b_cancella.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_nome.setText("");
                et_cognome.setText("");
                et_email.setText("");
                tw_UID.setText("");
            }
        });

        b_esci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        b_salva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();

            }
        });

        readUserData();

    }

    /**
     * readUserData:
     * legge i dati utente dalle shared se esistenti e li inserisce.
     */
    public void readUserData() {
        SharedPreferences sharedUserData = getApplicationContext()
                .getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        String nome = sharedUserData.getString("nome", "");
        String cognome = sharedUserData.getString("cognome", "");
        String email = sharedUserData.getString("email", "");
        String UID = sharedUserData.getString("UID", "");

        et_nome.setText(nome);
        et_cognome.setText(cognome);
        et_email.setText(email);
        tw_UID.setText(UID);
    }

    /**
     * saveUserData:
     * Salva i dati utente nelle shared preferences
     */
    public void saveUserData() {
        String nome = et_nome.getText().toString();
        String cognome = et_cognome.getText().toString();
        String email = et_email.getText().toString();
        String UID = tw_UID.getText().toString();

        // controllo i dati
        if (TextUtils.isEmpty(nome)) {
            et_nome.setError("Non può essere vuoto!");
            return;
        }

        if (TextUtils.isEmpty(cognome)) {
            et_cognome.setError("Non può essere vuoto!");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            et_email.setError("Non può essere vuoto!");
            return;
        }


        // Se UID non esiste, ne crea uno
        if (TextUtils.isEmpty(UID)) {
            long ts = System.currentTimeMillis() / 1000;
            UID = nome + "." + cognome + "." + email + "-" + String.valueOf(ts);
        }

        //scrivo i dati
        SharedPreferences sharedUserData = getApplicationContext()
                .getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedUserData.edit();
        editor.putString("nome", nome);
        editor.putString("cognome", cognome);
        editor.putString("email", email);
        editor.putString("UID", UID);
        editor.commit();

        //esco
        finish();

    }



}
