package org.msf.records.ui.patientlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.msf.records.R;
import org.msf.records.events.location.LocationsLoadedEvent;
import org.msf.records.filter.FilterGroup;
import org.msf.records.filter.PatientFilters;
import org.msf.records.filter.LocationUuidFilter;
import org.msf.records.filter.SimpleSelectionFilter;
import org.msf.records.location.LocationTree;
import org.msf.records.location.LocationTree.LocationSubtree;
import org.msf.records.ui.patientcreation.PatientCreationActivity;
import org.msf.records.utils.PatientCountDisplay;

import de.greenrobot.event.EventBus;

// TODO(akalachman): Split RoundActivity from Triage and Discharged, which may behave differently.
public class RoundActivity extends PatientSearchActivity {
    private String mLocationName;
    private String mLocationUuid;

    private int mLocationPatientCount;

    private RoundFragment mFragment;
    private SimpleSelectionFilter mFilter;

    private final LocationEventSubscriber mSubscriber = new LocationEventSubscriber();

    public static final String LOCATION_NAME_KEY = "location_name";
    public static final String LOCATION_PATIENT_COUNT_KEY = "location_patient_count";
    public static final String LOCATION_UUID_KEY = "location_uuid";

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        super.onCreateImpl(savedInstanceState);

        mLocationName = getIntent().getStringExtra(LOCATION_NAME_KEY);
        mLocationPatientCount = getIntent().getIntExtra(LOCATION_PATIENT_COUNT_KEY, 0);
        mLocationUuid = getIntent().getStringExtra(LOCATION_UUID_KEY);

        setTitle(PatientCountDisplay.getPatientCountTitle(
                this, mLocationPatientCount, mLocationName));
        setContentView(R.layout.activity_round);

        // TODO: Don't use this singleton.
        LocationTree locationTree = LocationTree.SINGLETON_INSTANCE;
        LocationSubtree subtree = locationTree.getLocationByUuid(mLocationUuid);
        mFilter = new FilterGroup(PatientFilters.getDefaultFilter(), new LocationUuidFilter(subtree));
    }

    @Override
    public void onExtendOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // TODO(akalachman): Move this back to onCreate when I figure out why it needs to be here.
        mFragment = (RoundFragment)getSupportFragmentManager()
                .findFragmentById(R.id.round_patient_list);
        mFragment.filterBy(mFilter);

        menu.findItem(R.id.action_add).setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        startActivity(
                                new Intent(RoundActivity.this, PatientCreationActivity.class));

                        return true;
                    }
                });

        super.onExtendOptionsMenu(menu);
    }

    @Override
    protected void onResumeImpl() {
        super.onResumeImpl();
        EventBus.getDefault().register(mSubscriber);
    }

    @Override
    protected void onPauseImpl() {
        super.onPauseImpl();
        EventBus.getDefault().unregister(mSubscriber);
    }

    private class LocationEventSubscriber {
        // Keep title up-to-date with any location changes.
        public void onEventMainThread(LocationsLoadedEvent event) {
            LocationSubtree subtree = event.locationTree.getLocationByUuid(mLocationUuid);
            mLocationPatientCount = subtree.getPatientCount();
            setTitle(PatientCountDisplay.getPatientCountTitle(
                    RoundActivity.this, mLocationPatientCount, mLocationName));
        }
    }
}
