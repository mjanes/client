package org.msf.records.data.app;

import android.content.ContentValues;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.msf.records.net.model.Patient;
import org.msf.records.sync.providers.Contracts;
import org.msf.records.utils.Utils;

import javax.annotation.concurrent.Immutable;

/**
 * A patient in the app model.
 */
@Immutable
public final class AppPatient extends AppTypeBase<String> implements Comparable<AppPatient> {

    public static final int GENDER_UNKNOWN = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    public final String uuid;
    public final String givenName;
    public final String familyName;
    public final int gender;
    public final LocalDate birthdate;
    public final DateTime admissionDateTime;
    public final String locationUuid;

    private AppPatient(Builder builder) {
        this.id = builder.mId;
        this.uuid = builder.mUuid;
        this.givenName = builder.mGivenName;
        this.familyName = builder.mFamilyName;
        this.gender = builder.mGender;
        this.birthdate = builder.mBirthdate;
        this.admissionDateTime = builder.mAdmissionDateTime;
        this.locationUuid = builder.mLocationUuid;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an instance of {@link AppPatient} from a network {@link Patient} object.
     */
    public static AppPatient fromNet(Patient patient) {
        return builder()
                .setId(patient.id)
                .setUuid(patient.uuid)
                .setGivenName(patient.given_name)
                .setFamilyName(patient.family_name)
                .setGender("M".equals(patient.gender) ? GENDER_MALE : GENDER_FEMALE)
                .setBirthdate(patient.birthdate)
                .setAdmissionDateTime(new DateTime(patient.admission_timestamp * 1000))
                .setLocationUuid(
                        patient.assigned_location == null ? null : patient.assigned_location.uuid)
                .build();
    }

    /**
     * Converts this instance of {@link AppPatient} to a {@link ContentValues} object for insertion
     * into a database or content provider.
     */
    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(
                Contracts.Patients._ID,
                id);
        contentValues.put(
                Contracts.Patients.UUID,
                uuid);
        contentValues.put(
                Contracts.Patients.GIVEN_NAME,
                givenName);
        contentValues.put(
                Contracts.Patients.FAMILY_NAME,
                familyName);
        contentValues.put(
                Contracts.Patients.GENDER,
                gender == Patient.GENDER_MALE ? "M" : "F");
        contentValues.put(
                Contracts.Patients.BIRTHDATE,
                Utils.localDateToString(birthdate));
        contentValues.put(
                Contracts.Patients.ADMISSION_TIMESTAMP,
                admissionDateTime == null ? null : admissionDateTime.getMillis() / 1000);
        contentValues.put(
                Contracts.Patients.LOCATION_UUID,
                locationUuid);

        return contentValues;
    }

    @Override
    public int compareTo(AppPatient other) {
        return Utils.alphanumericComparator.compare(id, other.id);
    }

    public static final class Builder {
        private String mId;
        private String mUuid;
        private String mGivenName;
        private String mFamilyName;
        private int mGender;
        private LocalDate mBirthdate;
        private DateTime mAdmissionDateTime;
        private String mLocationUuid;

        private Builder() {}

        public Builder setId(String id) {
            this.mId = id;
            return this;
        }

        public Builder setUuid(String uuid) {
            this.mUuid = uuid;
            return this;
        }

        public Builder setGivenName(String givenName) {
            this.mGivenName = givenName;
            return this;
        }

        public Builder setFamilyName(String familyName) {
            this.mFamilyName = familyName;
            return this;
        }

        public Builder setGender(int gender) {
            this.mGender = gender;
            return this;
        }

        public Builder setBirthdate(LocalDate birthdate) {
            this.mBirthdate = birthdate;
            return this;
        }

        public Builder setAdmissionDateTime(DateTime admissionDateTime) {
            this.mAdmissionDateTime = admissionDateTime;
            return this;
        }

        public Builder setLocationUuid(String locationUuid) {
            this.mLocationUuid = locationUuid;
            return this;
        }

        public AppPatient build() {
            return new AppPatient(this);
        }
    }
}
