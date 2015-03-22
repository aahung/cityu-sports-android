package xinhong.me.cityusportfacility;


import android.app.DownloadManager;

import java.util.Map;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.client.Request;
import retrofit.http.EncodedPath;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by aahung on 3/20/15.
 */
public interface API {

    @GET("/{url}")
    public void makeGetRequest(@EncodedPath("url") String url,
                               @Header("Referer") String referer, Callback<Response> callback);


    public void makeGetRequestDebug(String url, String referer, Callback<Response> callback);

    @FormUrlEncoded
    @POST("/{url}")
    public void makePostRequest(@EncodedPath("url") String url,
                                @Header("Referer") String referer, @FieldMap Map<String, String> parameters,
                                Callback<Response> callback);
}
