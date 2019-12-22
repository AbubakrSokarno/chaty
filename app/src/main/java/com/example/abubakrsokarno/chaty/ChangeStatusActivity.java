package com.example.abubakrsokarno.chaty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Console;

public class ChangeStatusActivity extends AppCompatActivity {
    private Button saveStatus  ;
    private TextInputLayout newStatus ;
    private Toolbar mToolbar ;
    private DatabaseReference database ;
    private ProgressDialog mProgress ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar) ;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Words...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgress = new ProgressDialog(ChangeStatusActivity.this) ;

        saveStatus = (Button) findViewById(R.id.save_status_btn);
        newStatus = (TextInputLayout) findViewById(R.id.status_txtInp) ;

        String old_status = getIntent().getStringExtra("old_status");
        newStatus.getEditText().setText(old_status);

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser() ;
        String UID = current_user.getUid() ;
        database = FirebaseDatabase.getInstance().getReference().child("Users").child(UID) ;
        saveStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = newStatus.getEditText().getText().toString() ;
                if(! TextUtils.isEmpty(status))
                {
                    mProgress.setTitle("Saving Status");
                    mProgress.setMessage("Saving your words...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                    database.child("status").setValue(status);
                    mProgress.hide();
                    returnBack() ;
                }
            }
        });

    }

    private void returnBack() {
        Intent retBack = new Intent(ChangeStatusActivity.this,SettingActivity.class);
        startActivity(retBack);
    }
}
