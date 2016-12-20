package org.team1515.morteam.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.activity.ChatActivity;
import org.team1515.morteam.entity.Chat;
import org.team1515.morteam.network.CookieRequest;

import java.util.ArrayList;
import java.util.List;

import static org.team1515.morteam.MorTeam.preferences;
import static org.team1515.morteam.MorTeam.queue;

public class ChatFragment extends Fragment {
    private RecyclerView chatList;
    private ChatAdapter chatAdapter;

    private SwipeRefreshLayout refreshLayout;

    private ProgressBar progress;
    private TextView errorView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatList = (RecyclerView) view.findViewById(R.id.chat_chatlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatAdapter = new ChatAdapter();
        chatList.setLayoutManager(layoutManager);
        chatList.setAdapter(chatAdapter);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.chatlist_swipelayout);
        refreshLayout.setColorSchemeResources(R.color.orange_theme);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatAdapter.getChats();
            }
        });

        progress = (ProgressBar) view.findViewById(R.id.chat_loading);
        progress.setVisibility(View.GONE);
        progress.getIndeterminateDrawable().setColorFilter(Color.rgb(255, 197, 71), android.graphics.PorterDuff.Mode.MULTIPLY);
        errorView = (TextView) view.findViewById(R.id.chat_error);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getChats();
    }

    public void getChats() {
        chatAdapter.getChats();
    }

    public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private List<Chat> chats;

        public ChatAdapter() {
            chats = new ArrayList<>();
        }

        public void getChats() {
            errorView.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);

            CookieRequest chatRequest = new CookieRequest(
                    Request.Method.GET,
                    "/chats",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                chats = new ArrayList<>();
                                JSONArray chatArray = new JSONArray(response);
                                for (int i = 0; i < chatArray.length(); i++) {
                                    JSONObject chatObject = chatArray.getJSONObject(i);
                                    String id = chatObject.getString("_id");
                                    String name = "";
                                    String picPath = "";
                                    boolean isTwoPeople = chatObject.getBoolean("isTwoPeople");
                                    if (!isTwoPeople) {
                                        name = chatObject.getString("name");
                                        picPath = "/images/group.png";
                                    } else {
                                        //For non-group chats, find name of other user
                                        JSONArray chatUserArray = chatObject.getJSONObject("audience").getJSONArray("users");
                                        String currentUserId = preferences.getString("_id", "");
                                        for (int user = 0; user < chatUserArray.length(); user++) {
                                            JSONObject userObject = chatUserArray.getJSONObject(user);
                                            if (!userObject.getString("_id").equals(currentUserId)) {
                                                name = userObject.getString("firstname") + " " + userObject.getString("lastname");
                                                picPath = userObject.getString("profpicpath") + "-60";
                                            }
                                        }
                                    }

                                    Chat chat = new Chat(name, id, picPath, isTwoPeople);

                                    chats.add(chat);

                                }
                                progress.setVisibility(View.GONE);
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                progress.setVisibility(View.GONE);
                                if (chats.isEmpty()) {
                                    errorView.setVisibility(View.VISIBLE);
                                }
                            } finally {
                                refreshLayout.setRefreshing(false);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            refreshLayout.setRefreshing(false);
                            progress.setVisibility(View.GONE);

                            if(chats.isEmpty()) {
                                errorView.setVisibility(View.VISIBLE);
                            }

                            Toast.makeText(getContext(), "Error connecting to the server. Try checking your internet connection and try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            queue.add(chatRequest);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_chat, parent, false);
            ViewHolder viewHolder = new ViewHolder(layout);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Chat currentChat = chats.get(position);

            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("name", currentChat.getName());
                    intent.putExtra("_id", currentChat.getId());
                    intent.putExtra("isGroup", currentChat.isGroup);
                    startActivity(intent);
                }
            });

            NetworkImageView chatPic = (NetworkImageView) holder.linearLayout.findViewById(R.id.chatlist_pic);
            MorTeam.setNetworkImage(currentChat.getPicPath(), chatPic);

            TextView name = (TextView) holder.linearLayout.findViewById(R.id.chatlist_name);
            name.setText(currentChat.getName());
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout linearLayout;

            public ViewHolder(LinearLayout linearLayout) {
                super(linearLayout);
                this.linearLayout = linearLayout;
            }
        }
    }
}
