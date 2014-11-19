package org.msf.records.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;
import org.msf.records.App;
import org.msf.records.R;
import org.msf.records.net.Constants;
import org.msf.records.net.OpenMrsXformsConnection;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.tasks.DiskSyncTask;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;


/**
 * An activity representing a list of Patients. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PatientDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PatientListFragment} and the item details
 * (if present) is a {@link PatientDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PatientListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class PatientListActivity extends FragmentActivity
        implements PatientListFragment.Callbacks {

    private static final String TAG = PatientListActivity.class.getSimpleName();

    private SearchView mSearchView;

    private View mScanBtn, mAddPatientBtn, mSettingsBtn;

    private OnSearchListener mSearchListener;

    interface OnSearchListener {
        void setQuerySubmitted(String q);
    }

    public void setOnSearchListener(OnSearchListener onSearchListener){
        this.mSearchListener = onSearchListener;
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_patient_list);

        getActionBar().setDisplayShowHomeEnabled(false);

        if (findViewById(R.id.patient_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            // Create a main screen shown when no patient is selected.
            MainScreenFragment mainScreenFragment = new MainScreenFragment();

            // Add the fragment to the container.
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.patient_detail_container, mainScreenFragment).commit();

            setupCustomActionBar();
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    private void setupCustomActionBar(){
        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_main, null);

        mAddPatientBtn = customActionBarView.findViewById(R.id.actionbar_add_patient);
        mScanBtn = customActionBarView.findViewById(R.id.actionbar_scan);
        mSettingsBtn = customActionBarView.findViewById(R.id.actionbar_settings);
        mSearchView = (SearchView) customActionBarView.findViewById(R.id.actionbar_custom_main_search);
        mSearchView.setIconifiedByDefault(false);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * Callback method from {@link PatientListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        Intent detailIntent = new Intent(this, PatientDetailActivity.class);
        detailIntent.putExtra(PatientDetailFragment.PATIENT_ID_KEY, id);
        startActivity(detailIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        if(!mTwoPane) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main, menu);

            menu.findItem(R.id.action_add).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    startActivity(PatientAddActivity.class);
                    return false;
                }
            });

            menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new OnMenuItemClickListener() {
              @Override
              public boolean onMenuItemClick(MenuItem item) {
                startActivity(SettingsActivity.class);
                return false;
              }
            });

            menu.findItem(R.id.action_scan).setOnMenuItemClickListener(new OnMenuItemClickListener() {
              @Override
              public boolean onMenuItemClick(MenuItem item) {
                startScanBracelet();
                return false;
              }
            });

            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            mSearchView = (SearchView) searchMenuItem.getActionView();
            mSearchView.setIconifiedByDefault(false);

            searchMenuItem.expandActionView();
        } else {
          mAddPatientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startActivity(PatientAddActivity.class);
            }
          });

          mSettingsBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  startActivity(SettingsActivity.class);
              }
          });

          mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startScanBracelet();
            }
          });
        }

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
          @Override
          public boolean onQueryTextSubmit(String query) {

            InputMethodManager mgr = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            return true;
          }

          @Override
          public boolean onQueryTextChange(String newText) {
            if (mSearchListener != null)
              mSearchListener.setQuerySubmitted(newText);
            return true;
          }
        });

        return true;
    }

    private enum ScanAction {
        PLAY_WITH_ODK,
        FETCH_XFORMS,
        FAKE_SCAN,
    }

    private void startScanBracelet() {
        ScanAction scanAction = ScanAction.PLAY_WITH_ODK;
        switch (scanAction) {
            case PLAY_WITH_ODK:
                showFirstFormFromSdcard();
                break;
            case FAKE_SCAN:
                showFakeScanProgress();
                break;
        }
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.new_patient_button:
                OdkActivityLauncher.fetchXforms(this, Constants.ADD_PATIENT_UUID);
                break;
        }
    }

    private void showFirstFormFromSdcard() {
        // Sync the local sdcard forms into the database
        new DiskSyncTask().execute((Void[]) null);
        OdkActivityLauncher.showOdkCollect(this, 1L);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != OdkActivityLauncher.ODK_COLLECT_REQUEST_CODE) {
            return;
        }

        if (data == null || data.getData() == null) {
            // Cancelled.
            Log.i(TAG, "No data for form result, probably cancelled.");
            return;
        }

        Uri uri = data.getData();

        if (!getContentResolver().getType(uri).equals(
                InstanceProviderAPI.InstanceColumns.CONTENT_ITEM_TYPE)) {
            Log.e(TAG, "Tried to load a content URI of the wrong type: " + uri);
            return;
        }

        Cursor instanceCursor = null;
        try {
            instanceCursor = getContentResolver().query(uri,
                    null, null, null, null);
            if (instanceCursor.getCount() != 1) {
                Log.e(TAG, "The form that we tried to load did not exist: " + uri);
                return;
            }
            instanceCursor.moveToFirst();
            String instancePath = instanceCursor.getString(
                    instanceCursor.getColumnIndex(INSTANCE_FILE_PATH));
            if (instancePath == null) {
                Log.e(TAG, "No file path for form instance: " + uri);
                return;

            }
            int columnIndex = instanceCursor
                    .getColumnIndex(InstanceProviderAPI.InstanceColumns._ID);
            if (columnIndex == -1) {
                Log.e(TAG, "No id to delete for after upload: " + uri);
                return;
            }
            final long idToDelete = instanceCursor.getLong(columnIndex);

            sendFormToServer(null /* create new patient */, readFromPath(instancePath),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "Created new patient successfully on server"
                                    + response.toString());

                            // Code largely copied from InstanceUploaderTask to delete on upload
                            DeleteInstancesTask dit = new DeleteInstancesTask();
                            dit.setContentResolver(
                                    Collect.getInstance().getApplication().getContentResolver());
                            dit.execute(idToDelete);
                        }
                    });
        } catch (IOException e) {
            Log.e(TAG, "Failed to read xml form into a String " + uri, e);
        } finally {
            if (instanceCursor != null) {
                instanceCursor.close();
            }
        }
    }

    private String readFromPath(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private void showFakeScanProgress() {
        final ProgressDialog progressDialog = ProgressDialog
                .show(PatientListActivity.this, null, "Scanning for near by bracelets ...", true);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    private void sendFormToServer(String patientId, String xml,
                                  Response.Listener<JSONObject> successListener) {
        OpenMrsXformsConnection connection = App.getmOpenMrsXformsConnection();
        connection.postXformInstance(patientId, xml,
                successListener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Did not submit  form to server successfully", error);
                    }
                });
    }

    private void startActivity(Class<?> activityClass) {
      Intent intent = new Intent(PatientListActivity.this, activityClass);
      startActivity(intent);
    }
}
