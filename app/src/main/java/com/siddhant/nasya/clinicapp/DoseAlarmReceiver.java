package com.siddhant.nasya.clinicapp;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class DoseAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_SNOOZE = "com.siddhant.nasya.ACTION_SNOOZE";
    public static final String ACTION_NORMAL = "com.siddhant.nasya.ACTION_NORMAL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String doseType = intent.getStringExtra("DOSE_TYPE");
        if (doseType == null) doseType = "Dose";

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // =========================================================
        // NEW: SNOOZE LOGIC (Reschedules for 15 minutes later)
        // =========================================================
        if (ACTION_SNOOZE.equals(action)) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent snoozeIntent = new Intent(context, DoseAlarmReceiver.class);
            snoozeIntent.setAction(ACTION_NORMAL);
            snoozeIntent.putExtra("DOSE_TYPE", doseType);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    (int) System.currentTimeMillis(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 15); // The 15-minute allowable window

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                }
            }

            int notifId = intent.getIntExtra("NOTIF_ID", 0);
            nm.cancel(notifId); // Dismiss the current notification

            Toast.makeText(context, doseType + " snoozed for 15 minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        // =========================================================
        // NORMAL NOTIFICATION LOGIC
        // =========================================================
        String channelId = "TRIAL_CHANNEL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Trial Alerts", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        int notifId = (int) System.currentTimeMillis();

        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the Snooze Button Intent
        Intent snoozeActionIntent = new Intent(context, DoseAlarmReceiver.class);
        snoozeActionIntent.setAction(ACTION_SNOOZE);
        snoozeActionIntent.putExtra("DOSE_TYPE", doseType);
        snoozeActionIntent.putExtra("NOTIF_ID", notifId);
        PendingIntent snoozePi = PendingIntent.getBroadcast(context, notifId, snoozeActionIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Pratimarsha Nasya Reminder")
                .setContentText("Time for your " + doseType + " 2 drops now. Tap 'I applied' after applying.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_popup_reminder, "💤 Snooze 15m", snoozePi); // Adds the button to the notification

        nm.notify(notifId, builder.build());
    }
}