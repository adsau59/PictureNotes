package in.definex.picturenotes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import in.definex.picturenotes.models.ImageData;

/**
 * Created by adam_ on 27-11-2016.
 */

public class DbService {


    public static void deleteAllImageWithCode(Context context, String code){
        String selection = ImageDBHelper.ImageEntry.COLUMN_CODE + " = ?";
        String[] selectionArgs = {code};

        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getWritableDatabase();

        db.delete(
                ImageDBHelper.ImageEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        db.close();
    }

    public static void updateAllImages(Context context,List<ImageData> imageDatas){
        for(ImageData imageData: imageDatas)
            imageData.updateImageToDB(context);
    }

    public static void deleteNoteWithCode(Context c, String code){
        String selection1 = ImageDBHelper.NoteEntry.COLUMN_CODE + " = ?";
        String[] selectionArgs1 = {code};

        ImageDBHelper imageDBHelper = new ImageDBHelper(c);
        SQLiteDatabase db = imageDBHelper.getWritableDatabase();

        db.delete(
                ImageDBHelper.NoteEntry.TABLE_NAME,
                selection1,
                selectionArgs1
        );
        db.close();
    }


}
