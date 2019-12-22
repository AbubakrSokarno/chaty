package com.example.abubakrsokarno.chaty;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar ;
    private ViewPager mViewPager ;
    private MainPagerAdapter mainPagerAdapter ;
    private TabLayout mTabLayout ;
    private DatabaseReference userDB ;


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String user_id = intent.getStringExtra("userID") ;
            String username = intent.getStringExtra("username") ;

            System.out.println("sender id " +user_id);
            Intent chatIntent = new Intent(context,ChatActivity.class) ;
            chatIntent.putExtra("userID",user_id) ;
            chatIntent.putExtra("username",username) ;
            startActivity(chatIntent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter("com.example.abubakrsokarno.chaty_MSG_NOTIFICATION"));

        if (mAuth.getCurrentUser() != null)
        {
            userDB = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()) ;
        }


        mToolbar = (Toolbar) findViewById(R.id.main_toolbar) ;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chaty");

        mViewPager = (ViewPager) findViewById(R.id.main_view_pager) ;
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager()) ;

        mViewPager.setAdapter(mainPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs) ;
        mTabLayout.setupWithViewPager(mViewPager);

//        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        };

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null)
        {
            changeUI() ;
        }
        else{
            userDB.child("online").setValue(true) ;
            userDB.child("last_seen").setValue(ServerValue.TIMESTAMP);
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter("com.example.abubakrsokarno.chaty_MSG_NOTIFICATION"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() { // on minimizing the application it set online to false
        super.onStop();
        if (userDB != null)
        {
            userDB.child("online").setValue(false) ;
            userDB.child("last_seen").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void changeUI() {
        Intent intent = new Intent(MainActivity.this,StartActivity.class) ;
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.main_logout_btn)
        {
            FirebaseAuth.getInstance().signOut();
            changeUI() ;
        }

        else if(item.getItemId()==R.id.main_settings_btn)
        {
            Intent intent = new Intent(MainActivity.this,SettingActivity.class) ;
            startActivity(intent);
        }

        else if(item.getItemId() == R.id.find_friends)
        {
            Intent intent = new Intent(MainActivity.this,FindFriendsActivity.class) ;
            startActivity(intent);
        }
        else if(item.getItemId()== R.id.timeline)
        {
            Intent intent = new Intent(MainActivity.this,TimelineActivity.class) ;
            startActivity(intent);
        }

        return true ;
    }
}
