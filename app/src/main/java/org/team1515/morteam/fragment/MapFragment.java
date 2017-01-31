package org.team1515.morteam.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.network.CookieRequest;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    MapView mMapView;
    EditText searchMap;
    private GoogleMap googleMap;

    boolean teamFound;

    List<Marker> markers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        markers = new ArrayList<>();

        mMapView = (MapView) rootView.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        searchMap = (EditText) rootView.findViewById(R.id.map_search);
        searchMap.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                String searchText = arg0.toString();
                searchLocations(searchText);
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }
        });

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                //Checks for location permission
                int hasPerm = getContext().getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, getContext().getPackageName());
                int hasPerm2 = getContext().getPackageManager().checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, getContext().getPackageName());

                if (hasPerm == PackageManager.PERMISSION_GRANTED || hasPerm2 == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        });

        getMapLocations();

        return rootView;
    }

    public void getMapLocations() {
        CookieRequest mapRequest = new CookieRequest(
                Request.Method.GET,
                "/teamLocations.json",
                false,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject resObject = new JSONObject(response);

                            JSONArray teams = resObject.names();
                            teamFound = false;

                            for (int i = 0; i < teams.length(); i++) {
                                String teamNum = teams.getString(i);

                                double lat = resObject.getJSONObject(teams.getString(i)).getDouble("lat");
                                double lng = resObject.getJSONObject(teams.getString(i)).getDouble("lng");

                                LatLng team = new LatLng(lat, lng);
                                markers.add(googleMap.addMarker(new MarkerOptions().position(team).title(teamNum)));

                                if (teamNum.equals("1515")) {
                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(team).zoom(12).build();
                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                }
                            }
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
        MorTeam.queue.add(mapRequest);
    }

    public void searchLocations(String searchText) {
        teamFound = false;

        for (int i = 0; i < markers.size(); i++) {
            Marker currentMarker = markers.get(i);

            if (currentMarker.getTitle().contains(searchText)) {
                currentMarker.setVisible(true);

                if (currentMarker.getTitle().equals(searchText)) {
                    teamFound = true;
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(currentMarker.getPosition()).zoom(12).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

                if (!teamFound) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(currentMarker.getPosition()).zoom(12).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            } else {
                currentMarker.setVisible(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}