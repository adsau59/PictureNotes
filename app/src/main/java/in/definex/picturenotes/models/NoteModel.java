package in.definex.picturenotes.models;

import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.ArrayList;
import java.util.List;

import in.definex.picturenotes.activity.MainActivity;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.DaoException;

/**
 * Created by adam_ on 29-11-2016.
 */

@Entity
public class NoteModel {

    @Id(autoincrement = true)
    private Long id;

    private String code;
    private String description;
    private boolean isFav;
    private String readonly;
    private String password;

    @ToMany(referencedJoinProperty = "nodeId")
    private List<ImageData> imageDatas;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 826845092)
    private transient NoteModelDao myDao;

    //region custom constructors
    public NoteModel(String code, String description) {
        this(code, description, false, "", "");
    }

    public NoteModel(String code, String description, boolean isFav){
        this(code, description, isFav, "", "");
    }

    public NoteModel(String code, String description, boolean isFav, String readonly, String password) {
        this(null, code, description, isFav, readonly, password);
    }


    //endregion

    //region Static Functions
    public static boolean DoesCodeExists(String code){
        return GetDao().queryBuilder().where(NoteModelDao.Properties.Code.eq(code)).build().unique() != null;
    }

    public static List<NoteModel> getFavNotesFromDB(Context context){
        return GetDao().queryBuilder().where(NoteModelDao.Properties.IsFav.eq(true)).build().list();
    }

    public static List<NoteModel> getAllNotes(Context context){
        return GetDao().loadAll();
    }
    public static String[] getAllCodes(Context context){

        List<NoteModel> notes = GetDao().loadAll();

        List<String> codes = new ArrayList<>();
        for(NoteModel n: notes)
            codes.add(n.code);

        return codes.toArray(new String[0]);
    }

    public static NoteModelDao GetDao()
    {
        return MainActivity.GetDaoSession().getNoteModelDao();
    }
    //endregion

    //region GetSet
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


    public boolean getIsFav() {
        return this.isFav;
    }


    public void setIsFav(boolean isFav) {
        this.isFav = isFav;
    }


    public String getReadonly() {
        return this.readonly;
    }


    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }
    //endregion

    //region methods
    public void updateAllImages(){
        for(ImageData i: getImageDatas())
            i.update();
    }

    public void updateFavStatusInDB(boolean fav){
        this.isFav = fav;
        update();
    }

    public NoteModel changeCodeAndSaveToDB(String newCode){
        this.code = newCode;
        update();
        return this;
    }

    public static NoteModel GetNoteByCode(String code){
        return GetDao().queryBuilder().where(NoteModelDao.Properties.Code.eq(code)).build().unique();

    }

    public void saveNoteInDB(){
        GetDao().save(this);
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

    public void deleteNoteAndImagesFromDB(){
        //delete images
        for(ImageData i: imageDatas)
            i.delete();

        //delete note
        delete();
    }
    //endregion

    //region Dao Generated


    @Generated(hash = 1532285157)
    public NoteModel() {
    }

    @Generated(hash = 827459215)
    public NoteModel(Long id, String code, String description, boolean isFav, String readonly, String password) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.isFav = isFav;
        this.readonly = readonly;
        this.password = password;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1458652996)
    public synchronized void resetImageDatas() {
        imageDatas = null;
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

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 834232635)
    public List<ImageData> getImageDatas() {
        if (imageDatas == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ImageDataDao targetDao = daoSession.getImageDataDao();
            List<ImageData> imageDatasNew = targetDao._queryNoteModel_ImageDatas(id);
            synchronized (this) {
                if (imageDatas == null) {
                    imageDatas = imageDatasNew;
                }
            }
        }
        return imageDatas;
    }

    
/** called by internal mechanisms, do not call yourself. */
@Generated(hash = 1253770181)
public void __setDaoSession(DaoSession daoSession) {
    this.daoSession = daoSession;
    myDao = daoSession != null ? daoSession.getNoteModelDao() : null;
}

    //endregion
}
