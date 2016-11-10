package com.zubin.personalspace;

import android.app.Fragment;
import android.app.NotificationManager;
import android.database.DataSetObserver;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by zubin on 11/9/2016.
 */

public class ChatFragment extends Fragment {
    private FirebaseListAdapter<ChatMessage> adapter;
    private EditText input;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ImageButton send =
                (ImageButton) view.findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input = (EditText) view.findViewById(R.id.input);
                String message = input.getText().toString();
                input.setText("");

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(message,
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName())
                        );

                displayChatMessages(view);
            }
        });

        return view;
    }

    public void displayChatMessages(final View view){
        ListView listOfMessages = (ListView) view.findViewById(R.id.messages);

        adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(R.drawable.ic_chat)
                                .setContentTitle(model.getMessageUser())
                                .setContentText(model.getMessageText());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
                NotificationManager mNotifyMgr;
                mNotifyMgr = (NotificationManager) mActivity.getSystemService(NOTIFICATION_SERVICE);

                mNotifyMgr.notify(001, mBuilder.build());
            }
        };

        listOfMessages.setAdapter(adapter);
    }
}
