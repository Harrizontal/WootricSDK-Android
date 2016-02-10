package com.wootric.androidsdk;

import android.util.Log;

import com.wootric.androidsdk.network.WootricRemoteClient;
import com.wootric.androidsdk.network.responses.EligibilityResponse;
import com.wootric.androidsdk.network.tasks.CheckEligibilityTask;
import com.wootric.androidsdk.objects.EndUser;
import com.wootric.androidsdk.objects.Settings;
import com.wootric.androidsdk.objects.User;
import com.wootric.androidsdk.utils.PreferencesUtils;

/**
 * Created by maciejwitowski on 9/3/15.
 */
public class SurveyValidator implements CheckEligibilityTask.Callback {

    private OnSurveyValidatedListener onSurveyValidatedListener;
    private final User user;
    private final EndUser endUser;
    private final Settings settings;
    private final WootricRemoteClient wootricRemoteClient;
    private final PreferencesUtils preferencesUtils;

    private static final String TAG = "WOOTRIC_SDK";

    SurveyValidator(User user, EndUser endUser, Settings settings,
                    WootricRemoteClient wootricRemoteClient, PreferencesUtils preferencesUtils) {
        this.user = user;
        this.endUser = endUser;
        this.settings = settings;
        this.wootricRemoteClient = wootricRemoteClient;
        this.preferencesUtils = preferencesUtils;
    }

    public void setOnSurveyValidatedListener(OnSurveyValidatedListener onSurveyValidatedListener) {
        this.onSurveyValidatedListener = onSurveyValidatedListener;
    }

    public void validate() {

        if(needsSurvey()) {
            checkEligibility();
        }
        else {
            notifyShouldNotShowSurvey();
        }
    }

    @Override
    public void onEligibilityChecked(EligibilityResponse eligibilityResponse) {
        if(eligibilityResponse.isEligible()) {
            notifyShouldShowSurvey(eligibilityResponse.getSettings());
        } else {
            notifyShouldNotShowSurvey();
        }
    }

    private boolean needsSurvey() {

        /*return !preferencesUtils.wasRecentlySurveyed() &&
                (settings.isSurveyImmediately() ||
                !endUser.isCreatedAtSet() ||
                firstSurveyDelayPassed() || lastSeenDelayPassed());*/

        Boolean isi = settings.isSurveyImmediately();
        Boolean wrs = preferencesUtils.wasRecentlySurveyed();
        Boolean cas = endUser.isCreatedAtSet();
        Boolean fsdp = firstSurveyDelayPassed();
        Boolean lsdp = lastSeenDelayPassed();

        Log.v(TAG, "IS SURVERY INMEDIATELY ENABLED: " + isi);
        Log.v(TAG, "WAS RECENTLY SURVEYED: " + wrs);
        Log.v(TAG, "HAS CREATED DATE: " + cas); //BOKU: Why is this value accounted on the previous version? It's not necessary.
        Log.v(TAG, "FIRST SURVEY DELAY PASSED: " + fsdp);
        Log.v(TAG, "LAST SEEN DELAY PASSED: " + lsdp);

        if (isi || !wrs || fsdp || lsdp) {
            Log.v(TAG, "Needs survey. Will check with server.");
            return true;
        }

        Log.v(TAG, "Doesn't need survey. Will not check with server.");
        return false;
    }

    private boolean firstSurveyDelayPassed() {
        return settings.firstSurveyDelayPassed(endUser.getCreatedAt());
    }

    private boolean lastSeenDelayPassed(){
        return preferencesUtils.isLastSeenSet() &&
                settings.firstSurveyDelayPassed(preferencesUtils.getLastSeen());
    }

    private void checkEligibility() {
        wootricRemoteClient.checkEligibility(user, endUser, settings, this);
    }

    private void notifyShouldShowSurvey(Settings settings) {
        if(onSurveyValidatedListener != null) {
            onSurveyValidatedListener.onSurveyValidated(settings);
        }
    }

    private void notifyShouldNotShowSurvey() {
        if(onSurveyValidatedListener != null) {
            onSurveyValidatedListener.onSurveyNotNeeded();
        }
    }

    interface OnSurveyValidatedListener {
        void onSurveyValidated(Settings settings);
        void onSurveyNotNeeded();
    }
}
