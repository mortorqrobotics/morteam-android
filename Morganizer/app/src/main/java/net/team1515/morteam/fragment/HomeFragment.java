package net.team1515.morteam.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;
import net.team1515.morteam.network.CookieRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swipeLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        preferences = getActivity().getSharedPreferences(null, 0);

        recyclerView = (RecyclerView) view.findViewById(R.id.announcement_view);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AnnouncementAdapter();
        recyclerView.setAdapter(adapter);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeLayout.setColorSchemeResources(R.color.orange_theme);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.requestAnnouncements();
            }
        });

        return view;
    }

    public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

        private CookieRequest request;
        private RequestQueue queue;

        private ArrayList<Announcement> announcements;

        public AnnouncementAdapter() {
            announcements = new ArrayList<>();
            queue = Volley.newRequestQueue(getContext());
            request = new CookieRequest(Request.Method.POST, "/f/getAnnouncementsForUser", preferences, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray array = new JSONArray(response);
                        announcements = new ArrayList<>();
                        for(int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            announcements.add(new Announcement(
                                    object.getJSONObject("author").getString("firstname") + " " + object.getJSONObject("author").getString("lastname"),
                                    object.getString("content"), object.getString("timestamp")));
                        }

                        //Tell adapter to update once request is finished
                        notifyDataSetChanged();
                        swipeLayout.setRefreshing(false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error);
                }
            });
            requestAnnouncements();
        }

        public void requestAnnouncements() {
            queue.add(request);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CardView cardView;

            public ViewHolder(CardView cardView) {
                super(cardView);
                this.cardView = cardView;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView view = (CardView)LayoutInflater.from(parent.getContext()).inflate(R.layout.announcement, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TextView author = (TextView) holder.cardView.findViewById(R.id.author);
            author.setText(announcements.get(position).author);

            TextView date = (TextView) holder.cardView.findViewById(R.id.date);
            //Fix up the iso date format string for parsing
            String dateString = announcements.get(position).date.replace("Z", "+0000");

            try {
                CharSequence formattedDate = DateFormat.format("h:m a - MMMM d, yyyy", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(dateString));
                date.setText(formattedDate);
            } catch (ParseException e) {
                date.setText("error");
            }

            TextView message = (TextView) holder.cardView.findViewById(R.id.message);
            message.setText(Html.fromHtml(announcements.get(position).message));
        }

        @Override
        public int getItemCount() {
            return announcements.size();
        }

        private class Announcement {
            public final String author;
            public final String message;
            public final String date;

            public Announcement(String author, String message, String date) {
                this.author = author;
                this.message = message;
                this.date = date;
            }
        }
    }
}
