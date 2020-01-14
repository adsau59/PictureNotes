package in.definex.picturenotes.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import in.definex.picturenotes.Adapters.FavouriteRecyclerAdapter;
import in.definex.picturenotes.models.Note;
import in.definex.picturenotes.R;

public class ViewAllNotes extends AppCompatActivity {

    Context c;
    FavouriteRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_notes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        c = this;

        List<Note> notes = Note.getAllNotes();

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.viewAllNotesRecycler);
        adapter = new FavouriteRecyclerAdapter(c, notes){
            public void onItemClick(int pos) {
                Log.d("Pressed", pos + "");
                Intent intent = new Intent(c, ShowImageActivity.class);
                intent.putExtra("code", notes.get(pos).getCode());
                startActivity(intent);

            }
        };

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Note> notes = Note.getAllNotes();

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.viewAllNotesRecycler);
        adapter = new FavouriteRecyclerAdapter(c, notes){
            public void onItemClick(int pos) {
                Log.d("Pressed", pos + "");
                Intent intent = new Intent(c, ShowImageActivity.class);
                intent.putExtra("code", notes.get(pos).getCode());
                startActivity(intent);

            }
        };

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
