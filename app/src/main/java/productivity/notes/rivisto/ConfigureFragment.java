package productivity.notes.rivisto;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class ConfigureFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_NEW_ACCOUNT = "AUTHENTICATION";
    private static final int QR_CODE_ACTIVITY_CODE = 2;
    private EditText apiKey, messagingSenderID, databaseURL;
    private Button configureRivisto, easyConnect, createAccount;
    private FirebaseApp firebaseApp;

    public ConfigureFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_configure, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));

        apiKey = (EditText) view.findViewById(R.id.configureAPIKey);
        messagingSenderID = (EditText) view.findViewById(R.id.configureMessagingSenderID);
        databaseURL = (EditText) view.findViewById(R.id.configureDatabaseURL);

        configureRivisto = (Button) view.findViewById(R.id.configureRivisto);
        easyConnect = (Button) view.findViewById(R.id.easyConnect);
        createAccount = (Button) view.findViewById(R.id.createAccount);

        configureRivisto.setOnClickListener(this);
        easyConnect.setOnClickListener(this);
        createAccount.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onClick(View view) {
        int buttonID = view.getId();

        switch (buttonID){
            case R.id.configureRivisto:
                String key = apiKey.getText().toString();
                String senderID = messagingSenderID.getText().toString();
                String url = databaseURL.getText().toString();

                saveCredentialsAndInitFirebase(key, senderID, url, null, false);
                break;
            case R.id.easyConnect:
                Intent intent = new Intent(getActivity(), QRCodeReaderActivity.class);
                startActivityForResult(intent, QR_CODE_ACTIVITY_CODE);
                break;
            case R.id.createAccount:
                createNewAccount();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == QR_CODE_ACTIVITY_CODE) {
//            Log.i("Activity Result", "Request code confirmed");
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
//                Log.i("Activity Result", "Result code confirmed");
                saveCredentialsAndInitFirebase(
                        data.getStringExtra("apiKey"),
                        data.getStringExtra("messagingSenderID"),
                        data.getStringExtra("databaseURL"),
                        null,
                        false
                );
            }
        } else if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
                Log.i(LOG_NEW_ACCOUNT, "User Signed In");
                String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Log.i(LOG_NEW_ACCOUNT, userKey);
                saveCredentialsAndInitFirebase(null, null, null, userKey, true);
            } else {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a message
                Log.i(LOG_NEW_ACCOUNT, "User Could Not Sign In");
            }
        }
    }

    private void saveCredentialsAndInitFirebase(String key, String senderID, String url, String userKey, Boolean accountHolder){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("FirebaseCredentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(getString(R.string.isConfigured), true);
        editor.putBoolean(getString(R.string.isAccountHolder), accountHolder);

        if (accountHolder){
            editor.putString(getString(R.string.userKey), userKey);
        } else {
            editor.putString(getString(R.string.apiKey), key);
            editor.putString(getString(R.string.messagingSenderID), senderID);
            editor.putString(getString(R.string.databaseURL), url);
        }

        editor.commit();

        ((MainActivity) getActivity()).initFirebase(key, senderID, url, accountHolder);

        ((MainActivity) getActivity()).openNotesFragment(userKey, accountHolder);
    }

    private void createNewAccount(){
        firebaseApp = FirebaseApp.initializeApp(getActivity());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Log.i(LOG_NEW_ACCOUNT, "Signed In");
            // already signed in
        } else {
            // not signed in
            Log.i(LOG_NEW_ACCOUNT, "Not Signed In");
            startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setTheme(R.style.FirebaseUITheme)
                            .build(),
                    RC_SIGN_IN);
        }
    }

}
