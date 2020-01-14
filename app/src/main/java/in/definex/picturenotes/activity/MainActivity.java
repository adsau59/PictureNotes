package in.definex.picturenotes.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import in.definex.picturenotes.Adapters.BackupRecyclerAdapter;
import in.definex.picturenotes.Adapters.FavouriteRecyclerAdapter;
import in.definex.picturenotes.models.BackupModel;
import in.definex.picturenotes.models.DaoMaster;
import in.definex.picturenotes.models.DaoSession;
import in.definex.picturenotes.models.FavouriteViewModel;
import in.definex.picturenotes.models.ImageData;
import in.definex.picturenotes.models.NoteModel;
import in.definex.picturenotes.R;
import in.definex.picturenotes.util.GooglePlayManager;
import in.definex.picturenotes.util.UtilityFunctions;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static in.definex.picturenotes.util.GooglePlayManager.PREF_ACCOUNT_NAME;
import static in.definex.picturenotes.util.GooglePlayManager.REQUEST_ACCOUNT_PICKER;
import static in.definex.picturenotes.util.GooglePlayManager.REQUEST_PERMISSION_GET_ACCOUNTS;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, GooglePlayManager.ChooseAccount{

    GooglePlayManager gpm;
    private static DaoSession daoSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        //dao
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "picturenotes-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!UtilityFunctions.getPermisionCompleteStatus(this)){
            Intent intent = new Intent(this, PermisionAskingActivity.class);
            startActivity(intent);
        }

        UtilityFunctions.checkNews(this);

        final Activity c = this;

        ViewTarget target = new ViewTarget(R.id.fab, this);
        sv = new ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(target)
                .setContentTitle(getString(R.string.lets_make_a_note))
                .setContentText(getString(R.string.tap_the_plus_button_to_create_note))
                .setStyle(R.style.CustomShowcaseTheme2)
                .replaceEndButton(R.layout.view_custom_button)
                .build();

        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        sv.overrideButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sv.hide();
                UtilityFunctions.setTutorialStatus(c,false);
            }
        });
        sv.setButtonText(getString(R.string.skip));
        sv.setButtonPosition(lps);
        sv.hide();

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(UtilityFunctions.makeIcon(this,"\uF067"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(c, CreateNoteActivity.class);
                startActivity(intent);
                sv.hide();

            }
        });


        //NOTEME FIND BUTTON
        findViewById(R.id.see_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = ((EditText)findViewById(R.id.editText)).getText().toString().toLowerCase();

                if(code.isEmpty()){
                    Toast.makeText(c, R.string.enter_code_to_search, Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(c, ShowImageActivity.class);
                intent.putExtra("code", code);
                startActivity(intent);
            }
        });

        setFavList();





        //NOTEME GPM INITIALIZATION
        gpm = new GooglePlayManager(c, this);
        gpm.mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(GooglePlayManager.SCOPES))
                .setBackOff(new ExponentialBackOff());

        //when tapped on .pnd
        checkForIntent();

    }
    ShowcaseView sv;

    private void checkForIntent()   {
        final Uri uri = getIntent().getData();

        if(uri != null) {

            if (uri.getPath().equals("/picturenotes")) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle(R.string.import_title)
                        .setMessage(R.string.do_you_wish_to_imort_from_this_url)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gpm.setExecuteResult(new Callable<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        new UrlImporter(c,gpm.mCredential,uri.getQueryParameter("id")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        return null;
                                    }
                                });
                                gpm.getResultsFromApi();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            } else {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle(R.string.import_title)
                        .setMessage(R.string.do_you_wish_to_import_this_file)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new fileImporter(c, uri).execute();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

            }
        }
    }

    //dao
    public static DaoSession GetDaoSession() {
        return daoSession;
    }

    //NOTEME GPM INTERFACE METHODS

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

    //GPM INTERFACE METHOD END

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);

        MenuItem item = menu.getItem(0);

        if (item.getItemId() == R.id.importNote)
            item.setIcon(UtilityFunctions.makeIcon(this, "\uf0ed"));

        return super.onCreateOptionsMenu(menu);
    }

    //GPM INTERFACE METHOD END

    final int FILE_REQUEST_CODE = 5684;
    final int READ_REQUEST_CODE_FOR_IMPORT = 5673;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.importNote:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    permisionAsking(this, Manifest.permission.READ_EXTERNAL_STORAGE,READ_REQUEST_CODE_FOR_IMPORT);
                }

                CharSequence[] options = new CharSequence[]{getString(R.string.local), getString(R.string.from_drive)};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.from_where_you_would_like_to_import)
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        askForFile();
                                        return;
                                    case 1:

                                        //TODO START DRIVE STUFF
                                        gpm.setExecuteResult(new Callable<Void>() {
                                            @Override
                                            public Void call() throws Exception {
                                                new driveFolderSearcher(c,gpm.mCredential).execute();
                                                return null;
                                            }
                                        });
                                        gpm.getResultsFromApi();
                                        return;
                                }
                            }
                        });

                builder.show();

                break;

            case R.id.viewAllNotes:
                Intent intent = new Intent(this, ViewAllNotes.class);
                startActivity(intent);
                break;

            case R.id.setttings:
                Intent intent1 = new Intent(this, SettingsActivity.class);
                startActivity(intent1);
                break;
        }
        return true;
    }

    private void askForFile(){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case FILE_REQUEST_CODE:
                if(resultCode == RESULT_OK) {

                    Uri uri = data.getData();

                    new fileImporter(this,uri).execute();
                }

                break;
        }

        gpm.manageResults(requestCode,resultCode,data);
    }


    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        //NOTEME GPM METHOD
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);

        switch (requestCode) {
            case READ_REQUEST_CODE_FOR_IMPORT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    askForFile();
                }
            }
            break;
        }
    }

    List<FavouriteViewModel> favList;
    FavouriteRecyclerAdapter adapter;
    Context c;
    private void setFavList(){
        c = this;

        favList = new ArrayList<>();
        List<NoteModel> noteModels = NoteModel.getFavNotesFromDB(c);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favRecycler);

        for (NoteModel noteModel : noteModels)
            favList.add(FavouriteViewModel.noteToFavVM(noteModel, c));

        //Log.d("size of fav", favList.size()+"");


        adapter = new FavouriteRecyclerAdapter(c, favList){
            @Override
            public void onItemClick(int pos) {
                Intent intent = new Intent(c, ShowImageActivity.class);
                intent.putExtra("code", favList.get(pos).note.getCode());
                startActivity(intent);
            }
        };

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    boolean favoritesChanged = false;

    @Override
    protected void onResume() {
        favoritesChanged = UtilityFunctions.getFavDisturbed(this);

        if(favoritesChanged) {
            setFavList();
            favoritesChanged = false;
            UtilityFunctions.setFavDisturbed(this, false);
        }



        //tuts
        if(UtilityFunctions.getTutorialStatus(this)) {
            sv.show();
        }



        //autocomplete search
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, NoteModel.getAllCodes(this));

        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.editText);
        textView.setAdapter(adapter);

        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {

        setIntent(intent);

        if(intent.getBooleanExtra("showDownloadProgress",false)){
            if(!downloadComplete)
                mDownloadProgress.show();
        }else
            checkForIntent();



            //TODO THIS IS A HACK DONT USE THIS


        super.onNewIntent(intent);

    }

    public static boolean permisionAsking(Activity c, String s){
        return permisionAsking(c,s,123456);
    }

    public static boolean permisionAsking(Activity c, String s, int requestCode){
        //REQUEST FOR PERMISION IF DOESNT HAVE
        if(!(ContextCompat.checkSelfPermission(c, s) == PermissionChecker.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(c, new String[]{s}, requestCode);

        return ContextCompat.checkSelfPermission(c, s) == PermissionChecker.PERMISSION_GRANTED;
    }


    private class fileImporter extends AsyncTask<Void,String,Boolean>{

        ProgressDialog dialog;
        Uri uri;
        Context context;
        String jsonString = "";

        fileImporter(Context context, Uri uri){
            this.context = context;
            this.uri = uri;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "",
                    getString(R.string.loading_please_wait), true);
        }



        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));

                String content;
                jsonString = "";
                while((content = reader.readLine())!=null){
                    jsonString += content;
                }


                return true;

            }catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            dialog.dismiss();

            new jsonToDB(jsonString,"",context).execute();



        }
    }

    private class jsonToDB extends AsyncTask<Void, Void, Boolean>{
        String importPath;
        String jsonString;
        String code;
        Context context;

        ProgressDialog mProgress;

        public jsonToDB(String jsonString, String code, Context context){
            importPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PictureNotes/imports/";
            this.jsonString = jsonString;
            this.code = code;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(context);
            mProgress.setMessage(getString(R.string.unzipping_please_wait));
            mProgress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                JSONObject jsonObject = new JSONObject(jsonString);

                if(code.isEmpty())
                    code = jsonObject.getString("code");

                String dsc = jsonObject.getString("description");

                if(NoteModel.DoesCodeExists(code))
                    return false;

                NoteModel noteModel = new NoteModel(code, dsc, false);
                noteModel.saveNoteInDB();

                JSONArray jsonArray = jsonObject.getJSONArray("imageDatas");
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject imageDataJson = jsonArray.getJSONObject(i);
                    byte[] bytes = Base64.decode(imageDataJson.getString("image"),Base64.DEFAULT);
                    String imageUrl = importPath+code+"/"+"image"+imageDataJson.getInt("number")+".jpg";
                    File imageFile = new File(imageUrl);

                    if(!imageFile.getParentFile().exists())
                        imageFile.getParentFile().mkdirs();

                    FileOutputStream fos = new FileOutputStream(imageFile);
                    fos.write(bytes);
                    fos.flush();
                    fos.close();

                    new ImageData(imageDataJson.getInt("number"), imageUrl, imageDataJson.getString("name"), noteModel).save();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mProgress.dismiss();

            downloadComplete = true;

            if(success) {
                Toast.makeText(context, R.string.done_importing, Toast.LENGTH_LONG).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Note Imported")
                        .setMessage("Do you wish to open it?")
                        .setPositiveButton("Open", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(context, ShowImageActivity.class);
                                intent.putExtra("code", code);
                                startActivity(intent);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();

            }
            else {
                Toast.makeText(context, R.string.importing_failed, Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final EditText editText = new EditText(context);
                editText.setHint(R.string.new_code);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(100,10,100,10);
                editText.setLayoutParams(lp);
                builder.setTitle(getString(R.string.code_space)+code+getString(R.string.space_already_exists))
                        .setView(editText)
                        .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new jsonToDB(jsonString, editText.getText().toString().toLowerCase(), context).execute() ;
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        }
    }

    final int SHOW_DOWNLOAD_PROGRESS_REQUEST_CODE = 4135;


    ProgressDialog mDownloadProgress;
    private boolean downloadComplete = true;
    //NOTEME DOWNLOAD FROM DRIVE STUFF
    private class UrlImporter extends AsyncTask<Void,Integer,String>{
        Drive mService = null;
        Context context;
        String fileId;
        Exception mLastError = null;

        NotificationManager mNotifyManager;
        NotificationCompat.Builder mBuilder;
        int id = 1;

        public UrlImporter(Context context, GoogleAccountCredential credential, String fileId){
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Picture Notes")
                    .build();

            this.context = context;
            this.fileId = fileId;

            mNotifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.download_in_progress))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(true);

            mDownloadProgress = new ProgressDialog(context);
            mDownloadProgress.setMessage(getString(R.string.downloading_file));
            mDownloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDownloadProgress.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.hide), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mDownloadProgress.dismiss();
                }
            });

            Intent intent = getIntent();
            intent.putExtra("showDownloadProgress",true);
            PendingIntent contentIntent = PendingIntent.getActivity(context, SHOW_DOWNLOAD_PROGRESS_REQUEST_CODE, ((Activity)context).getIntent(), PendingIntent.FLAG_UPDATE_CURRENT );

            mBuilder.setContentIntent(contentIntent);

            downloadComplete = false;



        }

        int fileSize = 0;

        @Override
        protected String doInBackground(Void... params) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                com.google.api.services.drive.model.File file = mService.files().get(fileId).setFields("size").execute();
                Log.d("Size", String.valueOf(file.getSize()));
                fileSize = file.getSize().intValue();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBuilder.setProgress(fileSize, 0, false);
                        mNotifyManager.notify(id,mBuilder.build());

                        mDownloadProgress.setMax(fileSize);
                        mDownloadProgress.show();
                    }
                });


                Drive.Files.Get request = mService.files().get(fileId);
                request.getMediaHttpDownloader().setProgressListener(new DownloadProgressListener()).setChunkSize(1000000);
                request.executeMediaAndDownloadTo(outputStream);


                //mService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                return outputStream.toString("UTF-8");

            }catch (Exception e) {
                e.getStackTrace();

                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String jsonString) {
            mDownloadProgress.hide();

            mBuilder.setContentText(getString(R.string.download_complete))
                    .setProgress(0,0,false)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());

            if(jsonString!=null)
                new jsonToDB(jsonString, "", context).execute();
        }

        @Override
        protected void onCancelled() {
            mDownloadProgress.hide();

            if (mLastError != null) {


                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    gpm.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GooglePlayManager.REQUEST_AUTHORIZATION);
                }
                else {

                    try {
                        JSONObject jsonObject = new JSONObject(mLastError.getMessage().split("\n",2)[1]);
                        int code = jsonObject.getJSONObject("error").getInt("code");
                        if(code == 404){

                            new AlertDialog.Builder(context).setTitle("Alert!")
                                    .setMessage(R.string.share_note_has_been_changed)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                        }else {
                            Toast.makeText(context,getString(R.string.the_following_error_occurred)
                                    + mLastError.getMessage().split("\n",2)[1],Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                }

            } else {
                Toast.makeText(context, R.string.request_cancelled, Toast.LENGTH_LONG).show();
            }
        }

        //custom listener for download progress
        class DownloadProgressListener implements MediaHttpDownloaderProgressListener {
            @Override
            public void progressChanged(final MediaHttpDownloader downloader) throws IOException {
                switch (downloader.getDownloadState()){

                    //Called when file is still downloading
                    //ONLY CALLED AFTER A CHUNK HAS DOWNLOADED,SO SET APPROPRIATE CHUNK SIZE
                    case MEDIA_IN_PROGRESS:
                        //Add code for showing progress

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mBuilder.setProgress(fileSize, (int) downloader.getNumBytesDownloaded(), false);
                                mNotifyManager.notify(id,mBuilder.build());

                                mDownloadProgress.setMax(fileSize);
                                mDownloadProgress.setProgress((int) downloader.getNumBytesDownloaded());
                            }
                        });

                        break;
                    //Called after download is complete
                    case MEDIA_COMPLETE:
                        //Add code for download completion
                        break;
                }
            }
        }
    }

    private class driveFolderSearcher extends AsyncTask<Void, Void, FileList>{

        Context context;
        ProgressDialog mProgress;
        Exception mLastError = null;
        Drive mService = null;

        public driveFolderSearcher(Context context,GoogleAccountCredential credential){
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Picture Notes")
                    .build();
            this.context = context;

            mProgress = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            mProgress.setMessage(getString(R.string.fetching_data_please_wait));
            mProgress.show();
        }

        @Override
        protected FileList doInBackground(Void... params) {
            try {
                FileList result = mService.files().list()
                        .setSpaces("appDataFolder")
                        .setFields("nextPageToken, files(id, name, modifiedTime)")
                        .execute();

                return result;

            }catch (Exception e) {
                e.getStackTrace();

                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(FileList result) {
            mProgress.dismiss();

            final List<BackupModel> list = new ArrayList<>();
            if(fileList()!=null) {
                for (com.google.api.services.drive.model.File file : result.getFiles()) {
                    System.out.printf(getString(R.string.found_file)+": %s (%s)\n",
                            file.getName(), file.getId());

                    list.add(new BackupModel(file.getId(),file.getName(),file.getModifiedTime().toString()));
                }

                if(list.size()<=0){
                    Toast.makeText(context, R.string.no_back_up_found, Toast.LENGTH_SHORT).show();
                    return;
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final RecyclerView recyclerView = new RecyclerView(context);
                final BackupRecyclerAdapter adapter = new BackupRecyclerAdapter(context, list);

                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setPadding(100,30,100,30);
                //lv.setAdapter(new SimpleAdapter(context, ));
                builder.setTitle(R.string.which_one_would_you_like_to_restore)
                        .setView(recyclerView);
                final AlertDialog dialog = builder.show();

                adapter.setOnDeleteCLickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int pos = recyclerView.indexOfChild((View) v.getParent());
                        Log.d("index",pos+"");
                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    mService.files().delete(list.get(pos).id).execute();
                                    list.remove(pos);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                adapter.updateList(list);
                                Toast.makeText(context, list.get(pos).name+getString(R.string.space_backup_deleted_from_drive),Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                    }
                });

                adapter.setOnItemClick(new BackupRecyclerAdapter.OnItemClick() {
                    @Override
                    public void onItemClickListenter(int pos) {
                        new UrlImporter(context, gpm.mCredential, list.get(pos).id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        dialog.dismiss();
                    }
                });


            }
        }



        @Override
        protected void onCancelled() {
            mProgress.dismiss();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    gpm.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GooglePlayManager.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(context,getString(R.string.the_following_error_occurred)
                            + mLastError.getMessage(),Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, R.string.Request_cancelled, Toast.LENGTH_LONG).show();
            }
        }
    }


    //NOTEME DOUBLETAP TO EXIT
    boolean backPressedOnce=false;
    @Override
    public void onBackPressed() {
        if(backPressedOnce)
            super.onBackPressed();
        else{
            Toast.makeText(c, R.string.press_back_again_to_exit, Toast.LENGTH_SHORT).show();
            backPressedOnce = true;

            new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    backPressedOnce = false;
                }
            }.start();
        }
    }



}
