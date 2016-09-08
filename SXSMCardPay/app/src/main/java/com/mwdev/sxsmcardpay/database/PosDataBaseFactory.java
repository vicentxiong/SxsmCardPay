package com.mwdev.sxsmcardpay.database;

import android.content.Context;
import android.content.res.Resources;

import com.mwdev.sxsmcardpay.PosApplication;
import com.mwdev.sxsmcardpay.R;
import com.mwdev.sxsmcardpay.util.PosLog;
import com.ta.TAApplication;
import com.ta.util.db.TASQLiteDatabase;
import com.ta.util.db.TASQLiteDatabasePool;

import java.util.List;

/**
 * Created by xiongxin on 16-8-16.
 */
public class PosDataBaseFactory {
    private static final String TAG = PosDataBaseFactory.class.getName();
    private static PosDataBaseFactory mFactory;
    private TASQLiteDatabase mTaSqlDatabase;
    private TAApplication mContex;
    private Resources r;

    private PosDataBaseFactory(){};

    public static PosDataBaseFactory getIntance(){
        if(mFactory == null)
            mFactory = new PosDataBaseFactory();

        return mFactory;
    }

    /**
     * 初始化生成数据库
     *
     */
    public void initiedFactory(TAApplication cx){
        mContex = cx;
        r = mContex.getResources();
        mContex.setSQLiteDatabasePool(createSqliteDatabasePool(r.getString(R.string.db_name),
                                                                r.getInteger(R.integer.db_version)));
        mTaSqlDatabase = mContex.getSQLiteDatabasePool().getSQLiteDatabase();
        String[] tables =r.getStringArray(R.array.db_tables);
        try{
            for(int i = 0;i<tables.length;i++){
                Class c = Class.forName(tables[i]);
                if(mTaSqlDatabase!=null){
                    if(!mTaSqlDatabase.hasTable(c));
                        mTaSqlDatabase.creatTable(c);
                }
            }
        }catch (ClassNotFoundException ex){
            PosLog.w(TAG,ex.getMessage());
        }
        mContex.getSQLiteDatabasePool().closeSQLiteDatabase();
        mTaSqlDatabase=null;
    }

    /**
     *替换TAApplication 中的 TASQLiteDatabasePool实例化对象
     *
     */
    private TASQLiteDatabasePool createSqliteDatabasePool(String dbName,int dbVersion){
        TASQLiteDatabasePool pool = TASQLiteDatabasePool.getInstance(mContex, dbName, dbVersion, false);
        pool.createPool();
        return pool;
    }
    /**
     *链接数据库对象
     *
     */
    public void openPosDatabase(){
        if(mTaSqlDatabase==null){
            mContex.getSQLiteDatabasePool().createPool();
            mTaSqlDatabase = mContex.getSQLiteDatabasePool().getSQLiteDatabase();
        }
    }
    /**
     *断开链接
     *
     */
    public void closePosDatabase(){
        mContex.getSQLiteDatabasePool().closeSQLiteDatabase();
        mTaSqlDatabase=null;
    }

    /**
     *释放数据库，但并不断开链接 ，
     * 在多线程访问数据库时用到
     *
     */
    public void reselsePosDatabase(){
        if(mTaSqlDatabase!=null)
            mContex.getSQLiteDatabasePool().releaseSQLiteDatabase(mTaSqlDatabase);
    }

    public void insert(Object entity){
        if(mTaSqlDatabase!=null){
            boolean r =mTaSqlDatabase.insert(entity);
        }

    }

    public void delete(Object entity){
        if(mTaSqlDatabase!=null)
            mTaSqlDatabase.delete(entity);
    }

    public void delete(Class<?> clazz,String where){
        if(mTaSqlDatabase!=null)
            mTaSqlDatabase.delete(clazz,where);
    }

    public <T>List<T> query(Class<?> clazz, String where,
                      String groupBy, String having, String orderBy, String limit){
        List<T> list=null;
        if(mTaSqlDatabase!=null)
            list = mTaSqlDatabase.query(clazz,false,where,groupBy,having,orderBy,limit);
        return list;
    }
}
