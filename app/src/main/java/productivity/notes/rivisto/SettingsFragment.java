package productivity.notes.rivisto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    Preference resetRivisto, signOut;
    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);

        sharedPref = getActivity().getSharedPreferences("FirebaseCredentials", Context.MODE_PRIVATE);

        resetRivisto = findPreference(getString(R.string.settings_reset));
        signOut = findPreference(getString(R.string.settings_sign_out));

        if (sharedPref.getBoolean(getString(R.string.isAccountHolder), false)) {
            signOut.setOnPreferenceClickListener(this);
        } else {
            getPreferenceScreen().removePreference(signOut);
        }

        resetRivisto.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String prefKey = preference.getKey();

        if (prefKey.equalsIgnoreCase(getString(R.string.settings_sign_out))) {
            AuthUI.getInstance()
                    .signOut(getActivity())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            SharedPreferences sharedPref = getActivity().getSharedPreferences("FirebaseCredentials", Context.MODE_PRIVATE);
                            sharedPref.edit().clear().commit();

                            getActivity().finishAffinity();
                        }
                    });
        } else if (prefKey.equalsIgnoreCase(getString(R.string.settings_reset))) {
            SharedPreferences sharedPref = getActivity().getSharedPreferences("FirebaseCredentials", Context.MODE_PRIVATE);
            sharedPref.edit().clear().commit();

            getActivity().finishAffinity();
        }

        return true;
    }
}