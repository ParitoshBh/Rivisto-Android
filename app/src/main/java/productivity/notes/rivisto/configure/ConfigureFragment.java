package productivity.notes.rivisto.configure;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import productivity.notes.rivisto.MainActivity;
import productivity.notes.rivisto.R;
import productivity.notes.rivisto.utils.Helpers;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class ConfigureFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_NEW_ACCOUNT = "AUTHENTICATION";
    private static final String LOG_CAMERA_PERMISSION = "CameraPermission";
    private static final int QR_CODE_ACTIVITY_CODE = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 3;
    private Button automaticConfigure, managedAccount;
    private FirebaseApp firebaseApp;
    private View view;

    public ConfigureFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_configure, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));
//        applyFontToToolbarTitle(((MainActivity) getActivity()).getToolbar());

        setHasOptionsMenu(true);

        automaticConfigure = (Button) view.findViewById(R.id.automaticConfigure);
        managedAccount = (Button) view.findViewById(R.id.managedAccount);

        automaticConfigure.setOnClickListener(this);
        managedAccount.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_configure, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.action_manual_configure){
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ManualConfigFragment())
                    .addToBackStack("ManualConfigFragment")
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        String googlePlayServicesStatus = Helpers.isGooglePlayServicesAvailable(getActivity());

        if (googlePlayServicesStatus.equalsIgnoreCase("success")){
            int buttonID = view.getId();

            switch (buttonID) {
                case R.id.automaticConfigure:
                    boolean cameraPermission = checkCameraPermission();

                    if (cameraPermission) {
                        openQRCodeActivity();
                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(getActivity(), new String[]{
                                Manifest.permission.CAMERA
                        }, PERMISSION_REQUEST_CAMERA);
                    }
                    break;
                case R.id.managedAccount:
                    createNewAccount();
                    break;
            }
        } else {
            Snackbar.make(view, googlePlayServicesStatus, Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean checkCameraPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return false;
        }
    }

    public void openQRCodeActivity() {
        Intent intent = new Intent(getActivity(), QRCodeReaderActivity.class);
        startActivityForResult(intent, QR_CODE_ACTIVITY_CODE);
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
            } else if (resultCode == RESULT_CANCELED){
                Snackbar.make(view, "Unable to connect. Please try again.", Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
                //Log.i(LOG_NEW_ACCOUNT, "User Signed In");
                String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                //Log.i(LOG_NEW_ACCOUNT, userKey);
                saveCredentialsAndInitFirebase(null, null, null, userKey, true);
            } else {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a message
                //Log.i(LOG_NEW_ACCOUNT, "User Could Not Sign In");
                Snackbar.make(view, "Couldn't sign in. Please try again.", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void saveCredentialsAndInitFirebase(String key, String senderID, String url, String userKey, Boolean accountHolder) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("FirebaseCredentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(getString(R.string.isConfigured), true);
        editor.putBoolean(getString(R.string.isAccountHolder), accountHolder);

        if (accountHolder) {
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

    private void createNewAccount() {
        firebaseApp = FirebaseApp.initializeApp(getActivity());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            //Log.i(LOG_NEW_ACCOUNT, "Signed In");
            // already signed in
        } else {
            // not signed in
            //Log.i(LOG_NEW_ACCOUNT, "Not Signed In");
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

//    private void applyFontToToolbarTitle(Toolbar toolbar){
//        for(int i = 0; i < toolbar.getChildCount(); i++){
//            View view = toolbar.getChildAt(i);
//            if(view instanceof TextView){
//                TextView tv = (TextView) view;
//                Typeface titleFont = Typeface.
//                        createFromAsset(getActivity().getAssets(), "fonts/italianno_regular.otf");
////                if(tv.getText().equals(toolbar.getTitle())){
////                    tv.setTypeface(titleFont);
////                    break;
////                }
//                tv.setTypeface(titleFont);
//                tv.setTextSize(34);
//            }
//        }
//    }

}
