package com.example.abubakrsokarno.chaty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    private Button mRegister ;
    private static final String TAG = "Registration Activity";
    private EditText mEmail , mPassword, mUsername ;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar ;
    private ProgressDialog mProgress ;
    private DatabaseReference database ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegister = (Button) findViewById(R.id.registration_btn) ;
        mEmail = (EditText) findViewById(R.id.email) ;
        mPassword = (EditText) findViewById(R.id.password) ;
        mUsername = (EditText) findViewById(R.id.username) ;
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this) ;
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar) ;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chaty");
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = mUsername.getText().toString() ;
                String email = mEmail.getText().toString() ;
                String password = mPassword.getText().toString() ;


                if(!TextUtils.isEmpty(username)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password))
                {
                    mProgress.setTitle("Creating Account");
                    mProgress.setMessage("Loading...\nPlease Wait!");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                    register_user(username,email,password) ;
                }
                else
                    Toast.makeText(RegisterActivity.this, "Fields are required !",
                            Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void register_user(final String username, final String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser() ;
                            String UID = current_user.getUid() ;

                            database = FirebaseDatabase.getInstance().getReference().child("Users").child(UID) ;

                            String deviceToken = FirebaseInstanceId.getInstance().getToken() ;

                            HashMap<String,String> userInfo = new HashMap<>() ;
                            userInfo.put("device_token",deviceToken) ;
                            userInfo.put("name",username) ;
                            userInfo.put("status","I'm an active user in CHATY application") ;
                            userInfo.put("image","default") ;
                            userInfo.put("thumb_image","default") ;
                            userInfo.put("email",email);

                            database.setValue(userInfo) ;

                            mProgress.dismiss();
                            Intent intent = new Intent(RegisterActivity.this,MainActivity.class) ;
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            mProgress.hide();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}
