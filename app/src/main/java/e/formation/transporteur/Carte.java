package e.formation.transporteur;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//Mapbox
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Carte extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener{

    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private LocationComponent locationcomponent;
    private Location originLocation;
    private Point originPosition;
    private Point destinationPosition;
    private Marker destinationMarker;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_carte);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    };

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //Present toast or dialog.
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            enableLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        enableLocation();

        Intent intent = getIntent();
        if(intent.hasExtra("latitude")){

            Bundle extras = intent.getExtras();
            //LatLng de la destination
            Double latitudeD = Double.parseDouble(extras.getString("latitude"));
            Double longitudeD = Double.parseDouble(extras.getString("longitude"));

            //LatLng de l'utilisateur
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Double latitudeO = location.getLatitude();
            Double longitudeO = location.getLongitude();

            //Création de l'objet latlng par rapport à la destination
            LatLng latLng = new LatLng();
            latLng.setLatitude(latitudeD);
            latLng.setLongitude(longitudeD);

            if(destinationMarker != null){
                map.removeMarker(destinationMarker);
            }
            else {
                destinationMarker = map.addMarker(new MarkerOptions().position(latLng));

                originPosition = Point.fromLngLat(longitudeD, latitudeD);
                destinationPosition = Point.fromLngLat(longitudeO, latitudeO);

                Button btnNavigation = (Button) findViewById(R.id.btnNavigation);
                Button btnRefus = (Button) findViewById(R.id.btnRefus);
                btnNavigation.setVisibility(View.VISIBLE);
                btnRefus.setVisibility(View.VISIBLE);

                getRoute(originPosition, destinationPosition);

                btnNavigation.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){

                        //Message au conducteur
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(Conduire.telConducteur1, null, "Un conducteur est en chemin", null, null);
                        btnNavigation.setVisibility(View.GONE);
                        btnRefus.setVisibility(View.GONE);

                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .origin(originPosition)
                                .destination(destinationPosition)
                                .shouldSimulateRoute(false)
                                .build();
                        NavigationLauncher.startNavigation(Carte.this, options);
                    }
                });

                btnRefus.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        btnNavigation.setVisibility(View.GONE);
                        btnRefus.setVisibility(View.GONE);

                        navigationMapRoute.removeRoute();
                    }
                });
            }
        }
    }

    private void getRoute(Point origin, Point destination){
        NavigationRoute.builder()
                .accessToken(getString(R.string.access_token))
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body() == null){
                            Toast.makeText(getApplicationContext(),"Aucune direction n'a été trouvée, vérifiez les autorisations de l'application",Toast.LENGTH_LONG).show();
                            return;
                        } else if(response.body().routes().size() == 0){
                            Toast.makeText(getApplicationContext(),"Aucune direction n'a été trouvée pour cette destination",Toast.LENGTH_LONG).show();
                            return;
                        }

                        DirectionsRoute currentRoute = response.body().routes().get(0);

                        if(navigationMapRoute != null){
                            navigationMapRoute.removeRoute();
                        } else{
                            navigationMapRoute = new NavigationMapRoute(null, mapView, map);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }
                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG, "Erreur : "+t.getMessage());
                    }
                });
    }

    @SuppressWarnings("MissingPermission")
    private void enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            initializeLocationEngine();
            LocationComponent locationComponent = map.getLocationComponent();
            locationComponent.activateLocationComponent(this);
            locationComponent.setRenderMode(RenderMode.GPS);
            locationComponent.setLocationComponentEnabled(true);
            //initializeLocationLayer();

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation != null){
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer(){
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraPosition(Location location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.conduire :
                Intent intent1 = new Intent(this,Conduire.class);
                this.startActivity(intent1);
                return true;
            case R.id.carte :
                Intent intent2 = new Intent(this, Carte.class);
                this.startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
