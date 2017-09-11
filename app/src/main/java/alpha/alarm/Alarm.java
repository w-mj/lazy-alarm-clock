package alpha.alarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class Alarm extends AppCompatActivity implements SensorEventListener{
    private boolean ringing = false;
    SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Alarm", "进入Alarm");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // 获得传感器服务
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);



        if (MainActivity.DEBUG)
            ringing = true;

        long now = System.currentTimeMillis();
        for (AlarmItemView v : AlarmList.getInstance().list) {
            if (Math.abs(v.getCalendar().getTimeInMillis() - now) < 5000) {
//                wakePhoneAndUnlock();
                ringing = true;
                startAlarm();  // 响铃
            }
        }

        // 如果被系统唤醒但是没有触发任意一个闹钟, 退出
        if (!ringing && !MainActivity.DEBUG)
            finish();
    }

//    //点亮屏幕并解锁
//    private void wakePhoneAndUnlock() {
//        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "WakeLock");
//        mWakelock.acquire();//唤醒屏幕//......
//        KeyguardManager mManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock mKeyguardLock = mManager.newKeyguardLock("Lock");
//        mKeyguardLock.disableKeyguard();
//        mWakelock.release();
//        //释放
//    }

    private void startAlarm() {}
    private void stopAlarm() {}

    private long startShakeTime = 0, lastDetectedTime = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        if (!ringing)
            return;

        if (sensorType == Sensor.TYPE_LINEAR_ACCELERATION) {
            float values[] = event.values;
            // 三个方向的加速度值
            float x = values[0];
            float y = values[1];
            float z = values[2];
            int value = 10;
            Log.i("out detected", "x=" + x + "y=" + y + "z=" + z);
            if (x >= value || x <= -value || y >= value || y <= -value || z >= value || z <= -value) {
                Log.i("in detected", "x=" + x + "y=" + y + "z=" + z + "total time=" + String.valueOf(System.currentTimeMillis() - startShakeTime));
                if (lastDetectedTime == 0 || System.currentTimeMillis() - lastDetectedTime > 2000) {
                    // 超过一秒没摇, 重新计时
                    Log.i("Alarm", "重新计时 last detected time=" + lastDetectedTime + "now = " + System.currentTimeMillis());
                    startShakeTime = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - startShakeTime > 6000) {
                    // 持续摇动六秒, 停止闹钟
                    Log.i("Alarm", "停止闹钟");
                    stopAlarm();
                    Toast.makeText(this, "停止闹钟", Toast.LENGTH_LONG).show();
                }
                lastDetectedTime = System.currentTimeMillis();

            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }
}
