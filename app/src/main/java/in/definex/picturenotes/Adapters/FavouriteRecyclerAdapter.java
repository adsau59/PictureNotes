package in.definex.picturenotes.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import in.definex.picturenotes.R;
import in.definex.picturenotes.models.Note;

/**
 * Created by adam_ on 02-12-2016.
 */

public class FavouriteRecyclerAdapter extends RecyclerView.Adapter<FavouriteRecyclerAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    private List<Note> notes;
    private ThumbnailBitmaps[] bitmaps;
    public Bitmap defaultBitmap;

    //region helper classes
    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView codeTv;
        TextView descTv;
        ImageView imageView1;
        ImageView imageView2;
        TextView moreTv;

        View itemView;

        public MyViewHolder(View itemView) {
            super(itemView);

            codeTv = itemView.findViewById(R.id.codeFavTv);
            descTv = itemView.findViewById(R.id.descFavTv);
            imageView1 = itemView.findViewById(R.id.favImageView1);
            imageView2 = itemView.findViewById(R.id.favImageView2);
            moreTv = itemView.findViewById(R.id.favMoreTv);
            this.itemView = itemView;
        }
    }

    class ThumbnailBitmaps{
        Bitmap bitmap1;
        Bitmap bitmap2;

        public ThumbnailBitmaps(Bitmap bitmap1, Bitmap bitmap2) {
            this.bitmap1 = bitmap1;
            this.bitmap2 = bitmap2;
        }
    }
    //endregion

    public FavouriteRecyclerAdapter(Context context, List<Note> notes){
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.notes = notes;

        defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.defaut_img);

        bitmaps = new ThumbnailBitmaps[notes.size()];
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.favourite_recycler_list_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final int pos = position;

        holder.codeTv.setText(notes.get(pos).getCode());
        holder.descTv.setText(notes.get(pos).getDescription());

        int moreNumber = notes.get(pos).getImageDatas().size() -2;

        if(moreNumber>0)
            holder.moreTv.setText(String.format(context.getString(R.string.space_more), moreNumber));
        else
            holder.itemView.findViewById(R.id.view5).setVisibility(View.INVISIBLE);

        holder.itemView.setOnClickListener(v -> onItemClick(pos));


        if(bitmaps[pos] == null)
            new AsyncImageLoad(holder.imageView1, holder.imageView2, position)
                    .executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            notes.get(pos).getImageDatas().get(0).getUrl(),
                            notes.get(pos).getImageDatas().get(1).getUrl()
                    );
        else{
            holder.imageView1.setImageBitmap(bitmaps[pos].bitmap1);
            holder.imageView2.setImageBitmap(bitmaps[pos].bitmap2);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void onItemClick(int pos){}

    private class AsyncImageLoad extends AsyncTask<String, Void, Bitmap[]> {

        private WeakReference<ImageView> weakReference;
        private WeakReference<ImageView> weakReference2;
        private int position;

        AsyncImageLoad(ImageView imageView, ImageView imageView2, int position){
            this.weakReference = new WeakReference<>(imageView);
            this.weakReference2 = new WeakReference<>(imageView2);
            this.position = position;
        }

        @Override
        protected Bitmap[] doInBackground(String... params) {

            Bitmap[] scaled = {urlToBitmap(params[0]), urlToBitmap(params[1])};

            bitmaps[position] = new ThumbnailBitmaps(scaled[0], scaled[1]);

            return scaled;
        }

        private Bitmap urlToBitmap(String url){
            if(url == null || url.isEmpty())
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.defaut_img);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(url, options);

            if(bitmap == null)
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.defaut_img);

            int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
            return Bitmap.createScaledBitmap(bitmap, 512, nh, true);
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmap) {
            ImageView imageView = weakReference.get();
            if (imageView != null) {
                if (bitmap[0] != null) {
                    imageView.setImageBitmap(bitmap[0]);
                }
            }

            imageView = weakReference2.get();
            if (imageView != null) {
                if (bitmap[1] != null) {
                    imageView.setImageBitmap(bitmap[1]);
                }
            }
        }
    }
}
