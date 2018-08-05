package in.definex.picturenotes.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import in.definex.picturenotes.R;
import in.definex.picturenotes.util.GooglePlayManager;
import in.definex.picturenotes.util.UtilityFunctions;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static in.definex.picturenotes.util.GooglePlayManager.PREF_ACCOUNT_NAME;
import static in.definex.picturenotes.util.GooglePlayManager.REQUEST_ACCOUNT_PICKER;
import static in.definex.picturenotes.util.GooglePlayManager.REQUEST_PERMISSION_GET_ACCOUNTS;

public class PermisionAskingActivity extends Activity implements EasyPermissions.PermissionCallbacks, GooglePlayManager.ChooseAccount {

    final int PERMISSION_REQUEST_CODE= 4567;

    GooglePlayManager gpm;
    Boolean gpmLogedIn = false;
    Boolean permissionGiven = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permision_asking);


        final Activity c = this;

        //NOTEME GPM INITIALIZATION
        gpm = new GooglePlayManager(c, this);
        gpm.mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(GooglePlayManager.SCOPES))
                .setBackOff(new ExponentialBackOff());
        gpm.setExecuteResult(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                if(permissionGiven){
                    UtilityFunctions.setPermisionStatus(c,true);
                    finish();
                }

                return null;
            }
        });

        findViewById(R.id.grantPermissionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(c, permissions, PERMISSION_REQUEST_CODE);
                }else{
                    permissionGiven = true;
                    gpm.getResultsFromApi();
                }


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        gpm.manageResults(requestCode,resultCode,data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length == permissions.length){
                        Log.d("yo","yooooo");
                        permissionGiven = true;
                        gpm.getResultsFromApi();
                }
                break;

            case REQUEST_PERMISSION_GET_ACCOUNTS:
                if(grantResults.length == permissions.length){
                    gpm.getResultsFromApi();
                }
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, R.string.need_permission_to_use_the_app, Toast.LENGTH_LONG).show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    public void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getSharedPreferences("prefs",Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                gpm.mCredential.setSelectedAccountName(accountName);
                gpm.getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        gpm.mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.this_app_needs_to_access_your_google_account),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }
}
