package alpha.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    public static boolean DEBUG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button add_alarm_button = (Button) findViewById(R.id.add_alarm_button);

        final LinearLayout alarm_list = (LinearLayout) findViewById(R.id.alarm_list);
        final MainActivity mainActivity = this;

        AlarmList.getInstance().readDataFromDataBase(this, alarm_list);

        add_alarm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MainActivity", "添加闹钟");
                AlarmItemView view = new AlarmItemView(mainActivity);
                alarm_list.addView(view);
                AlarmList.getInstance().insertToDatabase(mainActivity,
                        view.getCalendar().get(Calendar.HOUR_OF_DAY),
                        view.getCalendar().get(Calendar.MINUTE),
                        view.mEvery,
                        view.isAlive()?1:0);
            }
        });

        Button debugButton = (Button)findViewById(R.id.debug_button);
        debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity, Alarm.class);
                startActivity(intent);
                Log.i("MainActivity", "进入Alarm");
            }
        });

        Button button30s = (Button)findViewById(R.id.button_30s);
        button30s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmManager alarmManager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE); // 获得系统闹钟服务
                Intent intent = new Intent(mainActivity, Alarm.class);
                PendingIntent pi = PendingIntent.getActivity(mainActivity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30000, pi);
                Toast.makeText(mainActivity, "30s", Toast.LENGTH_SHORT).show();
            }
        });

        if (!DEBUG) {
            debugButton.setVisibility(View.GONE);
            button30s.setVisibility(View.GONE);
        }
    }
}
