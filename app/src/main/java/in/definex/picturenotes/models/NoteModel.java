package in.definex.picturenotes.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import in.definex.picturenotes.database.DbService;
import in.definex.picturenotes.database.ImageDBHelper;

/**
 * Created by adam_ on 29-11-2016.
 */

public class NoteModel {

    private int id;
    private String code;
    private String description;
    private boolean isFav;
    private String readonly;
    private String password;

    public NoteModel(String code, String description) {
        this(code, description, false, "", "");
    }


    public NoteModel(String code, String description, boolean isFav){
        this(code, description, isFav, "", "");
    }

    public NoteModel(String code, String description, boolean isFav, String readonly, String password) {
        this.code = code;
        this.description = description;
        this.isFav = isFav;
        this.readonly = readonly;
        this.password = password;
    }

    public NoteModel(NoteModel n){
        this(n.code, n.description, n.isFav, n.readonly, n.password);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean bool){
        isFav = bool;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCode(String code) {
        this.code = code;
    }





    public void updateFavStatusInDB(Context context, boolean fav){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(ImageDBHelper.NoteEntry.COLUMN_FAVOURITE, fav);

        String selection = ImageDBHelper.NoteEntry.COLUMN_CODE + " = ?";
        String[] selectionArgs = { code };

        db.update(
                ImageDBHelper.NoteEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();
    }

    public void updateNoteDb(Context context){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(ImageDBHelper.NoteEntry.COLUMN_CODE, code);
        values.put(ImageDBHelper.NoteEntry.COLUMN_DESCRIPTION, description);
        values.put(ImageDBHelper.NoteEntry.COLUMN_FAVOURITE, isFav);

        String selection = ImageDBHelper.NoteEntry.COLUMN_CODE + " = ?";
        String[] selectionArgs = { code };

        db.update(
                ImageDBHelper.NoteEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();
    }

    public static NoteModel getNoteByCode(Context context, String code){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getReadableDatabase();

        String[] projection1 = {
                ImageDBHelper.NoteEntry.COLUMN_CODE,
                ImageDBHelper.NoteEntry.COLUMN_DESCRIPTION,
                ImageDBHelper.NoteEntry.COLUMN_FAVOURITE
        };

        String selection1 = ImageDBHelper.NoteEntry.COLUMN_CODE + " = ?";
        String[] selectionArgs1 = {code};

        Cursor c1 = db.query(
                ImageDBHelper.NoteEntry.TABLE_NAME,
                projection1,
                selection1,
                selectionArgs1,
                null,
                null,
                null
        );

        if(!c1.moveToFirst()){
            return null;
        }

        NoteModel noteModel = new NoteModel(
                code,
                c1.getString(c1.getColumnIndex(ImageDBHelper.NoteEntry.COLUMN_DESCRIPTION)),
                c1.getInt(c1.getColumnIndex(ImageDBHelper.NoteEntry.COLUMN_FAVOURITE)) == 1
        );
        c1.close();
        db.close();

        return noteModel;
    }

    public void saveNoteInDB(Context context){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ImageDBHelper.NoteEntry.COLUMN_CODE, code);
        values.put(ImageDBHelper.NoteEntry.COLUMN_DESCRIPTION, description);
        values.put(ImageDBHelper.NoteEntry.COLUMN_FAVOURITE, 0);
        values.put(ImageDBHelper.NoteEntry.COLUMN_READONLY_PASSWORD,"");
        values.put(ImageDBHelper.NoteEntry.COLUMN_PASSWORD,"");


        db.insert(ImageDBHelper.NoteEntry.TABLE_NAME, null, values);

        db.close();
    }

    public static boolean isCodeInDB(Context context, String code){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getReadableDatabase();

        String[] projection1 = {
                ImageDBHelper.NoteEntry.COLUMN_CODE
        };

        String selection1 = ImageDBHelper.NoteEntry.COLUMN_CODE + " = ?";
        String[] selectionArgs1 = {code};

        Cursor c1 = db.query(
                ImageDBHelper.NoteEntry.TABLE_NAME,
                projection1,
                selection1,
                selectionArgs1,
                null,
                null,
                null
        );

        boolean ans = c1.moveToFirst();
        c1.close();

        return ans;
    }

    public static List<NoteModel> getFavNotesFromDB(Context context){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getReadableDatabase();

        String[] projection1 = {
                ImageDBHelper.NoteEntry.COLUMN_CODE,
                ImageDBHelper.NoteEntry.COLUMN_DESCRIPTION
        };

        String selection1 = ImageDBHelper.NoteEntry.COLUMN_FAVOURITE + " = ?";
        String[] selectionArgs1 = {"1"};

        Cursor c1 = db.query(
                ImageDBHelper.NoteEntry.TABLE_NAME,
                projection1,
                selection1,
                selectionArgs1,
                null,
                null,
                null
        );

        List<NoteModel> noteModels = new ArrayList<>();

        if(c1.moveToFirst()){
            do {
                noteModels.add(new NoteModel(
                                c1.getString(c1.getColumnIndex(ImageDBHelper.NoteEntry.COLUMN_CODE)),
                                c1.getString(c1.getColumnIndex(ImageDBHelper.NoteEntry.COLUMN_DESCRIPTION)),
                                true
                ));
            }while (c1.moveToNext());

        }
        c1.close();

        db.close();

        return noteModels;
    }
    public static List<NoteModel> getAllNotes(Context context){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getReadableDatabase();

        Cursor c1 = db.rawQuery("SELECT * FROM "+ ImageDBHelper.NoteEntry.TABLE_NAME, null);

        List<NoteModel> noteModels = new ArrayList<>();

        if(c1.moveToFirst()){
            do {
                noteModels.add(new NoteModel(
                                c1.getString(c1.getColumnIndex(ImageDBHelper.NoteEntry.COLUMN_CODE)),
                                c1.getString(c1.getColumnIndex(ImageDBHelper.NoteEntry.COLUMN_DESCRIPTION)),
                                true
                ));
            }while (c1.moveToNext());

        }
        c1.close();
        db.close();

        return noteModels;
    }

    public void cacheSharedNote(Context context,String fileId){
        SharedPreferences.Editor editor = context.getSharedPreferences("prefs",Context.MODE_PRIVATE).edit();
        editor.putString(code+"fileId", fileId);
        editor.putBoolean(code+"integrity",true);
        editor.apply();
    }

    public void cachedShareNoteDisturbed(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("prefs",Context.MODE_PRIVATE).edit();
        editor.putBoolean(code+"integrity",false);
        editor.apply();
    }

    public String getCachedSharedNote(Context context){
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(code+"fileId","");
    }

    public Boolean getCachedNoteIntegrity(Context context){
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean(code+"integrity",false);
    }

    public static String[] getAllCodes(Context context){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getReadableDatabase();

        Cursor c1 = db.rawQuery("SELECT "+ ImageDBHelper.NoteEntry.COLUMN_CODE+" FROM "+ ImageDBHelper.NoteEntry.TABLE_NAME, null);

        List<String> codes = new ArrayList<>();

        if(c1.moveToFirst()){
            do {
                codes.add(c1.getString(c1.getColumnIndex(ImageDBHelper.NoteEntry.COLUMN_CODE)));
            }while (c1.moveToNext());

        }
        c1.close();
        db.close();

        return codes.toArray(new String[codes.size()]);
    }


    public void deleteNoteAndImagesFromDB(Context c){

        //delete images
        DbService.deleteAllImageWithCode(c, code);
        //delete note
        DbService.deleteNoteWithCode(c, code);
    }

    public NoteModel changeCodeAndSaveToDB(Context context, String newCode){
        List<ImageData> list = ImageData.getImagesFromCode(context,code);
        for(ImageData data: list)
            data.changeCode(context, newCode);

        NoteModel noteModel = new NoteModel(this);
        noteModel.code = newCode;

        DbService.deleteNoteWithCode(context, code);

        noteModel.saveNoteInDB(context);

        return noteModel;
    }

}
