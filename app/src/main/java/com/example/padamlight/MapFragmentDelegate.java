package com.example.padamlight;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.ExecutionException;

interface MapFragmentDelegate {


    void updateMap(LatLng... latLngs);

    void updateMarker(MapFragment.MarkerType type, String markerName, LatLng markerLatLng);

    void clearMap();

    void Itineraire(LatLng origin, LatLng destination);
}
