package in.definex.picturenotes.util;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;

import java.util.List;
import java.util.concurrent.Callable;

import in.definex.picturenotes.activity.MainActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by adam_ on 04-12-2016.
 */

public class GooglePlayManager {

    //NOTEME FUNTIONS FOR GOOGLE PLAY SERVICES
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final String PREF_ACCOUNT_NAME = "accountName";

    public static final String[] SCOPES = { DriveScopes.DRIVE, DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE };

    public GoogleAccountCredential mCredential;

    public Activity context;

    private ChooseAccount chooseAccount;

    public GooglePlayManager(Activity context, ChooseAccount chooseAccount){

        this.context = context;
        this.chooseAccount = chooseAccount;
    }



    //call gpm.getResultsFromApi to run execute
    public void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount.chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(context,"No network connection available.",Toast.LENGTH_LONG).show();
        } else {
            try {
                executeResult.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Callable<Void> executeResult;

    public void setExecuteResult(Callable<Void> executeResult) {
        this.executeResult = executeResult;
    }

    public boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    public void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                context,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }




    public void manageResults(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            //NOTEME FOR GPS
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(context,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.",
                            Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                context.getSharedPreferences("prefs",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(GooglePlayManager.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case GooglePlayManager.REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }

    }

    public interface ChooseAccount{
        public void chooseAccount();
    }

}
