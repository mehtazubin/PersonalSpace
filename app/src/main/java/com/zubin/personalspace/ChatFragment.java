package com.zubin.personalspace;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by zubin on 11/9/2016.
 */


public class ChatFragment extends Fragment {
    private FirebaseRecyclerAdapterMultiLayout adapter;
    //private FireBaseListAdapterMultiLayout adapter;
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
                if(!message.trim().isEmpty()) {
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
                }
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                displayChatMessages(view);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return view;
    }


    public void displayChatMessages(final View view){

        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.messages);
        recycler.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setStackFromEnd(true);
        recycler.setLayoutManager(manager);
        adapter = new FirebaseRecyclerAdapterMultiLayout(R.layout.message_right, R.layout.message_left, FirebaseDatabase.getInstance().getReference());
        recycler.setAdapter(adapter);
    }
}
