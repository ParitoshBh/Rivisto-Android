package productivity.notes.rivisto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private FirebaseApp firebaseApp;
    private SharedPreferences sharedPref;

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
                    .replace(R.id.fragment_container, new ConfigureFragment())
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

}
