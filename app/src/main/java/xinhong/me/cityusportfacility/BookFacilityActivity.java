package xinhong.me.cityusportfacility;

import android.app.ProgressDialog;
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


public class BookFacilityActivity extends ActionBarActivity {

    @InjectView(R.id.table)
    ListView table;

    FacilityAdapter facilityAdapter;

    Connector connector;
    String eid, sid, password, userType, date;

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

        facilityAdapter = new FacilityAdapter();
        table.setAdapter(facilityAdapter);
        table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Booking booking = (Booking) facilityAdapter.getItem(position);
                Intent intent = new Intent(BookFacilityActivity.this, BookCourtActivity.class);
                Bundle b = new Bundle();
                b.putString("session", connector.getSessionId());
                b.putString("eid", eid);
                b.putString("sid", sid);
                b.putString("password", password);
                b.putString("user_type", userType);
                b.putString("date", date);
                b.putString("facility", booking.facility);
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
        progressDialog = ProgressDialog.show(this, null, "Request facilities");
        connector.requestFacilities(eid, sid, date, userType, new Callback<Connector.Result>() {
            @Override
            public void success(Connector.Result result, Response response) {
                progressDialog.dismiss();
                if (result.success) {
                    facilityAdapter.clear();
                    String[] facilities = (String[]) result.objects;
                    for (String facility : facilities) {
                        Booking booking = new Booking();
                        booking.facility = facility;
                        booking.facilityReadable = Parser.getFacilityNameByCode(facility);
                        facilityAdapter.addItem(booking);
                    }
                    facilityAdapter.notifyDataSetChanged();
                } else {
                    SimpleAlertController.showSimpleMessage("Oops", result.message, BookFacilityActivity.this);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    // list adapter

    public class FacilityAdapter extends BaseAdapter {
        private List<Booking> items;
        private LayoutInflater mInflator;
        public FacilityAdapter() {
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
            view = mInflator.inflate(R.layout.listitem_facility, null);
            viewHolder = new ViewHolder();
            viewHolder.facility = (TextView) view.findViewById(R.id.facilityText);
            Booking booking = (Booking) getItem(i);
            view.setTag(booking);
            viewHolder.facility.setText(booking.facilityReadable);
            return view;
        }

        public class ViewHolder {
            TextView facility;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
    }

}
