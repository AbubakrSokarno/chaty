package com.example.abubakrsokarno.chaty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private static final String TAG = "Find Friend Activity";
    private Button mSearchBtn ;
    private EditText mSearchField ;
    private ProgressDialog mProgressDialog;
    private Toolbar mToolbar ;
    private RecyclerView mUsersList ;
    private DatabaseReference database  ;
    private RecyclerView recyclerView ;
    private List<User> listOfUsers ;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        View parentLayout = findViewById(R.id.find_friends_layout);
        Snackbar.make(parentLayout, "Write your friend email or username", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        mToolbar = (Toolbar) findViewById(R.id.search_bar) ;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSearchBtn = (Button) findViewById(R.id.search_friend_btn) ;
        mSearchField = (EditText) findViewById(R.id.search_field) ;
        mProgressDialog = new ProgressDialog(this) ;
        listOfUsers = new ArrayList<>() ;


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mUsersList = (RecyclerView) findViewById(R.id.users_list) ;
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        database = FirebaseDatabase.getInstance().getReference().child("Users") ;

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String searchQuery = mSearchField.getText().toString() ;
                //Toast.makeText(FindFriendsActivity.this,"search query : "+searchQuery,Toast.LENGTH_LONG).show();
                searchInDatabase(database,searchQuery) ;
            }
        });
    }

    private void searchInDatabase(final DatabaseReference database, final String searchQuery) {
        listOfUsers.clear();

        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Searching for friend");
        mProgressDialog.setMessage("Searching...\nPlease Wait!");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        database.orderByChild("name").equalTo(searchQuery).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String UID = mCurrentUser.getUid() ;
                for (DataSnapshot data: dataSnapshot.getChildren())
                {
                    User searchResult = data.getValue(User.class);
                    searchResult.key = data.getKey() ;
                    if(!UID.equals(data.getKey()))
                        listOfUsers.add(searchResult);
                }
                recyclerView = (RecyclerView) findViewById(R.id.users_list) ;
                recyclerView.setLayoutManager(new LinearLayoutManager(FindFriendsActivity.this));
                recyclerView.setAdapter(new SearchAdapter(FindFriendsActivity.this,listOfUsers));
                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
