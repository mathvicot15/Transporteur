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
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
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
    private LocationComponent locationcomponent;
    private Location originLocation;
    private Point originPosition;
    private Point destinationPosition;
    private Marker destinationMarker;
    private NavigationMapRoute navigationMapRoute;
    private DirectionsRoute currentRoute;
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
            case R.id.config :
                Intent intent3 = new Intent(this, Config.class);
                this.startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        Button btnNavigation = (Button) findViewById(R.id.btnNavigation);
        if(location != null && btnNavigation.getVisibility() == View.GONE){
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.map = mapboxMap;
        enableLocation();

        //Si l'utilisateur à été redirigé sur la carte suite à la reception du sms
        Intent intent = getIntent();
        if(intent.hasExtra("latitude")){

            //Récuperer les paramètres de redirection
            Bundle extras = intent.getExtras();

            //LatLng de la destination
            Double latitudeD = Double.parseDouble(extras.getString("latitude"));
            Double longitudeD = Double.parseDouble(extras.getString("longitude"));

            //LatLng de l'origine
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Double latitudeO = location.getLatitude();
            Double longitudeO = location.getLongitude();

            //Création de l'objet latlng par rapport à la destination
            LatLng latLngD = new LatLng(latitudeD, longitudeD);
            LatLng latLngO = new LatLng(latitudeO, longitudeO);

            if(destinationMarker != null){
                map.removeMarker(destinationMarker);
            }
            else {

                //Origine
                originPosition = Point.fromLngLat(longitudeO, latitudeO);

                //Destination

                //__Code final__
                destinationPosition = Point.fromLngLat(longitudeD, latitudeD);
                //______________

                //__Code simulation__
                //Décommenter pour simuler une géolocalisation (Paris) - Commenter les lignes suivant "//__Code final__"
                //destinationPosition = Point.fromLngLat(2.333333, 48.866667);
                //latLngD = new LatLng(48.866667, 2.333333);
                //___________________

                //Ajouter le marqueur du point de destination
                destinationMarker = map.addMarker(new MarkerOptions().position(latLngD));

                //Créer la route
                getRoute(originPosition, destinationPosition);

                //Centrer sur l'origine et la destination
                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                        .include(latLngO)
                        .include(latLngD)
                        .build();
                map.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 300), 5000);

                //Gerer l'affichage des boutons
                Button btnNavigation = (Button) findViewById(R.id.btnNavigation);
                Button btnRefus = (Button) findViewById(R.id.btnRefus);
                btnNavigation.setVisibility(View.VISIBLE);
                btnRefus.setVisibility(View.VISIBLE);

                //Gerer le click sur le bouton de démarrage de la navigation
                btnNavigation.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){

                        //Message au conducteur
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(Config.telConducteur1, null, "Un conducteur est en chemin", null, null);

                        //Gerer l'affichage des boutons
                        Button btnArretNavigation = (Button) findViewById(R.id.btnArretNavigation);
                        btnArretNavigation.setVisibility(View.VISIBLE);
                        btnNavigation.setVisibility(View.GONE);
                        btnRefus.setVisibility(View.GONE);

                        //Centrer la camera sur la position de l'utilisateur
                        CameraPosition position = new CameraPosition.Builder()
                                .target(latLngO)
                                .zoom(18)
                                .bearing(0)
                                .tilt(60)
                                .build();

                        mapboxMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(position), 7000);

                        mapboxMap.setStyleUrl(Style.MAPBOX_STREETS);

                        //Gerer le click sur le bouton d'arret de la navigation
                        btnArretNavigation.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v){

                                //Affichage du bouton
                                btnArretNavigation.setVisibility(View.GONE);

                                //Supprimer la route et le marqueur de destination
                                map.removeMarker(destinationMarker);
                                navigationMapRoute.removeRoute();
                                mapboxMap.setStyleUrl(Style.LIGHT);
                            }
                        });
                    }
                });

                //Gerer le click sur le bouton de refus du trajet
                btnRefus.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){

                        //Affichage des boutons
                        btnNavigation.setVisibility(View.GONE);
                        btnRefus.setVisibility(View.GONE);

                        //Supprimer la route et le marqueur de destination
                        map.removeMarker(destinationMarker);
                        navigationMapRoute.removeRoute();
                    }
                });
            }
        }
    }

    private void getRoute(Point origin, Point destination){
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body() == null){
                            Toast.makeText(getApplicationContext(),"Aucune direction n'a été trouvée, vérifiez les autorisations de l'application",Toast.LENGTH_LONG).show();
                            return;
                        } else if(response.body().routes().size() == 0) {
                            Toast.makeText(getApplicationContext(), "Aucune direction n'a été trouvée pour cette destination", Toast.LENGTH_LONG).show();
                            return;
                        }
                        currentRoute = response.body().routes().get(0);

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

    private void setCameraPosition(Location location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18));
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
}
