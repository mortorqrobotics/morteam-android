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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.team1515.morteam.R;

import org.team1515.morteam.MorTeam;
import org.team1515.morteam.entity.Message;
import org.team1515.morteam.entity.User;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RelativeViewHolder> {
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
    public RelativeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message, parent, false);
        RelativeViewHolder viewHolder = new RelativeViewHolder(relativeLayout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RelativeViewHolder holder, int position) {
        final Message currentMessage = messages.get(position);

        TextView message = (TextView) holder.layout.findViewById(R.id.messagelist_message);
        message.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView date = (TextView) holder.layout.findViewById(R.id.messagelist_date);
        date.setText(currentMessage.getDate());

        CardView cardView = (CardView) holder.layout.findViewById(R.id.messagelist_cardview);
        final NetworkImageView messagePic = (NetworkImageView) holder.layout.findViewById(R.id.messagelist_pic);

        View.OnClickListener dateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (date.getVisibility() == View.GONE) {
                    date.setVisibility(View.VISIBLE);
                } else {
                    date.setVisibility(View.GONE);
                }
            }
        };
        cardView.setOnClickListener(dateClickListener);
        message.setOnClickListener(dateClickListener);

        SpannableStringBuilder messageString = new SpannableStringBuilder();
        SpannableString contentString = new SpannableString(Html.fromHtml(currentMessage.getContent()));

        if (currentMessage.isMyMessage) {
            messagePic.setVisibility(View.GONE);

            //Change background color and align to right
            cardView.setCardBackgroundColor(Color.argb(255, 255, 197, 71));

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            SpannableString nameString = new SpannableString(currentMessage.getFirstName() + ": ");
            nameString.setSpan(new StyleSpan(Typeface.BOLD), 0, nameString.length(), 0);
            messageString.append(nameString);

            MorTeam.setNetworkImage(currentMessage.getProfPicPath(), messagePic);
            messagePic.setVisibility(View.VISIBLE);

            cardView.setCardBackgroundColor(Color.WHITE);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }

        messageString.append(contentString);
        message.setText(messageString, TextView.BufferType.SPANNABLE);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}