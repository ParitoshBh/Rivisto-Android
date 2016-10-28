package productivity.notes.rivisto.configure;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import productivity.notes.rivisto.MainActivity;
import productivity.notes.rivisto.R;
import productivity.notes.rivisto.utils.Helpers;

public class ManualConfigFragment extends Fragment implements View.OnClickListener {
    private EditText apiKey, messagingSenderID, databaseURL;
    private Button configureRivisto;
    private View view;

    public ManualConfigFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_manual_config, container, false);

        setHasOptionsMenu(false);

        apiKey = (EditText) view.findViewById(R.id.configureAPIKey);
        messagingSenderID = (EditText) view.findViewById(R.id.configureMessagingSenderID);
        databaseURL = (EditText) view.findViewById(R.id.configureDatabaseURL);

        configureRivisto = (Button) view.findViewById(R.id.configureRivisto);

        configureRivisto.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        String googlePlayServicesStatus = Helpers.isGooglePlayServicesAvailable(getActivity());

        if (googlePlayServicesStatus.equalsIgnoreCase("success")){
            int buttonID = view.getId();

            switch (buttonID) {
                case R.id.configureRivisto:
                    String key = apiKey.getText().toString();
                    String senderID = messagingSenderID.getText().toString();
                    String url = databaseURL.getText().toString();

                    saveCredentialsAndInitFirebase(key, senderID, url, null, false);
                    break;
            }
        } else {
            Snackbar.make(view, googlePlayServicesStatus, Snackbar.LENGTH_LONG).show();
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
}
