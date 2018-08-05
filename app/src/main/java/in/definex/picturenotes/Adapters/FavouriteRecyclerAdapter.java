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
import java.util.List;

import in.definex.picturenotes.models.FavouriteViewModel;
import in.definex.picturenotes.R;

/**
 * Created by adam_ on 02-12-2016.
 */

public class FavouriteRecyclerAdapter extends RecyclerView.Adapter<FavouriteRecyclerAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    private List<FavouriteViewModel> favNotes;

    public Bitmap defaultBitmap;



    public FavouriteRecyclerAdapter(Context context, List<FavouriteViewModel> favNotes){
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.favNotes = favNotes;

        defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.defaut_img);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = inflater.inflate(R.layout.favourite_recycler_list_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(v);


        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final int pos = position;

        holder.codeTv.setText(favNotes.get(pos).note.getCode());
        holder.descTv.setText(favNotes.get(pos).note.getDescription());

        int moreNumber = favNotes.get(pos).numberOfImages -2;

        if(moreNumber>0)
            holder.moreTv.setText(moreNumber+context.getString(R.string.space_more));
        else
            holder.itemView.findViewById(R.id.view5).setVisibility(View.INVISIBLE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(pos);
            }
        });

        if(favNotes.get(pos).isBitmapsNull())
            new AsyncImageLoad(holder.imageView1, holder.imageView2, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,favNotes.get(pos).imageUrl1,favNotes.get(pos).imageUrl2);
        else{
            holder.imageView1.setImageBitmap(favNotes.get(pos).bitmap1);
            holder.imageView2.setImageBitmap(favNotes.get(pos).bitmap2);
        }

    }

    @Override
    public int getItemCount() {
        return favNotes.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView codeTv;
        TextView descTv;
        ImageView imageView1;
        ImageView imageView2;
        TextView moreTv;

        View itemView;

        public MyViewHolder(View itemView) {
            super(itemView);

            codeTv = (TextView)itemView.findViewById(R.id.codeFavTv);
            descTv = (TextView)itemView.findViewById(R.id.descFavTv);
            imageView1 = (ImageView)itemView.findViewById(R.id.favImageView1);
            imageView2 = (ImageView)itemView.findViewById(R.id.favImageView2);
            moreTv = (TextView)itemView.findViewById(R.id.favMoreTv);
            this.itemView = itemView;
        }
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

            favNotes.get(position).setBitmaps(scaled[0], scaled[1]);

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
