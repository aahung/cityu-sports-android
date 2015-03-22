package xinhong.me.cityusportfacility;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class BookCourtActivity extends ActionBarActivity {

    @InjectView(R.id.table)
    ListView table;

    DateAdapter courtAdapter;

    Connector connector;
    String eid, sid, password, userType, date, facility;
    Map<String, String> universalParameters;
    String bookReferer;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        ButterKnife.inject(this);
        Bundle b = getIntent().getExtras();
        USportApplication application = (USportApplication) getApplication();
        connector = new Connector(b.getString("session"), application.okClient);
        eid = b.getString("eid");
        sid = b.getString("sid");
        password = b.getString("password");
        userType = b.getString("user_type");
        date = b.getString("date");
        facility = b.getString("facility");

        courtAdapter = new DateAdapter();
        table.setAdapter(courtAdapter);
        table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Booking booking = (Booking) view.getTag();
                if (booking.id != "section") {
                    if (booking.message != null) {
                        SimpleAlertController.showSimpleMessage("Oops", booking.message, BookCourtActivity.this);
                    } else {
                        SimpleAlertController.showConstructiveMessageWithHandler("Hey",
                                "Are you going to book " + booking.courtReadable + "?",
                                "Sure", BookCourtActivity.this, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog = ProgressDialog.show(BookCourtActivity.this, "",
                                        "Request confirmation number");
                                Map<String, String> parameters = new HashMap<String, String>();
                                parameters.putAll(universalParameters);
                                parameters.put("p_date", booking.date);
                                parameters.put("p_court", booking.court);
                                parameters.put("p_venue", booking.venue);
                                parameters.put("p_stime", booking.stime);
                                parameters.put("p_facility_ref", booking.facilityRef);
                                connector.makeBooking(parameters, bookReferer, new Callback<Connector.Result>() {
                                    @Override
                                    public void success(Connector.Result result, Response response) {
                                        if (result.success) {
                                            String confirmNo = result.message;
                                            confirm(confirmNo);
                                        } else {
                                            progressDialog.dismiss();
                                            SimpleAlertController.showSimpleMessage("Oops", result.message, BookCourtActivity.this);
                                        }
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {

                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirm(String confirmNo) {
        progressDialog.setMessage("Confirm");
        connector.confirmBook(eid, password, confirmNo, new Callback<Connector.Result>() {
            @Override
            public void success(Connector.Result result, Response response) {
                progressDialog.dismiss();
                if (result.success) {
                    SimpleAlertController.showSimpleMessageWithHandler("Result",
                            result.message, BookCourtActivity.this, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(BookCourtActivity.this, ManageActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    Bundle b = new Bundle();
                                    b.putString("session", connector.getSessionId());
                                    b.putString("eid", eid);
                                    b.putString("sid", sid);
                                    b.putString("password", password);
                                    intent.putExtras(b);
                                    startActivity(intent);
                                }
                            });
                } else {
                    SimpleAlertController.showSimpleMessage("Oops", result.message, BookCourtActivity.this);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void refresh() {
        progressDialog = ProgressDialog.show(BookCourtActivity.this, null, "Request court url");
        connector.requestCourtURL(eid, sid, userType, date, facility, new Callback<Connector.Result>() {
            @Override
            public void success(Connector.Result result, Response response) {
                if (result.success) {
                    String url = result.message;
                    bookReferer = url;
                    refresh2(url);
                } else {
                    SimpleAlertController.showSimpleMessage("Oops", result.message, BookCourtActivity.this);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void refresh2(String url) {
        progressDialog.setMessage("Request courts, it takes around 5 seconds");
        connector.requestCourts(url, new Callback<Connector.Result>() {
            @Override
            public void success(Connector.Result result, Response response) {
                progressDialog.dismiss();
                if (result.success) {
                    courtAdapter.clear();
                    Map<String, Booking[]> courts = (Map<String, Booking[]>)result.objects;
                    universalParameters = (Map<String, String>) result.objects2;
                    for (String key : courts.keySet()) {
                        Booking sectionBooking = new Booking();
                        sectionBooking.id = "section"; // indicating it is a section
                        sectionBooking.timeReadable = key;
                        courtAdapter.addItem(sectionBooking);
                        for (Booking booking : courts.get(key))
                            courtAdapter.addItem(booking);
                    }

                    courtAdapter.notifyDataSetChanged();
                } else {
                    SimpleAlertController.showSimpleMessage("Oops", result.message, BookCourtActivity.this);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    // list adapter

    public class DateAdapter extends BaseAdapter {
        private List<Booking> items;
        private LayoutInflater mInflator;
        public DateAdapter() {
            super();
            items = new ArrayList<>();
            mInflator = getLayoutInflater();
        }
        public void addItem (Booking item) {
            items.add(item);
        }
        public void clear() {
            items.clear();
        }
        @Override
        public int getCount() {
            return items.size();
        }
        @Override
        public Object getItem(int i) {
            return items.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;


            Booking booking = (Booking) getItem(i);
            if (booking.id == "section") {
                view = mInflator.inflate(R.layout.listitem_section_header, null);
                viewHolder = new ViewHolder();
                viewHolder.court = (TextView) view.findViewById(R.id.headerText);
                viewHolder.court.setText(booking.timeReadable);
            } else {
                view = mInflator.inflate(R.layout.listitem_court, null);
                viewHolder = new ViewHolder();
                viewHolder.court = (TextView) view.findViewById(R.id.courtText);
                viewHolder.venue = (TextView) view.findViewById(R.id.venueText);
                viewHolder.court.setText(booking.courtReadable);
                viewHolder.venue.setText(booking.venueReadable);
            }
            view.setTag(booking);
            return view;
        }

        public class ViewHolder {
            TextView court;
            TextView venue;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
    }

}
