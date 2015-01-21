package org.msf.records.ui.patientcreation;

import android.util.Log;

import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.msf.records.data.app.AppLocationTree;
import org.msf.records.data.app.AppModel;
import org.msf.records.data.app.AppPatient;
import org.msf.records.data.app.AppPatientDelta;
import org.msf.records.events.CrudEventBus;
import org.msf.records.events.data.AppLocationTreeFetchedEvent;
import org.msf.records.events.data.PatientAddFailedEvent;
import org.msf.records.events.data.SingleItemCreatedEvent;
import org.msf.records.events.data.SingleItemFetchFailedEvent;
import org.msf.records.model.Zone;
import org.msf.records.utils.LocaleSelector;
import org.msf.records.utils.Logger;

import java.util.Locale;

/**
 * Controller for {@link PatientCreationActivity}.
 *
 * Avoid adding untestable dependencies to this class.
 */
final class PatientCreationController {

    private static final Logger LOG = Logger.create();

    static final int AGE_UNKNOWN = 0;
    static final int AGE_YEARS = 1;
    static final int AGE_MONTHS = 2;

    static final int SEX_UNKNOWN = 0;
    static final int SEX_MALE = 1;
    static final int SEX_FEMALE = 2;

    public interface Ui {

        static final int FIELD_UNKNOWN = 0;
        static final int FIELD_ID = 1;
        static final int FIELD_GIVEN_NAME = 2;
        static final int FIELD_FAMILY_NAME = 3;
        static final int FIELD_AGE = 4;
        static final int FIELD_AGE_UNITS = 5;
        static final int FIELD_SEX = 6;
        static final int FIELD_LOCATION = 7;

        void setLocationTree(AppLocationTree locationTree);

        /** Adds a validation error message for a specific field. */
        void showValidationError(int field, String message);

        /** Clears the validation error messages from all fields. */
        void clearValidationErrors();

        /** Invoked when the server RPC to create a patient fails. */
        void showErrorMessage(String error);

        /** Invoked when the server RPC to create a patient succeeds. */
        void quitActivity();
    }

    private final Ui mUi;
    private final CrudEventBus mCrudEventBus;
    private final AppModel mModel;

    private final EventSubscriber mEventBusSubscriber;

    public PatientCreationController(Ui ui, CrudEventBus crudEventBus, AppModel model) {
        mUi = ui;
        mCrudEventBus = crudEventBus;
        mModel = model;

        // TODO(dxchen): Inject this.
        mEventBusSubscriber = new EventSubscriber();
    }

    /** Initializes the controller, setting async operations going to collect data required by the UI. */
    public void init() {
        mCrudEventBus.register(mEventBusSubscriber);
        mModel.fetchLocationTree(mCrudEventBus, LocaleSelector.getCurrentLocale().getLanguage());
    }

    /** Releases any resources used by the controller. */
    public void suspend() {
        mCrudEventBus.unregister(mEventBusSubscriber);
    }

    public boolean createPatient(
            String id, String givenName, String familyName, String age, int ageUnits, int sex,
            String locationUuid) {
        // Validate the input.
        mUi.clearValidationErrors();
        boolean hasValidationErrors = false;
        if (id == null || id.equals("")) {
            mUi.showValidationError(Ui.FIELD_ID, "Please enter the patient ID.");
            hasValidationErrors = true;
        }
        if (givenName == null || givenName.equals("")) {
            mUi.showValidationError(Ui.FIELD_GIVEN_NAME, "Please enter the given name.");
            hasValidationErrors = true;
        }
        if (familyName == null || familyName.equals("")) {
            mUi.showValidationError(Ui.FIELD_FAMILY_NAME, "Please enter the family name.");
            hasValidationErrors = true;
        }
        if (age == null || age.equals("")) {
            mUi.showValidationError(Ui.FIELD_AGE, "Please enter the age.");
            hasValidationErrors = true;
        }
        int ageInt = 0;
        try {
            ageInt = Integer.parseInt(age);
        } catch (NumberFormatException e) {
            mUi.showValidationError(Ui.FIELD_AGE, "Age should be a whole number.");
            hasValidationErrors = true;
        }
        if (ageInt < 0) {
            mUi.showValidationError(Ui.FIELD_AGE, "Age should not be negative.");
            hasValidationErrors = true;
        }
        if (ageUnits != AGE_YEARS && ageUnits != AGE_MONTHS) {
            mUi.showValidationError(Ui.FIELD_AGE_UNITS, "Please select Years or Months.");
            hasValidationErrors = true;
        }
        if (sex != SEX_MALE && sex != SEX_FEMALE) {
            mUi.showValidationError(Ui.FIELD_SEX, "Please select Male or Female.");
            hasValidationErrors = true;
        }

        if (hasValidationErrors) {
            return false;
        }

        AppPatientDelta patientDelta = new AppPatientDelta();
        patientDelta.id = Optional.of(id);
        patientDelta.givenName = Optional.of(givenName);
        patientDelta.familyName = Optional.of(familyName);
        patientDelta.birthdate = Optional.of(getBirthdateFromAge(ageInt, ageUnits));
        patientDelta.gender = Optional.of(sex);
        patientDelta.assignedLocationUuid = (locationUuid == null)
                ? Optional.of(Zone.DEFAULT_LOCATION) : Optional.of(locationUuid);
        patientDelta.admissionDate = Optional.of(DateTime.now());

        mModel.addPatient(mCrudEventBus, patientDelta);

        return true;
    }

    private DateTime getBirthdateFromAge(int ageInt, int ageUnits) {
        DateTime now = DateTime.now();
        switch (ageUnits) {
            case AGE_YEARS:
                return now.minusYears(ageInt);
            case AGE_MONTHS:
                return now.minusMonths(ageInt);
            default:
                return null;
        }
    }

    @SuppressWarnings("unused") // Called by reflection from EventBus.
    private final class EventSubscriber {

        public void onEventMainThread(AppLocationTreeFetchedEvent event) {
            mUi.setLocationTree(event.tree);
        }

        public void onEventMainThread(SingleItemCreatedEvent<AppPatient> event) {
            mUi.quitActivity();
        }

        public void onEventMainThread(PatientAddFailedEvent event) {
            mUi.showErrorMessage(event.exception == null ? "unknown" : event.exception.getMessage());
            LOG.e("Patient add failed", event.exception);
        }

        public void onEventMainThread(SingleItemFetchFailedEvent event) {
            mUi.showErrorMessage(event.error);
        }
    }
}
