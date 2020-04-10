package ua.cn.stu.randomgallery.app.sync;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ua.cn.stu.randomgallery.app.R;

public class BootReceiver extends BroadcastReceiver {
    private static final int RQ_CODE = 1;
    public static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "default";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED
                .equals(intent.getAction())) return;
        prepareChannel(context);
        NotificationManagerCompat manager =
                NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID);
        String title = context.getString(R.string.update_title);
        String message = context
                .getString(R.string.update_message);
        String ticker = context
                .getString(R.string.update_ticker);
        String yes = context.getString(R.string.action_yes);
        String cancel = context
                .getString(R.string.action_cancel);
        builder
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setTicker(ticker)
                .addAction(0, yes, createConfirmAction(context))
                .addAction(0, cancel, createCancelAction(context));
        Notification notification = builder.build();
        manager.notify(NOTIFICATION_ID, notification);
    }
    private void prepareChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            String name = context
                    .getString(R.string.default_channel_name);
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            name,
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
            NotificationManagerCompat manager =
                    NotificationManagerCompat.from(context);
            manager.createNotificationChannel(channel);
        }
    }
    private PendingIntent createConfirmAction(Context context) {
        Intent intent = new Intent(context,
                ActionsHandlerService.class);
        intent.setAction(ActionsHandlerService
                .ACTION_CHECK_FOR_UPDATES);
        intent.putExtra(ActionsHandlerService
                .EXTRA_FROM_NOTIFICATION, true);
        return PendingIntent.getService(context, RQ_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
    private PendingIntent createCancelAction(Context context) {
        Intent intent = new Intent(context,
                ActionsHandlerService.class);
        intent.setAction(ActionsHandlerService.ACTION_CANCEL);
        return PendingIntent.getService(context, RQ_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
