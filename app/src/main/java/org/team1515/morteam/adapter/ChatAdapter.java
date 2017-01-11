package org.team1515.morteam.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.team1515.morteam.R;
import org.team1515.morteam.activity.ChatActivity;
import org.team1515.morteam.entity.Chat;
import org.team1515.morteam.fragment.AnnouncementFragment;
import org.team1515.morteam.fragment.ChatFragment;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Context context;
        ImageView pictureView;
        TextView nameView;
        ImageButton deleteButton;


        ViewHolder(LinearLayout layout) {
            super(layout);

            context = layout.getContext();
            pictureView = (ImageView) layout.findViewById(R.id.chatlist_pic);
            nameView = (TextView) layout.findViewById(R.id.chatlist_name);
            deleteButton = (ImageButton) layout.findViewById(R.id.delete_chat_button);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Chat chat = chats.get(position);

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", chat.getName());
                intent.putExtra("_id", chat.getId());
                intent.putExtra("isGroup", chat.isGroup);
                context.startActivity(intent);
            }
        }

    }

    private Context context;
    private List<Chat> chats;
    private ChatFragment fragment;

    public ChatAdapter(ChatFragment fragment, Context context, List<Chat> chats) {
        this.fragment = fragment;
        this.context = context;
        this.chats = chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Chat currentChat = chats.get(position);

        Glide
                .with(context)
                .load(currentChat.getPicPath())
                .centerCrop()
                .crossFade()
                .into(holder.pictureView);

        holder.nameView.setText(currentChat.getName());

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.deleteChat(currentChat.getId(), holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
}