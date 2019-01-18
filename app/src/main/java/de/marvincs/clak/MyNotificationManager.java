package de.marvincs.clak;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class MyNotificationManager {

    private static int notificationID = 1;
    private static final String CHANNEL_ID = "CLAK";

    public MyNotificationManager(Context ctx) {
        createNotificationChannel(ctx);
    }

    private void createNotificationChannel(Context ctx) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ctx.getString(R.string.channel_name);
            String description = ctx.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    void show(Context ctx, NotificationCompat.Builder mBuilder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationID++, mBuilder.build());
    }

    void connected(Context ctx) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, "CLAK")
                .setSmallIcon(R.drawable.ic_stat_leak_add)
                .setContentTitle("CLAK - Connected")
                .setContentText("Successfully connected")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        show(ctx, mBuilder);

    }

    void notRUBNetwork(Context ctx) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, "CLAK")
                .setSmallIcon(R.drawable.ic_stat_leak_remove)
                .setContentTitle("CLAK - Not connected")
                .setContentText("IP could not be fetched. It is possible that you are not in the RUB network.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        show(ctx, mBuilder);
    }

    void notSelectedWIFI(Context ctx) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, "CLAK")
                .setSmallIcon(R.drawable.ic_stat_leak_remove)
                .setContentTitle("CLAK - Not connected")
                .setContentText("You cannot be connected, because you are not in your selected network.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        show(ctx, mBuilder);
    }

    void wrongCredentials(Context ctx) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, "CLAK")
                .setSmallIcon(R.drawable.ic_stat_leak_remove)
                .setContentTitle("CLAK - Not connected")
                .setContentText("Connection could not be established. Please check your credentials")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        show(ctx, mBuilder);
    }
}
