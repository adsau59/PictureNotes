package in.definex.picturenotes.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import in.definex.picturenotes.database.ImageDBHelper;

/**
 * Created by adam_ on 02-12-2016.
 */

public class FavouriteViewModel {

    public NoteModel note;
    public String imageUrl1;
    public String imageUrl2;
    public int numberOfImages;

    public Bitmap bitmap1;
    public Bitmap bitmap2;

    public FavouriteViewModel(NoteModel note, String imageUrl1, String imageUrl2, int numberOfImages) {
        this.note = note;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
        this.numberOfImages = numberOfImages;

        bitmap1 = null;
        bitmap2 = null;
    }

    public void setBitmaps(Bitmap bitmap1, Bitmap bitmap2) {
        this.bitmap1 = bitmap1;
        this.bitmap2 = bitmap2;
    }

    public boolean isBitmapsNull(){
        if(bitmap1 == null || bitmap2 == null)
            return true;
        else
            return false;
    }


    public static FavouriteViewModel noteToFavVM(NoteModel noteModel, Context context){

        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getReadableDatabase();

        String code = noteModel.getCode();

        String selection = ImageDBHelper.ImageEntry.COLUMN_CODE + " = ?";
        String[] selectionArg = {code};
        String[] projection = {
                ImageDBHelper.ImageEntry.COLUMN_IMAGE_URL,
        };
        Cursor cursor = db.query(
                ImageDBHelper.ImageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArg,
                null,
                null,
                null
        );
        String url1="";
        String url2="";
        if(cursor.moveToFirst())
            url1 = cursor.getString(cursor.getColumnIndex(ImageDBHelper.ImageEntry.COLUMN_IMAGE_URL));
        if(cursor.moveToNext())
            url2 = cursor.getString(cursor.getColumnIndex(ImageDBHelper.ImageEntry.COLUMN_IMAGE_URL));


        FavouriteViewModel fvm = new FavouriteViewModel(noteModel, url1, url2, cursor.getCount());

        db.close();

        return fvm;


    }
}
