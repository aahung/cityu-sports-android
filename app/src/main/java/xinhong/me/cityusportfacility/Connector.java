package xinhong.me.cityusportfacility;


import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.http.GET;

/**
 * Created by aahung on 3/20/15.
 */
public class Connector {

    private static final String API_URL = "http://brazil.cityu.edu.hk:8754/";
    private String sessionId;
    private API api;

    public Connector(OkClient okClient) {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)  //call your base url
                .setRequestInterceptor(requestInterceptor)
                .setClient(okClient)
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
                    public void log(String msg) {
                        Log.i("retrofit", msg);
                    }
                })
                .build();
        api = restAdapter.create(API.class);
    }

    public Connector(String sessionId, OkClient okClient) {
        this(okClient);
        this.sessionId = sessionId;
    }

    RequestInterceptor requestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestInterceptor.RequestFacade request) {
            request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36");
            request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            request.addHeader("Accept-Language", "en;q=1, zh-Hans;q=0.9");
        }
    };

    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public void requestSessionId(final Callback<Result> callback) {
        api.makeGetRequest("fbi/owa/fbi_web_logon.show",
                "http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_first.show", new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Document document = Parser.getDocumentByResponse(response);
                if (document == null) {
                    callback.success(new Result(false, "Failed to connect to server."), response);
                    return;
                }
                String sessionId = Parser.getSessionId(document);
                if (sessionId == null) {
                    String message = Parser.getMessage(document);
                    callback.success(new Result(false, message), response);
                    return;
                }
                callback.success(new Result(true, sessionId), response);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.success(new Result(false,
                        error.getMessage()), null);
            }
        });
    }

    public void login(String eid, String password, final Callback<Result> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_status_code", "");
        parameters.put("p_sno", "");
        parameters.put("p_session", getSessionId());
        parameters.put("p_username", eid);
        parameters.put("p_password", password);

        api.makePostRequest("fbi/owa/fbi_web_logon.show",
                "http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_logon.show",
                parameters, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        String sid = Parser.getSid(document);
                        if (sid == null) {
                            callback.success(new Result(false, "Failed to get your sid, please check your eid and password."), response);
                            return;
                        }
                        callback.success(new Result(true, sid), response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public void requestMyBookings(String eid, String sid, final Callback<Result> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_session", getSessionId());
        parameters.put("p_username", eid);
        parameters.put("p_user_no", sid);

        api.makePostRequest("fbi/owa/fbi_web_enqbook.show",
                String.format("http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_main.toc?p_session=%s&p_username=%s&p_user_no=/%s/",
                        getSessionId(), eid, sid),
                parameters, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        Booking[] bookings = Parser.getBookings(document);
                        Result result = new Result(true);
                        result.objects = bookings;
                        callback.success(result, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }


    public void deleteBooking(final String eid, String sid, final String password, String bookingId, final Callback<Result> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_session", getSessionId());
        parameters.put("p_username", eid);
        parameters.put("p_user_no", sid);
        parameters.put("p_choice", bookingId + "/");
        parameters.put("p_enq", "Y");
        api.makePostRequest("fbi/owa/fbi_web_conf_msg_del.show",
                "http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_enqbook.show",
                parameters, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        String confirmNo = Parser.getConfirmNo(document);
                        if (confirmNo == null) {
                            callback.success(new Result(false, "Fail to get confirmation number."), response);
                            return;
                        }
                        confirmDelete(eid, password, confirmNo, callback);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public void confirmDelete(String eid, String password, String confirmNo, final Callback<Result> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_sno", confirmNo);
        parameters.put("p_username", eid);
        parameters.put("p_password", password);

        api.makePostRequest("fbi/owa/fbi_web_conf_msg_del.show",
                "http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_conf_msg_del.show",
                parameters, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        Result result = new Result(true);
                        callback.success(result, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public void requestDateURL(String eid, String sid, final Callback<Result> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_session", getSessionId());
        parameters.put("p_username", eid);
        parameters.put("p_user_no", sid);

        api.makePostRequest("fbi/owa/fbi_web_book.show",
                String.format("http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_main.toc?p_session=%s&p_username=%s&p_user_no=/%s/",
                        getSessionId(), eid, sid),
                parameters, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        String requestURL = Parser.getRequestURL(document);
                        if (requestURL == null) {
                            callback.success(new Result(false, "Fail to get date url."), response);
                            return;
                        }
                        String userType = Parser.getUserTypeByDateURL(requestURL);
                        if (userType == null) {
                            callback.success(new Result(false, "Fail to get user type number."), response);
                            return;
                        }
                        Result result = new Result(true);
                        result.message = requestURL;
                        result.message2 = userType;
                        callback.success(result, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public void requestDates(String eid, String sid, String url, final Callback<Result> callback) {
        url = url.replaceAll("&amp;", "&");
        api.makeGetRequest(url,
                String.format("http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_book.show?p_session=%s&p_username=%s&p_user_no=/%s/",
                        getSessionId(), eid, sid), new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        String[] dates = Parser.getDates(document);
                        Result result = new Result(true);
                        result.objects = dates;
                        callback.success(result, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public void requestFacilities(String eid, String sid, String date, String userType, final Callback<Result> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_session", getSessionId());
        parameters.put("p_username", eid);
        parameters.put("p_user_no", sid);
        parameters.put("p_user_type_no", userType);
        parameters.put("p_alter_adv_ref", "");
        parameters.put("p_alter_book_no", "");
        parameters.put("p_enq", "");
        parameters.put("p_date", date);
        parameters.put("p_empty", "");
        api.makePostRequest("fbi/owa/fbi_web_opt_fac_types.show",
                String.format("http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_calendar.show?p_session=%s&p_username=%s&p_user_no=/%s/",
                        getSessionId(), eid, sid), parameters, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        String[] facilities = Parser.getFacilities(document);
                        Result result = new Result(true);
                        result.objects = facilities;
                        callback.success(result, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public void requestCourtURL(String eid, String sid, String userType, String date, String facility, final Callback<Result> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_session", getSessionId());
        parameters.put("p_username", eid);
        parameters.put("p_user_no", sid);
        parameters.put("p_user_type_no", userType);
        parameters.put("p_alter_adv_ref", "");
        parameters.put("p_alter_book_no", "");
        parameters.put("p_enq", "");
        parameters.put("p_date", date);
        parameters.put("p_choice", facility);

        api.makePostRequest("fbi/owa/fbi_web_book_conf.show",
                "http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_opt_fac_types.show",
                parameters, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        String requestURL = Parser.getRequestURL(document);
                        if (requestURL == null) {
                            callback.success(new Result(false, "Fail to get court url."), response);
                            return;
                        }
                        Result result = new Result(true);
                        result.message = requestURL;
                        callback.success(result, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public void requestCourts(String url, final Callback<Result> callback) {
        url = url.replaceAll("&amp;", "&");
        api.makeGetRequest(url,
                "http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_book_conf.show", new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        Map<String, Booking[]> courts = Parser.getCourts(document);
                        Result result = new Result(true);
                        result.objects = courts;
                        result.objects2 = Parser.getParametersForBooking(document);
                        callback.success(result, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public void makeBooking(Map<String, String> parameters, String referer, final Callback<Result> callback) {

        api.makePostRequest("fbi/owa/fbi_web_conf_msg.show",
                API_URL + referer,
                parameters, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        String confirmNo = Parser.getConfirmNo(document);
                        if (confirmNo == null) {
                            callback.success(new Result(false, "Fail to get confirmation number."), response);
                            return;
                        }
                        Result result = new Result(true);
                        result.message = confirmNo;
                        callback.success(result, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public void confirmBook(String eid, String password, String confirmNo, final Callback<Result> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_sno", confirmNo);
        parameters.put("p_username", eid);
        parameters.put("p_password", password);

        api.makePostRequest("fbi/owa/fbi_web_conf_msg.show",
                "http://brazil.cityu.edu.hk:8754/fbi/owa/fbi_web_conf_msg.show",
                parameters, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Document document = Parser.getDocumentByResponse(response);
                        if (document == null) {
                            callback.success(new Result(false, "Failed to connect to server."), response);
                            return;
                        }
                        String message = Parser.getMessage(document);
                        if (message == null) {
                            callback.success(new Result(false, "Fail to get court url."), response);
                            return;
                        }
                        Result result = new Result(true);
                        result.message = message;
                        callback.success(result, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.success(new Result(false,
                                error.getMessage()), null);
                    }
                });
    }

    public static void checkUpdate(final Callback<Result> callback) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://aahung.github.io/")
                .build();
        API api = restAdapter.create(API.class);
        api.makeGetRequest("cityu-sports-android/version.txt", "", new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                String version = Parser.getHtml(response);
                if (version != null) {
                    Result result = new Result(true, version);
                    callback.success(result, response);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                // do nothing
            }
        });
    }

    public static class Result {
        public boolean success;
        public String message, message2;
        public Object objects, objects2;

        public Result(boolean success) {
            this.success = success;
        }

        public Result(boolean success, String message) {
            this(success);
            this.message = message;
        }
    }
}
