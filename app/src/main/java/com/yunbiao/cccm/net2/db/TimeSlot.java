package com.yunbiao.cccm.net2.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.Date;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Administrator on 2019/11/19.
 */

@Entity
public class TimeSlot {

    @Id
    private Long id;

    private String parentDate;

    @Unique
    private String dateTime;

    private String start;
    private String end;

    @Transient
    private Date startDate;

    @Transient
    private Date endDate;

    private boolean isPlaying;

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @ToMany(referencedJoinProperty = "dateTime")
    private List<ItemBlock> itemBlocks;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 994661112)
    private transient TimeSlotDao myDao;

    @Generated(hash = 1005210608)
    public TimeSlot(Long id, String parentDate, String dateTime, String start, String end,
            boolean isPlaying) {
        this.id = id;
        this.parentDate = parentDate;
        this.dateTime = dateTime;
        this.start = start;
        this.end = end;
        this.isPlaying = isPlaying;
    }

    @Generated(hash = 1337764006)
    public TimeSlot() {
    }

    public String getParentDate() {
        return parentDate;
    }

    public void setParentDate(String parentDate) {
        this.parentDate = parentDate;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Keep
    public List<ItemBlock> getItemBlocks() {
        if (itemBlocks == null) {
//            final DaoSession daoSession = this.daoSession;
//            if (daoSession == null) {
//                throw new DaoException("Entity is detached from DAO context");
//            }
//            ItemBlockDao targetDao = daoSession.getItemBlockDao();
//            List<ItemBlock> itemBlocksNew = targetDao._queryTimeSlot_ItemBlocks(dateTime);

            List<ItemBlock> itemBlocks = DaoManager.get().queryByDateTime(dateTime);
            synchronized (this) {
                if (this.itemBlocks == null) {
                    this.itemBlocks = itemBlocks;
                }
            }
        }
        return itemBlocks;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1550860155)
    public synchronized void resetItemBlocks() {
        itemBlocks = null;
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
    @Generated(hash = 1167801899)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTimeSlotDao() : null;
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "parentDate='" + parentDate + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", itemBlocks=" + itemBlocks +
                '}';
    }

    public boolean getIsPlaying() {
        return this.isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }
}
