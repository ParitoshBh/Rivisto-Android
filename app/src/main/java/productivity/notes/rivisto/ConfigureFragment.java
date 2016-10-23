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

import static android.app.Activity.RESULT_OK;

public class ConfigureFragment extends Fragment implements View.OnClickListener {
    private static final int QR_CODE_ACTIVITY_CODE = 2;
    private EditText apiKey, messagingSenderID, databaseURL;
    private Button configureRivisto, easyConnect;

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

        configureRivisto.setOnClickListener(this);
        easyConnect.setOnClickListener(this);

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

                saveCredentialsAndInitFirebase(key, senderID, url);
                break;
            case R.id.easyConnect:
                Intent intent = new Intent(getActivity(), QRCodeReaderActivity.class);
                startActivityForResult(intent, QR_CODE_ACTIVITY_CODE);
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
                        data.getStringExtra("databaseURL")
                );
            }
        }
    }

    private void saveCredentialsAndInitFirebase(String key, String senderID, String url){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("FirebaseCredentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.isConfigured), true);
        editor.putString(getString(R.string.apiKey), key);
        editor.putString(getString(R.string.messagingSenderID), senderID);
        editor.putString(getString(R.string.databaseURL), url);
        editor.commit();

        ((MainActivity) getActivity()).initFirebase(key, senderID, url);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new NotesFragment())
                .commit();
    }
}
