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
import in.definex.picturenotes.models.FavouriteViewModel;
import in.definex.picturenotes.models.NoteModel;
import in.definex.picturenotes.R;

public class ViewAllNotes extends AppCompatActivity {

    Context c;
    List<FavouriteViewModel> favList;
    FavouriteRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_notes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        c = this;

        favList = new ArrayList<>();
        List<NoteModel> noteModels = NoteModel.getAllNotes(c);


        for (NoteModel noteModel : noteModels) {
            favList.add(FavouriteViewModel.noteToFavVM(noteModel, c));
            Log.d("code",noteModel.getCode());
        }

        //Log.d("size of fav", favList.size()+"");

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.viewAllNotesRecycler);
        adapter = new FavouriteRecyclerAdapter(c, favList){
            public void onItemClick(int pos) {
                Log.d("Pressed", pos + "");
                Intent intent = new Intent(c, ShowImageActivity.class);
                intent.putExtra("code", favList.get(pos).note.getCode());
                startActivity(intent);

            }
        };

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        favList = new ArrayList<>();
        List<NoteModel> noteModels = NoteModel.getAllNotes(c);


        for (NoteModel noteModel : noteModels) {
            favList.add(FavouriteViewModel.noteToFavVM(noteModel, c));
            Log.d("code",noteModel.getCode());
        }

        //Log.d("size of fav", favList.size()+"");

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.viewAllNotesRecycler);
        adapter = new FavouriteRecyclerAdapter(c, favList){
            public void onItemClick(int pos) {
                Log.d("Pressed", pos + "");
                Intent intent = new Intent(c, ShowImageActivity.class);
                intent.putExtra("code", favList.get(pos).note.getCode());
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
