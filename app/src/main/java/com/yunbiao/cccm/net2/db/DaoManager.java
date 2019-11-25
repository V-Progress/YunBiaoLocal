package com.yunbiao.cccm.net2.db;

import android.util.Log;

import com.yunbiao.cccm.APP;

import org.greenrobot.greendao.database.Database;

import java.util.List;

public class DaoManager {
    private static final String TAG = "DaoManager";
    private static DaoManager daoManager = new DaoManager();
    private final String DB_NAME = "yb_meeting_db";
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public static final long FAILURE = -1;
    public static final long SUCCESS = 0;

    public static DaoManager get(){
        return daoManager;
    }

    private DaoManager(){
    }

    public void initDb(){
        Log.e(TAG, "initDb: ");
        MySQLiteHelper helper =new MySQLiteHelper(APP.getContext(),DB_NAME,null);
        Log.e(TAG, "initDb: " + helper);
        Database db = helper.getWritableDb();
        Log.e(TAG, "initDb: " + db);
        daoMaster = new DaoMaster(db);
        Log.e(TAG, "initDb: " + daoMaster);
        daoSession = daoMaster.newSession();
        Log.e(TAG, "initDb: " + daoSession);
        daoSession.clear();
        daoSession.getDailyDao().detachAll();
        daoSession.getTimeSlotDao().detachAll();
        daoSession.getItemBlockDao().detachAll();
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }

    public DaoMaster getDaoMaster(){
        return daoMaster;
    }

    public <T> long add(T clazz){
        if(daoSession == null){
            return FAILURE;
        }
        return daoSession.insert(clazz);
    }

    public <T> long addOrUpdate(T clazz){
        if(daoSession == null){
            return FAILURE;
        }
       return  daoSession.insertOrReplace(clazz);
    }

    public <T>long update(T t){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.update(t);
        return SUCCESS;
    }

    public <T>List<T> queryAll(Class<T> clazz){
        if(daoSession == null){
            return null;
        }
        return daoSession.loadAll(clazz);
    }

    public <T>long delete(T t){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.delete(t);
        return SUCCESS;
    }

    public <T>long deleteAll(Class<T> clazz){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.deleteAll(clazz);
        return SUCCESS;
    }

    public void deleteItemBlock(ItemBlock itemBlock){
        daoSession.getItemBlockDao().delete(itemBlock);
    }

    public void deleteTimeSlot(TimeSlot timeSlot){
        daoSession.getTimeSlotDao().delete(timeSlot);
    }

    public void deleteDaily(Daily daily){
        daoSession.getDailyDao().delete(daily);
    }

    public Daily queryByDate(String date){
        if(daoSession == null){
            return null;
        }
        return daoSession.getDailyDao().queryBuilder().where(DailyDao.Properties.Date.eq(date)).build().unique();
    }

    public List<Daily> queryExcludeDate(String date){
        if(daoSession == null){
            return null;
        }
        return daoSession.getDailyDao().queryBuilder().where(DailyDao.Properties.Date.notEq(date)).build().list();
    }

    public void deleteByDate(String date){
        if(daoSession == null){
            return;
        }

        Daily unique = daoSession.getDailyDao().queryBuilder().where(DailyDao.Properties.Date.eq(date)).build().unique();
        if(unique != null){
            for (TimeSlot timeSlot : unique.getTimeSlots()) {
                daoSession.getTimeSlotDao().delete(timeSlot);
            }
            daoSession.getDailyDao().delete(unique);
        }
    }

    public ItemBlock queryByFileName(String name) {
        return daoSession.getItemBlockDao().queryBuilder().where(ItemBlockDao.Properties.Name.eq(name)).unique();
    }
    public List<ItemBlock> queryByDateTime(String dateTime) {
        return daoSession.getItemBlockDao().queryBuilder().where(ItemBlockDao.Properties.DateTime.eq(dateTime)).list();
    }
}
