package net.team1515.morteam.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import net.team1515.morteam.R;
import net.team1515.morteam.network.CookieRequest;
import net.team1515.morteam.network.ImageCookieRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    SharedPreferences preferences;
    RequestQueue queue;

    private String chatName;
    private String chatId;
    private boolean isGroup;

    RecyclerView messageList;
    MessageAdapter messageAdapter;
    LinearLayoutManager layoutManager;

    private Socket socket;
    {
        try {
            socket = IO.socket("http://www.morteam.com");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get messages
        Intent intent = getIntent();
        chatName = intent.getStringExtra("name");
        chatId = intent.getStringExtra("_id");
        isGroup = intent.getBooleanExtra("isGroup", false);

        messageList = (RecyclerView) findViewById(R.id.chat_messagelist);
        messageAdapter = new MessageAdapter();
        layoutManager = new LinearLayoutManager(this);
        messageList.setLayoutManager(layoutManager);
        messageList.setAdapter(messageAdapter);

        socket = socket.connect();
        System.out.println(socket.connected());
    }


    public void sendClick(View view) {
        EditText messageText = (EditText) findViewById(R.id.chat_message);
        final String messageContent = messageText.getText().toString();

        Map<String, String> params = new HashMap<>();
        params.put("chat_id", chatId);
        params.put("content", messageContent);

        CookieRequest sendRequest = new CookieRequest(Request.Method.POST, "/f/sendMessage",
                params,
                preferences,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject messageObject = new JSONObject();
                try {
                    messageObject.put("chat_id", chatId);
                    messageObject.put("content", messageContent);
                    if(isGroup) {
                        messageObject.put("type", "group");
                        messageObject.put("chat_name", chatName);
                    } else {
                        messageObject.put("type", "private");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(messageObject);
                socket.emit("message", messageObject.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        queue.add(sendRequest);
    }

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private CookieRequest messageRequest;
        private Map<String, String> params;
        private List<Message> messages;

        public MessageAdapter() {
            messages = new ArrayList<>();
            params = new HashMap<>();
            params.put("chat_id", chatId);
            messageRequest = new CookieRequest(
                    Request.Method.POST,
                    "/f/loadMessagesForChat",
                    params,
                    preferences,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray messageArray = new JSONArray(response);
                                for(int i = messageArray.length() - 1; i >= 0; i--) {
                                    JSONObject messageObject = messageArray.getJSONObject(i);
                                    String id = messageObject.getString("_id");
                                    String content = messageObject.getString("content");

                                    JSONObject authorObject = messageObject.getJSONObject("author");
                                    String name = authorObject.getString("firstname") + " " + authorObject.getString("lastname");
                                    String picPath = authorObject.getString("profpicpath") + "-60";
                                    boolean isMyChat = false;
                                    if(authorObject.getString("_id").equals(preferences.getString("_id", ""))) {
                                        isMyChat = true;
                                    }

                                    final Message message = new Message(name, content, id, picPath, isMyChat);

                                    ImageCookieRequest messagePicRequest = new ImageCookieRequest(
                                            "http://www.morteam.com" + message.picPath,
                                            preferences,
                                            new Response.Listener<Bitmap>() {
                                                @Override
                                                public void onResponse(Bitmap response) {
                                                    message.pic = response;
                                                    notifyDataSetChanged();
                                                }
                                            }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            System.out.println(error);
                                        }
                                    });
                                    queue.add(messagePicRequest);

                                    messages.add(message);
                                }
                                notifyDataSetChanged();
                                messageList.scrollToPosition(messages.size() - 1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("ERROR: " + error);
                        }
                    }
            );
            queue.add(messageRequest);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.messagelist_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(cardView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Message currentMessage = messages.get(position);

            TextView message = (TextView) holder.cardView.findViewById(R.id.messagelist_message);
            message.setText(Html.fromHtml(currentMessage.content));

            final ImageView messagePic = (ImageView) holder.cardView.findViewById(R.id.messagelist_pic);
            messagePic.setImageBitmap(currentMessage.pic);

            if(currentMessage.isMyChat) {
                holder.cardView.setCardBackgroundColor(Color.argb(255, 255, 197, 71));
            } else {
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CardView cardView;

            public ViewHolder(CardView cardView) {
                super(cardView);
                this.cardView = cardView;
            }
        }
    }

    public class Message {
        public String name;
        public String content;
        public String id;
        public String picPath;
        public Bitmap pic;
        public boolean isMyChat;

        public Message(String name, String content, String id, String picPath, boolean isMyChat) {
            this.name = name;
            this.content = content;
            this.id = id;
            this.picPath = picPath;
            this.isMyChat = isMyChat;
        }
    }
}
