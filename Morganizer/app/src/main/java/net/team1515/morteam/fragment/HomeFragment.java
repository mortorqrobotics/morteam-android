package net.team1515.morteam.fragment;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.team1515.morteam.R;
import net.team1515.morteam.network.CookieRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private SharedPreferences preferences;
    private AnnouncementAdapter adapter;
    private SwipeRefreshLayout swipeLayout;
    private SlidingUpPanelLayout slidingLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        preferences = getActivity().getSharedPreferences(null, 0);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.announcement_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
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

        slidingLayout = (SlidingUpPanelLayout) view.findViewById(R.id.slidingLayout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingLayout.setTouchEnabled(false);

        return view;
    }

    public void openNewAnnouncement() {
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void collapseNewAnnouncement() {
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        TextView message = (TextView) getView().findViewById(R.id.new_message);
        message.setText("");
    }

    public SlidingUpPanelLayout.PanelState getNewAnnouncementStatus() {
        return slidingLayout.getPanelState();
    }

    public void requestAnnouncements() {
        adapter.requestAnnouncements();
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
                                    object.getString("content"), object.getString("timestamp"), object.getJSONObject("author").getString("profpicpath")));

                            final int announcementNum = i;
                            System.out.println("http://www.morteam.com" + announcements.get(i).picSrc + "-60");
                            ImageRequest profPicRequest = new ImageRequest("http://www.morteam.com" + announcements.get(i).picSrc + "-60.png", new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    announcements.get(announcementNum).setPic(response);
                                    notifyDataSetChanged();
                                    System.out.println("0");
                                }
                            }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println(error);
                                }
                            });
                            queue.add(profPicRequest);
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
            ImageView profPic = (ImageView) holder.cardView.findViewById(R.id.announcement_pic);
            profPic.setImageBitmap(announcements.get(position).pic);

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
            public final String picSrc;
            public Bitmap pic;

            public Announcement(String author, String message, String date, String picSrc) {
                this.author = author;
                this.message = message;
                this.date = date;
                this.picSrc = picSrc;
                this.pic = null;
            }

            public void setPic(Bitmap pic) {
                this.pic = pic;
            }
        }
    }
}