package com.example.abubakrsokarno.chaty;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView mProfileName, mProfileStatus ;
    private Button mSendRequest  ;
    private ImageView mProfileImage ;
    private Toolbar mToolbar ;
    private FirebaseUser mCurrentUser ;
    private DatabaseReference databaseReference, mFriendsRef, mNotificationDB, mProfileInfo , mRootRef ;
    private String friend_request_state="not_friends" ;
    private ProgressDialog mProgress, requestSending ;
    private String CANCEL = "Cancel Friend Request" , ACCEPT = "Accept friend request" , UNFRIEND = "Unfriend ", SEND="Send friend request" ;
    private String username , status , email , image ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProgress = new ProgressDialog(this) ;

        requestSending = new ProgressDialog(this) ;

        final String requestUserId = getIntent().getStringExtra("userID") ;

        mProgress.setTitle("Collecting Information");
        mProgress.setMessage("Please Wait!");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        mProfileInfo = FirebaseDatabase.getInstance().getReference().child("Users").child(requestUserId) ;
        mProfileInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.child("name").getValue().toString() ;
                status = dataSnapshot.child("status").getValue().toString() ;
                email = dataSnapshot.child("email").getValue().toString()  ;
                image = dataSnapshot.child("image").getValue().toString() ;

                mProfileName.setText(username);
                mProfileStatus.setText(status);

                if(!image.equals("default"))
                    Picasso.with(ProfileActivity.this)
                            .load(image)
                            .placeholder(R.drawable.avatar2)
                            .into(mProfileImage);
                else
                    Picasso.with(ProfileActivity.this).load(R.drawable.avatar2).placeholder(R.drawable.avatar2).into(mProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mToolbar = (Toolbar) findViewById(R.id.setting_bar) ;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(username);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mProfileName = (TextView) findViewById(R.id.searched_user_name) ;
        mProfileStatus = (TextView) findViewById(R.id.searched_user_status) ;
        mSendRequest = (Button) findViewById(R.id.searched_send_request_btn);
        mProfileImage = (ImageView) findViewById(R.id.searched_profile_image) ;

        databaseReference = FirebaseDatabase.getInstance().getReference().child("friends_requests") ;
        mNotificationDB = FirebaseDatabase.getInstance().getReference().child("notifications") ;

        mFriendsRef = FirebaseDatabase.getInstance().getReference().child("friends") ;
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser() ;
        mRootRef  = FirebaseDatabase.getInstance().getReference() ;




        databaseReference.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(requestUserId))
                {
                    String request_type = dataSnapshot.child(requestUserId).child("request_type").getValue().toString() ;
                    if (request_type.equals("received"))
                    {
                        friend_request_state = "request_received";
                        mSendRequest.setText(ACCEPT);
                        // zawed el decline button , aw refuse friend request
                    }
                    else if(request_type.equals("sent"))
                    {
                        friend_request_state = "sent_request";
                        mSendRequest.setText(CANCEL);
                    }
                }
                mProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendsRef.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(requestUserId))
                {
                    friend_request_state = "friends";
                    mSendRequest.setText(UNFRIEND+username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestSending.setTitle("Requesting");
                requestSending.setMessage("Please Wait!");
                requestSending.setCanceledOnTouchOutside(false);
                requestSending.show();

                if (friend_request_state.equals("not_friends"))
                {
                    DatabaseReference notificationRequest = mRootRef.child("notifications").child(requestUserId).push() ;
                    String notificationId = notificationRequest.getKey() ;

                    HashMap<String, String> notificationData = new HashMap<>() ;
                    notificationData.put("from",mCurrentUser.getUid()) ;
                    notificationData.put("type","Friend Request") ;

                    Map childsMap = new HashMap() ;
                    childsMap.put("friends_requests/"+mCurrentUser.getUid()+"/"+requestUserId+"/request_type","sent") ;
                    childsMap.put("friends_requests/"+requestUserId+"/"+mCurrentUser.getUid()+"/request_type","received") ;
                    childsMap.put("notifications/"+requestUserId+"/"+notificationId,notificationData) ;

                    mRootRef.updateChildren(childsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null)
                            {
                                Toast.makeText(ProfileActivity.this,"Error in sending request",Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(ProfileActivity.this,"Request Sent successfully",Toast.LENGTH_LONG).show();
                                friend_request_state = "sent_request" ;
                                mSendRequest.setText(CANCEL);
                            }
                            requestSending.dismiss();
                        }
                    });

                }
                else if(friend_request_state.equals("sent_request")){
                    Map unfriend = new HashMap<>() ;

                    unfriend.put("friends_requests/"+mCurrentUser.getUid()+"/"+requestUserId,null) ;
                    unfriend.put("friends_requests/"+requestUserId+"/"+mCurrentUser.getUid(),null) ;

                    mRootRef.updateChildren(unfriend, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null)
                            {
                                Toast.makeText(ProfileActivity.this,"Error ,Check internet connection",Toast.LENGTH_LONG).show();
                            }
                            else{
                                friend_request_state = "not_friends" ;
                                mSendRequest.setText(SEND);
                            }
                            requestSending.dismiss();
                        }
                    });
                }

                else if(friend_request_state.equals("request_received"))
                {
                    final String CurrentDate = DateFormat.getDateInstance().format(new Date()) ;

                    Map requestReceivedQueries = new HashMap<>() ;

                    requestReceivedQueries.put("friends/"+mCurrentUser.getUid()+"/"+requestUserId+"/date",CurrentDate) ;
                    requestReceivedQueries.put("friends/"+requestUserId+"/"+mCurrentUser.getUid()+"/date",CurrentDate) ;

                    requestReceivedQueries.put("friends_requests/"+mCurrentUser.getUid()+"/"+requestUserId,null) ;
                    requestReceivedQueries.put("friends_requests/"+requestUserId+"/"+mCurrentUser.getUid(),null) ;

                    mRootRef.updateChildren(requestReceivedQueries, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null)
                            {
                                Toast.makeText(ProfileActivity.this,"Request error",Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(ProfileActivity.this,"Congrats for your new friend!",Toast.LENGTH_LONG).show();
                                friend_request_state = "friends" ;
                                mSendRequest.setText(UNFRIEND+username);

                            }
                            requestSending.dismiss();

                        }
                    });

                }

                else if(friend_request_state.equals("friends"))
                {
                    Map unfriend = new HashMap<>() ;

                    unfriend.put("friends/"+mCurrentUser.getUid()+"/"+requestUserId,null) ;
                    unfriend.put("friends/"+requestUserId+"/"+mCurrentUser.getUid(),null) ;

                    mRootRef.updateChildren(unfriend, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null)
                            {
                                Toast.makeText(ProfileActivity.this,"Error ,Check internet connection",Toast.LENGTH_LONG).show();
                            }
                            else{
                                friend_request_state = "not_friends" ;
                                mSendRequest.setText(SEND);
                            }
                            requestSending.dismiss();
                        }
                    });
                }
            }
        });

    }
}
