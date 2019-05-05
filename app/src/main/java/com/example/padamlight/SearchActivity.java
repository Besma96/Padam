package com.example.padamlight;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.padamlight.DirectionHelpers.FetchURL;
import com.example.padamlight.DirectionHelpers.TaskLoadedCallback;
import com.example.padamlight.utils.Toolbox;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    @Bind(R.id.spinner_from)
    Spinner mSpinnerFrom;
    @Bind(R.id.spinner_to)
    Spinner mSpinnerTo;
    @Bind(R.id.button_search)
    Button mButtonSearch;

    private MapActionsDelegate mapDelegate;
    private HashMap<String, Suggestion> mFromList;
    private HashMap<String, Suggestion> mToList;
    private Polyline currentPolyline;

    /*
        Constants FROM lat lng
     */
    static LatLng PADAM = new LatLng(48.8609, 2.349299999999971);
    static LatLng TAO = new LatLng(47.9022, 1.9040499999999838);
    static LatLng FLEXIGO = new LatLng(48.8598, 2.0212400000000343);
    static LatLng LA_NAVETTE = new LatLng(48.8783804, 2.590549);
    static LatLng ILEVIA = new LatLng(50.632, 3.05749000000003);
    static LatLng NIGHT_BUS = new LatLng(45.4077, 11.873399999999947);
    static LatLng FREE2MOVE = new LatLng(33.5951, -7.618780000000015);

    /*
       Constants TO lat lng
    */
    static LatLng EIFFEL = new LatLng(48.8609, 2.287592);
    static LatLng AUBERVILLIERS = new LatLng(48.934196, 1.863724);
    static LatLng BANLIEU = new LatLng(48.286054, 4.068336);
    static LatLng UKRAINE = new LatLng(50.781946, 26.909509);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding UI elements defined below
        ButterKnife.bind(this);

        initializeTextViews();
        initSpinners();
        initMap();
    }

    private void initializeTextViews() {
        mButtonSearch.setText(R.string.button_search);
    }

    private void initMap() {
        /*
            Instanciate MapFragment to get the map on the page
         */
        MapFragment mapFragment = MapFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_map, mapFragment)
                .commitAllowingStateLoss();

        //
        this.mapDelegate = mapFragment;
    }

    /**
     * Initialize spinners from and to
     */
    // TODO : Initialize the "To" spinner with data of your choice
    private void initSpinners() {
        List<String> fromList = Toolbox.formatHashmapToList(initFromHashmap());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fromList);
        mSpinnerFrom.setAdapter(adapter);

        List<String> toList = Toolbox.formatHashmapToList(initToHashmap());
        ArrayAdapter<String> convert = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, toList);
        mSpinnerTo.setAdapter(convert);
    }

    /**
     * Using Hashmap to initialize FROM suggestion list
     */
    private HashMap<String, Suggestion> initFromHashmap() {
        mFromList = new HashMap<>();
        mFromList.put("Padam", new Suggestion(PADAM));
        mFromList.put("Tao Résa'Est", new Suggestion(TAO));
        mFromList.put("Flexigo", new Suggestion(FLEXIGO));
        mFromList.put("La Navette", new Suggestion(LA_NAVETTE));
        mFromList.put("Ilévia", new Suggestion(ILEVIA));
        mFromList.put("Night Bus", new Suggestion(NIGHT_BUS));
        mFromList.put("Free2Move", new Suggestion(FREE2MOVE));
        return mFromList;
    }

    /**
     * Using Hashmap to initialize TO suggestion list
     */
    private HashMap<String, Suggestion> initToHashmap() {
        mToList = new HashMap<>();
        mToList.put("Tour Eiffel", new Suggestion(EIFFEL));
        mToList.put("Fort d'Aubervilliers", new Suggestion(AUBERVILLIERS));
        mToList.put("Ouest Parisien", new Suggestion(BANLIEU));
        mToList.put("Ukraine", new Suggestion(UKRAINE));
        mToList.put("Ilévia", new Suggestion(ILEVIA));
        return mToList;
    }

    /**
     * Define what to do after the button click interaction
     */
    //TODO : Implement the same thing for "To" spinner
    @OnClick(R.id.button_search)
    void onClickSearch() {

        /*
            Retrieve selection of "From" spinner
         */
        String selectedFrom = String.valueOf(mSpinnerFrom.getSelectedItem());
        String selectedTo = String.valueOf(mSpinnerTo.getSelectedItem());

        if (selectedFrom != null || !selectedFrom.isEmpty()|| selectedTo != null || !selectedTo.isEmpty()) {
            mapDelegate.clearMap();

            Suggestion selectedFromSuggestion = mFromList.get(selectedFrom);
            mapDelegate.updateMarker(MarkerType.PICKUP, selectedFrom, selectedFromSuggestion.getLatLng());
            mapDelegate.updateMap(selectedFromSuggestion.getLatLng());

            Suggestion selectedToSuggestion = mToList.get(selectedTo);
            mapDelegate.updateMarker(MarkerType.DROPOFF, selectedTo, selectedToSuggestion.getLatLng());
            mapDelegate.updateMap(selectedToSuggestion.getLatLng());

            new FetchURL(SearchActivity.this).execute(getUrl(selectedFromSuggestion.getLatLng(), selectedToSuggestion.getLatLng(), "driving"), "driving");


            mapDelegate.getMapAsync(this);
        }

    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        mapDelegate.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapDelegate.onMapReady(googleMap);
    }
}


