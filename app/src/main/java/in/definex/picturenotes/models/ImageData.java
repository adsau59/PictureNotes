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
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.DaoException;

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

    private int nodeId;

    @ToOne(joinProperty = "nodeId")
    private NoteModel note;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1781744822)
    private transient ImageDataDao myDao;

    @Generated(hash = 2139518147)
    private transient Integer note__resolvedKey;

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
    @Generated(hash = 1558377871)
    public ImageData(int id, String name, String url, int number, Boolean selected, int nodeId) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.number = number;
        this.selected = selected;
        this.nodeId = nodeId;
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
    public int getNodeId() {
        return this.nodeId;
    }
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 579068990)
    public NoteModel getNote() {
        int __key = this.nodeId;
        if (note__resolvedKey == null || !note__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            NoteModelDao targetDao = daoSession.getNoteModelDao();
            NoteModel noteNew = targetDao.load(__key);
            synchronized (this) {
                note = noteNew;
                note__resolvedKey = __key;
            }
        }
        return note;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1518509770)
    public void setNote(@NotNull NoteModel note) {
        if (note == null) {
            throw new DaoException(
                    "To-one property 'nodeId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.note = note;
            nodeId = note.getId();
            note__resolvedKey = nodeId;
        }
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2025839145)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getImageDataDao() : null;
    }
}
