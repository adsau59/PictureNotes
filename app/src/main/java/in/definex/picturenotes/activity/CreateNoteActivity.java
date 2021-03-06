package in.definex.picturenotes.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.define.Define;

import java.io.File;
import java.util.ArrayList;

import in.definex.picturenotes.models.ImageData;
import in.definex.picturenotes.models.NoteModel;
import in.definex.picturenotes.R;
import in.definex.picturenotes.util.DEFINE;
import in.definex.picturenotes.util.UtilityFunctions;

public class CreateNoteActivity extends AppCompatActivity {

    Activity c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        c = this;

        //if from ShowImageActivity
        String foriegnCode = getIntent().getStringExtra("code");
        if(foriegnCode!=null){
            EditText editText = ((EditText)findViewById(R.id.codeEditText));
            EditText editText1 = ((EditText)findViewById(R.id.descriptionEditText));
            editText.setText(foriegnCode);
            editText1.setText(getIntent().getStringExtra("description"));
            editText.setEnabled(false);
            editText1.setEnabled(false);
        }



        if(UtilityFunctions.getTutorialStatus(this)){
            final EditText editText1 = (EditText)findViewById(R.id.codeEditText);
            final EditText editText2 = (EditText)findViewById(R.id.descriptionEditText);



            editText1.setText("tutorial note");
            editText2.setText(R.string.just_to_show_how_it_works);

            editText1.setGravity(Gravity.CENTER);
            editText2.setGravity(Gravity.CENTER);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (tutCounter){
                        case 0:
                            sv.setShowcase(new ViewTarget(findViewById(R.id.descriptionEditText)), false);
                            sv.setContentTitle(getString(R.string.creating_a_note));
                            sv.setContentText(getString(R.string.here_we_write_description));
                            break;

                        case 1:
                            sv.setShowcase(new ViewTarget(findViewById(R.id.action_next)),false);
                            sv.setContentTitle(getString(R.string.selecting_images));
                            sv.setContentText(getString(R.string.now_press_next));
                            sv.hideButton();
                            break;

                        case 2:
                            sv.hide();
                            editText1.setGravity(Gravity.NO_GRAVITY);
                            editText2.setGravity(Gravity.NO_GRAVITY);
                            break;

                    }
                    tutCounter++;
                }
            };

            sv = new ShowcaseView.Builder(this)
                    //.withMaterialShowcase()
                    .setTarget(new ViewTarget(findViewById(R.id.codeEditText)))
                    .replaceEndButton(R.layout.view_custom_button)
                    .setOnClickListener(onClickListener)
                    .setContentTitle(getString(R.string.creating_a_note))
                    .setContentText(getString(R.string.here_we_write_a_unique_code))
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .build();
            sv.setButtonText(getString(R.string.next));



        }


    }


    ShowcaseView sv;
    int tutCounter = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_image_menu,menu);
        MenuItem menuItem = menu.getItem(0);


        if(menuItem.getItemId() == R.id.action_next)
            menuItem.setIcon(UtilityFunctions.makeIcon(this,"\uf105"));

        return true;
    }

    String code;

    //NOTEME if next clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_next){

            if(sv!=null)
                sv.hide();

            EditText codeEt = ((EditText)findViewById(R.id.codeEditText));
            //get from edittext

            code = codeEt.getText().toString().toLowerCase().trim();
            final String dsc = ((EditText)findViewById(R.id.descriptionEditText)).getText().toString();

            if(code.isEmpty()){
                Toast.makeText(this, R.string.enter_code_to_create_a_note, Toast.LENGTH_LONG).show();
                return false;
            }

            if(code.length()> DEFINE.CODE_CHAR_LIMIT){
                Toast.makeText(this, R.string.the_name_is_too_long, Toast.LENGTH_LONG).show();
                return false;
            }

            if(dsc.length()>DEFINE.DESC_CHAR_LIMIT){
                Toast.makeText(this, R.string.the_desc_is_too_long, Toast.LENGTH_LONG).show();
                return false;
            }

            //if code already exists
            if(NoteModel.isCodeInDB(this, code)){
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);


                builder.setMessage(R.string.code_already_exists)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openNote(code);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }

            //else
            //new NoteModel(code, dsc, false).saveNoteInDB(this);
            noteModel = new NoteModel(code,dsc,false);
            FishBun.BaseProperty baseProperty = FishBun.with(CreateNoteActivity.this);
            baseProperty.setActionBarColor(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .setCamera(true)
                    .setButtonInAlbumActivity(true)
                    .startAlbum();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    NoteModel noteModel;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Define.ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    noteModel.saveNoteInDB(this);
                    final ArrayList<String> paths = data.getStringArrayListExtra(Define.INTENT_PATH);
                    final Context context = this;
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle(R.string.loading_please_wait);
                    progressDialog.show();

                    new Thread(){
                        @Override
                        public void run() {
                            int lastImageNumber = 0;
                            for (String path : paths) {
                                lastImageNumber++;

                                switch (UtilityFunctions.getCopyMode(context)){
                                    case NONE:
                                        break;

                                    case COPY:
                                        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PictureNotes/local/"+code+"/";
                                        String outputFile = outputPath+new File(path).getName();
                                        UtilityFunctions.copyFile(path, outputFile);
                                        path = outputFile;
                                        break;

                                    case MOVE:
                                        String outputPath1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PictureNotes/local/"+code+"/";
                                        String outputFile1 = outputPath1+new File(path).getName();
                                        UtilityFunctions.copyFile(path, outputFile1);

                                        UtilityFunctions.deleteImage(context, path);

                                        path = outputFile1;
                                        break;

                                }

                                new ImageData(lastImageNumber, path).saveImageDataToDB(context, code);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    openNote(code);
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }.start();
                }
                break;
        }
    }



    private void openNote(String code){
        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra("code",code);
        finish();
        startActivity(intent);
    }


}
