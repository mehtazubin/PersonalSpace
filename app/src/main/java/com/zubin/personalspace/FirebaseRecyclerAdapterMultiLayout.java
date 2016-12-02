package com.zubin.personalspace;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


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

    protected  int empty_list_item = R.layout.empty_list_item;
    protected int mModelLayoutLeft;
    protected  int mModelLayoutRight;
    protected  boolean right;
    private int lastPosition = -1;
    protected String myUid;
    protected String curUid;
    FirebaseArray mSnapshots;
    public class ChatMessageViewHolder extends RecyclerView.ViewHolder{
        TextView messageText;
        TextView messageUser;
        TextView messageTime;
        int layout;
        public ChatMessageViewHolder(View view, int layout){
            super(view);
            this.layout = layout;
            if(this.layout == R.layout.message_left){
                right = false;
                messageText = (TextView) view.findViewById(R.id.message_left_text);
                messageUser = (TextView) view.findViewById(R.id.message_left_user);
                messageTime = (TextView) view.findViewById(R.id.timestamp_left_user);
            }
            else{
                right = true;
                messageText = (TextView) view.findViewById(R.id.message_right_text);
                messageUser = (TextView) view.findViewById(R.id.message_right_user);
                messageTime = (TextView) view.findViewById(R.id.timestamp_right_user);
            }
        }
    }

    FirebaseRecyclerAdapterMultiLayout(String myUid, String curUid,
                            int modelLayoutRight, int modelLayoutLeft,
                            FirebaseArray snapshots) {
        mModelLayoutRight = modelLayoutRight;
        mModelLayoutLeft = modelLayoutLeft;
        this.myUid = myUid;
        this.curUid = curUid;
        mSnapshots = snapshots;
        this.setHasStableIds(true);

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
    public FirebaseRecyclerAdapterMultiLayout(String myUid, String curUid,
                                   int modelLayoutRight, int modelLayoutLeft,
                                   Query ref) {
        this(myUid, curUid, modelLayoutRight, modelLayoutLeft, new FirebaseArray(ref));
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
        ChatMessageViewHolder vh = new ChatMessageViewHolder(view, viewType);
        if(viewType == empty_list_item){
            vh.itemView.setVisibility(View.GONE);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder viewHolder, int position) {
        ChatMessage model = getItem(position);
        populateViewHolder(viewHolder, model, position);
        setAnimation(viewHolder.itemView.getRootView(), model, position);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(getItem(position).getSenderUid()) &&
                    MainActivity.curUid.equals(getItem(position).getRecepientUid())) {
                return mModelLayoutRight;
            } else if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(getItem(position).getRecepientUid()) &&
                    MainActivity.curUid.equals(getItem(position).getSenderUid())) {
                return mModelLayoutLeft;
            } else {
                return empty_list_item;
            }
        } catch (Exception e){};
        return empty_list_item;
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
        try{
            viewHolder.messageText.setText(model.getMessageText());
            viewHolder.messageUser.setText(model.getMessageUser());
            Date date = new Date(model.getMessageTime());
            DateFormat formatter = new SimpleDateFormat("h:mm a");
            String dateFormatted = formatter.format(date);
            viewHolder.messageTime.setText(dateFormatted);
            viewHolder.itemView.clearAnimation();
        } catch (Exception e){

        }
     }

    private void setAnimation(View viewToAnimate, ChatMessage model, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            lastPosition = position;
            Animation animation;
            if(myUid == model.getSenderUid() || right ) {
                animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.slide_in_right);
            }
            else {
                animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.slide_in_left);
            }
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}