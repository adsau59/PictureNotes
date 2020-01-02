package in.definex.picturenotes.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.File;

import in.definex.picturenotes.models.ImageData;
import in.definex.picturenotes.models.NoteModel;
import in.definex.picturenotes.R;
import in.definex.picturenotes.util.DEFINE;
import in.definex.picturenotes.util.ImageSelector;
import in.definex.picturenotes.util.UtilityFunctions;


/**
 * CreateNoteActivity
 *
 * Insert code and description, select images to create a note
 */
public class CreateNoteActivity extends AppCompatActivity {

    Activity c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    /**
     * Executed when next is clicked,
     * does form validation, then opens image selection
     * @param item R.id.action_next
     * @return false if form is incorrect
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_next){

            if(sv!=null)
                sv.hide();

            //form validation starts

            //get from edittext
            EditText codeEt = ((EditText)findViewById(R.id.codeEditText));
            code = codeEt.getText().toString().toLowerCase().trim();
            final String dsc = ((EditText)findViewById(R.id.descriptionEditText)).getText().toString();

            //if code not entered
            if(code.isEmpty()){
                Toast.makeText(this, R.string.enter_code_to_create_a_note, Toast.LENGTH_LONG).show();
                return false;
            }

            //code too long
            if(code.length()> DEFINE.CODE_CHAR_LIMIT){
                Toast.makeText(this, R.string.the_name_is_too_long, Toast.LENGTH_LONG).show();
                return false;
            }

            //description too long
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

            //form validation ends

            //create new note and start image selection using fish buns
            noteModel = new NoteModel(code,dsc,false);

            imageSelector = new ImageSelector(this, code);
            imageSelector.StartIntent();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ImageSelector imageSelector;
    NoteModel noteModel;

    /**
     * Executed after returned from image selection
     * @param requestCode Define.ALBUM_REQUEST_CODE
     * @param resultCode RESULT_OK if success
     * @param intent `data.getStringArrayListExtra(Define.INTENT_PATH)` gets list of images
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode != DEFINE.GALLERY_CODE && resultCode != Activity.RESULT_OK)
            return;

        noteModel.saveNoteInDB(this);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.loading_please_wait);
        progressDialog.show();

        imageSelector.HandleCallback(intent, (o)->{
            progressDialog.dismiss();
            openNote(code);
            return null;
        });

    }



    private void openNote(String code){
        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra("code",code);
        finish();
        startActivity(intent);
    }




}
