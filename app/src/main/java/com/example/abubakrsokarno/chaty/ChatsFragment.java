package com.example.abubakrsokarno.chaty;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View mChatsView;
    private RecyclerView chats ;
    private DatabaseReference dbMyChats, dbUsers, dbMessages ;
    private FirebaseAuth mAuth ;
    private String currentUserId ;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        chats = (RecyclerView) mChatsView.findViewById(R.id.chats_list) ;
        mAuth = FirebaseAuth.getInstance() ;

        currentUserId = mAuth.getCurrentUser().getUid() ;

        dbUsers = FirebaseDatabase.getInstance().getReference().child("Users") ;
        dbUsers.keepSynced(true);

        dbMyChats = FirebaseDatabase.getInstance().getReference().child("Chats").child(currentUserId) ;
        dbMyChats.keepSynced(true);

        dbMessages = FirebaseDatabase.getInstance().getReference().child("messages") ;
        dbMessages.keepSynced(true);

        chats.setHasFixedSize(true);
        chats.setLayoutManager(new LinearLayoutManager(getContext()));
        return mChatsView ;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter <Chats,ChatViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatViewHolder>(
                Chats.class,
                R.layout.user_single_layout,
                ChatViewHolder.class,
                dbMyChats
        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, Chats model, int position) {
                final String friendUID = getRef(position).getKey() ;
                dbUsers.child(friendUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // ageeb el esm w el soora
                        // ageeb ba3d kda mn el messages 2a5er message a7otaha fel status
                        final String chattingWithName = dataSnapshot.child("name").getValue().toString() ;
                        viewHolder.setUsername(chattingWithName);

                        String chattingWithImage = dataSnapshot.child("image").getValue().toString() ;
                        viewHolder.setImage(chattingWithImage,getContext());


                        Query lastMessageQuery = dbMessages.child(currentUserId).child(friendUID).orderByKey().limitToLast(1) ;
                        lastMessageQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String lastMessage = null;
                                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                    if (childSnapshot.child("type").getValue().toString().equals("text"))
                                    {
                                        lastMessage = childSnapshot.child("message").getValue().toString() ;
                                    }
                                    else
                                        lastMessage = "Image" ;
                                }
                                viewHolder.setLastMessage(lastMessage);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        viewHolder.chatViewHolder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(),ChatActivity.class) ;
                                chatIntent.putExtra("userID",friendUID) ;
                                chatIntent.putExtra("username",chattingWithName) ;
                                startActivity(chatIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } ;

        chats.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        View chatViewHolder ;

        public ChatViewHolder(View itemView) {
            super(itemView);
            chatViewHolder = itemView ;
        }

        public void setUsername(String username)
        {
            TextView chattingWithUsername = chatViewHolder.findViewById(R.id.single_user_name) ;
            chattingWithUsername.setText(username);
        }

        public void setImage(String image, Context context)
        {
            CircleImageView profileImage = (CircleImageView) chatViewHolder.findViewById(R.id.single_user_image) ;
            if(!image.equals("default"))
                Picasso.with(context)
                        .load(image)
                        .placeholder(R.drawable.avatar2)
                        .into(profileImage);
            else
                Picasso.with(context).load(R.drawable.avatar2).placeholder(R.drawable.avatar2).into(profileImage);
        }

        public void setLastMessage(String message)
        {
            TextView lastMessage = (TextView) chatViewHolder.findViewById(R.id.single_req_user_status) ;
            lastMessage.setText(message) ;
        }

    }

}
