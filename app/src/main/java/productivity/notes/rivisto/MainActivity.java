package productivity.notes.rivisto;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import productivity.notes.rivisto.configure.ConfigureFragment;

public class MainActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private FirebaseApp firebaseApp;
    private SharedPreferences sharedPref;
    private static final int PERMISSION_REQUEST_CAMERA = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getString(R.string.all_notes));

        sharedPref = this.getSharedPreferences("FirebaseCredentials", Context.MODE_PRIVATE);

        if (sharedPref.getBoolean(getString(R.string.isConfigured), false)) {
            if (sharedPref.getBoolean(getString(R.string.isAccountHolder), false)) {
                initFirebase(null, null, null, true);

                openNotesFragment(sharedPref.getString(getString(R.string.userKey), null), true);
            } else {
                initFirebase(
                        sharedPref.getString(getString(R.string.apiKey), null),
                        sharedPref.getString(getString(R.string.messagingSenderID), null),
                        sharedPref.getString(getString(R.string.databaseURL), null),
                        false
                );

                openNotesFragment(null, false);
            }
        } else {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ConfigureFragment(), "ConfigureFragment")
                    .commit();
        }

    }

    public void initFirebase(String apiKey, String messagingID, String databaseURL, Boolean isAccountHolder) {
        //Log.i("FirebaseApps", FirebaseApp.getApps(this).toString());

        if (isAccountHolder) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        } else {
            //Log.i("FirebaseInstance", FirebaseApp.getApps(this).size() + "");

            if (FirebaseApp.getApps(this).size() == 1){
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(messagingID)
                        .setDatabaseUrl(databaseURL)
                        .build();
                firebaseApp = FirebaseApp.initializeApp(this, options, "Firebase");
                firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
            } else {
                firebaseApp = FirebaseApp.getInstance("Firebase");
                firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
            }
        }
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public void openNotesFragment(String userKey, Boolean isAccountHolder) {
        NotesFragment notesFragment = new NotesFragment();

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.userKey), userKey);
        bundle.putBoolean(getString(R.string.isAccountHolder), isAccountHolder);
        notesFragment.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, notesFragment)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("CameraPermission", "Permission Result. Code -> " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    ((ConfigureFragment) getFragmentManager()
                            .findFragmentByTag("ConfigureFragment"))
                            .openQRCodeActivity();
                } else {
                    // permission denied, boo!
                    Toast.makeText(this, "Oops. Rivisto needs camera to scan QR code and setup automagically.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
