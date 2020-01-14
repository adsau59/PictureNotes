package in.definex.picturenotes.models;

import android.content.Context;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.List;

import in.definex.picturenotes.activity.MainActivity;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.DaoException;

/**
 * Created by Adam on 27-07-2016.
 */
@Entity
public class ImageData {

    @Id(autoincrement = true)
    private Long id;

    private String name;
    private String url;
    private int number;

    private transient boolean selected;

    private Long nodeId;

    @ToOne(joinProperty = "nodeId")
    private NoteModel note;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1781744822)
    private transient ImageDataDao myDao;

    @Generated(hash = 1056330060)
    private transient Long note__resolvedKey;

    public ImageData(int number, String url, NoteModel noteModel){
        this(number, url, null, noteModel);
    }

    public ImageData(int number, String url, String name, NoteModel noteModel){
        this.number = number;
        this.url = url;
        this.name = name;
        setNote(noteModel);
    }



    @Generated(hash = 950102263)
    public ImageData() {
    }



    @Generated(hash = 376669339)
    public ImageData(Long id, String name, String url, int number, Long nodeId) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.number = number;
        this.nodeId = nodeId;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public Long getId() {
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
    public void setUrl(String url) {
        this.url = url;
    }
    public Boolean getSelected() {
        return this.selected;
    }



    public static ImageDataDao GetDao()
    {
        return MainActivity.GetDaoSession().getImageDataDao();
    }

    public void updateImageToDB(Context context){
        GetDao().update(this);
    }

    public  void changeCode(Context context, String code){
    }

    public void deleteImageFromDB(Context context){
        GetDao().delete(this);
    }

    public void saveImageDataToDB(){
        GetDao().save(this);
    }

    public static List<ImageData> getImagesFromCode(Context context, String code){
        return null;
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
    public void setId(Long id) {
        this.id = id;
    }
    public Long getNodeId() {
        return this.nodeId;
    }



    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }



    /** To-one relationship, resolved on first access. */
    @Generated(hash = 489069693)
    public NoteModel getNote() {
        Long __key = this.nodeId;
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
    @Generated(hash = 919884059)
    public void setNote(NoteModel note) {
        synchronized (this) {
            this.note = note;
            nodeId = note == null ? null : note.getId();
            note__resolvedKey = nodeId;
        }
    }



    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2025839145)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getImageDataDao() : null;
    }
}
