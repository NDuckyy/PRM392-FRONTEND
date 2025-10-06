package com.example.prm392_frontend;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity {
    private MapView map;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private final double STORE_LAT = 21.028511;
    private final double STORE_LNG = 105.804817;

    private GeoPoint currentLocation;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView txtInfo;
    private Marker userMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_maps);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        txtInfo = findViewById(R.id.txtInfo);

        // Marker cửa hàng
        IMapController mapController = map.getController();
        GeoPoint storePoint = new GeoPoint(STORE_LAT, STORE_LNG);
        mapController.setZoom(15.0);
        mapController.setCenter(storePoint);

        Marker storeMarker = new Marker(map);
        storeMarker.setPosition(storePoint);
        storeMarker.setTitle("My Store");
        storeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(storeMarker);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button btnMyLocation = findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(v -> requestCurrentLocation(false));

        Button btnDirections = findViewById(R.id.btnDirections);
        btnDirections.setOnClickListener(v -> requestCurrentLocation(true));

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void requestCurrentLocation(boolean drawRoute) {
        if (!checkPermission()) return;

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000
        ).setMaxUpdates(1).build();

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateUserMarker(location);

                    if (drawRoute) {
                        GeoPoint storePoint = new GeoPoint(STORE_LAT, STORE_LNG);
                        BoundingBox box = BoundingBox.fromGeoPointsSafe(
                                List.of(currentLocation, storePoint)
                        );
                        map.zoomToBoundingBox(box, true, 100);
                        getRoute(currentLocation, storePoint);
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Không tìm thấy vị trí GPS", Toast.LENGTH_SHORT).show();
                }

                // dừng sau khi lấy 1 lần
                fusedLocationClient.removeLocationUpdates(this);
            }
        }, getMainLooper());
    }

    private void updateUserMarker(Location location) {
        currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        Log.d("MapsActivity", "User location: lat=" + currentLocation.getLatitude() +
                ", lon=" + currentLocation.getLongitude());

        if (userMarker != null) {
            map.getOverlays().remove(userMarker);
        }

        userMarker = new Marker(map);
        userMarker.setPosition(currentLocation);
        userMarker.setTitle("You are here");
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(userMarker);

        map.getController().animateTo(currentLocation);
        map.getController().setZoom(17.0);
    }

    private void getRoute(GeoPoint start, GeoPoint end) {
        String url = "https://router.project-osrm.org/route/v1/driving/"
                + start.getLongitude() + "," + start.getLatitude() + ";"
                + end.getLongitude() + "," + end.getLatitude()
                + "?overview=full&geometries=geojson";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MapsActivity.this, "Lỗi khi gọi OSRM API", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());

                        JSONObject routeObj = json.getJSONArray("routes").getJSONObject(0);
                        double distance = routeObj.getDouble("distance") / 1000.0;
                        double duration = routeObj.getDouble("duration") / 60.0;

                        JSONArray coords = routeObj
                                .getJSONObject("geometry")
                                .getJSONArray("coordinates");

                        List<GeoPoint> geoPoints = new ArrayList<>();
                        for (int i = 0; i < coords.length(); i++) {
                            JSONArray point = coords.getJSONArray(i);
                            double lon = point.getDouble(0);
                            double lat = point.getDouble(1);
                            geoPoints.add(new GeoPoint(lat, lon));
                        }

                        runOnUiThread(() -> {
                            Polyline line = new Polyline();
                            line.setPoints(geoPoints);
                            line.setColor(Color.BLUE);
                            line.setWidth(8f);
                            map.getOverlays().add(line);
                            map.invalidate();

                            txtInfo.setText(String.format("Khoảng cách: %.1f km – Thời gian: %.0f phút", distance, duration));
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Chưa có quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }
}
