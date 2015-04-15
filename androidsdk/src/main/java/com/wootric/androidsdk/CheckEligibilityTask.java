package com.wootric.androidsdk;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.wootric.androidsdk.Constants.NOT_SET;

/**
 * Created by maciejwitowski on 4/11/15.
 */
public class CheckEligibilityTask extends AsyncTask<Void, Void, Boolean> {

    private String accountToken;
    private String email;

    // Optional
    private int dailyResponseCap    = NOT_SET;
    private int registeredPercent   = NOT_SET;
    private int visitorPercent      = NOT_SET;
    private int resurveyThrottle    = NOT_SET;

    CheckEligibilityTask(String accountToken, String email, int dailyResponseCap,
                         int registeredPercent, int visitorPercent, int resurveyThrottle) {
        if(accountToken == null || email == null) {
            throw new IllegalArgumentException
                    ("Account token and email must not be null.");
        }

        this.accountToken       = accountToken;
        this.email              = email;
        this.dailyResponseCap   = dailyResponseCap;
        this.registeredPercent  = registeredPercent;
        this.visitorPercent     = visitorPercent;
        this.resurveyThrottle   = resurveyThrottle;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String urlWithParams = Constants.ELIGIBILITY_URL + "?" + eligibilityRequestParams();
        HttpGet request = new HttpGet(urlWithParams);

        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(request);

            HttpEntity responseEntity = response.getEntity();
            String result = EntityUtils.toString(responseEntity);

            return result.contains("true");
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String eligibilityRequestParams() {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(Constants.PARAM_ACCOUNT_TOKEN, accountToken));
        params.add(new BasicNameValuePair(Constants.PARAM_EMAIL, email));

        if(dailyResponseCap != NOT_SET) {
            params.add(new BasicNameValuePair(Constants.PARAM_DAILY_RESPONSE_CAP, email));
        }

        if(registeredPercent != NOT_SET) {
            params.add(new BasicNameValuePair(Constants.PARAM_EMAIL, email));
        }

        if(visitorPercent != NOT_SET) {
            params.add(new BasicNameValuePair(Constants.PARAM_EMAIL, email));
        }

        if(resurveyThrottle != NOT_SET) {
            params.add(new BasicNameValuePair(Constants.PARAM_RESURVEY_THROTTLE, email));
        }

        return URLEncodedUtils.format(params, "utf-8");
    }
}
