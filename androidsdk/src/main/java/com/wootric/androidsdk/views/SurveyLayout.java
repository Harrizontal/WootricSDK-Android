package com.wootric.androidsdk.views;

import com.wootric.androidsdk.objects.Settings;

/**
 * Created by maciejwitowski on 9/30/15.
 */
interface SurveyLayout {

    void setSurveyLayoutListener(SurveyLayoutListener surveyLayoutListener);

    void initWithSettings(Settings settings);
    void setupState(int surveyState, int selectedScore, String feedback);

    int getSelectedScore();
    int getSelectedState();
    String getFeedback();

    void showThankYouLayout();
}
