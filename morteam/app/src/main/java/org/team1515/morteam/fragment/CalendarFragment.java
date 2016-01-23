package org.team1515.morteam.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.views.llm.LinearLayoutManager;
import org.team1515.morteam.activity.MainActivity;
import org.team1515.morteam.activity.ProfileActivity;
import org.team1515.morteam.entities.Event;
import org.team1515.morteam.entities.Subdivision;
import org.team1515.morteam.entities.User;
import org.team1515.morteam.network.CookieRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {
    private SharedPreferences preferences;
    private RequestQueue queue;

    private Spinner monthSpinner;
    private ArrayAdapter<CharSequence> monthAdapter;
    protected String selectedMonth;
    protected int selectedMonthNum;

    private Spinner yearSpinner;
    private ArrayAdapter<CharSequence> yearAdapter;
    protected String selectedYear;

    private RecyclerView dayView;
    private DayAdapter dayAdapter;
    private LinearLayoutManager dayLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        preferences = getActivity().getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(getContext());

        monthSpinner = (Spinner) view.findViewById(R.id.calendar_months);
        selectedMonth = "";
        selectedMonthNum = 0;
        monthAdapter = ArrayAdapter.createFromResource(getContext(), R.array.months, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = monthAdapter.getItem(position).toString();
                selectedMonthNum = position;
                dayAdapter.getDays();
                dayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Get current year and next 5
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        List<CharSequence> years = new ArrayList<>();
        for (int i = 2015; i < year + 5; i++) {
            years.add(i + "");
        }
        yearSpinner = (Spinner) view.findViewById(R.id.calendar_years);
        selectedYear = "2016";
        yearAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setSelection(years.indexOf(year + ""));
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = yearAdapter.getItem(position).toString();
                dayAdapter.getDays();
                dayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        dayView = (RecyclerView) view.findViewById(R.id.calendar_days);
        dayAdapter = new DayAdapter();
        dayLayoutManager = new LinearLayoutManager(getContext());
        dayView.setAdapter(dayAdapter);
        dayView.setLayoutManager(dayLayoutManager);

        return view;
    }

    public class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
        int daysInMonth;
        final String[] dayNames = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        public List<Event> events;

        public DayAdapter() {
            daysInMonth = 0;
            events = new ArrayList<>();
            getDays();
        }

        public void getEvents() {
            Map<String, String> params = new HashMap<>();
            params.put("month", selectedMonthNum + "");
            params.put("year", selectedYear);

            CookieRequest eventRequest = new CookieRequest(Request.Method.POST,
                    "/f/getEventsForUserInTeamInMonth",
                    params,
                    preferences,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println(response);
                            try {
                                JSONArray eventArray = new JSONArray(response);

                                events = new ArrayList<>();
                                for (int i = 0; i < eventArray.length(); i++) {
                                    JSONObject eventObject = eventArray.getJSONObject(i);

                                    User creator = new User(eventObject.getString("creator"));

                                    List<User> userAttendees = new ArrayList<>();
                                    List<Subdivision> subdivisionAttendees = new ArrayList<>();

                                    //Because someone refuses to actually fix the database
                                    boolean entireTeam = false;
                                    if (eventObject.has("entireTeam")) {
                                        entireTeam = eventObject.getBoolean("entireTeam");
                                    }

                                    if (!entireTeam) {
                                        JSONArray usersArray = eventObject.getJSONArray("userAttendees");
                                        for (int j = 0; j < usersArray.length(); j++) {
                                            userAttendees.add(new User(usersArray.getString(j)));
                                        }
                                        JSONArray subdivisionsArray = eventObject.getJSONArray("subdivisionAttendees");
                                        for (int j = 0; j < subdivisionsArray.length(); j++) {
                                            subdivisionAttendees.add(new Subdivision(subdivisionsArray.getString(j)));
                                        }
                                    } else {
                                        //EntireTeam = true; Get all dem users in the team
                                        for (User user : MainActivity.teamUsers) {
                                            userAttendees.add(user);
                                        }
                                    }

                                    String title = "";
                                    if (eventObject.has("name")) {
                                        title = eventObject.getString("name");
                                    }
                                    String description = "";
                                    if (eventObject.has("description")) {
                                        description = eventObject.getString("description");
                                    }

                                    Event event = new Event(creator,
                                            eventObject.getString("_id"),
                                            title,
                                            description,
                                            eventObject.getString("date"),
                                            userAttendees,
                                            subdivisionAttendees
                                    );
                                    events.add(event);
                                }

                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    }
            );
            queue.add(eventRequest);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout view;

            public ViewHolder(LinearLayout view) {
                super(view);
                this.view = view;
            }
        }

        public void getDays() {
            //Find number of days in month
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, selectedMonthNum);
            cal.set(Calendar.YEAR, Integer.parseInt(selectedYear));
            daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            getEvents();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_date, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Calendar currentDay = Calendar.getInstance();
            currentDay.set(Calendar.DAY_OF_MONTH, position);
            currentDay.set(Calendar.MONTH, selectedMonthNum);
            currentDay.set(Calendar.YEAR, Integer.parseInt(selectedYear));

            TextView dayNum = (TextView) holder.view.findViewById(R.id.calendar_daynum);
            dayNum.setText(Integer.toString(position + 1));

            TextView dayName = (TextView) holder.view.findViewById(R.id.calendar_dayname);
            dayName.setText(dayNames[currentDay.get(Calendar.DAY_OF_WEEK) - 1]);

            ImageView newEventView = (ImageView) holder.view.findViewById(R.id.calendar_newevent);
            if (preferences.getString("position", "").equals("admin") || preferences.getString("position", "").equals("leader")) {
                newEventView.setVisibility(View.VISIBLE);
                newEventView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_createevent, null);

                        final RecyclerView attendeesView = (RecyclerView) dialogView.findViewById(R.id.createevent_attendees);
                        LinearLayoutManager attendeesManager = new LinearLayoutManager(dialogView.getContext());
                        attendeesView.setLayoutManager(attendeesManager);


                        CheckBox inviteEveryoneView = (CheckBox) dialogView.findViewById(R.id.createevent_inviteeveryone);
                        inviteEveryoneView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked) {
                                    attendeesView.setVisibility(View.GONE);
                                } else {
                                    attendeesView.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setView(dialogView);
                        builder.setTitle("Create Event");
                        builder.setNegativeButton("Cancel", null);
                        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.create().show();

                    }
                });
            } else {
                newEventView.setVisibility(View.GONE);
            }


            List<Event> dayEvents = new ArrayList<>();
            for (Event event : events) {
                if (event.getDay() == position) {
                    dayEvents.add(event);
                }
            }

            RecyclerView eventView;
            EventAdapter eventAdapter;
            LinearLayoutManager eventManager;
            eventView = (RecyclerView) holder.view.findViewById(R.id.calendar_events);
            eventAdapter = new EventAdapter(dayEvents);
            eventManager = new LinearLayoutManager(holder.view.getContext());
            eventView.setAdapter(eventAdapter);
            eventView.setLayoutManager(eventManager);
        }

        @Override
        public int getItemCount() {
            return daysInMonth;
        }
    }

    public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
        public List<Event> events;


        public EventAdapter(List<Event> events) {
            this.events = events;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_event, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Event currentEvent = events.get(position);

            TextView titleView = (TextView) holder.layout.findViewById(R.id.event_title);
            titleView.setText(Html.fromHtml(currentEvent.getTitle()));

            TextView descriptionView = (TextView) holder.layout.findViewById(R.id.event_description);
            descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
            descriptionView.setText(Html.fromHtml(currentEvent.getDescription()));

            ImageView deleteView = (ImageView) holder.layout.findViewById(R.id.event_delete);
            if (preferences.getString("position", "").equals("admin") || preferences.getString("position", "").equals("leader")) {
                deleteView.setVisibility(View.VISIBLE);
                deleteView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.layout.getContext());
                        builder.setTitle("Are you sure you want to delete this event?");
                        builder.setNegativeButton("Cancel", null);
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Delete event
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("event_id", currentEvent.getId());
                                CookieRequest deleteRequest = new CookieRequest(Request.Method.POST,
                                        "/f/deleteEvent",
                                        params,
                                        preferences,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                dayAdapter.getEvents();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                error.printStackTrace();
                                            }
                                        }
                                );
                                queue.add(deleteRequest);
                            }
                        });
                        builder.create().show();
                    }
                });
            } else {
                deleteView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layout;

            public ViewHolder(View layout) {
                super(layout);
                this.layout = (LinearLayout) layout;
            }
        }
    }
}
