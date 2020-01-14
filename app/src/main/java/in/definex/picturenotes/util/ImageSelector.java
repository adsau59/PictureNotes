package in.definex.picturenotes.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.common.base.Function;

import java.io.File;

import in.definex.picturenotes.R;
import in.definex.picturenotes.models.ImageData;
import in.definex.picturenotes.models.NoteModel;

public class ImageSelector {


    private Activity activity;
    private  String code;

    public ImageSelector(Activity activity, String code){
        this.activity = activity;
        this.code = code;
    }

    public void StartIntent()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent,activity.getResources().getString(R.string.select_images)), DEFINE.GALLERY_CODE);
    }

    public void HandleCallback(final Intent intent,final NoteModel noteModel, final Function postCallback)
    {
        final ClipData data = intent.getClipData();

        new Thread(){
            @Override
            public void run() {
                int lastImageNumber = 0;

                if(intent.getClipData() != null) {
                    for (int i = 0; i < data.getItemCount(); i++) {

                        String path = get_image_path_from_uri(data.getItemAt(i).getUri());
                        path = move_or_copy_image(path);

                        ImageData imageData = new ImageData(++lastImageNumber, path, noteModel);
                        imageData.save();

                    }
                }else if(intent.getData() != null) {

                    String imagePath = get_image_path_from_uri(intent.getData());
                    String path = move_or_copy_image(imagePath);

                    ImageData imageData = new ImageData(++lastImageNumber, path, noteModel);
                    imageData.save();
                }
                activity.runOnUiThread(() -> {
                    postCallback.apply(null);
                });
            }
        }.start();
    }

    private String move_or_copy_image(String path){

        switch (UtilityFunctions.getCopyMode(activity)) {

            case COPY:
                String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PictureNotes/local/" + code + "/";
                String outputFile = outputPath + new File(path).getName();
                UtilityFunctions.copyFile(path, outputFile);
                return outputFile;

            case MOVE:
                String outputPath1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PictureNotes/local/" + code + "/";
                String outputFile1 = outputPath1 + new File(path).getName();
                UtilityFunctions.copyFile(path, outputFile1);
                UtilityFunctions.deleteImage(activity, path);
                return outputFile1;
        }

        return path;

    }

    private String get_image_path_from_uri(Uri uri) {
        String path = null, image_id = null;

        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            image_id = cursor.getString(0);
            image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
            cursor.close();
        }

        cursor = activity.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        if (cursor!=null) {
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return path;
    }


}
