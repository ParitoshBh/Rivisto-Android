package productivity.notes.rivisto.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class Helpers {

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public static String isGooglePlayServicesAvailable(Context context){
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        //SUCCESS, SERVICE_MISSING, SERVICE_UPDATING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, SERVICE_INVALID
        switch (status){
            case ConnectionResult.SUCCESS:
                return "success";
            case ConnectionResult.SERVICE_UPDATING:
                return "Google Play Services is updating";
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                return "Google Play Services need to be updated";
            default: // Disabled, Invalid, Missing
                return "Google Play Services is missing.";
        }
    }
}
