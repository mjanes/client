package org.msf.records.data.app.tasks;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;

import org.msf.records.data.app.AppEncounter;
import org.msf.records.data.app.AppModel;
import org.msf.records.data.app.AppPatient;
import org.msf.records.data.app.AppPatientDelta;
import org.msf.records.data.app.AppTypeBase;
import org.msf.records.data.app.converters.AppTypeConverter;
import org.msf.records.data.app.converters.AppTypeConverters;
import org.msf.records.events.CrudEventBus;
import org.msf.records.filter.db.SimpleSelectionFilter;
import org.msf.records.net.Server;
import org.msf.records.net.model.Encounter;

/**
 * An assisted injection factory that creates {@link AppModel} {@link AsyncTask}s.
 */
public class AppAsyncTaskFactory {

    private final AppTypeConverters mConverters;
    private final Server mServer;
    private final ContentResolver mContentResolver;

    /**
     * Creates a new {@link AppAsyncTaskFactory}.
     */
    public AppAsyncTaskFactory(
            AppTypeConverters converters, Server server, ContentResolver contentResolver) {
        mConverters = converters;
        mServer = server;
        mContentResolver = contentResolver;
    }

    /**
     * Creates a new {@link AppAddPatientAsyncTask}.
     */
    public AppAddPatientAsyncTask newAddPatientAsyncTask(
            AppPatientDelta patientDelta, CrudEventBus bus) {
        return new AppAddPatientAsyncTask(
                this, mConverters, mServer, mContentResolver, patientDelta, bus);
    }

    /**
     * Creates a new {@link AppUpdatePatientAsyncTask}.
     */
    public AppUpdatePatientAsyncTask newUpdatePatientAsyncTask(
            AppPatient originalPatient, AppPatientDelta patientDelta, CrudEventBus bus) {
        return new AppUpdatePatientAsyncTask(
                this, mConverters, mServer, mContentResolver, originalPatient, patientDelta, bus);
    }

    /**
     * Creates a new {@link AppAddEncounterAsyncTask}.
     */
    public AppAddEncounterAsyncTask newAddEncounterAsyncTask(
            AppPatient appPatient, AppEncounter appEncounter, CrudEventBus bus) {
        return new AppAddEncounterAsyncTask(
                this, mConverters, mServer, mContentResolver, appPatient, appEncounter, bus);
    }

    /**
     * Creates a new {@link FetchSingleAsyncTask}.
     */
    public <T extends AppTypeBase<?>> FetchSingleAsyncTask<T> newFetchSingleAsyncTask(
            Uri contentUri,
            String[] projectionColumns,
            SimpleSelectionFilter filter,
            String constraint,
            AppTypeConverter<T> converter,
            CrudEventBus bus) {
        return new FetchSingleAsyncTask<>(
                mContentResolver, contentUri, projectionColumns, filter, constraint, converter,
                bus);
    }
}
