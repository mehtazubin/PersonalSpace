package com.zubin.personalspace;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.zubin.personalspace.R.id.toolbar;


/**
 * Created by zubin on 11/9/2016.
 */


public class ChatFragment extends Fragment {
    private FirebaseRecyclerAdapterMultiLayout adapter;
    private EditText input;
    private String curUser;
    private String curUid;
    private String myUser;
    private String myUid;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        myUser = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference uid = FirebaseDatabase.getInstance().getReference().child("User").child(myUid);
        uid.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    ((MainActivity) getContext()).setCurUid((String) dataSnapshot.child("lastUid").getValue());
                    ((MainActivity) getContext()).setCurUser((String) dataSnapshot.child("last").getValue());
                } catch (NullPointerException e){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            curUser = bundle.getString("name");
            curUid = bundle.getString("uid");
            FirebaseDatabase.getInstance().getReference().child("User").child(myUid).child("last").setValue(curUser);
            FirebaseDatabase.getInstance().getReference().child("User").child(myUid).child("lastUid").setValue(curUid);
            ((MainActivity) getActivity()).setCurUser(curUser);
            ((MainActivity) getActivity()).setCurUid(curUid);
        }
        else {
            curUser = ((MainActivity) getActivity()).getCurUser();
            curUid = ((MainActivity) getActivity()).getCurUid();
        }
        if(curUser != null && !curUser.equals(myUser)) {
            ((MainActivity) getActivity()).setTitle(curUser);
        }
        else {
            ((MainActivity) getActivity()).setTitle("Chat");
        }
        FloatingActionButton fab = (FloatingActionButton) container.getRootView().findViewById(R.id.fab);
        fab.hide();
        final RecyclerView recycler = (RecyclerView) view.findViewById(R.id.messages);
        displayChatMessages(recycler);
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
                            .child("Messages")
                            .push()
                            .setValue(new ChatMessage(message,
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser()
                                            .getDisplayName(),
                                    curUid,myUid
                            ));
                }
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("Messages");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                scrollBottom(recycler);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return view;
    }

    public void displayChatMessages(RecyclerView recycler){

        recycler.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setStackFromEnd(true);
        recycler.setLayoutManager(manager);
        adapter = new FirebaseRecyclerAdapterMultiLayout(
                myUid, curUid,
                R.layout.message_right,
                R.layout.message_left,
                FirebaseDatabase.getInstance().getReference().child("Messages"));
        recycler.setAdapter(adapter);
    }

    public void scrollBottom(RecyclerView recycler){
        recycler.scrollBy(0,500);
    }
}
