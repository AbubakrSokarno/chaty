package com.example.abubakrsokarno.chaty;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private View mFriendsView ;
    private RecyclerView mFriendsList  ;
    private DatabaseReference mFriendsDB, mUsers ;
    private FirebaseAuth mAuth ;
    private String currentUserId ;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFriendsView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = (RecyclerView) mFriendsView.findViewById(R.id.friends_list) ;

        mAuth = FirebaseAuth.getInstance() ;
        currentUserId = mAuth.getCurrentUser().getUid() ;
        mFriendsDB = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserId) ;
        mFriendsDB.keepSynced(true);
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users") ;
        mUsers.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab = (FloatingActionButton) mFriendsView.findViewById(R.id.add_friend_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent findFriend = new Intent(mFriendsView.getContext(),FindFriendsActivity.class) ;
                Snackbar.make(view, "Write your friend email or username", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(findFriend);

            }
        });

        return mFriendsView ;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecycleViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.user_single_layout,
                FriendsViewHolder.class,
                mFriendsDB
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, final int position) {
//                viewHolder.setDate(model.getDate());
                final String friendUID = getRef(position).getKey() ;
                mUsers.child(friendUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("name").getValue().toString() ;
                        final String userImage = dataSnapshot.child("image").getValue().toString() ;
                        String userOnline = dataSnapshot.child("online").getValue().toString() ;
                        String userStatus = dataSnapshot.child("status").getValue().toString() ;
//                        String last_seen = dataSnapshot.child("last_seen").getValue().toString() ;

                        viewHolder.setUsername(username);
                        viewHolder.setImage(userImage,getContext());
                        viewHolder.setOnline(userOnline);
                        viewHolder.setStatus(userStatus);

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open Profile","Open Chat"} ;
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("3ayez eh enta delwa2ty??") ;
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i==0)
                                        {
                                            Intent profileIntent = new Intent(getContext(),ProfileActivity.class) ;
                                            profileIntent.putExtra("userID",friendUID) ;
                                            startActivity(profileIntent);
                                        }
                                        else if(i==1)
                                        {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class) ;
                                            chatIntent.putExtra("userID",friendUID) ;
                                            chatIntent.putExtra("username",username) ;
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendsList.setAdapter(friendsRecycleViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View viewHolder ;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            viewHolder = itemView ;
        }

        public void setStatus(String date)
        {
            TextView status = (TextView) viewHolder.findViewById(R.id.single_req_user_status) ;
            status.setText(date) ;
        }

        public void setUsername(String username)
        {
            TextView usernameRow = (TextView) viewHolder.findViewById(R.id.single_user_name) ;
            usernameRow.setText(username) ;
        }

        public void setImage(String image, Context context)
        {
            CircleImageView profileImage = (CircleImageView) viewHolder.findViewById(R.id.single_user_image) ;
            if(!image.equals("default"))
                Picasso.with(context)
                        .load(image)
                        .placeholder(R.drawable.avatar2)
                        .into(profileImage);
            else
                Picasso.with(context).load(R.drawable.avatar2).placeholder(R.drawable.avatar2).into(profileImage);


        }


        public void setOnline(String online) {
//            TextView onlineIcon = (TextView) viewHolder.findViewById(R.id.single_last_seen) ;
//            if (online.equals("true"))
//            {
//                onlineIcon.setVisibility(View.VISIBLE);
//            }
//            else{
//                onlineIcon.setVisibility(View.INVISIBLE);
//            }
        }
    }

}
