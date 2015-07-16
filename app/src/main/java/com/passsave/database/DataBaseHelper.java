package com.passsave.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.passsave.model.UserPass;

/**
 * Created by GaoBiao on 2015/6/14.
 */
public class DataBaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String CREATE_TABLE_SQL = "CREATE TABLE t_userpass (_id INTEGER PRIMARY KEY AUTOINCREMENT, username VARCHAR( 255 ), password VARCHAR( 255 ), domain VARCHAR( 255 ),name VARCHAR( 255 ), user_id INTEGER);";
    private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS t_userpass";
    private static final String DATABASE_NAME = "PassSave.db";
    private static final int DATABASE_VERSION = 2;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, UserPass.class);
        } catch (Exception e) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
        try {
            TableUtils.dropTable(connectionSource, UserPass.class, true);
        } catch (Exception e) {
        }
    }


}
