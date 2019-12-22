package com.example.abubakrsokarno.chaty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class TimelineActivity extends AppCompatActivity {

    public FirebaseAuth mAuth ;
    public String mCurrentUserID ;
    public DatabaseReference mRootRef ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        Toolbar mTimelineBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTimelineBar);
        getSupportActionBar().setTitle("Timeline");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance() ;

        mCurrentUserID = mAuth.getCurrentUser().getUid() ;

        mRootRef = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TimelineActivity.this);
                builder.setTitle("Leave a message to the world");
                builder.setIcon(R.drawable.chaticon);

                final EditText timelineMessage = new EditText(TimelineActivity.this);

                builder.setView(timelineMessage) ;

                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String message = timelineMessage.getText().toString();
                        if (isStringNullOrWhiteSpace(message))
                        {
                            Toast.makeText(TimelineActivity.this,"Message Sent",Toast.LENGTH_LONG).show();
                            sendMessage(message);
                        }
                        else{
                            Toast.makeText(TimelineActivity.this,"Message can not be empty!",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(TimelineActivity.this,"Cancelled",Toast.LENGTH_LONG).show();
                    }
                });

                builder.show();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    private void sendMessage(String message) {
        if (isStringNullOrWhiteSpace(message))
        {
            String addingMsgToCurrUserQuery = "timeline/"+mCurrentUserID+"/" ;
            DatabaseReference pushingMsg = mRootRef.child("timeline").child(mCurrentUserID).push() ;
            String MsgKey = pushingMsg.getKey() ;

            build_map_message(message,"text",addingMsgToCurrUserQuery,MsgKey) ;
        }
    }

    public void build_map_message(String message ,String messageType,String addingMsgToCurrUserQuery,String MsgKey)
    {
        Map messageBlock = new HashMap() ;
        messageBlock.put("message",message) ;
        messageBlock.put("seen",true) ;
        messageBlock.put("timestamp", ServerValue.TIMESTAMP) ;
        messageBlock.put("type",messageType) ; // text or image
        messageBlock.put("from",mCurrentUserID) ;

        Map messageContainer = new HashMap() ;
        messageContainer.put(addingMsgToCurrUserQuery+"/"+MsgKey,messageBlock) ;

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
