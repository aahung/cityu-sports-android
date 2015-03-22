package xinhong.me.cityusportfacility;

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import retrofit.client.OkClient;

/**
 * Created by aahung on 3/22/15.
 */
public class USportApplication extends Application {
    public OkClient okClient;
    public OkHttpClient okHttpClient;

    public USportApplication() {
        super();
        okHttpClient = new OkHttpClient();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        okHttpClient.setCookieHandler(cookieManager);
        okClient = new OkClient(okHttpClient);
        CookieHandler.setDefault(cookieManager);
    }
}
