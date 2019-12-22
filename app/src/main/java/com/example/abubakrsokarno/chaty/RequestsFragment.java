package com.example.abubakrsokarno.chaty;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    public View mRequestsView  ;
    public RecyclerView requestsList ;
    public FirebaseAuth mAuth ;
    public String currentUserId ;
    public DatabaseReference requestsDB, usersDB, mRootRef ;
    public ProgressDialog requestSending ;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRequestsView = inflater.inflate(R.layout.fragment_requests,container,false)  ;
        requestsList = (RecyclerView) mRequestsView.findViewById(R.id.requests_list);
        mAuth = FirebaseAuth.getInstance() ;

        currentUserId = mAuth.getCurrentUser().getUid() ;

        requestsDB = FirebaseDatabase.getInstance().getReference().child("friends_requests").child(currentUserId) ;
        requestsDB.keepSynced(true);

        usersDB = FirebaseDatabase.getInstance().getReference().child("Users") ;
        usersDB.keepSynced(true);

        requestsList.setHasFixedSize(true);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRootRef  = FirebaseDatabase.getInstance().getReference() ;

        requestSending = new ProgressDialog(getContext()) ;
        return mRequestsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mRootRef.child("friends_requests").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                FirebaseRecyclerAdapter <Requests,RequestsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(
                        Requests.class,
                        R.layout.single_request_layout,
                        RequestsViewHolder.class,
                        requestsDB
                ) {
                    @Override
                    protected void populateViewHolder(final RequestsViewHolder viewHolder, Requests model, int position) {
                        final String friendRequestUID = getRef(position).getKey() ;
                        getRef(position).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(! dataSnapshot.exists())
                                {
                                    return;
                                }
                                final String request_type = dataSnapshot.child("request_type").getValue().toString();

                                usersDB.child(friendRequestUID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String friendRequestName = dataSnapshot.child("name").getValue().toString() ;
                                        String friendRequestImage = dataSnapshot.child("image").getValue().toString();
                                        String friendRequestStatus = dataSnapshot.child("status").getValue().toString() ;
                                        viewHolder.setImage(friendRequestImage,getContext());
                                        viewHolder.setStatus(request_type);
                                        viewHolder.setUsername(friendRequestName);
                                        viewHolder.setIcons(request_type);


                                        viewHolder.requestView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent gotoProfile = new Intent(getContext(),ProfileActivity.class) ;
                                                gotoProfile.putExtra("userID",friendRequestUID);
                                                getContext().startActivity(gotoProfile);
                                            }
                                        });

                                        if (request_type.equals("received"))
                                        {
                                            viewHolder.rejectRequest.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    requestSending.setTitle("Requesting");
                                                    requestSending.setMessage("Please Wait!");
                                                    requestSending.setCanceledOnTouchOutside(false);
                                                    requestSending.show();
                                                    Map requestReceivedQueries = new HashMap<>() ;

                                                    requestReceivedQueries.put("friends_requests/"+currentUserId+"/"+friendRequestUID,null) ;
                                                    requestReceivedQueries.put("friends_requests/"+friendRequestUID+"/"+currentUserId,null) ;

                                                    mRootRef.updateChildren(requestReceivedQueries, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if (databaseError != null)
                                                            {
                                                                Toast.makeText(getContext(),"Request error",Toast.LENGTH_LONG).show();
                                                            }
                                                            else{
                                                                Toast.makeText(getContext(),"Friend Request Canceled!",Toast.LENGTH_LONG).show();
                                                            }
                                                            requestSending.dismiss();
                                                        }
                                                    });

                                                }
                                            });
                                        }


                                        viewHolder.acceptRequest.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                requestSending.setTitle("Requesting");
                                                requestSending.setMessage("Please Wait!");
                                                requestSending.setCanceledOnTouchOutside(false);
                                                requestSending.show();

                                                final String CurrentDate = DateFormat.getDateInstance().format(new Date()) ;

                                                Map requestReceivedQueries = new HashMap<>() ;
                                                if(request_type.equals("received"))
                                                {
                                                    requestReceivedQueries.put("friends/"+currentUserId+"/"+friendRequestUID+"/date",CurrentDate) ;
                                                    requestReceivedQueries.put("friends/"+friendRequestUID+"/"+currentUserId+"/date",CurrentDate) ;

                                                    requestReceivedQueries.put("friends_requests/"+currentUserId+"/"+friendRequestUID,null) ;
                                                    requestReceivedQueries.put("friends_requests/"+friendRequestUID+"/"+currentUserId,null) ;

                                                }
                                                else{
                                                    requestReceivedQueries.put("friends_requests/"+currentUserId+"/"+friendRequestUID,null) ;
                                                    requestReceivedQueries.put("friends_requests/"+friendRequestUID+"/"+currentUserId,null) ;
                                                }


                                                mRootRef.updateChildren(requestReceivedQueries, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        String msg = null ;
                                                        if(request_type.equals("received"))
                                                        {
                                                            msg = "Congrats for your new friend! ;) ";
                                                        }
                                                        else{
                                                            msg = "friend request cancelled" ;
                                                        }
                                                        if (databaseError != null)
                                                        {
                                                            Toast.makeText(getContext(),"Request error",Toast.LENGTH_LONG).show();
                                                        }
                                                        else{
                                                            Toast.makeText(getContext(),msg,Toast.LENGTH_LONG).show();
                                                        }
                                                        requestSending.dismiss();
                                                    }
                                                });

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                };
                requestsList.setAdapter(firebaseRecyclerAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        View requestView ;
        public ImageButton acceptRequest, rejectRequest, cancelRequest ;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            requestView = itemView ;

            acceptRequest = (ImageButton) itemView.findViewById(R.id.single_req_accept);
            rejectRequest = (ImageButton) itemView.findViewById(R.id.single_req_reject) ;
        }

        public void setIcons(String requestType)
        {
            if (requestType.equals("received"))
            {
                acceptRequest = (ImageButton) itemView.findViewById(R.id.single_req_accept);
                rejectRequest = (ImageButton) itemView.findViewById(R.id.single_req_reject) ;
            }
            else{
                cancelRequest = (ImageButton) itemView.findViewById(R.id.single_req_accept);
                cancelRequest.setImageResource(R.drawable.delete_request);
                rejectRequest = (ImageButton) itemView.findViewById(R.id.single_req_reject) ;
                rejectRequest.setVisibility(View.INVISIBLE);
            }
        }

        public void setUsername(String username)
        {
            TextView chattingWithUsername = requestView.findViewById(R.id.single_request_user_name) ;
            chattingWithUsername.setText(username);
        }

        public void setImage(String image, Context context)
        {
            CircleImageView profileImage = (CircleImageView) requestView.findViewById(R.id.single_request_user_image) ;
            if(!image.equals("default"))
                Picasso.with(context)
                        .load(image)
                        .placeholder(R.drawable.avatar2)
                        .into(profileImage);
            else
                Picasso.with(context).load(R.drawable.avatar2).placeholder(R.drawable.avatar2).into(profileImage);
        }

        public void setStatus(String requestType)
        {
            TextView status = (TextView) requestView.findViewById(R.id.single_req_user_status) ;
            status.setText(requestType) ;
        }

    }

}
