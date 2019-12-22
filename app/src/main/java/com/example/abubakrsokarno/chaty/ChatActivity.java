package com.example.abubakrsokarno.chaty;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.okhttp.internal.Util;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String chattingWithUserName ,chattingWithUserID , mCurrentUserID  ;
    private Toolbar mCustomChatBar ;
    private DatabaseReference mRootRef ;
    private TextView mUsername ,mLastSeen ;
    private CircleImageView mUserImage ;
    private FirebaseAuth mAuth ;
    private EditText mMessageTxt ;
    private ImageButton mSendBtn , mMoreOptions ;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager ;
    private MessageAdapter messageAdapter ;
    private RecyclerView messagesRecyclerView ;
    private static final int PAGINATION = 100 ;
    private int currentPage = 1 ;
    private SwipeRefreshLayout mRefreshLayout ;
    private final int GALLERY_PICK = 1 ;
    private StorageReference mGalleryRef ;

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mGalleryRef = FirebaseStorage.getInstance().getReference() ;
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance() ;
        mCurrentUserID = mAuth.getCurrentUser().getUid() ;

        mMessageTxt = (EditText) findViewById(R.id.chat_message) ;
        mSendBtn = (ImageButton) findViewById(R.id.chat_send_msg) ;
        mMoreOptions = (ImageButton) findViewById(R.id.chat_add_more) ;

        messageAdapter = new MessageAdapter(messagesList) ;
        linearLayoutManager = new LinearLayoutManager(this) ;
        messagesRecyclerView = (RecyclerView) findViewById(R.id.chat_all_messages) ;
        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        mCustomChatBar = (Toolbar) findViewById(R.id.chat_custom_bar) ;
        setSupportActionBar(mCustomChatBar);

        ActionBar actionBar = getSupportActionBar() ;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        chattingWithUserName = getIntent().getStringExtra("username") ;
        chattingWithUserID = getIntent().getStringExtra("userID") ;


        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View ActionBarView = inflater.inflate(R.layout.custom_chat_bar,null);

        actionBar.setCustomView(ActionBarView);

        mUsername = (TextView) findViewById(R.id.custom_name) ;
        mLastSeen = (TextView) findViewById(R.id.custom_last_seen) ;
        mUserImage = (CircleImageView) findViewById(R.id.custom_image) ;

        mUsername.setText(chattingWithUserName);

        getConversation() ;

        init_header() ;

        init_chats();

//        swipe_refresh() ;

        send_message() ;

        send_more() ; // for images
    }

    public void init_header() {
        mRootRef.child("Users").child(chattingWithUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String last_seen = dataSnapshot.child("last_seen").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString() ;

                if (! image.equals("default"))
                {
                    Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.avatar2).into(mUserImage);
                }
                else{
                    Picasso.with(ChatActivity.this).load(R.drawable.avatar2).placeholder(R.drawable.avatar2).into(mUserImage);
                }

                if (online.equals("true"))
                {
                    mLastSeen.setText("Online");
                }
                else{
                    TimeAgo timeAgo = new TimeAgo();
                    long lastTime = Long.parseLong(last_seen);
                    String lastTimeAgo = timeAgo.getTimeAgo(lastTime,getApplicationContext());

                    mLastSeen.setText(lastTimeAgo);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void send_message(){
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage() ;
            }
        });
    }

    public void send_more(){
        mMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]{"Send Image","Voice Record"} ;
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this) ;
                builder.setTitle("Multimedia message") ;
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0)
                        {
                            Intent gallery = new Intent() ;
                            gallery.setType("image/*") ;
                            gallery.setAction(Intent.ACTION_GET_CONTENT) ;
                            startActivityForResult(Intent.createChooser(gallery,"Choose Picture"),GALLERY_PICK);
                        }
                        else if(i==1)
                        {
                            Toast.makeText(getApplicationContext(),"Voice record feature will add soon",Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.show();
//                Intent gallery = new Intent() ;
//                gallery.setType("image/*") ;
//                gallery.setAction(Intent.ACTION_GET_CONTENT) ;
//                startActivityForResult(Intent.createChooser(gallery,"Choose Picture"),GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri imageURI = data.getData() ;

            final String currentUserRef = "messages/"+mCurrentUserID+"/"+chattingWithUserID ;
            final String chattingUserRef = "messages/"+chattingWithUserID+"/"+mCurrentUserID;

            DatabaseReference  image_message_push = mRootRef.child("messages").child(mCurrentUserID).child(chattingWithUserID).push() ;

            final String message_push_id = image_message_push.getKey() ;

            StorageReference imagePath = mGalleryRef.child("message_images").child(message_push_id+".jpg") ;

            imagePath.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful())
                    {

                        String image_download_url = task.getResult().getDownloadUrl().toString() ;

                        build_map_message(image_download_url,"image",currentUserRef,chattingUserRef,message_push_id) ;


                    }
                }
            });

        }

    }

    public void swipe_refresh() {
//        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_swipe_refresh) ;
//        mRefreshLayout.setColorSchemeColors(Color.BLUE);
//        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                messagesList.clear();
//                currentPage++ ;
//                getConversation();
//            }
//        });
    }
    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
    }

    public void init_chats(){
        mRootRef.child("Chats").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( !dataSnapshot.hasChild(chattingWithUserID) )
                {
                    Map initChat = new HashMap() ;
                    initChat.put("seen",false);
                    initChat.put("timestamp", ServerValue.TIMESTAMP) ;

                    Map chatMap = new HashMap() ;
                    chatMap.put("Chats/"+mCurrentUserID+"/"+chattingWithUserID,initChat) ;
                    chatMap.put("Chats/"+chattingWithUserID+"/"+mCurrentUserID,initChat) ;

                    mRootRef.updateChildren(chatMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null)
                            {
                                Log.d("CHAT_INIT_ERR",databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getConversation()
    {
//        DatabaseReference messagesRef = mRootRef.child("messages").child(mCurrentUserID).child(chattingWithUserID) ;
        mRootRef.child("messages").child(mCurrentUserID).child(chattingWithUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Messages message = snapshot.getValue(Messages.class);
                    messagesList.add(message) ;
                }
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        })  ;

    }

    private void sendMessage() {
        String message = mMessageTxt.getText().toString() ;

        if (isStringNullOrWhiteSpace(message))
        {
            String addingMsgToCurrUserQuery = "messages/"+mCurrentUserID+"/"+chattingWithUserID ;
            String addingMsgToChattingUserQuery = "messages/"+chattingWithUserID +"/"+mCurrentUserID;

            DatabaseReference pushingMsg = mRootRef.child("messages").child(mCurrentUserID).child(chattingWithUserID).push() ;
            String MsgKey = pushingMsg.getKey() ;

            build_map_message(message,"text",addingMsgToCurrUserQuery,addingMsgToChattingUserQuery,MsgKey) ;

            mMessageTxt.setText("");
        }
    }

    public void build_map_message(String message ,String messageType,String addingMsgToCurrUserQuery,String addingMsgToChattingUserQuery,String MsgKey)
    {
        Map messageBlock = new HashMap() ;
        messageBlock.put("message",message) ;
        messageBlock.put("seen",true) ;
        messageBlock.put("timestamp",ServerValue.TIMESTAMP) ;
        messageBlock.put("type",messageType) ; // text or image
        messageBlock.put("from",mCurrentUserID) ;

        Map messageContainer = new HashMap() ;
        messageContainer.put(addingMsgToCurrUserQuery+"/"+MsgKey,messageBlock) ;
        messageContainer.put(addingMsgToChattingUserQuery+"/"+MsgKey,messageBlock) ;

        mRootRef.updateChildren(messageContainer, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError!=null)
                    Log.d("CHAT_LOG",databaseError.getMessage().toString()) ;
            }
        });
    }

    public static boolean isStringNullOrWhiteSpace(String value) {
        if (value == null) {
            return true;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }

        return false;
    }
}
