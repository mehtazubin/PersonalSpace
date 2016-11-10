package com.zubin.personalspace;


import android.app.NotificationManager;
import android.app.Service;
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

public class FireBaseService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        String user = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        System.out.println(user);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    ChatMessage message = child.getValue(ChatMessage.class);
                    if(!message.getMessageUser().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()) && !message.getNotified()) {
                        child.getRef().child("notified").setValue(true);
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.ic_chat)
                                        .setContentTitle(message.getMessageUser())
                                        .setContentText(message.getMessageText());
                        NotificationManager mNotifyMgr;
                        mNotifyMgr = (NotificationManager) getApplication().getSystemService(NOTIFICATION_SERVICE);

                        mNotifyMgr.notify(001, mBuilder.build());
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
}