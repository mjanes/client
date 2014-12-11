package org.msf.records.location;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import javax.annotation.Nullable;

import org.msf.records.events.CreatePatientSucceededEvent;
import org.msf.records.events.location.LocationsLoadFailedEvent;
import org.msf.records.events.location.LocationsLoadedEvent;
import org.msf.records.events.sync.SyncFailedEvent;
import org.msf.records.events.sync.SyncSucceededEvent;
import org.msf.records.sync.SyncManager;

import de.greenrobot.event.EventBus;

/**
 * Loads the {@link LocationTree} from disk cache or from the network.
 *
 * <p>Caches the current {@link LocationTree}.
 *
 * <p>All classes that care about locations should handle the following bus events:
 * <ul>
 *     <li>{@link org.msf.records.events.location.LocationsLoadedEvent}</li>
 *     <li>{@link org.msf.records.events.location.LocationsLoadFailedEvent}</li>
 * </ul>
 *
 * <p>This class's public methods should only be called from the main thread.
 */
public class LocationManager {

    private static final String TAG = "LocationManager";
    private static final boolean DEBUG = true;

    private final EventBus mEventBus;
    private final Context mContext;
    private final EventBusSubscriber mEventBusSubscriber = new EventBusSubscriber();

    @Nullable private LocationTree mLocationTree;

    public LocationManager(EventBus eventBus, Context context) {
    	mEventBus = checkNotNull(eventBus);
    	mContext = checkNotNull(context);
    }

    public void init() {
        mEventBus.register(mEventBusSubscriber);
    }

    /**
     * Loads the set of locations asynchronously, from memory, disk cache or over the network.
     *
     * <p>This method will post a {@link org.msf.records.events.location.LocationsLoadedEvent} if
     * the locations were successfully loaded and a
     * {@link org.msf.records.events.location.LocationsLoadFailedEvent} otherwise.
     *
     * <p>This method will only perform a local cache lookup once per application lifetime.
     */
    public void loadLocations() {
        if (mLocationTree != null) {
        	if (DEBUG) {
        		Log.d(TAG, "Location tree already in memory");
        	}
        	// Already loaded.
        	mEventBus.post(new LocationsLoadedEvent(mLocationTree));
        } else {
        	if (DEBUG) {
        		Log.d(TAG, "Location tree not in memory. Attempting to load from cache.");
        	}
        	// Need to load from disk cache, or possible from the network.
            new LoadLocationsTask().execute();
        }
    }

    @SuppressWarnings("unused") // Called by reflection from EventBus.
    private final class EventBusSubscriber {
	    public void onEventMainThread(CreatePatientSucceededEvent event) {
	        // If a patient was just created, we need to rebuild the location tree so that patient
	        // counts remain up-to-date.
	        // TODO(akalachman): If/when we can access the new patient here, only update counts.
	    	new LoadLocationsTask().execute();
	    }

	    public void onEventMainThread(SyncSucceededEvent event) {
	    	new LoadLocationsTask().execute();
	    }

	    public void onEventMainThread(SyncFailedEvent event) {
	        Log.e(TAG, "Failed to retrieve location data from server");
	        mEventBus.post(
	                new LocationsLoadFailedEvent(LocationsLoadFailedEvent.REASON_SERVER_ERROR));
	    }
    }

    /** Loads locations from disk cache. If not found, triggers loading from network. */
    private final class LoadLocationsTask extends AsyncTask<Void, Void, LocationTree> {
        @Override
        protected LocationTree doInBackground(Void... voids) {
        	// Note: It's not possible to construct a CursorLoader on a background thread
        	// without calling this.
        	// TODO(rjlothian): Investigate and see if we can avoid this.
        	if (Looper.myLooper() == null) {
        		Looper.prepare();
        	}

        	LocationTree loadedFromDiskCache = new LocationTreeFactory(mContext).build();
        	if (loadedFromDiskCache != null) {
        		if (DEBUG) {
            		Log.d(TAG, "Location tree successfully loaded from cache.");
            	}
        		return loadedFromDiskCache;
        	}
        	if (DEBUG) {
        		Log.d(TAG, "Location tree not in cache. Attempting to load from network.");
        	}
            SyncManager syncManager = SyncManager.INSTANCE;
            if (!syncManager.isSyncing()) {
                syncManager.forceSync();
            } else {
            	Log.d(TAG, "Already syncing");
            }
            return null;
        }

        @Override
        protected void onPostExecute(LocationTree result) {
        	if (result != null) {
        		mLocationTree = result;
        		LocationTree.SINGLETON_INSTANCE = result;
        		mEventBus.post(new LocationsLoadedEvent(result));
        	}
        }
    }
}
