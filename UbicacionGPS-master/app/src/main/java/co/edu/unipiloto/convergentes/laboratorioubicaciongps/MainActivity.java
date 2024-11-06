package co.edu.unipiloto.convergentes.laboratorioubicaciongps;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button bt_location, bt_route;
    TextView text_view1, text_view2, text_view3, text_view4, text_view5, text_view6, location_view;
    FusedLocationProviderClient fusedLocationProviderClient;
    ListView listView;
    ArrayList<String> locationList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bt_location = findViewById(R.id.bt_location);
        bt_route = findViewById(R.id.bt_route);

        text_view1 = findViewById(R.id.text_view1);
        text_view2 = findViewById(R.id.text_view2);
        text_view3 = findViewById(R.id.text_view3);
        text_view4 = findViewById(R.id.text_view4);
        text_view5 = findViewById(R.id.text_view5);
        text_view6 = findViewById(R.id.text_view6);
        location_view = findViewById(R.id.location_view);

        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationList);
        listView.setAdapter(adapter);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Verificar permisos al iniciar la aplicación
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        bt_location.setOnClickListener(v -> {
            location_view.setText("Checking permissions");

            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location_view.setText("Permission granted :)");
                getLocation();
            } else {
                location_view.setText("Permission denied, requesting permission");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        });

        bt_route.setOnClickListener(v -> {
            location_view.setText("Checking permissions");

            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                showRouteToCurrentLocation();
            } else {
                location_view.setText("Permission denied, requesting permission");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLocation = locationList.get(position);
            location_view.setText("Route to: " + selectedLocation);
            showRouteToSelectedLocation(selectedLocation);
        });
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1
                            );

                            String addressInfo = "Lat: " + location.getLatitude() + ", Long: " + location.getLongitude();
                            locationList.add(addressInfo);  // Agregar la ubicación a la lista
                            adapter.notifyDataSetChanged();  // Actualizar ListView

                            // Actualiza la información mostrada
                            text_view1.setText("Latitude: " + location.getLatitude());
                            text_view2.setText("Longitude: " + location.getLongitude());
                            text_view3.setText("Country: " + addresses.get(0).getCountryName());
                            text_view4.setText("City: " + addresses.get(0).getLocality());
                            text_view5.setText("Address: " + addresses.get(0).getAddressLine(0));
                            text_view6.setText("SubLocality: " + addresses.get(0).getSubLocality());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error getting the address", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        location_view.setText("Location not available.");
                    }
                }
            });
        }
    }


    // Método para mostrar la ruta hacia la ubicación actual
    public void showRouteToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        // Usamos la latitud y longitud de la ubicación actual
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location.getLatitude() + "," + location.getLongitude() + "&mode=d");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        // Verifica si Google Maps está instalado y luego abre la aplicación
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        }
                    } else {
                        location_view.setText("Location not available.");
                    }
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para mostrar la ruta a una ubicación seleccionada
    public void showRouteToSelectedLocation(String location) {
        // En este caso, obtenemos las coordenadas de la ubicación seleccionada desde el ListView
        double selectedLat = 4.6399488;  // Ejemplo de Latitud (ajustar según la selección)
        double selectedLong = -74.088448;  // Ejemplo de Longitud (ajustar según la selección)

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + selectedLat + "," + selectedLong + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    // En el método de solicitud de permisos, asegúrate de que los permisos sean concedidos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();  // Vuelve a obtener la ubicación si el permiso se concede
            } else {
                Toast.makeText(MainActivity.this, "Permission denied, cannot fetch location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

