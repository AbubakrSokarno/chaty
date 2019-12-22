package com.example.abubakrsokarno.chaty;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Abubakr Sokarno on 1/20/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private FirebaseAuth mAuth ;
    private List<Messages> messagesList ;


    public MessageAdapter(List<Messages> messages){
        this.messagesList = messages ;
    }

    @Override
    public int getItemViewType(int position) {
        Messages m = messagesList.get(position);
        return  m.getItsMe(m.getFrom()) ;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance() ;
        View v = null ;

        switch (viewType){
            case 2:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
                return new FromMessageViewHolder(v);
            case 1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_message_single_layout,parent,false);
                return new MyMessageViewHolder(v);
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
                return new FromMessageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Messages m = messagesList.get(position);
        String MessageType = m.getType() ;
        TimeAgo timeAgo = new TimeAgo();
        switch (holder.getItemViewType()) {
            case 2 :
                final FromMessageViewHolder fromMessageViewHolder = (FromMessageViewHolder) holder ;
                fromMessageViewHolder.messageText.setText(m.getMessage());
                long msgtime = m.getTimestamp() ;
                String msgTimestamp = timeAgo.getTimeAgo(msgtime,fromMessageViewHolder.itemView.getContext()) ;
                fromMessageViewHolder.messageTimestamp.setText(msgTimestamp);
                fromMessageViewHolder.messageText.setBackgroundResource(R.drawable.message_single_shape);

                if(MessageType.equals("image"))
                {
                    fromMessageViewHolder.messageText.setVisibility(View.INVISIBLE);
                    fromMessageViewHolder.imageMessage.setVisibility(View.VISIBLE);
                    Picasso.with(fromMessageViewHolder.imageMessage.getContext())
                            .load(m.getMessage())
                            .placeholder(R.drawable.loading)
                            .resize(700,500)
                            .into(fromMessageViewHolder.imageMessage);
                    fromMessageViewHolder.imageMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            Toast.makeText(view.getContext(),"message clicked",Toast.LENGTH_LONG).show();
                            viewImageFullScreen(m.getMessage(),view) ;
                        }
                    });
                }
                else {
                    fromMessageViewHolder.messageText.setVisibility(View.VISIBLE);
                    fromMessageViewHolder.imageMessage.setVisibility(View.INVISIBLE);
                }
                break;
            case 1 :
                final MyMessageViewHolder myMessageViewHolder = (MyMessageViewHolder) holder ;
                myMessageViewHolder.messageText.setText(m.getMessage());
                long mymsgtime = m.getTimestamp() ;
                String mymsgTimestamp = timeAgo.getTimeAgo(mymsgtime,holder.itemView.getContext()) ;
                myMessageViewHolder.messageTimestamp.setText(mymsgTimestamp);
                myMessageViewHolder.messageText.setBackgroundResource(R.drawable.my_message_single_shap);

                if(MessageType.equals("image"))
                {
                    myMessageViewHolder.messageText.setVisibility(View.INVISIBLE);
                    myMessageViewHolder.imageMessage.setVisibility(View.VISIBLE);
                    Picasso.with(myMessageViewHolder.imageMessage.getContext())
                            .load(m.getMessage())
                            .placeholder(R.drawable.loading)
                            .resize(700,500)
                            .into(myMessageViewHolder.imageMessage);
                    myMessageViewHolder.imageMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            Toast.makeText(view.getContext(),"message clicked",Toast.LENGTH_LONG).show();
                            viewImageFullScreen(m.getMessage(),view) ;
                        }
                    });
                }
                else {
                    myMessageViewHolder.messageText.setVisibility(View.VISIBLE);
                    myMessageViewHolder.imageMessage.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                break;
        }

    }

    public void viewImageFullScreen(String image, View view)
    {
        Uri uri =  Uri.parse(image) ;
        Intent fullScreenIntent = new Intent(view.getContext(), FullscreenImageActivity.class);
        fullScreenIntent.setData(uri);
        view.getContext().startActivity(fullScreenIntent);
    }

    private String saveToInternalStorage(Bitmap bitmapImage,Context appContext){
        ContextWrapper cw = new ContextWrapper(appContext);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }



    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class FromMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public TextView messageTimestamp ;
        public ImageView imageMessage ;
        public FromMessageViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.chatting_with_message);
            messageTimestamp = (TextView) itemView.findViewById(R.id.chatting_msg_timestamp);
            imageMessage = (ImageView) itemView.findViewById(R.id.chatting_with_img_message) ;
        }
    }

    public class MyMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public TextView messageTimestamp ;
        public ImageView imageMessage ;
        public MyMessageViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.my_message);
            messageTimestamp = (TextView) itemView.findViewById(R.id.my_msg_timestamp);
            imageMessage = (ImageView)itemView.findViewById(R.id.my_image_message) ;
        }
    }


    class RetrieveImageTask extends AsyncTask<String, Void , Messages>{

        @Override
        protected Messages doInBackground(String... urls) {

            return null;
        }
    }

}
