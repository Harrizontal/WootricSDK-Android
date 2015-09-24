package com.wootric.androidsdk;

import android.content.Context;

import com.wootric.androidsdk.network.SurveyClient;
import com.wootric.androidsdk.network.TrackingPixelClient;
import com.wootric.androidsdk.network.WootricApiClient;
import com.wootric.androidsdk.objects.CustomMessage;
import com.wootric.androidsdk.objects.CustomThankYou;
import com.wootric.androidsdk.objects.EndUser;
import com.wootric.androidsdk.objects.Settings;
import com.wootric.androidsdk.objects.User;
import com.wootric.androidsdk.utils.PermissionsValidator;
import com.wootric.androidsdk.utils.PreferencesUtils;

import java.util.HashMap;

/**
 * Created by maciejwitowski on 4/10/15.
 */
public class Wootric {

    final Context context;
    final EndUser endUser;
    final User user;
    final Settings settings;
    String originUrl;

    boolean surveyInProgress;
    PreferencesUtils preferencesUtils;
    PermissionsValidator permissionsValidator;

    static Wootric singleton;

    public static Wootric init(Context context, String clientId, String clientSecret, String accountToken) {
        if(singleton == null) {
            singleton = new Wootric(context, clientId, clientSecret, accountToken);
        }

        return singleton;
    }

    public static void notifySurveyFinished() {
        if(singleton != null) {
            singleton.setSurveyFinished();
        }
    }

    private void setSurveyFinished() {
        surveyInProgress = false;
        preferencesUtils.touchLastSurveyed();
    }
    
    public void setEndUserEmail(String email) {
        endUser.setEmail(email);
    }

    public void setProperties(HashMap<String, String> properties) {
        endUser.setProperties(properties);
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public void setSurveyImmediately(boolean surveyImmediately) {
        this.settings.setSurveyImmediately(surveyImmediately);
    }

    public void setCustomMessage(CustomMessage customMessage) {
        settings.setLocalCustomMessage(customMessage);
    }

    public void setDailyResponseCap(int value) {
        settings.setDailyResponseCap(value);
    }

    public void setRegisteredPercent(int value) {
        settings.setRegisteredPercent(value);
    }

    public void setVisitorPercent(int value) {
        settings.setVisitorPercent(value);
    }

    public void setResurveyThrottle(int value) {
        settings.setResurveyThrottle(value);
    }

    public void setLanguageCode(String languageCode) {
        settings.setLanguageCode(languageCode);
    }

    public void setProductName(String productName) {
        settings.setProductName(productName);
    }

    public void setRecommendTarget(String recommendTarget) {
        settings.setRecommendTarget(recommendTarget);
    }

    public void setFacebookPageId(String facebookPage) {
        settings.setFacebookPageId(facebookPage);
    }

    public void setTwitterPage(String tweeterPage) {
        settings.setTwitterPage(tweeterPage);
    }

    public void setCustomThankYou(CustomThankYou customThankYou) {
        settings.setCustomThankYou(customThankYou);
    }

    public void survey() {
        if(!permissionsValidator.check() || surveyInProgress)
            return;

        WootricApiClient wootricApiClient = new WootricApiClient();
        TrackingPixelClient trackingPixelClient = new TrackingPixelClient();
        SurveyClient surveyClient = new SurveyClient();

        SurveyValidator surveyValidator = buildSurveyValidator(user, endUser, settings,
                surveyClient, preferencesUtils);
        SurveyManager surveyManager = buildSurveyManager(context, wootricApiClient, trackingPixelClient,
                user, endUser, settings, originUrl, preferencesUtils, surveyValidator);

        surveyManager.start();
        surveyInProgress = true;
    }

    private Wootric(Context context, String clientId, String clientSecret, String accountToken) {
        if(context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }

        if(clientId == null || clientSecret == null || accountToken == null) {
            throw new IllegalArgumentException("Client Id, Client Secret and Account token must not be null");
        }

        this.context = context;
        endUser = new EndUser();
        user = new User(clientId, clientSecret, accountToken);
        settings = new Settings();
        preferencesUtils = new PreferencesUtils(context);
        permissionsValidator = new PermissionsValidator(context);
    }

    SurveyValidator buildSurveyValidator(User user, EndUser endUser, Settings settings,
                                         SurveyClient surveyClient, PreferencesUtils preferencesUtils) {
        return new SurveyValidator(user, endUser, settings, surveyClient, preferencesUtils);
    }

    SurveyManager buildSurveyManager(Context context, WootricApiClient wootricApiClient, TrackingPixelClient trackingPixelClient, User user,
                                     EndUser endUser, Settings settings, String originUrl,
                                     PreferencesUtils preferencesUtils, SurveyValidator surveyValidator) {
        return new SurveyManager(context, wootricApiClient, trackingPixelClient, user, endUser,
                settings, originUrl, preferencesUtils, surveyValidator);
    }
}
