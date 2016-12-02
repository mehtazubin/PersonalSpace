package com.zubin.personalspace;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by zubin on 11/17/2016.
 */

public class ContactsFragment extends Fragment {
    private static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView tile;

        public ContactViewHolder(View itemView) {
            super(itemView);
            userName = (TextView)itemView.findViewById(R.id.username);
            tile = (ImageView) itemView.findViewById(R.id.contact_thumbnail);

        }
    }
    private FirebaseRecyclerAdapter<Contact, ContactViewHolder > adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        ((MainActivity) getActivity()).setTitle("Contacts");
        ((MainActivity) getActivity()).setNavChecked(1);
        try {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e){

        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("User");
        final RecyclerView recycler = (RecyclerView) view.findViewById(R.id.contacts_list);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(manager);
        adapter = new FirebaseRecyclerAdapter<Contact, ContactViewHolder>(Contact.class,
                R.layout.contact, ContactViewHolder.class, ref) {
            @Override
            public int getItemViewType(int position) {
                if (getItem(position).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    return R.layout.empty_list_item;
                }
                return R.layout.contact;

            }
            @Override
            public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
                ContactViewHolder vh = new ContactViewHolder(view);
                if(viewType == R.layout.empty_list_item){
                    vh.itemView.setVisibility(View.GONE);
                }
                return vh;
            }
            @Override
            protected void populateViewHolder(ContactViewHolder viewHolder, Contact model, int position) {
                try {
                    viewHolder.userName.setText(model.getName());
                    final Resources res = getResources();
                    final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

                    final LetterTileProvider tileProvider = new LetterTileProvider(getContext());
                    final Bitmap letterTile = tileProvider.getLetterTile(model.getName(), model.getUid(), tileSize, tileSize);
                    viewHolder.tile.setImageBitmap(letterTile);
                } catch (Exception e) {};

            }
        };
        recycler.setAdapter(adapter);
        recycler.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recycler, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                String uid = adapter.getItem(position).getUid();
                String name = adapter.getItem(position).getName();
                ((MainActivity)getContext()).setCurUid(uid);
                ((MainActivity)getContext()).setCurUser(name);
                bundle.putString("name", name);
                bundle.putString("uid", uid);
                Fragment fragment = new Fragment();
                try {
                    fragment = (Fragment) ChatFragment.class.newInstance();
                } catch (Exception e){
                    e.printStackTrace();
                }
                fragment.setArguments(bundle);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.content, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));

        return view;
    }

}
