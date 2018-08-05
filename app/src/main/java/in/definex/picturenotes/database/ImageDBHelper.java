package in.definex.picturenotes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


/**
 * Created by adam_ on 26-09-2016.
 */
public class ImageDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "imageDb.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_NOTES_ENTRIES =
            "CREATE TABLE " + NoteEntry.TABLE_NAME + " (" +
                    NoteEntry.COLUMN_CODE + TEXT_TYPE + " PRIMARY KEY"+ COMMA_SEP +
                    NoteEntry.COLUMN_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    NoteEntry.COLUMN_FAVOURITE + INT_TYPE + COMMA_SEP +
                    NoteEntry.COLUMN_READONLY_PASSWORD + TEXT_TYPE + COMMA_SEP +
                    NoteEntry.COLUMN_PASSWORD + TEXT_TYPE+ " )";

            ;
    private static final String SQL_CREATE_IMAGE_ENTRIES =
            "CREATE TABLE " + ImageEntry.TABLE_NAME + " (" +
                    ImageEntry._ID + " INTEGER PRIMARY KEY," +
                    ImageEntry.COLUMN_CODE + TEXT_TYPE + COMMA_SEP +
                    ImageEntry.COLUMN_IMAGE_NAME + TEXT_TYPE + COMMA_SEP +
                    ImageEntry.COLUMN_IMAGE_URL + TEXT_TYPE + COMMA_SEP +
                    ImageEntry.COLUMN_IMAGE_NUMBER + INT_TYPE +" )";

    private static final String SQL_DELETE_IMAGE_ENTRIES =
            "DROP TABLE IF EXISTS " + ImageEntry.TABLE_NAME;
    private static final String SQL_DELETE_NOTE_ENTRIES =
            "DROP TABLE IF EXISTS " + NoteEntry.TABLE_NAME;


    public ImageDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NOTES_ENTRIES);
        db.execSQL(SQL_CREATE_IMAGE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_IMAGE_ENTRIES);
        db.execSQL(SQL_DELETE_NOTE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static class NoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "code_table";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_DESCRIPTION= "description";
        public static final String COLUMN_FAVOURITE= "favourite";
        public static final String COLUMN_READONLY_PASSWORD = "readonly";
        public static final String COLUMN_PASSWORD= "password";
    }

    public static class ImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "image_table";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_IMAGE_NAME = "image_name";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_IMAGE_NUMBER = "image_number";
    }

}
