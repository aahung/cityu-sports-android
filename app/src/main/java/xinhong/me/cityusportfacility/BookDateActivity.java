package xinhong.me.cityusportfacility;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class BookDateActivity extends ActionBarActivity {

    @InjectView(R.id.table)
    ListView table;

    DateAdapter dateAdapter;

    Connector connector;
    String eid, sid, password, userType;

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

        dateAdapter = new DateAdapter();
        table.setAdapter(dateAdapter);
        table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Booking booking = (Booking) dateAdapter.getItem(position);
                Intent intent = new Intent(BookDateActivity.this, BookFacilityActivity.class);
                Bundle b = new Bundle();
                b.putString("session", connector.getSessionId());
                b.putString("eid", eid);
                b.putString("sid", sid);
                b.putString("password", password);
                b.putString("user_type", userType);
                b.putString("date", booking.date);
                intent.putExtras(b);
                startActivity(intent);
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

    private void refresh() {
        progressDialog = ProgressDialog.show(BookDateActivity.this, null, "Request date url");
        connector.requestDateURL(eid, sid, new Callback<Connector.Result>() {
            @Override
            public void success(Connector.Result result, Response response) {
                if (result.success) {
                    String url = result.message;
                    userType = result.message2;
                    refresh2(url);
                } else {
                    SimpleAlertController.showSimpleMessage("Oops", result.message, BookDateActivity.this);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void refresh2(String url) {
        progressDialog.setMessage("Request dates");
        connector.requestDates(eid, sid, url, new Callback<Connector.Result>() {
            @Override
            public void success(Connector.Result result, Response response) {
                progressDialog.dismiss();
                if (result.success) {
                    dateAdapter.clear();
                    String[] dates = (String[]) result.objects;
                    for (String date : dates) {
                        Booking booking = new Booking();
                        booking.date = date;
                        dateAdapter.addItem(booking);
                    }
                    dateAdapter.notifyDataSetChanged();
                } else {
                    SimpleAlertController.showSimpleMessage("Oops", result.message, BookDateActivity.this);
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
            view = mInflator.inflate(R.layout.listitem_date, null);
            viewHolder = new ViewHolder();
            viewHolder.date = (TextView) view.findViewById(R.id.dateText);
            viewHolder.weekday = (TextView) view.findViewById(R.id.weekdayText);
            Booking booking = (Booking) getItem(i);
            String dateStr = booking.date.substring(0, 8);
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date date = null;
            view.setTag(booking);
            try {
                date = format.parse(dateStr);
            } catch (ParseException e) {

            }
            if (date != null) {
                format = new SimpleDateFormat("yyyy-MM-dd");
                viewHolder.date.setText(format.format(date));
                format = new SimpleDateFormat("EEEE");
                viewHolder.weekday.setText(format.format(date));
            }
            return view;
        }

        public class ViewHolder {
            TextView date;
            TextView weekday;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
    }

}
