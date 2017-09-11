package alpha.alarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.LinkedList;

/**
 * Created by mj on 17-9-10.
 * 保存闹钟列表的单例类
 */

class AlarmList {
    LinkedList<AlarmItemView> list;
    private static AlarmList instance = null;

    private AlarmList() {
        list = new LinkedList<>();
    }

    static AlarmList getInstance() {
        if (instance == null) {
            synchronized(AlarmList.class) {
                instance = new AlarmList();
            }
        }
        return instance;
    }

    void readDataFromDataBase(Context context, LinearLayout viewList) {
        DataBase helper = new DataBase(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataBase.TABLE_NAME, new String[] {"Hour", "Minute", "Every", "Alive"}, null, null, null, null, null);
        while(cursor.moveToNext()) {
            int hour = cursor.getInt(cursor.getColumnIndex("Hour"));
            int minute = cursor.getInt(cursor.getColumnIndex("Minute"));
            int every = cursor.getInt(cursor.getColumnIndex("Every"));
            int alive = cursor.getInt(cursor.getColumnIndex("Alive"));
            AlarmItemView v = new AlarmItemView(context, hour, minute, every, alive != 0);
            list.add(v);
            viewList.addView(v);

            Log.i("读取时间", "h:" + hour + " m:" + minute + " every:" + every + " is alive" + alive);
        }
        cursor.close();
        db.close();
    }

    void insertToDatabase(Context context, int hour, int minute, int every, int alive) {
        DataBase helper = new DataBase(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Hour", hour);
        cv.put("Minute", minute);
        cv.put("Every", every);
        cv.put("Alive", alive);
        db.insert(DataBase.TABLE_NAME, null, cv);
        db.close();
        Log.i("添加时间", "h:" + hour + " m:" + minute + " every:" + every + " is alive" + alive);
    }

    void changeAndSaveToDataBase(Context context, int oldHour, int oldMinute, int oldEvery,
                                 int newHour, int newMinute, int newEvery, int isAlive) {
        DataBase helper = new DataBase(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        ContentValues cv = new ContentValues();
        cv.put("Hour", newHour);
        cv.put("Minute", newMinute);
        cv.put("Every", newEvery);
        cv.put("Alive", isAlive);
        db.update(DataBase.TABLE_NAME, cv, "Hour = ? and Minute = ? and Every = ?",
                new String[] {String.valueOf(oldHour), String.valueOf(oldMinute), String.valueOf(oldEvery)});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        Log.i("修改时间", "h:" + oldHour + " m:" + oldMinute + " every:" + oldEvery + "\n" +
                "h:" + newHour + " m:" + newMinute + " every:" + newEvery + " is alive" + isAlive);
    }

    void delete(Context context, int hour, int minute, int every) {
        DataBase helper = new DataBase(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DataBase.TABLE_NAME, "Hour = ? and Minute = ? and Every = ?",
                new String[] {String.valueOf(hour), String.valueOf(minute), String.valueOf(every)});
        db.close();
        Log.i("删除时间", "h:" + hour + " m:" + minute + " every:" + every);
    }
}
