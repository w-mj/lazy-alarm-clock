package alpha.alarm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


public class MainActivity extends AppCompatActivity {
    public static boolean DEBUG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button add_alarm_button = (Button) findViewById(R.id.add_alarm_button);
        final LinearLayout alarm_list = (LinearLayout) findViewById(R.id.alarm_list);
        final MainActivity mainActivity = this;
        add_alarm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MainActivity", "添加闹钟");
                alarm_list.addView(new AlarmItemView(mainActivity));
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

        if (!DEBUG) {
            debugButton.setVisibility(View.GONE);
        }
    }
}
