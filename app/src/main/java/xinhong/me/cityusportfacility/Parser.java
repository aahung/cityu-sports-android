package xinhong.me.cityusportfacility;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.client.Response;

/**
 * Created by aahung on 3/21/15.
 */
public class Parser {


    public static String getHtml(Response response) {
        InputStream inputStream;
        try {
            inputStream = response.getBody().in();
        } catch (IOException e) {
            return null;
        }
        String html;
        try {
            html = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return html;
    }

    static public Document getDocumentByResponse(Response response) {
        String html = getHtml(response);
        if (html == null) return null;
        Document document;
        document = Jsoup.parse(html);

        return document;
    }

    static public String getMessage(Document document) {
        Elements elements = document.select("small");
        String message = "";
        for (Element element : elements) {
            message = message + element.text();
        }
        if (elements.size() == 0) return null;
        return message;
    }

    static public String getSessionId(Document document) {
        Elements elements = document.select("input[name=\"p_session\"]");
        if (elements.size() == 0) return null;
        return elements.get(0).attr("value");
    }

    static public String getSid(Document document) {
        Elements elements = document.select("frame[name=\"main_win\"]");
        if (elements.size() == 0) return null;
        Element userInfoFrame = elements.get(0);
        String source = userInfoFrame.attr("src");
        if (source != null) {
            String[] tokens = source.split("&");
            for (String token : tokens) {
                if (token.contains("p_user_no")) {
                    tokens = token.split("/");
                    if (tokens.length > 1) return tokens[1];
                }
            }
        }
        return null;
    }

    static public Booking[] getBookings(Document document) {
        List<Booking> bookings = new ArrayList<>();
        Elements trs = document.select("tr");
        for (Element tr : trs) {
            if (tr.attr("bgcolor") == "") continue;
            Elements fonts = tr.select("font");
            if (fonts.size() != 4) continue;
            Booking booking = new Booking();

            // get id
            Elements delAs = tr.select("a");
            if (delAs.size() > 0) {
                Element delA = delAs.get(0);
                String delHref = delA.attr("href");
                String[] tokens = delHref.split("/");
                if (tokens.length > 0) {
                    tokens = tokens[0].split("'");
                    if (tokens.length > 1) {
                        booking.id = tokens[1];
                    }
                }
            }

            Elements smalls;
            Element font;

            font = fonts.get(0);
            smalls = font.select("small");
            if (smalls.size() > 0) {
                String str = "";
                for (Element small : smalls) {
                    if (small == smalls.get(0)) {
                        booking.dateReadable = small.text().replaceAll("  ", " ");
                    } else {
                        str += small.text();
                    }
                }
                booking.timeReadable = str;
            }

            font = fonts.get(1);
            smalls = font.select("small");
            if (smalls.size() > 0) {
                String str = "";
                for (Element small : smalls) {
                    str += small.text() + " ";
                }
                booking.courtReadable = str;
            }

            font = fonts.get(2);
            smalls = font.select("small");
            if (smalls.size() > 0) {
                String str = "";
                for (Element small : smalls) {
                    str += small.text();
                }
                booking.venueReadable = str;
            }

            font = fonts.get(3);
            smalls = font.select("small");
            if (smalls.size() > 0) {
                String str = "";
                for (Element small : smalls) {
                    str += small.text();
                }
                booking.paymentInfo = str;
            }

            bookings.add(booking);
        }
        return bookings.toArray(new Booking[bookings.size()]);
    }

    public static String getRequestURL(Document document) {
        String[] tokens;
        String html = document.outerHtml();
        if (html.contains("opt_left_win")) {
            tokens = html.split("\" name=\"opt_left_win\"");
        } else {
            tokens = html.split("\" name=\"body_win\"");
        }
        if (tokens.length > 0) {
            tokens = tokens[0].split("<frame src=\"");
            if (tokens.length > 0) {
                String link = tokens[tokens.length - 1];
                return link.replaceFirst("/", ""); // better validate it
            }
        }
        return null;
    }

    public static String getUserTypeByDateURL(String url) {
        String[] tokens = url.split("p_user_type_no=");
        if (tokens.length > 1) {
            String userType = tokens[1];
            tokens = userType.split("&");
            if (tokens.length > 0) {
                userType = tokens[0];
                return userType;
            }
        }
        return null;
    }

    public static String[] getDates(Document document) {
        List<String> dates = new ArrayList<>();

        Elements as = document.select("a");
        for (Element a : as) {
            String href = a.attr("href");
            if (href != "") {
                String[] tokens = href.split("date_data\\('");
                if (tokens.length < 2) continue;
                String date = tokens[1];
                tokens = date.split("','");
                if (tokens.length < 1) continue;
                date = tokens[0];
                dates.add(date);
            }
        }

        return dates.toArray(new String[dates.size()]);
    }

    public static String[] getFacilities(Document document) {
        List<String> facilities = new ArrayList<>();

        Elements as = document.select("a");
        for (Element a : as) {
            String href = a.attr("href");
            if (href != "") {
                String[] tokens = href.split("sub_data\\('");
                if (tokens.length < 2) continue;
                String facility = tokens[1];
                tokens = facility.split("'"); // only one single quota
                if (tokens.length < 1) continue;
                facility = tokens[0];
                facilities.add(facility);
            }
        }

        return facilities.toArray(new String[facilities.size()]);
    }

    public static String getFacilityNameByCode(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("BMT", "Badminton");
        map.put("BB", "Basketball");
        map.put("GDR", "Golf Driving");
        map.put("GSIM", "Golf Simulation");
        map.put("ODBB", "Outdoor Basketball");
        map.put("PF", "Physical Fitness");
        map.put("PG 2", "Practice Gymnasium 2");
        map.put("PG 4", "Practice Gymnasium 4");
        map.put("SQ", "Squash");
        map.put("TT", "Table Tennis");
        map.put("VB", "Volleyball");
        map.put("JSFS", "Handball / 5-on-5 Soccer (Joint Sports Center)");
        map.put("JSBV", "Basketball / Volleyball (Joint Sports Center)");
        map.put("JSBF", "Basketball / Volleyball (Night) (Joint Sports Center)");
        map.put("JSGO", "Golf");
        map.put("JSSOA", "Soccer Pitch (Afternoon) (Joint Sports Center)");
        map.put("JSSOF", "Soccer Pitch (Evening) (Joint Sports Center)");
        map.put("JST", "Tennis (Joint Sports Center)");
        map.put("JSTF", "Tennis (Night) (Joint Sports Center)");
        if (map.containsKey(code)) return map.get(code);
        return code;
    }

    public static Map<String, Booking[]> getCourts(Document document) {
        Map<String, List<Booking>> courts = new HashMap<>();

        Elements as = document.select("a");
        for (Element a : as) {
            Elements imgs = a.select("img");
            if (imgs.size() == 0) continue;
            Element img = imgs.get(0);
            String[] tokens;

            if (img.attr("src").contains("sq_cyan.gif")) {
                Booking court = new Booking();
                String href = a.attr("href");
                if (href.contains("alert")) {
                    String message;
                    tokens = href.split("alert\\(");
                    if (tokens.length > 1) {
                        tokens = tokens[1].split(";");
                        message = tokens[0];
                        court.message = message;
                    }
                } else {
                    tokens = href.split("sub_data");
                    if (tokens.length < 2) continue;
                    String string = tokens[1];
                    string = string.replaceAll("[\\(\\);']", "");
                    tokens = string.split(",");
                    if (tokens.length > 4) {
                        court.date = tokens[0];
                        court.court = tokens[1];
                        court.venue = tokens[2];
                        court.facilityRef = tokens[3];
                        court.stime = tokens[4];
                    }
                }

                String mouseOver = a.attr("onmouseover");

                if (mouseOver == "") continue;
                String readable;


                court.courtReadable = getReadableFromMouseOver(mouseOver, "Facility No.: ");
                court.timeReadable = getReadableFromMouseOver(mouseOver, "Time: \\$");
                court.venueReadable = getReadableFromMouseOver(mouseOver, "Venue: \\$/");

                if (!courts.containsKey(court.timeReadable)) {
                    courts.put(court.timeReadable, new ArrayList<Booking>());
                }

                courts.get(court.timeReadable).add(court);
            }
        }

        Map<String, Booking[]> courtsArray = new HashMap<>();
        for (String key : courts.keySet()) {
            courtsArray.put(key, courts.get(key).toArray(new Booking[courts.get(key).size()]));
        }

        return courtsArray;
    }

    private static String getReadableFromMouseOver(String mouseOver, String cut) {
        String[] tokens = mouseOver.split(cut);
        String readable;
        if (tokens.length > 1) {
            readable = tokens[1];
            tokens = readable.split("/");
            readable = tokens[0];
            return readable;
        }
        return null;
    }

    public static Map<String, String> getParametersForBooking(Document document) {
        Map<String, String> parameters = new HashMap<>();
        Elements inputs = document.select("input[type=\"hidden\"]");
        for (Element input : inputs) {
            String name = input.attr("name");
            String value = input.attr("value");
            parameters.put(name, value);
        }
        return parameters;
    }

    public static String getConfirmNo(Document document) {
        Elements nodes = document.select("input[name=\"p_sno\"]");
        if (nodes.size() > 0 && nodes.get(0).attr("value") != "") {
            return nodes.get(0).attr("value");
        }
        return null;
    }
}
