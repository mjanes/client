// Copyright 2015 The Project Buendia Authors
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at: http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distrib-
// uted under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
// OR CONDITIONS OF ANY KIND, either express or implied.  See the License for
// specific language governing permissions and limitations under the License.

package org.projectbuendia.client;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;

/** Type-safe access to application settings. */
public class AppSettings {
    static final int APK_UPDATE_INTERVAL_DEFAULT = 90; // default to 1.5 minutes.
    SharedPreferences mSharedPreferences;
    Resources mResources;

    public AppSettings(SharedPreferences sharedPreferences, Resources resources) {
        mSharedPreferences = sharedPreferences;
        mResources = resources;
    }

    /** Constructs the URL for a given URL path under the OpenMRS root URL. */
    public String getOpenmrsUrl(String urlPath) {
        return getOpenmrsUrl().replaceAll("/*$", "") + urlPath;
    }

    /** Gets the root URL of the OpenMRS server providing the Buendia API. */
    public String getOpenmrsUrl() {
        return mSharedPreferences.getString("openmrs_root_url",
            mResources.getString(R.string.openmrs_root_url_default));
    }

    /** Gets the OpenMRS username. */
    public String getOpenmrsUser() {
        return mSharedPreferences.getString("openmrs_user",
            mResources.getString(R.string.openmrs_user_default));
    }

    /** Gets the OpenMRS password. */
    public String getOpenmrsPassword() {
        return mSharedPreferences.getString("openmrs_password",
            mResources.getString(R.string.openmrs_password_default));
    }

    /** Constructs the URL for a given URL path on the package server. */
    public String getPackageServerUrl(String urlPath) {
        return getPackageServerUrl().replaceAll("/*$", "") + urlPath;
    }

    /** Gets the root URL of the package server providing APK updates. */
    public String getPackageServerUrl() {
        return mSharedPreferences.getString("package_server_root_url",
            mResources.getString(R.string.package_server_root_url_default));
    }

    /** Gets the index of the preferred chart zoom level. */
    public int getChartZoomIndex() {
        return mSharedPreferences.getInt("chart_zoom_index", 0);
    }

    /** Sets the preferred chart zoom level. */
    public void setChartZoomIndex(int zoom) {
        mSharedPreferences.edit().putInt("chart_zoom_index", zoom).commit();
    }

    /**
     * Gets the minimum period between checks for APK updates, in seconds.
     * Repeated calls to UpdateManager.checkForUpdate() within this period
     * will not check the package server for new updates.
     */
    public int getApkUpdateInterval() {
        return mSharedPreferences.getInt("apk_update_interval_secs", APK_UPDATE_INTERVAL_DEFAULT);
    }

    /** Gets the flag for whether to save filled-in forms locally. */
    public boolean getKeepFormInstancesLocally() {
        return mSharedPreferences.getBoolean("keep_form_instances", false);
    }

    public boolean shouldSkipToPatientChart() {
        return !getStartingPatientId().isEmpty();
    }

    public @NonNull String getStartingPatientId() {
        return mSharedPreferences.getString("starting_patient_id", "").trim();
    }

    /** Gets the flag indicating whether the sync account has been initialized. */
    public boolean getSyncAccountInitialized() {
        return mSharedPreferences.getBoolean("sync_account_initialized", false);
    }

    /** Sets the flag indicating whether the sync account has been initialized. */
    public void setSyncAccountInitialized(boolean value) {
        mSharedPreferences.edit().putBoolean("sync_account_initialized", value).commit();
    }

    /** Gets the flag controlling whether to assume no wifi means no network. */
    public boolean getRequireWifi() {
        return mSharedPreferences.getBoolean("require_wifi",
            mResources.getBoolean(R.bool.require_wifi_default));
    }
}
