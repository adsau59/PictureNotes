package in.definex.picturenotes.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import in.definex.picturenotes.Adapters.ShowImageRecyclerAdapter;
import in.definex.picturenotes.models.ImageData;
import in.definex.picturenotes.models.NoteModel;
import in.definex.picturenotes.R;
import in.definex.picturenotes.database.DbService;
import in.definex.picturenotes.util.DEFINE;
import in.definex.picturenotes.util.GooglePlayManager;
import in.definex.picturenotes.util.ImageSelector;
import in.definex.picturenotes.util.TextDrawable;
import in.definex.picturenotes.util.UtilityFunctions;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static in.definex.picturenotes.util.GooglePlayManager.PREF_ACCOUNT_NAME;
import static in.definex.picturenotes.util.GooglePlayManager.REQUEST_ACCOUNT_PICKER;
import static in.definex.picturenotes.util.GooglePlayManager.REQUEST_PERMISSION_GET_ACCOUNTS;

public class ShowImageActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, GooglePlayManager.ChooseAccount {

    String code;
    NoteModel noteModel;

    FloatingActionButton fab;
    TextDrawable favOn;
    TextDrawable favOff;

    GooglePlayManager gpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        code = getIntent().getStringExtra("code");

        Log.d("code", code);
        //get note from db
        noteModel = NoteModel.getNoteByCode(this, code);

        favOff = UtilityFunctions.makeIcon(this, "\uf006",20);
        favOn = UtilityFunctions.makeIcon(this, "\uf005",20);


        //if none found
        if(noteModel == null) {
            Toast.makeText(this, R.string.no_image_found_with_given_code, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setIconHelper(true);



        //setting views
        update_code_and_desc_view();

        loadImageLvFromDb();

        fab = (FloatingActionButton) findViewById(R.id.addImageFab);
        setFabMode(false);


        //NOTEME GPM CREDENTIALS INITIALIZATION
        gpm = new GooglePlayManager(this,this);
        // Initialize credentials and service object.
        gpm.mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(GooglePlayManager.SCOPES))
                .setBackOff(new ExponentialBackOff());

        //call gpm.getResultsFromApi to run execute


        final Context context = this;

        //tutorial stuff
        if(UtilityFunctions.getTutorialStatus(this)){

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (tutCounter){
                        case 0:
                            View imageView = findViewById(R.id.imageView);

                            if(imageView == null)
                                sv.setTarget(Target.NONE);
                            else
                                sv.setTarget(new ViewTarget(imageView));


                            sv.setContentTitle(getString(R.string.view_image));
                            sv.setContentText(getString(R.string.tap_on_any_image_to_view_it));
                            break;

                        case 1:
                            sv.setTarget(Target.NONE);
                            sv.setContentTitle(getString(R.string.re_arrange_images));
                            sv.setContentText(getString(R.string.tap_hold_move_images));
                            break;

                        case 2:
                            sv.setTarget(new ViewTarget(findViewById(R.id.favourite)));
                            sv.setContentTitle(getString(R.string.actions));
                            sv.setContentText(getString(R.string.you_can_share_backup_favourite_remove_notes));
                            break;

                        case 3:
                            sv.setTarget(new ViewTarget(findViewById(R.id.shareNote)));
                            break;

                        case 4:
                            sv.setTarget(Target.NONE);
                            sv.setContentTitle(getString(R.string.enjoy));
                            sv.setContentText(getString(R.string.tutorial_can_be_viewed_again_from_settings));
                            sv.setButtonText(getString(R.string.close));
                            UtilityFunctions.setTutorialStatus(context,false);
                            break;

                        case 5:
                            sv.hide();
                            noteModel.deleteNoteAndImagesFromDB(context);
                            finish();
                            break;
                    }
                    tutCounter++;
                }
            };


            sv = new ShowcaseView.Builder(this)
                    //.withMaterialShowcase()
                    .setTarget(Target.NONE)
                    .replaceEndButton(R.layout.view_custom_button)
                    .setOnClickListener(onClickListener)
                    .setContentTitle(getString(R.string.your_note))
                    .setContentText(getString(R.string.here_is_your_note))
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .build();
            sv.setShouldCentreText(true);
            sv.setButtonText(getString(R.string.next));



        }


        //edit description
        ImageView editDescIv = (ImageView)findViewById(R.id.editDesc);
        editDescIv.setImageDrawable(UtilityFunctions.makeIcon(context, "\uf044",30, Color.BLACK));
        editDescIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final View v = getEditDialouge(context);
                ((EditText)v.findViewById(R.id.editDescEt)).setText(noteModel.getDescription());
                ((EditText)v.findViewById(R.id.editCodeEt)).setText(noteModel.getCode());


                final AlertDialog dialog = new AlertDialog.Builder(context).setTitle(R.string.edit_code_and_description)
                        .setView(v)
                        .setPositiveButton(R.string.ok, null)
                        .setNegativeButton(R.string.cancel, null).create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String newCode = ((EditText)v.findViewById(R.id.editCodeEt)).getText().toString().toLowerCase().trim();
                                String newDesc = ((EditText)v.findViewById(R.id.editDescEt)).getText().toString();

                                if(newCode.isEmpty()){
                                    Toast.makeText(context, R.string.enter_a_code, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if(!newCode.equals(noteModel.getCode()) && NoteModel.isCodeInDB(context, newCode)){
                                    Toast.makeText(context, R.string.code_must_be_unique, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if(newCode.length()> DEFINE.CODE_CHAR_LIMIT){
                                    Toast.makeText(context, R.string.the_name_is_too_long, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if(newDesc.length()>DEFINE.DESC_CHAR_LIMIT){
                                    Toast.makeText(context, R.string.the_desc_is_too_long, Toast.LENGTH_LONG).show();
                                    return;
                                }


                                //validation ends

                                noteModel.setDescription(newDesc);

                                if(!newCode.equals(noteModel.getCode()))
                                    noteModel = noteModel.changeCodeAndSaveToDB(context, newCode);

                                code = noteModel.getCode();
                                update_code_and_desc_view();

                                UtilityFunctions.setFavDisturbed(context, true);
                                dialog.dismiss();
                            }
                        });

                    }
                });

                dialog.show();


            }
        });
    }

    private void update_code_and_desc_view(){
        ((TextView)findViewById(R.id.codeTextView)).setText(noteModel.getCode());
        ((TextView)findViewById(R.id.descriptionTextView)).setText(noteModel.getDescription());
    }

    ShowcaseView sv;
    int tutCounter = 0;

    private int lastImageNumber;
    ShowImageRecyclerAdapter adapter;
    //NOTEME load Image
    private void loadImageLvFromDb(){
        List<ImageData> imageDatas = ImageData.getImagesFromCode(this,code);
        lastImageNumber = imageDatas.size() !=0 ?imageDatas.get(imageDatas.size()-1).getNumber():0;

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        adapter = new ShowImageRecyclerAdapter(this, imageDatas, noteModel);

        adapter.setActivityFuncs(new ShowImageRecyclerAdapter.ActivityFuncs() {
            @Override
            public void hideStatus() {
                final View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
                // if (Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN)
                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

            }

            @Override
            public void showStatus() {
                // if (Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN)
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                final View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));

        final Context context = this;




        //NOTEME drag and drop
        // Extend the Callback class
        ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {

            //and in your imlpementaion of
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPosition = viewHolder.getAdapterPosition();
                final int toPosition = target.getAdapterPosition();

                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(adapter.imageDatas, i, i + 1);
                        adapter.exchangeBitMaps(i,i+1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(adapter.imageDatas, i, i - 1);
                        adapter.exchangeBitMaps(i,i-1);
                    }
                }
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }


            View oldViewItem;
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                if(viewHolder != null){
                    oldViewItem = viewHolder.itemView;
                    oldViewItem.setRotation(10);
                }else{
                    oldViewItem.setRotation(0);
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                for(int i=0, num=1; i<adapter.imageDatas.size(); i++,num++)
                    adapter.imageDatas.get(i).setNumber(num);

                new Thread(){
                    @Override
                    public void run() {
                        DbService.updateAllImages(context, adapter.imageDatas);
                    }
                }.start();

                noteModel.cachedShareNoteDisturbed(context);
                UtilityFunctions.setFavDisturbed(context, true);

                adapter.notifyDataSetChanged();
            }
        };

        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ItemTouchHelper ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(recyclerView);
    }

    /**
     * Handles the request from image selection
     *
     * @param requestCode DEFINE.GALLERY_CODE for image selection
     * @param resultCode Activity.RESULT_OK if activity was successful
     * @param data intent result
     */
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        gpm.manageResults(requestCode,resultCode,data);

        if(requestCode != DEFINE.GALLERY_CODE && resultCode != Activity.RESULT_OK)
            return;

        imageSelector.HandleCallback(data, (o)->{
            noteModel.cachedShareNoteDisturbed(this);
            UtilityFunctions.setFavDisturbed(this, true);

            List<ImageData> imageDatas = ImageData.getImagesFromCode(this,code);
            adapter.updateListData(imageDatas);
            return null;
        });

    }

    private void changeFavIcon(boolean fav){
        String favText;
        if(fav) {
            menu.findItem(R.id.favourite).setIcon(favOn);
            favText = getString(R.string.unfavourite);
        }
        else {
            menu.findItem(R.id.favourite).setIcon(favOff);
            favText = getString(R.string.favourite);
        }
        menu.findItem(R.id.favourite).setTitle(favText);
    }

    //NOTEME WHEN CALLED CHANGES FAV IN DB THEN CHANGES ICON
    private void updateFavStatus(boolean fav){
        noteModel.updateFavStatusInDB(this, fav);
        requestUpdateFavView();
        changeFavIcon(fav);
    }

    private void requestUpdateFavView(){
        UtilityFunctions.setFavDisturbed(this, true);
    }

    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.view_image_menu,menu);
        setIconHelper(false);

        return true;
    }

    boolean onCreateComplete = false;
    boolean onMenuComplete = false;
    private void setIconHelper(boolean isCreate){
        if(isCreate)
            onCreateComplete = true;
        else
            onMenuComplete = true;

        if(onCreateComplete && onMenuComplete){
            setIcons();
        }
    }

    public static View getDeleteImageDialogeView(Context c){
        LayoutInflater inflater1 = (LayoutInflater)c.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        final View v1 = inflater1.inflate(R.layout.delete_note_image_dialog_layout, null);
        v1.findViewById(R.id.deleteImageText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox)v1.findViewById(R.id.deleteImagesCheckBox);
                checkBox.setChecked(!checkBox.isChecked());
            }
        });

        return v1;
    }

    public static View getEditDialouge(Context c){
        LayoutInflater inflater1 = (LayoutInflater)c.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View v1 = inflater1.inflate(R.layout.edit_code_dialog_layout, null);
        return v1;
    }

    private void setIcons(){
        for(int i=0; i<menu.size(); i++){
            switch (menu.getItem(i).getItemId()){
                case R.id.shareNote:
                    menu.getItem(i).setIcon(UtilityFunctions.makeIcon(this, "\uf1e0", 20));
                    break;

                case R.id.favourite:
                    changeFavIcon(noteModel.isFav());
                    break;

                case R.id.cancleDeleteMenuButton:
                    menu.getItem(i).setIcon(UtilityFunctions.makeIcon(this, "\uf00d", 20));
            }
        }

    }

    private ImageSelector imageSelector;

    /**
     * Changes the mode of action button
     * changes icon, color and callback
     *
     * on delete mode, it starts the delete operation
     * on non delete mode, it adds new images
     *
     * @param deleteMode true if on delete mode
     */
    private void setFabMode(Boolean deleteMode){
        if(!deleteMode){
            fab.setImageDrawable(UtilityFunctions.makeIcon(this, "\uF067"));
            fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));
            imageSelector = new ImageSelector(this, code);
            fab.setOnClickListener(view -> imageSelector.StartIntent());
        }else{
            //NOTEME DELETE SCRIPT
            fab.setImageDrawable(UtilityFunctions.makeIcon(this, "\uf1f8"));
            fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secodaryColorAccent)));
            final Context c = this;
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    final View v1 = getDeleteImageDialogeView(c);

                    AlertDialog.Builder builder2 = new AlertDialog.Builder(c);

                    final List<ImageData> selectedImages = adapter.selectedImages();

                    if(selectedImages.size()<=0){
                        Toast.makeText(c, R.string.select_images_to_delete_first, Toast.LENGTH_LONG).show();
                        return;
                    }

                    builder2.setMessage(R.string.delete_images)
                            .setView(v1)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    boolean deleteFile = ((CheckBox)v1.findViewById(R.id.deleteImagesCheckBox)).isChecked();

                                    for(int i=0; i<selectedImages.size(); i++){

                                        if(deleteFile)
                                            UtilityFunctions.deleteImage(c,selectedImages.get(i).getUrl());

                                        selectedImages.get(i).deleteImageFromDB(c);


                                        adapter.imageDatas.remove(selectedImages.get(i));
                                    }

                                    for(int i=0, num=1; i<adapter.imageDatas.size(); i++,num++)
                                        adapter.imageDatas.get(i).setNumber(num);

                                    new Thread(){
                                        @Override
                                        public void run() {
                                            DbService.updateAllImages(c, adapter.imageDatas);
                                        }
                                    }.start();

                                    noteModel.cachedShareNoteDisturbed(c);

                                    adapter.updateData(adapter.imageDatas);
                                    adapter.notifyDataSetChanged();

                                    setDeletMode(false);
                                    requestUpdateFavView();

                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder2.show();


                }
            });

        }
    }

    private boolean deleteMode=false;

    private void setDeletMode(Boolean bool){

        this.deleteMode = bool;
        menu.clear();
        if(bool){
                getMenuInflater().inflate(R.menu.show_image_delete_menu,menu);
        }else{
                getMenuInflater().inflate(R.menu.view_image_menu,menu);
        }
        setIcons();
        adapter.setSelector(bool);
        setFabMode(bool);
        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        final Context c = this;

        switch (item.getItemId()){
            case R.id.delete:
                menu.close();
                setDeletMode(true);

                break;

            case R.id.cancleDeleteMenuButton:
                setDeletMode(false);
                break;

            case R.id.favourite:


                noteModel.setFav(!noteModel.isFav());
                updateFavStatus(noteModel.isFav());

                break;

            case R.id.remove:

                final View v = getDeleteImageDialogeView(c);

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setMessage(R.string.delete_note)
                        .setView(v)
                        .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(((CheckBox)v.findViewById(R.id.deleteImagesCheckBox)).isChecked()){
                                    for(ImageData imageData : adapter.imageDatas)
                                        UtilityFunctions.deleteImage(c, imageData.getUrl());
                                }

                                requestUpdateFavView();

                                noteModel.deleteNoteAndImagesFromDB(c);
                                finish();



                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();

                break;

            //NOTEME BACKUP
            case R.id.exportNote:


                final Activity context = this;
                //asking permision
                boolean permision = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    permision = MainActivity.permisionAsking(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

                CharSequence[] options = new CharSequence[]{getString(R.string.local), getString(R.string.to_drive)};
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);

                builder1.setTitle(R.string.where_would_you_like_to_backup)
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        new ExportNoteTask(context,false).execute();
                                        return;
                                    case 1:

                                        ExportNoteTask exportNoteTask = new ExportNoteTask(c,false){
                                            @Override
                                            public void callBackMethod() {
                                                exportedFilePath = this.getFilePath();
                                                gpm.setExecuteResult(new Callable<Void>() {
                                                    @Override
                                                    public Void call() throws Exception {
                                                        new MakeRequestTask(c, gpm.mCredential,false).execute();
                                                        return null;
                                                    }
                                                });
                                                gpm.getResultsFromApi();
                                            }
                                        };
                                        exportNoteTask.execute();

                                        return;
                                }
                            }
                        });
                builder1.show();

                break;

            case R.id.shareNote:

                //NOTEME CREATING EXPORTNOTETASK AND ADDING CALLBACKMETHOD TO RUN MAKEREQUESTASK AFTER EXPORTING

                //CHECKING IF NOTE IS UNCHANGED
                if(noteModel.getCachedNoteIntegrity(this)){
                    String fileId = noteModel.getCachedSharedNote(this);
                    shareFromFileId(noteModel, this, fileId);
                }else{
                    //ELSE
                    ExportNoteTask exportNoteTask = new ExportNoteTask(this,true){
                        @Override
                        public void callBackMethod() {
                            exportedFilePath = this.getFilePath();
                            gpm.setExecuteResult(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    new MakeRequestTask(c, gpm.mCredential,true).execute();
                                    return null;
                                }
                            });
                            gpm.getResultsFromApi();
                        }
                    };
                    exportNoteTask.execute();
                }


                break;
        }

        return true;
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

    private String exportedFilePath;

    //NOTEME EXPORTING(LOCAL BACKUP) ASYNCTASK
    private class ExportNoteTask extends AsyncTask<Void, Void, Boolean>{
        ProgressDialog progressDialog;
        Context context;
        String filePath;
        boolean toShare;

        ExportNoteTask(Context context, boolean toShare){
            this.context = context;
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(getString(R.string.exporting_file));
            this.toShare = toShare;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //getting latest export location
            SharedPreferences sharedPreferences = context.getSharedPreferences("prefs",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int exportNo = sharedPreferences.getInt("exportNo",0);
            editor.putInt("exportNo", exportNo+1);
            editor.apply();

            final String exportLoc = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PictureNotes/Exports/";

            //creating json from db and images
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("code", code);
                jsonObject.put("description",noteModel.getDescription() );

                JSONArray jsonArray = new JSONArray();
                JSONObject jsonImageObj;
                for(ImageData imageData: adapter.imageDatas){
                    jsonImageObj = new JSONObject();

                    jsonImageObj.put("name", imageData.getName());
                    jsonImageObj.put("number", imageData.getNumber());


                    Bitmap bm = BitmapFactory.decodeFile(imageData.getUrl(), null);

                    if(bm == null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, R.string.couldnt_find_all_images, Toast.LENGTH_LONG).show();
                            }
                        });
                        return false;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] b = baos.toByteArray();
                    String imageBase64 = Base64.encodeToString(b, Base64.DEFAULT);

                    jsonImageObj.put("image",imageBase64);

                    jsonArray.put(jsonImageObj);
                }

                jsonObject.put("imageDatas",jsonArray);


                //File IO
                String subTitle = toShare ?" exports":"backup";

                final String fileName = noteModel.getCode()+subTitle+exportNo+".pnd";
                File dir = new File(exportLoc);

                if(!dir.exists()){
                    dir.mkdirs();
                }
                File file = new File(exportLoc, fileName);
                filePath  = file.getPath();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(jsonObject.toString().getBytes());
                fos.flush();
                fos.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, getString(R.string.exported_to_space)+exportLoc+fileName, Toast.LENGTH_LONG).show();
                    }
                });


            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            progressDialog.dismiss();

            if(success)
                callBackMethod();
        }

        public String getFilePath(){
            return filePath;
        }

        public void callBackMethod(){}
    }



    //NOTEME DRIVE API STUFF
    private class MakeRequestTask extends AsyncTask<Void, Void, String> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;
        private String fileId;
        private String pictureNotesFolderId;
        private ProgressDialog mProgress;
        private Context context;
        private String filedir;

        SharedPreferences sharedPreferences;

        NotificationManager mNotifyManager;
        NotificationCompat.Builder mBuilder;
        int notifyId = 513;

        boolean toShare = false;

        public MakeRequestTask(final Context context, GoogleAccountCredential credential, boolean toShare) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Picture Notes")
                    .build();

            mProgress = new ProgressDialog(context);
            mProgress.setMessage(getString(R.string.uploading_to_drive));
            mProgress.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.hide), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mProgress.hide();
                    Toast.makeText(context, R.string.uploading_hide_toast, Toast.LENGTH_LONG).show();
                }
            });


            mNotifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle("Picture Notes")
                    .setContentText("Uploading Note")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setProgress(0,0,true)
                    .setOngoing(true);

            this.context = context;
            this.filedir = exportedFilePath;

            this.toShare = toShare;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                sharedPreferences = context.getSharedPreferences("prefs",Context.MODE_PRIVATE);
                pictureNotesFolderId = sharedPreferences.getString("pictureNotesFolderId","");


                if(toShare){
                    return toShareBG();
                }else{
                    return backUp();
                }


            }catch (Exception e) {
                e.getStackTrace();

                mLastError = e;
                cancel(true);
                return null;
            }

            //TODO RUN STUFF TO 1) CREATE FILE IN DRIVE 2) CHANGE PERMISION 3) GET AND SHARE ITS URL
        }

        private String toShareBG() throws IOException {

            if(!noteModel.getCachedSharedNote(context).isEmpty() && !noteModel.getCachedNoteIntegrity(context)){
                deleteFile(noteModel.getCachedSharedNote(context));
            }

            //if folder was never created
            if(pictureNotesFolderId.isEmpty()){

                //then create folders
                pictureNotesFolderId = createFolderAndUpdatePrefs("pictureNotesFolderId", null, "Picture Notes Shared");

            }else{

                //if folder was created
                Log.d("folders", "already saved");

                //search for folder
                com.google.api.services.drive.model.File pictureNotesFiles = findFolder("Picture Notes Shared", pictureNotesFolderId);

                //if there are no picturenote folder
                if(pictureNotesFiles == null){
                    //create new picturenotesfolder and update save
                    pictureNotesFolderId = createFolderAndUpdatePrefs("pictureNotesFolderId", null, "Picture Notes Shared");

                    Log.d("folders","picture got deleted");
                }


            }

            fileId = uploadFileToApi(pictureNotesFolderId);
            makeFileSharable(fileId);
            noteModel.cacheSharedNote(context, fileId);
            return fileId;

        }

        private void deleteFile(String fileId) throws IOException {
            mService.files().delete(fileId).execute();
        }

        private String backUp() throws IOException{
            fileId = uploadFileToApi("appDataFolder");
            return fileId;
        }

        private String createFolderAndUpdatePrefs(String prefKey, String parentId, String folderName) throws IOException {
            String id = createFolder(parentId, folderName);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(prefKey,id);
            editor.apply();
            return id;
        }

        //NOTEME MY METHODS
        private com.google.api.services.drive.model.File findFolder(String folderName, String currentId) throws IOException {
            String pageToken = null;
            do {
                FileList result = mService.files().list()
                        .setQ("mimeType='application/vnd.google-apps.folder' and fullText contains '"+folderName+"' and trashed=false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, parents)")
                        .setPageToken(pageToken)
                        .execute();

                for(com.google.api.services.drive.model.File file: result.getFiles()) {
                    System.out.printf("Found file: %s (%s, %s)\n",
                            file.getName(), file.getId(), file.getParents().size());

                    if(currentId.equals(file.getId()))
                        return file;
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
            System.out.println("didnt find any matching folder");
            return null;
        }

        private String createFolder(String parentFolderId, String folderName) throws IOException {
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            if(parentFolderId != null)
                fileMetadata.setParents(Collections.singletonList(parentFolderId));


            com.google.api.services.drive.model.File file = mService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("Folder ID: " + file.getId());
            return file.getId();
        }

        private String uploadFileToApi(String folderId) throws IOException{
            java.io.File filePath = new java.io.File(filedir);

            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(filePath.getName());
            fileMetadata.setMimeType("text/plain");
            fileMetadata.setParents(Collections.singletonList(folderId));


            FileContent mediaContent = new FileContent("text/plain", filePath);
            com.google.api.services.drive.model.File file = mService.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();
            System.out.println("File ID: " + file.getId());

            return file.getId();
        }

        private void makeFileSharable(String fileId) throws IOException {
            JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
                @Override
                public void onFailure(GoogleJsonError e,
                                      HttpHeaders responseHeaders)
                        throws IOException {
                    // Handle error
                    System.err.println(e.getMessage());
                }

                @Override
                public void onSuccess(Permission permission,
                                      HttpHeaders responseHeaders)
                        throws IOException {
                    System.out.println("Permission ID: " + permission.getId());
                }
            };

            BatchRequest batch = mService.batch();
            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader");

            mService.permissions().create(fileId, permission)
                    .setFields("id")
                    .queue(batch, callback);

            batch.execute();
        }




        @Override
        protected void onPreExecute() {
            //mOutputText.setText("");
            mProgress.show();
            mNotifyManager.notify(notifyId, mBuilder.build());
        }

        @Override
        protected void onPostExecute(String link) {
            mProgress.dismiss();
            mBuilder.setOngoing(false)
                    .setProgress(0,0,false)
                    .setContentText("Upload Complete");
            mNotifyManager.notify(notifyId, mBuilder.build());



            File file = new File(exportedFilePath);
            file.delete();

            if(toShare) {
                shareFromFileId(noteModel, context, link);
            }else{
                Toast.makeText(context,
                        context.getString(R.string.uploaded_to_drive)+" ("+context.getString(R.string.app_name)+")",
                        Toast.LENGTH_SHORT).show();
            }


            /*if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Drive API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }*/
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    gpm.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            gpm.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(context,getString(R.string.the_following_error_occurred)
                            + mLastError.getMessage(),Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context,getString(R.string.request_cancelled), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void shareFromFileId(NoteModel noteModel, Context context, String fileId){
        String newDesc = "";
        if (!noteModel.getDescription().isEmpty())
            newDesc = "\n" + noteModel.getDescription();


        String shareBody = context.getString(R.string.shared_via_space)+ context.getString(R.string.app_name) + "\n\n" + context.getString(R.string.code)+": " + noteModel.getCode() + newDesc + "\n" + makeSharableLink(fileId);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.shared_via_space)+context.getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.app_name)+", "+context.getString(R.string.share_via)));
    }

    private static String makeSharableLink(String fileId){
        return "https://definex.in/picturenotes?id="+fileId;
    }

    @Override
    public void onBackPressed() {
        if(deleteMode)
            setDeletMode(false);
        else{
            super.onBackPressed();
        }

    }
}
