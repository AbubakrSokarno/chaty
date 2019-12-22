package com.example.abubakrsokarno.chaty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private TextView username , status ;
    private FirebaseUser mCurrentUser;
    private DatabaseReference database ;
    private Button mChangeStatus ,mProfileImageBtn ;
    private final static int GALLERY_REQUEST = 1;
    private StorageReference mPP ; // profile picture
    private ProgressDialog mProgress ,mProgress2 ;
    private CircleImageView mProfileImage ;
    private User user ;
    private Toolbar mToolbar ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mProgress = new ProgressDialog(this) ;
        mProgress2 = new ProgressDialog(this) ;
        mChangeStatus = (Button) findViewById(R.id.change_status_btn) ;
        username = (TextView) findViewById(R.id.display_username) ;
        status = (TextView) findViewById(R.id.display_status) ;
        mProfileImageBtn = (Button) findViewById(R.id.change_image_btn) ;
        mPP = FirebaseStorage.getInstance().getReference() ;
        mProfileImage = (CircleImageView) findViewById(R.id.profile_image) ;
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser() ;
        mToolbar = (Toolbar) findViewById(R.id.setting_bar_original) ;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changeStatusIntent = new Intent(SettingActivity.this,ChangeStatusActivity.class) ;
                changeStatusIntent.putExtra("old_status",user.status);
                mProgress.dismiss();
                startActivity(changeStatusIntent);
            }
        });

        mProfileImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(SettingActivity.this,"clicked",Toast.LENGTH_LONG).show();
                Intent chooseImage = new Intent();
                chooseImage.setType("image/*") ;
                chooseImage.setAction(Intent.ACTION_GET_CONTENT) ;
                mProgress.dismiss();
                startActivityForResult(Intent.createChooser(chooseImage,"Select Profile Image"),GALLERY_REQUEST);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GALLERY_REQUEST && resultCode==RESULT_OK)
        {
            Uri imageURI = data.getData() ;
            CropImage.activity(imageURI).setAspectRatio(1,1).start(this) ;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgress.setTitle("Uploading Image");
                mProgress.setMessage("Updating...\nPlease Wait!");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();
                Uri resultUri = result.getUri();
                String UID = mCurrentUser.getUid() ;
                StorageReference filePath = mPP.child("PP").child(UID+".jpg") ;

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            String downloadURL = task.getResult().getDownloadUrl().toString();
                            database.child("image").setValue(downloadURL).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        mProgress.hide();
                                        Toast.makeText(SettingActivity.this,"Image Uploaded successfully",Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        mProgress.hide();
                                        Toast.makeText(SettingActivity.this,"Error while uploading",Toast.LENGTH_LONG).show();
                                    }
                                }
                            }) ;
                        }
                        else{
                            mProgress.hide();
                            Toast.makeText(SettingActivity.this,"Error while uploading",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


    private void init() {
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser() ;
        String UID = current_user.getUid() ;

        mProgress2.setTitle("Getting Profile Info");
        mProgress2.setMessage("Just Seconds!");
        mProgress2.setCanceledOnTouchOutside(false);
        mProgress2.show();

        database = FirebaseDatabase.getInstance().getReference().child("Users").child(UID) ;

        database.keepSynced(true);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class) ;
                username.setText(user.name);
                status.setText(user.status);
                if (! user.image.equals("default"))
                {
                    Picasso.with(SettingActivity.this).load(user.image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar2)
                            .into(mProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(SettingActivity.this).load(user.image)
                                            .placeholder(R.drawable.avatar2)
                                            .into(mProfileImage) ;
                                }
                            });

                }
                else
                    Picasso.with(SettingActivity.this).load(R.drawable.avatar2).placeholder(R.drawable.avatar2).into(mProfileImage);

                mProgress2.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
