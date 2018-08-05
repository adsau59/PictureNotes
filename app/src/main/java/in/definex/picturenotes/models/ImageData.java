package in.definex.picturenotes.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import in.definex.picturenotes.database.ImageDBHelper;

/**
 * Created by Adam on 27-07-2016.
 */
public class ImageData {
    private int id;
    private String name;
    private String url;
    private int number;
    private Boolean selected;

    public ImageData(int number, String url) {
        this(-1, number, url, "Image");
    }
    public ImageData(int number, String url, String name) {
        this(-1, number, url, name);
    }

    public ImageData(int id, int number, String url, String name) {
        this.id = id;
        this.number = number;
        this.url = url;
        this.name = name;
        selected = false;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setName(String name) {
        this.name = name;
    }





    public void updateImageToDB(Context context){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(ImageDBHelper.ImageEntry.COLUMN_IMAGE_NAME, name);
        values.put(ImageDBHelper.ImageEntry.COLUMN_IMAGE_NUMBER, number);

        // Which row to update, based on the title
        String selection = ImageDBHelper.ImageEntry._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(getId()) };

        db.update(
            ImageDBHelper.ImageEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs);

        db.close();
    }

    public  void changeCode(Context context, String code){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(ImageDBHelper.ImageEntry.COLUMN_CODE, code);

        String selection = ImageDBHelper.ImageEntry._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(getId()) };

        db.update(
                ImageDBHelper.ImageEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();
    }

    public void deleteImageFromDB(Context context){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getWritableDatabase();

        String selection = ImageDBHelper.ImageEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(getId()) };

        db.delete(ImageDBHelper.ImageEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    public void saveImageDataToDB(Context context, String code){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getWritableDatabase();
        ContentValues values = null;
        values = new ContentValues();
        values.put(ImageDBHelper.ImageEntry.COLUMN_IMAGE_URL, getUrl());
        values.put(ImageDBHelper.ImageEntry.COLUMN_IMAGE_NAME, getName());
        values.put(ImageDBHelper.ImageEntry.COLUMN_CODE, code);
        values.put(ImageDBHelper.ImageEntry.COLUMN_IMAGE_NUMBER, getNumber());
        db.insert(ImageDBHelper.ImageEntry.TABLE_NAME, null, values);
        db.close();
    }

    public static List<ImageData> getImagesFromCode(Context context, String code){
        ImageDBHelper imageDBHelper = new ImageDBHelper(context);
        SQLiteDatabase db = imageDBHelper.getReadableDatabase();



        //getting images from db
        String[] projection = {
                ImageDBHelper.ImageEntry._ID,
                ImageDBHelper.ImageEntry.COLUMN_IMAGE_NUMBER,
                ImageDBHelper.ImageEntry.COLUMN_IMAGE_NAME,
                ImageDBHelper.ImageEntry.COLUMN_IMAGE_URL
        };

        String selection = ImageDBHelper.ImageEntry.COLUMN_CODE + " = ?";
        String[] selectionArgs = {code};
        String sortOrder = ImageDBHelper.ImageEntry.COLUMN_IMAGE_NUMBER + " ASC";

        Cursor c = db.query(
                ImageDBHelper.ImageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<ImageData> imageDatas = new ArrayList<>();

        //if images are there, then add them to imageDatas
        if(c.moveToFirst()){
            do{
                imageDatas.add(new ImageData(
                        Integer.parseInt(c.getString(c.getColumnIndex(ImageDBHelper.ImageEntry._ID))),
                        Integer.parseInt(c.getString(c.getColumnIndex(ImageDBHelper.ImageEntry.COLUMN_IMAGE_NUMBER))),
                        c.getString(c.getColumnIndex(ImageDBHelper.ImageEntry.COLUMN_IMAGE_URL)),
                        c.getString(c.getColumnIndex(ImageDBHelper.ImageEntry.COLUMN_IMAGE_NAME))
                ));
            }while(c.moveToNext());
        }

        db.close();
        c.close();

        return imageDatas;

    }
}
