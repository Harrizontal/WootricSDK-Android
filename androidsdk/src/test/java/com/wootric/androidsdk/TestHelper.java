package com.wootric.androidsdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.test.mock.MockPackageManager;

import com.wootric.androidsdk.objects.EndUser;
import com.wootric.androidsdk.objects.User;
import com.wootric.androidsdk.utils.PreferencesUtils;

import java.lang.ref.WeakReference;

public class TestHelper {

    private static final String CLIENT_ID = "testClientId";
    private static final String CLIENT_SECRET = "testClientSecret";
    private static final String ACCOUNT_TOKEN = "testAccountToken";
    public static final String ORIGIN_URL = "com.test.app";

    public static User testUser() {
        return new User(CLIENT_ID, CLIENT_SECRET, ACCOUNT_TOKEN);
    }

    public static EndUser testEndUser() {
        return new EndUser();
    }

    public static PreferencesUtils testPreferenceUtils() {
        WeakReference weakContext = new WeakReference<>(TEST_FRAGMENT_ACTIVITY.getApplicationContext());
        return new PreferencesUtils(weakContext);
    }

    public static final FragmentActivity TEST_FRAGMENT_ACTIVITY = new FragmentActivity() {
        @Override
        public PackageManager getPackageManager() {
            return TEST_PACKAGE_MANAGER;
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            return TEST_APPLICATION_INFO;
        }

        @Override
        public Context getApplicationContext() {
            return null;
        }
    };

    private static final ApplicationInfo TEST_APPLICATION_INFO = new ApplicationInfo() {
        public static final String packageName = "";
    };

    private static final MockPackageManager TEST_PACKAGE_MANAGER = new MockPackageManager() {
        @Override
        public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
            return TEST_APPLICATION_INFO;
        }
        @Override
        public CharSequence getApplicationLabel(ApplicationInfo info) {
            return "com.test.app";
        }
    };
}
