package course.labs.contentproviderlab;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import course.labs.contentproviderlab.provider.PlaceBadgesContract;
import course.labs.contentproviderlab.PlaceViewAdapter;
import course.labs.contentproviderlab.R;
import course.labs.contentproviderlab.PlaceDownloaderTask;
import course.labs.contentproviderlab.PlaceViewActivity;

public class PlaceViewActivity extends ListActivity implements
		LocationListener, LoaderCallbacks<Cursor> {
	private static final long FIVE_MINS = 5 * 60 * 1000;
	private static final long MEASURE_TIME = 1000 * 30;

	private static String TAG = "Lab-ContentProvider";

	// The last valid location reading
	private Location mLastLocationReading;

	// The ListView's adapter
	// private PlaceViewAdapter mAdapter;
	private PlaceViewAdapter mCursorAdapter;

	// default minimum time between new location readings
	private long mMinTime = 5000;

	// default minimum distance between old and new readings.
	private float mMinDistance = 1000.0f;

	// Reference to the LocationManager
	private LocationManager mLocationManager;

	// A fake location provider used for testing
	private MockLocationProvider mMockLocationProvider;
	
	private LoaderManager loadermanager;		
    private CursorLoader cursorLoader;
    private Cursor mCursor;
	private int mFlags;
	private LocationListener mLocationListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager=getLoaderManager();
        // TODO - Set up the app's user interface
        // This class is a ListActivity, so it has its own ListView
		mCursorAdapter = new PlaceViewAdapter(getApplicationContext(),mCursor,mFlags);
		getListView().setFooterDividersEnabled(true);
		getListView().setAdapter(mCursorAdapter);
		if (null == mLocationManager){mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);}
		checkLocation(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		mLocationListener = this;

        // TODO - add a footerView to the ListView
        // You can use footer_view.xml to define the footer
		TextView footview = (TextView) getLayoutInflater().inflate(R.layout.footer_view, null);
		getListView().addFooterView(footview);
		footview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO - When the footerView's onClick() method is called, it must issue the
		        // following log call
		        // log("Entered footerView.OnClickListener.onClick()");
				log("Entered footerView.OnClickListener.onClick()");

				if (mLastLocationReading == null){
					log("Location data is not available");
					Toast.makeText(getApplicationContext(), "No valid location data", Toast.LENGTH_SHORT).show();
					return;
				}
			
				if (mCursorAdapter.intersects(mLastLocationReading)){
					// 2) The current location has been seen before - issue Toast message.
					// Issue the following log call:
					log("You already have this location badge");
					Toast.makeText(getApplicationContext(), "You already have this badge", Toast.LENGTH_SHORT).show();
					return;
				}
				if (mLastLocationReading !=null){
					log("Starting Place Download");
					new PlaceDownloaderTask(PlaceViewActivity.this).execute(mLastLocationReading);
				}
		
		
		
				// TODO - Create and set empty PlaceViewAdapter
		        // ListView's adapter should be a PlaceViewAdapter called mCursorAdapter
				setListAdapter(mCursorAdapter);
		
		
		
				
			}
		});
		// TODO - Initialize a CursorLoader
		loadermanager.initLoader(1, null,this);
		getListView().setAdapter(mCursorAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mMockLocationProvider = new MockLocationProvider(
				LocationManager.NETWORK_PROVIDER, this);

		// TODO - Check NETWORK_PROVIDER for an existing location reading.
		// Only keep this last reading if it is fresh - less than 5 minutes old.
		checkLocation(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

		
		
		
		// TODO - Register to receive location updates from NETWORK_PROVIDER
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
				mMinTime, mMinDistance, mLocationListener);
		Executors.newScheduledThreadPool(1).schedule(new Runnable() {

			@Override
			public void run() {

				Log.i(TAG, "location updates cancelled");

				mLocationManager.removeUpdates(mLocationListener);

			}
		}, MEASURE_TIME, TimeUnit.MILLISECONDS);
		
		
		
	}

	private boolean checkLocation(Location newLocation){
		//if no reading yet then set
		if (mLastLocationReading == null) {mLastLocationReading = newLocation;return true;}
		//check for 5 min first
		long now = System.currentTimeMillis();
		if (mLastLocationReading.getTime()+FIVE_MINS > now ){
			mLastLocationReading = newLocation;
			return true;
		}
		return false;
	}
	
	@Override
	protected void onPause() {

		mMockLocationProvider.shutdown();

		// TODO - Unregister for location updates
		mLocationManager.removeUpdates(mLocationListener);
		
		
		super.onPause();
	}

	public void addNewPlace(PlaceRecord place) {

		log("Entered addNewPlace()");

		mCursorAdapter.add(place);

	}

	@Override
	public void onLocationChanged(Location currentLocation) {

		// TODO - Handle location updates
		// Cases to consider
		// 1) If there is no last location, keep the current location.
		// 2) If the current location is older than the last location, ignore
		// the current location
		// 3) If the current location is newer than the last locations, keep the
		// current location.
		if (mLastLocationReading == null){mLastLocationReading = currentLocation;return;}
		if (age(currentLocation)>age(mLastLocationReading)){return;}
		mLastLocationReading = currentLocation;
		return;

	
	
	
	}

	@Override
	public void onProviderDisabled(String provider) {
		// not implemented
	}

	@Override
	public void onProviderEnabled(String provider) {
		// not implemented
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// not implemented
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		log("Entered onCreateLoader()");

		// TODO - Create a new CursorLoader and return it
		
		cursorLoader = new CursorLoader(getApplicationContext(), PlaceBadgesContract.CONTENT_URI, null, null, null, null);
		return cursorLoader;
        
	}

	@Override
	public void onLoadFinished(Loader<Cursor> newLoader, Cursor newCursor) {

		// TODO - Swap in the newCursor
		if(mCursorAdapter!=null && newCursor!=null){mCursorAdapter.swapCursor(newCursor);}

	
    }

	@Override
	public void onLoaderReset(Loader<Cursor> newLoader) {

		// TODO - Swap in a null Cursor
		if(mCursorAdapter!=null){mCursorAdapter.swapCursor(null);}
	
    }

	private long age(Location location) {
		return System.currentTimeMillis() - location.getTime();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.print_badges:
			ArrayList<PlaceRecord> currData = mCursorAdapter.getList();
			for (int i = 0; i < currData.size(); i++) {
				log(currData.get(i).toString());
			}
			return true;
		case R.id.delete_badges:
			mCursorAdapter.removeAllViews();
			return true;
		case R.id.place_one:
			mMockLocationProvider.pushLocation(37.422, -122.084);
			return true;
		case R.id.place_invalid:
			mMockLocationProvider.pushLocation(0, 0);
			return true;
		case R.id.place_two:
			mMockLocationProvider.pushLocation(38.996667, -76.9275);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static void log(String msg) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG, msg);
	}
}
