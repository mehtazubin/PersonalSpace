package com.zubin.personalspace;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FireBaseService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    ChatMessage message = child.getValue(ChatMessage.class);
                    if(!message.getMessageUser().equalsIgnoreCase(FirebaseAuth.getInstance()
                            .getCurrentUser().getDisplayName())
                            && !message.getNotified()
                            && !getClassStatus()) {
                        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                        resultIntent.putExtra("menu", "Chat");
                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        getApplicationContext(),
                                        0,
                                        resultIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.ic_chat)
                                        .setContentTitle(message.getMessageUser())
                                        .setContentText(message.getMessageText())
                                        .setContentIntent(resultPendingIntent);

                        NotificationManager mNotifyMgr;
                        mNotifyMgr = (NotificationManager) getApplication().getSystemService(NOTIFICATION_SERVICE);
                        Notification noti = mBuilder.build();
                        noti.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
                        mNotifyMgr.notify(001, noti);
                        child.getRef().child("notified").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean getClassStatus() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        System.out.println(componentInfo);
        return componentInfo.getPackageName().equals("com.zubin.personalspace");
    }
}