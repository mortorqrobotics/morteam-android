package org.team1515.morteam.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.entity.Message;
import org.team1515.morteam.entity.User;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout layout;
        public TextView messageView;
        public TextView dateView;
        public CardView cardView;
        public NetworkImageView pictureView;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = (RelativeLayout) itemView;
            messageView = (TextView) layout.findViewById(R.id.messagelist_message);
            dateView = (TextView) layout.findViewById(R.id.messagelist_date);
            cardView = (CardView) layout.findViewById(R.id.messagelist_cardview);
            pictureView = (NetworkImageView) layout.findViewById(R.id.messagelist_pic);
        }
    }

    private List<Message> messages;

    public ChatMessageAdapter() {
        messages = new ArrayList<>();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(String firstName, String lastName, String content, String date, String chatId, String profPicPath, boolean isMyChat) {
        messages.add(0, new Message(new User(firstName, lastName, null, profPicPath), content, date, chatId, isMyChat));
        notifyItemInserted(0);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message, parent, false);
        ViewHolder viewHolder = new ViewHolder(relativeLayout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Message currentMessage = messages.get(position);

        holder.messageView.setMovementMethod(LinkMovementMethod.getInstance());

        holder.dateView.setText(currentMessage.getDate());

        View.OnClickListener dateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.dateView.getVisibility() == View.GONE) {
                    holder.dateView.setVisibility(View.VISIBLE);
                } else {
                    holder.dateView.setVisibility(View.GONE);
                }
            }
        };
        holder.cardView.setOnClickListener(dateClickListener);
        holder.messageView.setOnClickListener(dateClickListener);

        SpannableStringBuilder messageString = new SpannableStringBuilder();
        SpannableString contentString = new SpannableString(Html.fromHtml(currentMessage.getContent()));

        if (currentMessage.isMyMessage) {
            holder.pictureView.setVisibility(View.GONE);

            //Change background color and align to right
            holder.cardView.setCardBackgroundColor(Color.argb(255, 255, 197, 71));

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.cardView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            SpannableString nameString = new SpannableString(currentMessage.getFirstName() + ": ");
            nameString.setSpan(new StyleSpan(Typeface.BOLD), 0, nameString.length(), 0);
            messageString.append(nameString);

            MorTeam.setNetworkImage(currentMessage.getProfPicPath(), holder.pictureView);
            holder.pictureView.setVisibility(View.VISIBLE);

            holder.cardView.setCardBackgroundColor(Color.WHITE);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.cardView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }

        messageString.append(contentString);
        holder.messageView.setText(messageString, TextView.BufferType.SPANNABLE);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}