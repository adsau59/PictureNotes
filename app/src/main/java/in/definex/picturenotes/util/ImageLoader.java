package in.definex.picturenotes.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import in.definex.picturenotes.Adapters.ShowImageRecyclerAdapter;

/**
 * Created by Adam on 27-07-2016.
 */
public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    Context context;
    String imageName;

    private ShowImageRecyclerAdapter adapter;
    public ImageLoader(ImageView iv, ShowImageRecyclerAdapter adapter, Context context, String imageName) {
        this.imageViewReference = new WeakReference<ImageView>(iv);
        this.adapter = adapter;
        this.context = context;

        this.imageName = imageName;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(params[0], options);


        if(bitmap != null) {
            int nh = (int) (bitmap.getHeight() * (400.0 / bitmap.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 400, nh, true);
            adapter.bitmapHashMap.put(Integer.parseInt(params[1]), scaled);
            return scaled;
        }else{
            adapter.bitmapHashMap.put(Integer.parseInt(params[1]), adapter.defaultBitmap);
            return null;
        }

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView imageView = imageViewReference.get();
        if (imageView != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

    }

}
