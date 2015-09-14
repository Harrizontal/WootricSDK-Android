package com.wootric.androidsdk.network.responses;

import com.wootric.androidsdk.objects.Settings;

/**
 * Created by maciejwitowski on 9/14/15.
 */
public class EligibilityResponse {

    private boolean eligible;
    private Settings settings;

    public boolean isEligible() {
        return eligible;
    }

    public Settings getSettings() {
        return settings;
    }
}
