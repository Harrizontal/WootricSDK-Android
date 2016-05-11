package com.wootric.androidsdk.views;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.wootric.androidsdk.OfflineDataHandler;
import com.wootric.androidsdk.R;
import com.wootric.androidsdk.Wootric;
import com.wootric.androidsdk.network.WootricRemoteClient;
import com.wootric.androidsdk.objects.EndUser;
import com.wootric.androidsdk.objects.Settings;
import com.wootric.androidsdk.utils.PreferencesUtils;
import com.wootric.androidsdk.utils.ScreenUtils;
import com.wootric.androidsdk.utils.SocialHandler;
import com.wootric.androidsdk.views.phone.ThankYouDialogFactory;

import java.lang.ref.WeakReference;

/**
 * Created by maciejwitowski on 9/4/15.
 */
public class SurveyFragment extends DialogFragment
    implements SurveyLayoutListener {

    private static final String ARG_ORIGIN_URL = "com.wootric.androidsdk.arg.origin_url";
    private static final String ARG_END_USER = "com.wootric.androidsdk.arg.end_user";
    private static final String ARG_SETTINGS = "com.wootric.androidsdk.arg.settings";
    private static final String ARG_RESPONSE_SENT = "com.wootric.androidsdk.arg.response_sent";
    private static final String ARG_ACCESS_TOKEN = "com.wootric.androidsdk.arg.access_token";

    private SurveyLayout mSurveyLayout;
    private LinearLayout mFooter;

    private EndUser mEndUser;
    private String mOriginUrl;
    private String mAccessToken;
    private Settings mSettings;

    private boolean mResponseSent;

    private WootricRemoteClient mWootricApiClient;
    private SocialHandler mSocialHandler;

    private boolean mIsTablet;

    private boolean isResumedOnConfigurationChange;

    public static SurveyFragment newInstance(EndUser endUser, String originUrl, String accessToken,
                                             Settings settings) {
        SurveyFragment fragment = new SurveyFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_END_USER, endUser);
        args.putString(ARG_ORIGIN_URL, originUrl);
        args.putString(ARG_ACCESS_TOKEN, accessToken);
        args.putParcelable(ARG_SETTINGS, settings);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NO_TITLE, 0);
        setupState(savedInstanceState);

        mSocialHandler = new SocialHandler(getActivity());
        PreferencesUtils prefUtils = new PreferencesUtils(new WeakReference<Context>(this.getActivity()));
        OfflineDataHandler offlineDataHandler = new OfflineDataHandler(prefUtils);
        mWootricApiClient = new WootricRemoteClient(offlineDataHandler);

        mIsTablet = getResources().getBoolean(R.bool.isTablet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wootric_fragment_survey, container, false);
        
        if(!mIsTablet) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        mSurveyLayout = (SurveyLayout) view.findViewById(R.id.wootric_survey_layout);
        mSurveyLayout.setSurveyLayoutListener(this);

        mFooter = (LinearLayout) view.findViewById(R.id.wootric_footer);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSurveyLayout.initWithSettings(mSettings);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(!mIsTablet)
            measurePhoneDialog();
    }

    private void measurePhoneDialog() {
        final Activity activity = getActivity();

        Dialog dialog = getDialog();
        if (dialog == null) return;

        final Window window = dialog.getWindow();
        if (window == null) return;

        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes());

        final int screenHeight = ScreenUtils.getScreenHeight(activity);
        final int screenWidth = ScreenUtils.getScreenWidth(activity);

        final boolean launchedInPortrait = (screenHeight > screenWidth);
        final boolean isPortraitMode = (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        if(launchedInPortrait) {
            if(isPortraitMode) {
                lp.height = screenHeight*4/5;
                lp.width = screenWidth;
            } else {
                lp.height = screenWidth;
                lp.width = screenHeight*4/5;
            }
        } else {
            if(isPortraitMode) {
                lp.height = screenWidth*4/5;
                lp.width = screenHeight;
            } else {
                lp.height = screenHeight;
                lp.width = screenWidth*4/5;
            }
        }

        window.setAttributes(lp);
        mFooter.setVisibility(isPortraitMode ? View.VISIBLE : View.GONE);
    }

    private void setupState(Bundle savedInstanceState) {
        Bundle args = getArguments();
        mEndUser = args.getParcelable(ARG_END_USER);
        mOriginUrl = args.getString(ARG_ORIGIN_URL);
        mAccessToken = args.getString(ARG_ACCESS_TOKEN);
        mSettings = args.getParcelable(ARG_SETTINGS);

        if(savedInstanceState != null) {
            mResponseSent = savedInstanceState.getBoolean(ARG_RESPONSE_SENT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ARG_RESPONSE_SENT, mResponseSent);
        super.onSaveInstanceState(outState);
    }

    private void createDecline() {
        mWootricApiClient.createDecline(mEndUser.getId(), mAccessToken, mOriginUrl);
    }

    @Override
    public void onSurveySubmit(int score, String text) {
        mWootricApiClient.createResponse(mEndUser.getId(), mAccessToken, mOriginUrl, score, text);
        mResponseSent = true;
    }

    @Override
    public void onFacebookLikeBtnClick() {
        if (mSocialHandler == null) return;

        String facebookId = mSettings.getFacebookPageId();
        mSocialHandler.goToFacebook(facebookId);

        dismiss();
    }

    @Override
    public void onFacebookBtnClick() {
        if (mSocialHandler == null) return;

        String facebookId = mSettings.getFacebookPageId();
        mSocialHandler.shareOnFacebook(facebookId);

        dismiss();
    }

    @Override
    public void onTwitterBtnClick() {
        if (mSocialHandler == null) return;

        String twitterPage = mSettings.getTwitterPage();
        mSocialHandler.goToTwitter(twitterPage, mSurveyLayout.getFeedback());

        dismiss();
    }

    @Override
    public void onThankYouActionClick() {
        final Uri uri = mSettings.getThankYouLinkUri(mSurveyLayout.getSelectedScore(), mSurveyLayout.getFeedback());
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final Activity activity = getActivity();

        if (activity != null) {
            activity.startActivity(intent);
        }

        dismiss();
    }

    @Override
    public void dismiss() {
        if(mIsTablet) {
            Wootric.notifySurveyFinished(true);
        }

        super.dismiss();
    }

    @Override
    public void onDismissClick() {
        if (!mResponseSent) {
            createDecline();
        }

        dismiss();
    }

    @Override
    public void onShouldShowSimpleDialog() {
        final Activity activity = getActivity();

        ThankYouDialogFactory.create(activity, mSettings, mSurveyLayout.getSelectedScore()).show();
        dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isResumedOnConfigurationChange = true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(!isResumedOnConfigurationChange) {
            Wootric.notifySurveyFinished(true);
        }
    }
}
