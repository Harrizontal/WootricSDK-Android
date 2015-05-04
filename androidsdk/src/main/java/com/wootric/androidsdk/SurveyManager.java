package com.wootric.androidsdk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewTreeObserver;

import com.wootric.androidsdk.objects.EndUser;
import com.wootric.androidsdk.objects.User;
import com.wootric.androidsdk.objects.WootricCustomMessage;
import com.wootric.androidsdk.tasks.CreateEndUserTask;
import com.wootric.androidsdk.tasks.GetAccessTokenTask;
import com.wootric.androidsdk.tasks.GetEndUserTask;
import com.wootric.androidsdk.tasks.GetTrackingPixelTask;
import com.wootric.androidsdk.tasks.UpdateEndUserTask;
import com.wootric.androidsdk.utils.Blur;
import com.wootric.androidsdk.utils.ConnectionUtils;
import com.wootric.androidsdk.utils.Constants;
import com.wootric.androidsdk.utils.ImageUtils;
import com.wootric.androidsdk.utils.PreferencesUtils;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Created by maciejwitowski on 4/10/15.
 */
public class SurveyManager implements
        SurveyValidator.OnSurveyValidatedListener,
        GetEndUserTask.OnEndUserReceivedListener,
        CreateEndUserTask.OnEndUserCreatedListener,
        GetAccessTokenTask.OnAccessTokenReceivedListener {

    private final WeakReference<Activity> weakActivity;
    final SurveyValidator surveyValidator;
    private WootricCustomMessage mCustomMessage;

    private final String mOriginUrl;
    private User mUser;
    private String mProductName;

    private EndUser mEndUser;

    private boolean mActivityValid = true;

    private final PreferencesUtils prefs;
    private final ConnectionUtils connectionUtils;

    SurveyManager(WeakReference<Activity> weakActivity, User user, EndUser endUser,
                  SurveyValidator surveyValidator, String mOriginUrl,
                  PreferencesUtils prefs, ConnectionUtils connectionUtils) {

        if(weakActivity == null || surveyValidator == null || endUser == null || mOriginUrl == null) {
            throw new IllegalArgumentException
                    ("Mandatory params cannot be null.");
        }

        this.weakActivity = weakActivity;
        this.surveyValidator = surveyValidator;
        this.mOriginUrl = mOriginUrl;
        this.prefs = prefs;
        this.connectionUtils = connectionUtils;

        mEndUser = endUser;
        mUser = user;
    }

    public SurveyManager surveyImmediately() {
        surveyValidator.setSurveyImmediately(true);
        return this;
    }

    public SurveyManager createdAt(long createdAt) {
        mEndUser.setCreatedAt(createdAt);
        return this;
    }

    public SurveyManager dailyResponseCap(int dailyResponseCap) {
        surveyValidator.setDailyResponseCap(dailyResponseCap);
        return this;
    }

    public SurveyManager registeredPercent(int registeredPercent) {
        surveyValidator.setRegisteredPercent(registeredPercent);
        return this;
    }

    public SurveyManager visitorPercent(int visitorPercent) {
        surveyValidator.setVisitorPercent(visitorPercent);
        return this;
    }

    public SurveyManager resurveyThrottle(int resurveyThrottle) {
        surveyValidator.setResurveyThrottle(resurveyThrottle);
        return this;
    }

    public SurveyManager customMessage(WootricCustomMessage customMessage) {
        mCustomMessage = customMessage;
        return this;
    }

    public SurveyManager productName(String productName) {
        mProductName = productName;
        return this;
    }

    public SurveyManager forceSurvey() {
        surveyValidator.forceSurvey();
        return this;
    }

    EndUser getEndUser() {
        return mEndUser;
    }

    public void survey() {
        getTrackingPixel();
        updateLastSeen();
        setupSurveyValidator();
    }

    private boolean isAlreadyValidated() {
        return prefs.getAccessToken() != null && prefs.getEndUserId() != Constants.INVALID_ID;
    }

    private void getTrackingPixel() {
        new GetTrackingPixelTask(mUser, mEndUser, mOriginUrl).execute();
    }

    void setupSurveyValidator() {
        surveyValidator.setOnSurveyValidatedListener(this);
        surveyValidator.validate();
    }

    void updateLastSeen() {
        if(!prefs.wasRecentlyLastSeen()) {
            prefs.setLastSeen(new Date().getTime());
        }
    }

    @Override
    public void onSurveyValidated() {
        if(isAlreadyValidated()) {
            mEndUser.setId(prefs.getEndUserId());

            setupSurveyForCurrentView();
        } else {
            new GetAccessTokenTask(mUser, this, connectionUtils).execute();
        }
    }

    @Override
    public void onAccessTokenReceived(String accessToken) {
        prefs.setAccessToken(accessToken);

        new GetEndUserTask(
                mEndUser.getEmail(),
                accessToken,
                this,
                connectionUtils).execute();
    }

    @Override
    public void onEndUserReceived(EndUser endUser) {
        if(endUser == null) {
            new CreateEndUserTask(mEndUser, prefs.getAccessToken(), this, connectionUtils).execute();
        } else {
            mEndUser.setId(endUser.getId());

            prefs.setEndUserId(mEndUser.getId());

            updateEndUserProperties();
            setupSurveyForCurrentView();
        }
    }

    void invalidateActivity() {
        mActivityValid = false;
    }

    boolean isActivityValid() {
        return mActivityValid;
    }

    private void updateEndUserProperties() {
        if(mEndUser.hasProperties()) {
            new UpdateEndUserTask(mEndUser, prefs.getAccessToken(), connectionUtils).execute();
        }
    }

    @Override
    public void onEndUserCreated(EndUser endUser) {
        mEndUser = endUser;

        prefs.setEndUserId(mEndUser.getId());

        if(mEndUser != null) {
            setupSurveyForCurrentView();
        }
    }

    private void setupSurveyForCurrentView() {
        final Activity activity = weakActivity.get();

        if(activity != null) {
            final View view = activity.findViewById(android.R.id.content);

            if(view.getHeight() > 0) {
                startSurveyActivity();
            } else {
                view.getViewTreeObserver().addOnPreDrawListener(startSurveyBeforeDrawListener(view));
            }
        }
    }

    private ViewTreeObserver.OnPreDrawListener startSurveyBeforeDrawListener(final View view) {
        return new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ViewTreeObserver observer = view.getViewTreeObserver();
                if(observer != null) {
                    observer.removeOnPreDrawListener(this);
                }

                startSurveyActivity();

                return true;
            }
        };
    }

    private void startSurveyActivity() {
        final Activity activity = weakActivity.get();

        if(activity != null && isActivityValid()) {
            Bitmap screenshot = ImageUtils.takeActivityScreenshot(activity, 4);
            Bitmap blurredScreenshot = Blur.blur(activity, screenshot, 8);

            SurveyActivity.start(activity, blurredScreenshot, prefs.getAccessToken(), mUser, mEndUser,
                    mOriginUrl, mCustomMessage, mProductName);
        }
    }
}
