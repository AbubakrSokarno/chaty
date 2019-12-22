package com.example.abubakrsokarno.chaty;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Abubakr Sokarno on 1/5/2018.
 */

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context ;
    List<User> users ;
    public SearchAdapter(Context context,List<User> searchedUsers)
    {
        this.context = context ;
        this.users = searchedUsers;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context) ;
        View view = inflater.inflate(R.layout.user_single_layout,parent,false) ;
        Item item = new Item(view) ;
        return item ;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ((Item)holder).username.setText(users.get(position).name);
        ((Item)holder).status.setText(users.get(position).status);
        Picasso.with(context).load(users.get(position).image).placeholder(R.drawable.avatar2).into(((Item)holder).profileImage);
        ((Item)holder).view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoProfile = new Intent(context,ProfileActivity.class) ;
                gotoProfile.putExtra("userID",users.get(position).key);
                context.startActivity(gotoProfile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size() ;
    }

    public class Item extends RecyclerView.ViewHolder{
        TextView username, status ;
        CircleImageView profileImage ;
        View view ;
        public Item(View itemView) {
            super(itemView);
            this.view =itemView ;
            username = (TextView) itemView.findViewById(R.id.single_user_name) ;
            status = (TextView) itemView.findViewById(R.id.single_req_user_status) ;
            profileImage = (CircleImageView) itemView.findViewById(R.id.single_user_image) ;
        }
    }
}
