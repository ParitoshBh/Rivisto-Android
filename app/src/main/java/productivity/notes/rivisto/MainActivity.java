package productivity.notes.rivisto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

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

        if(sharedPref.getBoolean(getString(R.string.isConfigured), false)){
            initFirebase(
                    sharedPref.getString(getString(R.string.apiKey), null),
                    sharedPref.getString(getString(R.string.messagingSenderID), null),
                    sharedPref.getString(getString(R.string.databaseURL), null)
            );

            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new NotesFragment())
                    .commit();
        } else {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new ConfigureFragment())
                    .commit();
        }

    }

    public void initFirebase(String apiKey, String messagingID, String databaseURL) {
        if (FirebaseApp.getApps(this).isEmpty()){
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(messagingID)
                    .setDatabaseUrl(databaseURL)
                    .build();
            firebaseApp = FirebaseApp.initializeApp(this, options, "Firebase");
        } else {
            firebaseApp = FirebaseApp.getInstance("Firebase");
        }
        firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
    }

    public FirebaseDatabase getFirebaseDatabase(){
        return firebaseDatabase;
    }
}
