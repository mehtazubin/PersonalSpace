package com.zubin.personalspace;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FirebaseService extends Service {
    public static final String KEY_NOTIFICATION_REPLY = "KEY_NOTIFICATION_REPLY";
    public static String Uid;
    public static String curUid;

    @Override
    public void onCreate() {
        super.onCreate();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("Messages");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    ChatMessage message = child.getValue(ChatMessage.class);
                    try {
                        if (message.getRecepientUid().equals(FirebaseAuth.getInstance()
                                .getCurrentUser().getUid())
                                && !MainActivity.isVisible
                                && !message.getNotified()) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("User")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("last").setValue(message.getMessageUser());
                            FirebaseDatabase.getInstance().getReference()
                                    .child("User")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("lastUid").setValue(message.getSenderUid());
                            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                            resultIntent.putExtra("menu", "Chat");
                            PendingIntent resultPendingIntent = null;
                            PendingIntent detailsPendingIntent =
                                    PendingIntent.getActivity(
                                            getApplicationContext(),
                                            0,
                                            resultIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );
                            if (Build.VERSION.SDK_INT < 24) {
                                resultPendingIntent = detailsPendingIntent;
                            } else {
                                resultIntent = new Intent(FirebaseService.this, ReplyReceiver.class);
                                resultIntent.putExtra("user", message.getMessageUser());
                                resultIntent.putExtra("content", message.getMessageText());
                                Uid = message.getRecepientUid();
                                curUid = message.getSenderUid();

                                resultIntent.putExtra("muid", message.getRecepientUid());
                                resultPendingIntent = PendingIntent.getBroadcast(
                                        FirebaseService.this,
                                        0,
                                        resultIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);
                            }

                            RemoteInput remoteInput = new RemoteInput.Builder(KEY_NOTIFICATION_REPLY)
                                    .setLabel("Reply")
                                    .build();
                            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                                    android.R.drawable.ic_menu_save, "Reply", resultPendingIntent)
                                    .addRemoteInput(remoteInput)
                                    .build();
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic_chat)
                                            .setColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()))
                                            .setContentTitle(message.getMessageUser())
                                            .setContentText(message.getMessageText())
                                            .setContentIntent(detailsPendingIntent)
                                            .addAction(replyAction);

                            NotificationManager mNotifyMgr;
                            mNotifyMgr = (NotificationManager) getApplication().getSystemService(NOTIFICATION_SERVICE);
                            Notification noti = mBuilder.build();
                            noti.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
                            mNotifyMgr.notify(001, noti);
                            Vibrator v = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(500);
                            child.getRef().child("notified").setValue(true);
                        }
                    } catch(Exception e){};
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

}