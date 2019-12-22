package com.example.abubakrsokarno.chaty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login Activity" ;
    private EditText mEmail , mPassword ;
    private FirebaseAuth mAuth;
    private Button mLogin ;
    private ProgressDialog mProgressLogin ;
    private Toolbar mToolbar ;
    private DatabaseReference mDB ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = (EditText) findViewById(R.id.login_email) ;
        mPassword = (EditText) findViewById(R.id.login_password) ;
        mLogin = (Button) findViewById(R.id.login_btn);
        mProgressLogin = new ProgressDialog(this) ;
        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar) ;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chaty");

        mDB = FirebaseDatabase.getInstance().getReference().child("Users") ;

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString() ;
                String password = mPassword.getText().toString() ;
                // Toast.makeText(LoginActivity.this,"clicked",Toast.LENGTH_SHORT).show();
                if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password))
                {
                    mProgressLogin.setTitle("Login to your account");
                    mProgressLogin.setMessage("Loading...\nPlease Wait");
                    mProgressLogin.setCanceledOnTouchOutside(false);
                    mProgressLogin.show();
                    try{
                        loginUser(email,password) ;
                    }
                    catch (Exception e)
                    {
                        mProgressLogin.hide();
                        Toast.makeText(LoginActivity.this, "Login Failed" ,Toast.LENGTH_LONG).show();
                    }

                }
                else{
                    mProgressLogin.hide();
                    Toast.makeText(LoginActivity.this,"Email and password are required",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    String userID  = mAuth.getCurrentUser().getUid() ;
                    String deviceToken = FirebaseInstanceId.getInstance().getToken() ;

                    mDB.child(userID).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgressLogin.dismiss();
                            Intent goToMain = new Intent(LoginActivity.this,MainActivity.class) ;
                            goToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
                            startActivity(goToMain);
                            finish();
                        }
                    });
                }
                else{
                    mProgressLogin.hide();
                    Toast.makeText(LoginActivity.this,"Wrong Email Or Password",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
