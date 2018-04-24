package michaeloade.mallbeacon;

import android.Manifest;
import android.annotation.TargetApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import michaeloade.mallbeacon.dummy.DummyContent;
import michaeloade.mallbeacon.models.Visit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, OfferFragment.OnListFragmentInteractionListener {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BeaconManager beaconManager;
    protected static final String TAG = "BeaconActivity";
    private static final String iBEACON_FORMAT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    private ArrayList<Beacon> beaconList;
    private HashSet<Beacon> beaconHashSet;
    private ArrayAdapter<Beacon> beaconAdapter;
    private ListView listView;
    private MallService mallService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LoginActivity.class);
        //startActivity(intent);
        mallService = MallService.getInstance(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Sign up to our news letter", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog) {
                        showOffers();
                    }
                });
                builder.show();
            }
        }
        beaconHashSet = new HashSet<>();
        beaconList = new ArrayList<>(beaconHashSet);
        beaconAdapter = new ArrayAdapter<>(this, R.layout
                .beacon_item, beaconList);
        listView = findViewById(R.id.listView);
        listView.setAdapter(beaconAdapter);

        //Initialise and parse beacons
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(iBEACON_FORMAT));
        beaconManager.bind(this);

    }

    public void acknowledgeVisit() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new OfferFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Congratulations");
        builder.setMessage("Looks like there might be some offers.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @TargetApi(Build.VERSION_CODES.M)
            public void onDismiss(DialogInterface dialog) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        });
        builder.show();
    }

    public void showOffers() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, String.valueOf(R.string.permission_granted));
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.location_warning_title);
                    builder.setMessage(R.string.location_warning);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                String id = region.getId1().toString() + '.' + region.getId2() + '.' + region.getId3();
                String encodedId = Base64.encodeToString(id.getBytes(), Base64.NO_WRAP);
                mallService.beep(encodedId, 1, "made0073@yahoo.com").enqueue(new Callback<Visit>() {
                    @Override
                    public void onResponse(Call<Visit> call, Response<Visit> response) {
                        Log.d(TAG, "Success");
                        acknowledgeVisit();
                    }


                    @Override
                    public void onFailure(Call<Visit> call, Throwable t) {
                        Log.d(TAG, "Fail");
                    }
                });
                Log.i(TAG, "New beacon detected!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "Beacon no longer in range");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "State changed: " + state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("monitoringId", null, null, null));
        } catch (RemoteException e) {
        }

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconHashSet.addAll(beacons);
                    beaconList.clear();
                    beaconList.addAll(beaconHashSet);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            beaconAdapter.notifyDataSetChanged();
                        }
                    });
                    Log.i(TAG, "Beacon detected: " + beacons.iterator().next().getId1() + " about " + beacons.iterator().next().getId2() + " cm away.");
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("rangingId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
