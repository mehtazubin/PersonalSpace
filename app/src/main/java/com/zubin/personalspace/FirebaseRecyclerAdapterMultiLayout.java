package com.zubin.personalspace;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;


/**
 * This class is a generic way of backing an RecyclerView with a Firebase location.
 * It handles all of the child events at the given Firebase location. It marshals received data into the given
 * class type.
 * <p>
 * To use this class in your app, subclass it passing in all required parameters and implement the
 * populateViewHolder method.
 * <p>
 * <pre>
 *     private static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
 *         TextView messageText;
 *         TextView nameText;
 *
 *         public ChatMessageViewHolder(View itemView) {
 *             super(itemView);
 *             nameText = (TextView)itemView.findViewById(android.R.id.text1);
 *             messageText = (TextView) itemView.findViewById(android.R.id.text2);
 *         }
 *     }
 *
 *     FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder> adapter;
 *     DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
 *
 *     RecyclerView recycler = (RecyclerView) findViewById(R.id.messages_recycler);
 *     recycler.setHasFixedSize(true);
 *     recycler.setLayoutManager(new LinearLayoutManager(this));
 *
 *     adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder>(ChatMessage.class, android.R.layout.two_line_list_item, ChatMessageViewHolder.class, ref) {
 *         public void populateViewHolder(ChatMessageViewHolder chatMessageViewHolder, ChatMessage chatMessage, int position) {
 *             chatMessageViewHolder.nameText.setText(chatMessage.getName());
 *             chatMessageViewHolder.messageText.setText(chatMessage.getMessage());
 *         }
 *     };
 *     recycler.setAdapter(mAdapter);
 * </pre>
 *
 */
public class FirebaseRecyclerAdapterMultiLayout extends RecyclerView.Adapter<FirebaseRecyclerAdapterMultiLayout.ChatMessageViewHolder> {
    private static final String TAG = FirebaseRecyclerAdapterMultiLayout.class.getSimpleName();

    protected int mModelLayoutLeft;
    protected  int mModelLayoutRight;
    FirebaseArray mSnapshots;
    public class ChatMessageViewHolder extends RecyclerView.ViewHolder{
        TextView messageText;
        int layout;
        public ChatMessageViewHolder(View view, int layout){
            super(view);
            this.layout = layout;
            if(this.layout == R.layout.message_left){
                messageText = (TextView) view.findViewById(R.id.message_left_text);
            }
            else{
                messageText = (TextView) view.findViewById(R.id.message_right_text);
            }
        }
    }

    FirebaseRecyclerAdapterMultiLayout(
                            int modelLayoutRight, int modelLayoutLeft,
                            FirebaseArray snapshots) {
        mModelLayoutRight = modelLayoutRight;
        mModelLayoutLeft = modelLayoutLeft;
        mSnapshots = snapshots;

        mSnapshots.setOnChangedListener(new FirebaseArray.OnChangedListener() {
            @Override
            public void onChanged(EventType type, int index, int oldIndex) {
                switch (type) {
                    case ADDED:
                        notifyItemInserted(index);
                        break;
                    case CHANGED:
                        notifyItemChanged(index);
                        break;
                    case REMOVED:
                        notifyItemRemoved(index);
                        break;
                    case MOVED:
                        notifyItemMoved(oldIndex, index);
                        break;
                    default:
                        throw new IllegalStateException("Incomplete case statement");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                FirebaseRecyclerAdapterMultiLayout.this.onCancelled(databaseError);
            }
        });
    }

    /**
     *       Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayoutRight     This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of modelClass.
     *The class that hold references to all sub-views in an instance modelLayout.
     * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    public FirebaseRecyclerAdapterMultiLayout(
                                   int modelLayoutRight, int modelLayoutLeft,
                                   Query ref) {
        this(modelLayoutRight, modelLayoutLeft, new FirebaseArray(ref));
    }

    public void cleanup() {
        mSnapshots.cleanup();
    }

    @Override
    public int getItemCount() {
        return mSnapshots.getCount();
    }

    public ChatMessage getItem(int position) {
        return parseSnapshot(mSnapshots.getItem(position));
    }

    /**
     * This method parses the DataSnapshot into the requested type. You can override it in subclasses
     * to do custom parsing.
     *
     * @param snapshot the DataSnapshot to extract the model from
     * @return the model extracted from the DataSnapshot
     */
    protected ChatMessage parseSnapshot(DataSnapshot snapshot) {
        return snapshot.getValue(ChatMessage.class);
    }

    public DatabaseReference getRef(int position) {
        return mSnapshots.getItem(position).getRef();
    }

    @Override
    public long getItemId(int position) {
        // http://stackoverflow.com/questions/5100071/whats-the-purpose-of-item-ids-in-android-listview-adapter
        return mSnapshots.getItem(position).getKey().hashCode();
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new ChatMessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder viewHolder, int position) {
        ChatMessage model = getItem(position);
        populateViewHolder(viewHolder, model, position);
    }




    @Override
    public int getItemViewType(int position) {
        if(FirebaseAuth.getInstance().getCurrentUser().getDisplayName().equalsIgnoreCase(getItem(position).getMessageUser())){
            return mModelLayoutRight;
        }
        else{
            return mModelLayoutLeft;
        }
    }

    /**
     * This method will be triggered in the event that this listener either failed at the server,
     * or is removed as a result of the security and Firebase Database rules.
     *
     * @param error A description of the error that occurred
     */
    protected void onCancelled(DatabaseError error) {
        Log.w(TAG, error.toException());
    }

    /**
     * Each time the data at the given Firebase location changes, this method will be called for each item that needs
     * to be displayed. The first two arguments correspond to the mLayout and mModelClass given to the constructor of
     * this class. The third argument is the item's position in the list.
     * <p>
     * Your implementation should populate the view using the data contained in the model.
     *
     * @param viewHolder The view to populate
     * @param model      The object containing the data used to populate the view
     * @param position   The position in the list of the view being populated
     */
     protected void populateViewHolder(ChatMessageViewHolder viewHolder, ChatMessage model, int position){
         viewHolder.messageText.setText(model.getMessageText());


     }
}