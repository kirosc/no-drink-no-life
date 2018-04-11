package ccn2279.a16031806a.nodrinknolife;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.race604.drawable.wave.WaveDrawable;

import java.util.Calendar;
import java.util.TimeZone;

import ccn2279.a16031806a.nodrinknolife.sync.AlarmReceiver;
import ccn2279.a16031806a.nodrinknolife.utilities.NotificationUtils;
import ccn2279.a16031806a.nodrinknolife.utilities.SharedPreferencesUtils;

/**
 * Updated by Kiros Choi on 2018/04/11.
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "Debug_NoDrinkNoLife";

    private ImageView healthBar_iv;
    private TextView healthValue_tv;
    private WaveDrawable mWaveDrawable;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //      Initialize View object      //
        healthBar_iv = findViewById(R.id.healthBar);
        healthValue_tv = findViewById(R.id.healthValue);

        mWaveDrawable = new WaveDrawable(this, R.drawable.health_bar);
        healthBar_iv.setImageDrawable(mWaveDrawable);
        mWaveDrawable.setIndeterminate(false);
        mWaveDrawable.setWaveAmplitude(5);
        mWaveDrawable.setWaveSpeed(3);
        updateHealthUI((float) 100);
        //      End of initialization       //

        //ReminderUtilities.scheduleChargingReminder(this);
        int value = SharedPreferencesUtils.initSharedPreferences(this);

        Log.d(TAG, String.valueOf(value));

        mSharedPreferences = getSharedPreferences(SharedPreferencesUtils.PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        registerAlarm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setting) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.stat) {
            Intent intent = new Intent(this, StatActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, key);
        if (key.equals(getString(R.string.character_health))) {
            updateHealthUI(sharedPreferences.getFloat(key, (float) 0));
        }
    }

    public void testNotification(View view) {
        NotificationUtils.remindUserToDrink(this);
    }

    /**
     * Update the health UI with new health number.
     *
     * @param health The new health of the character.
     */
    public void updateHealthUI(float health) {
        healthValue_tv.setText(String.format("%.1f%%", health));
        // Min: 2570; Max: 9990
        health = 2570 + (7420 * health / 100);
        mWaveDrawable.setLevel((int) health);
        // Reload the Drawable
        mWaveDrawable.invalidateSelf();
    }

    /**
     * Set an alarm on every midnight
     */
    public void registerAlarm() {
        // Set a pending intent to be executed
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_SAVE_DAILY_DRINKS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the specific time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        // Repeat the alarm everyday
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }
}
