package in.definex.picturenotes.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;

import in.definex.picturenotes.activity.MainActivity;
import in.definex.picturenotes.database.ImageDBHelper;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Adam on 27-07-2016.
 */
@Entity
public class ImageData {

    @Id
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
    @Generated(hash = 1328844819)
    public ImageData(int id, String name, String url, int number, Boolean selected) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.number = number;
        this.selected = selected;
    }
    @Generated(hash = 950102263)
    public ImageData() {
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

    public void setId(int id) {
        this.id = id;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Boolean getSelected() {
        return this.selected;
    }




    public void updateImageToDB(Context context){
        DaoSession daoSession = ((MainActivity) context.getApplicationContext()).getDaoSession();
        daoSession.getImageDataDao().update(this);
    }

    public  void changeCode(Context context, String code){
    }

    public void deleteImageFromDB(Context context){
        DaoSession daoSession = ((MainActivity) context.getApplicationContext()).getDaoSession();
        daoSession.getImageDataDao().delete(this);
    }

    public void saveImageDataToDB(Context context, String code){
        DaoSession daoSession = ((MainActivity) context.getApplicationContext()).getDaoSession();
        daoSession.getImageDataDao().save(this);
    }

    public static List<ImageData> getImagesFromCode(Context context, String code){
        return null;
    }
}
