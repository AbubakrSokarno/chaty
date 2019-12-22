package com.example.abubakrsokarno.chaty;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    private Button mRegBtn, mLoginBtn;
    private Toolbar mToolbar ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar) ;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chaty");

        mRegBtn = (Button) findViewById(R.id.start_reg_btn) ;
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent = new Intent(StartActivity.this,RegisterActivity.class) ;
                startActivity(regIntent);
            }
        });

        mLoginBtn = (Button) findViewById(R.id.start_login_btn) ;
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(StartActivity.this,LoginActivity.class) ;
                startActivity(loginIntent);
            }
        });

    }
}
