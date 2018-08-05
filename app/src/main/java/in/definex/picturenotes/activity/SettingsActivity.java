package in.definex.picturenotes.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import in.definex.picturenotes.R;
import in.definex.picturenotes.util.UtilityFunctions;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String[] settings = new String[]{getString(R.string.view_tutorial_again), getString(R.string.change_copy_mode),getString(R.string.faqs),getString(R.string.about_devs),getString(R.string.any_suggestions),getString(R.string.donate), getString(R.string.check_for_update)};
        ListView listView = (ListView)findViewById(R.id.setttingLv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settings);
        listView.setAdapter(adapter);
        final Context context = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        UtilityFunctions.setTutorialStatus(context, true);
                        Toast.makeText(context, R.string.go_back_to_home_to_view, Toast.LENGTH_LONG).show();
                        break;

                    case 1:
                        final View v1 = getCopyModeDialouge(context);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle(R.string.change_copy_mode)
                                .setMessage("When you select Image?")
                                .setView(v1)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        int mode = 0;
                                        switch (((RadioGroup)v1.findViewById(R.id.radioGroup)).getCheckedRadioButtonId()){
                                            case R.id.radio0:
                                                mode = 0;
                                                break;
                                            case R.id.radio1:
                                                mode = 1;
                                                break;
                                            case R.id.radio2:
                                                mode = 2;
                                                break;
                                        }

                                        UtilityFunctions.setCopyMode(context, UtilityFunctions.COPY_MODE.getMod(mode));

                                    }
                                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();

                        break;

                    case 2:
                        openWeb("http://definex.in/app/picturenotes/faqs");
                        break;

                    case 3:
                        startActivity(new Intent(context, AboutDeveloperActivity.class));
                        break;

                    case 4:
                        startActivity(new Intent(context, ContactUsActivity.class));
                        break;

                    case 5:
                        openWeb("http://definex.in/donate/picturenotes");
                        break;

                    case 6:


                        if(!checkForUpdateTapped){
                            Toast.makeText(context, "Checking for Updates", Toast.LENGTH_SHORT).show();
                            UtilityFunctions.checkUpdate(context, true);
                            checkForUpdateTapped= true;

                            new Thread(){
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(30000);
                                        checkForUpdateTapped = false;
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }

                        break;
                }
            }
        });




    }

    private boolean checkForUpdateTapped=false;


    public void openWeb(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private static View getCopyModeDialouge(Context c){
        LayoutInflater inflater1 = (LayoutInflater)c.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        final View v1 = inflater1.inflate(R.layout.copy_mode_choose_dialog_layout, null);
        /*v1.findViewById(R.id.deleteImageText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox)v1.findViewById(R.id.deleteImagesCheckBox);
                checkBox.setChecked(!checkBox.isChecked());
            }
        });*/

        UtilityFunctions.COPY_MODE copyMode = UtilityFunctions.getCopyMode(c);

        switch (copyMode){
            case NONE:
                ((RadioButton)v1.findViewById(R.id.radio0)).setChecked(true);
                break;
            case COPY:
                ((RadioButton)v1.findViewById(R.id.radio1)).setChecked(true);
                break;
            case MOVE:
                ((RadioButton)v1.findViewById(R.id.radio2)).setChecked(true);
                break;
        }

        return v1;
    }
}
