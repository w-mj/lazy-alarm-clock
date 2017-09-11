package alpha.alarm;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.SyncStateContract;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by mj on 17-9-10.
 * 闹钟项目View
 */

public class AlarmItemView extends RelativeLayout implements View.OnLongClickListener{
    private Calendar mCalendar;
    private TextView mTime;
    private TextView mDate;
    private Switch mSwitcher;
    public int mEvery = 0;

    public AlarmItemView(Context context) {
        super(context);
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.HOUR_OF_DAY, mCalendar.get(Calendar.HOUR_OF_DAY) + 1); // 默认一小时后
        init();
        if (mSwitcher.isChecked())
            setAlarm(true, true);
    }

    public AlarmItemView(Context context, int hour, int minute, int every, boolean isAlive) {
        super(context);
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        if (mCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            // 如果闹钟时间比当前时间早, 设为第二天
            mCalendar.setTimeInMillis(mCalendar.getTimeInMillis() + 24 * 60 * 60 * 1000);
        }
        init();
        mSwitcher.setChecked(isAlive);
        if (mSwitcher.isChecked())
            setAlarm(true, false);
        mEvery = every;
    }

    private void init() {
        inflate(getContext(), R.layout.alarm_view, this);

        mTime = (TextView)findViewById(R.id.time);
        mDate = (TextView)findViewById(R.id.date);
        mSwitcher = (Switch)findViewById(R.id.switcher);
        mTime.setText(String.format(Locale.CHINA, "%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY) , mCalendar.get(Calendar.MINUTE)));

        mTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarm(false, false);  // 修改前先取消之前设定的事件
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Log.i("setTimeDialog", "修改时间");
                        int oldHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                        int oldMinute = mCalendar.get(Calendar.MINUTE);
                        int oldEvery = mEvery;
                        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mCalendar.set(Calendar.MINUTE, minute);
                        mTime.setText(String.valueOf(hourOfDay) + ":" + minute);
                        if (mSwitcher.isChecked())
                            setAlarm(true, true);
                        AlarmList.getInstance().changeAndSaveToDataBase(getContext(), oldHour, oldMinute, oldEvery,
                                mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), mEvery, mSwitcher.isChecked()?1:0);
                    }
                }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true
                );
                timePickerDialog.show();
            }
        });
        mTime.setOnLongClickListener(this);

        mDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mCalendar.set(Calendar.YEAR, year);
                        mCalendar.set(Calendar.MONTH, month);
                        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    }
                }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
        mDate.setVisibility(GONE);

        AlarmList.getInstance().list.add(this);

        setOnLongClickListener(this);

        mSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAlarm(isChecked, true);
            }
        });
    }

    public Calendar getCalendar() {return mCalendar;}

    public boolean isAlive() {
        return mSwitcher.isChecked();
    }


    @Override
    public boolean onLongClick(View v) {
        final AlarmItemView t = this;
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("你确定要删除吗");
        dialog.setNegativeButton("取消", null);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlarmList.getInstance().list.remove(t);
                t.setVisibility(GONE);
                t.mSwitcher.setChecked(false);
                AlarmList.getInstance().delete(getContext(), mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), mEvery);
            }
        });
        dialog.show();
        return false;
    }

    private void setAlarm(boolean isChecked, boolean showToast) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE); // 获得系统闹钟服务
        Intent intent = new Intent(getContext(), Alarm.class);
        PendingIntent pi = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (isChecked) {
            if (showToast) {
                long deltaMinutes = (long) ((mCalendar.getTimeInMillis() - System.currentTimeMillis()) / 60000.0 + 0.5);
                long hour = deltaMinutes / 60;
                long minute = deltaMinutes % 60;
                String toast = "闹钟将在";
                if (hour != 0) {
                    toast += String.format(Locale.CHINA, "%d小时", hour);
                }
                if (minute != 0) {
                    toast += String.format(Locale.CHINA, "%d分钟", minute);
                }
                toast += "后响铃";
                Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
            }

            mCalendar.set(Calendar.SECOND, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pi);
            Log.i("Alarm", "注册定时事件");
        } else {
            alarmManager.cancel(pi);
            Log.i("Alarm", "取消定时事件");
        }
    }
}
