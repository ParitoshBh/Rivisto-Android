package productivity.notes.rivisto;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;

public class ConfigureFragment extends Fragment implements View.OnClickListener {
    private EditText apiKey, messagingSenderID, databaseURL;
    private Button configureRivisto;

    public ConfigureFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_configure, container, false);

        apiKey = (EditText) view.findViewById(R.id.configureAPIKey);
        messagingSenderID = (EditText) view.findViewById(R.id.configureMessagingSenderID);
        databaseURL = (EditText) view.findViewById(R.id.configureDatabaseURL);

        configureRivisto = (Button) view.findViewById(R.id.configureRivisto);

        configureRivisto.setOnClickListener(this);

        //setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onClick(View view) {
        String key = apiKey.getText().toString();
        String senderID = messagingSenderID.getText().toString();
        String url = databaseURL.getText().toString();

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
