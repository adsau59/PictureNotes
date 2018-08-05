package in.definex.picturenotes.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import in.definex.picturenotes.R;
import in.definex.picturenotes.util.UtilityFunctions;

public class AboutDeveloperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_developer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DevViewModel[] models = new DevViewModel[]{new DevViewModel("Adam Saudagar","Programmer"), new DevViewModel("Paresh Parab", "Designer")};
        ListView lv = (ListView)findViewById(R.id.listview);

        DevAdapter adapter = new DevAdapter(this, R.layout.dev_list_layout,models);
        lv.setAdapter(adapter);


        ImageView webIcon = (ImageView)findViewById(R.id.imageView3);
        ImageView faceIcon = (ImageView)findViewById(R.id.imageView2);
        ImageView twitterIcon = (ImageView)findViewById(R.id.imageView1);

        webIcon.setImageDrawable(UtilityFunctions.makeIcon(this,"\uf0ac",30, Color.GRAY));
        faceIcon.setImageDrawable(UtilityFunctions.makeIcon(this,"\uf082",30, Color.parseColor("#3b5998")));
        twitterIcon.setImageDrawable(UtilityFunctions.makeIcon(this,"\uf081",30, Color.parseColor("#0084b4")));

        final Context context = this;

        webIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWeb("http://definex.in/");
            }
        });
        faceIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWeb("https://www.facebook.com/DefinexStudio/");
            }
        });
        twitterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWeb("https://twitter.com/DefinexStudio");
            }
        });
    }

    public void openWeb(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    class DevViewModel{
        String name;
        String job;

        public DevViewModel(String name, String job) {
            this.name = name;
            this.job = job;
        }
    }

    class DevAdapter extends ArrayAdapter<DevViewModel>{

        public DevAdapter(Context context, int resource, DevViewModel[] objects) {
            super(context, resource, objects);
        }

        private class ViewHolder{
            TextView nameTv;
            TextView jobTv;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if(convertView==null){

                convertView = LayoutInflater.from(this.getContext())
                        .inflate(R.layout.dev_list_layout, parent, false);

                holder = new ViewHolder();
                holder.nameTv = (TextView) convertView.findViewById(R.id.text1);
                holder.jobTv = (TextView) convertView.findViewById(R.id.text2);

                convertView.setTag(holder);

            }else
                holder = (ViewHolder)convertView.getTag();


            DevViewModel model = getItem(position);

            if(model!=null){
                holder.nameTv.setText(model.name);
                holder.jobTv.setText(model.job);
            }

            return convertView;
        }
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
}
