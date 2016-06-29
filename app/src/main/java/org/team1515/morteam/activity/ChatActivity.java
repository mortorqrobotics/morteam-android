package org.team1515.morteam.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import net.team1515.morteam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.adapter.ChatMessageAdapter;
import org.team1515.morteam.entity.Message;
import org.team1515.morteam.entity.User;
import org.team1515.morteam.network.CookieRequest;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {
    private String chatName;
    private String chatId;
    private boolean isGroup;

    private RecyclerView messageList;
    private ChatMessageAdapter messageAdapter;
    private LinearLayoutManager messageLayoutManager;
    private boolean loading = false;
    private boolean canLoadMore = true;
    private int firstItem, visibleItemCount, totalItemCount;

    private Socket socket;

    private boolean isClearingText;

    private List<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        //Get messages
        Intent intent = getIntent();
        chatName = intent.getStringExtra("name");
        chatId = intent.getStringExtra("_id");
        isGroup = intent.getBooleanExtra("isGroup", false);

        messageList = (RecyclerView) findViewById(R.id.chat_messagelist);
        messageAdapter = new ChatMessageAdapter();
        messageLayoutManager = new LinearLayoutManager(this);
        messageLayoutManager.setReverseLayout(true);
        messageList.setLayoutManager(messageLayoutManager);
        messageList.setAdapter(messageAdapter);

        messages = new ArrayList<>();

        messageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) //check for scroll up
                {
                    if (!loading && canLoadMore) {
                        visibleItemCount = messageLayoutManager.getChildCount();
                        totalItemCount = messageLayoutManager.getItemCount();
                        firstItem = messageLayoutManager.findFirstVisibleItemPosition();

                        if (visibleItemCount + firstItem >= totalItemCount - 2) {
                            loading = true;
                            getChats();
                        }
                    }
                }
            }
        });

        isClearingText = false;
        final EditText messageBox = (EditText) findViewById(R.id.chat_message);
        messageBox.addTextChangedListener(new TextWatcher() {
            Date lastTypeTime = null;

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                if (!isClearingText) {
                    try {
                        JSONObject typingObject = new JSONObject();
                        typingObject.put("chat_id", chatId);
                        socket.emit("start typing", typingObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                lastTypeTime = new Date();
            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // Dispatch after done typing (1 sec after)
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        Date runTime = new Date();

                        if ((lastTypeTime.getTime() + 1000) <= runTime.getTime()) {
                            try {
                                JSONObject typingObject = new JSONObject();
                                typingObject.put("chat_id", chatId);
                                socket.emit("stop typing", typingObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                };
                timer.schedule(timerTask, 1000);
            }
        });


        try {
            socket = IO.socket("http://www.morteam.com");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        final String sessionId = MorTeam.preferences.getString(CookieRequest.SESSION_COOKIE, "");

        socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Transport transport = (Transport) args[0];
                transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> headers = (Map<String, String>) args[0];

                        // set header
                        //Insert session-id cookie into header
                        if (sessionId.length() > 0) {
                            StringBuilder builder = new StringBuilder();
                            builder.append(CookieRequest.SESSION_COOKIE);
                            builder.append("=");
                            builder.append(sessionId);
                            if (headers.containsKey(CookieRequest.COOKIE_KEY)) {
                                builder.append("; ");
                                builder.append(headers.get(CookieRequest.COOKIE_KEY));
                            }
                            headers.put(CookieRequest.COOKIE_KEY, builder.toString());
                        }
                    }
                }).on(Transport.EVENT_RESPONSE_HEADERS, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> headers = (Map<String, String>) args[0];
                        //No headers to get here at the moment
                    }
                });
            }
        });
        socket = socket.connect();

        socket.emit("get clients");
        socket.on("get clients", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //TODO: Get online clients
            }
        });

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject messageObject = new JSONObject(args[0].toString());

                    String firstName = messageObject.getString("author_fn");
                    String lastName = messageObject.getString("author_ln");
                    String content = messageObject.getString("content");
                    String date = messageObject.getString("timestamp");
                    String chatId = messageObject.getString("chat_id");
                    String profPicPath = messageObject.getString("author_profpicpath") + "-60";
                    profPicPath = profPicPath.replace(" ", "+");

                    Intent intent = new Intent("message");
                    intent.putExtra("firstname", firstName);
                    intent.putExtra("lastname", lastName);
                    intent.putExtra("content", content);
                    intent.putExtra("date", date);
                    intent.putExtra("chatId", chatId);
                    intent.putExtra("profPicPath", profPicPath);
                    LocalBroadcastManager.getInstance(ChatActivity.this).sendBroadcast(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                messageAdapter.addMessage(
                        intent.getStringExtra("firstname"),
                        intent.getStringExtra("lastname"),
                        intent.getStringExtra("content"),
                        intent.getStringExtra("date"),
                        intent.getStringExtra("chatId"),
                        intent.getStringExtra("profPicPath"),
                        false
                );

                messageList.smoothScrollToPosition(0);
            }
        }, new IntentFilter("message"));

        getChats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        socket.disconnect();
        socket.off();
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void sendClick(View view) {
        //Disable send button until message sent
        final Button sendButton = (Button) findViewById(R.id.chat_send);
        sendButton.setClickable(false);

        final EditText messageText = (EditText) findViewById(R.id.chat_message);
        final String messageContent = messageText.getText().toString();

        if (!messageContent.isEmpty()) {

            Map<String, String> params = new HashMap<>();
            params.put("content", messageContent);

            CookieRequest sendRequest = new CookieRequest(Request.Method.POST, "/chats/id/" + chatId + "/messages",
                    params,
                    MorTeam.preferences,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject typingObject = new JSONObject();
                                typingObject.put("chat_id", chatId);
                                socket.emit("stop typing", typingObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            JSONObject messageObject = new JSONObject();
                            try {
                                messageObject.put("chat_id", chatId);
                                messageObject.put("content", messageContent);
                                if (isGroup) {
                                    messageObject.put("type", "group");
                                    messageObject.put("chat_name", chatName);
                                } else {
                                    messageObject.put("type", "private");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("message", messageObject);

                            isClearingText = true;
                            messageText.setText("");
                            isClearingText = false;

                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                            System.out.println(df.format(new Date()));
                            messageAdapter.addMessage(
                                    MorTeam.preferences.getString("firstname", ""),
                                    MorTeam.preferences.getString("lastname", ""),
                                    messageContent,
                                    df.format(new Date()),
                                    chatId,
                                    MorTeam.preferences.getString("profpicpath", "") + "-60",
                                    true
                            );
                            messageList.smoothScrollToPosition(0);
                            sendButton.setClickable(true);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    sendButton.setClickable(true);
                }
            });
            MorTeam.queue.add(sendRequest);
        }
    }


    public void getChats() {
        final int skip;
        if (!messages.isEmpty()) {
            skip = messages.size();
        } else {
            skip = 0;
        }
        CookieRequest messageRequest = new CookieRequest(
                Request.Method.GET,
                "/chats/id/" + chatId + "/messages?skip=" + skip,
                MorTeam.preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray messageArray = new JSONArray(response);
                            if (skip - messages.size() >= messageArray.length()) {
                                //No more messages are left in the chat - cease fire(ing request!)
                                canLoadMore = false;
                            } else {
                                for (int i = (skip == 0 ? 0 : skip - messages.size()); i < messageArray.length(); i++) {
                                    JSONObject messageObject = messageArray.getJSONObject(i);
                                    String id = messageObject.getString("_id");
                                    String content = messageObject.getString("content");
                                    String date = messageObject.getString("timestamp");

                                    JSONObject authorObject = messageObject.getJSONObject("author");
                                    String firstName = authorObject.getString("firstname");
                                    String lastName = authorObject.getString("lastname");
                                    String profPicPath = authorObject.getString("profpicpath") + "-60";
                                    profPicPath = profPicPath.replace(" ", "+");
                                    boolean isMyChat = false;
                                    if (authorObject.getString("_id").equals(MorTeam.preferences.getString("_id", ""))) {
                                        isMyChat = true;
                                    }

                                    final Message message = new Message(new User(firstName, lastName, null, profPicPath), content, date, chatId, isMyChat);

//                                    if (skip <= 0) {
                                        messages.add(message);
//                                    }
                                }

                                messageAdapter.setMessages(messages);
                            }
                            loading = false;
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
        MorTeam.queue.add(messageRequest);
    }

}
