package xinhong.me.cityusportfacility;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ManageActivity extends ActionBarActivity {

    BookingAdapter bookingAdapter;

    @InjectView(R.id.table)
    ListView table;

    Connector connector;
    String eid, sid, password;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        ButterKnife.inject(this);
        bookingAdapter = new BookingAdapter();
        table.setAdapter(bookingAdapter);
        Bundle b = getIntent().getExtras();
        USportApplication application = (USportApplication) getApplication();
        connector = new Connector(b.getString("session"), application.okClient);
        eid = b.getString("eid");
        sid = b.getString("sid");
        password = b.getString("password");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            SharedPreferences userInfo = getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor edit = userInfo.edit();
            edit.clear();
            edit.commit();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    private void refresh() {
        progressDialog = ProgressDialog.show(ManageActivity.this, null, "Fetch bookings");
        connector.requestMyBookings(eid, sid, new Callback<Connector.Result>() {
            @Override
            public void success(Connector.Result result, Response response) {
                progressDialog.dismiss();
                if (result.success) {
                    bookingAdapter.clear();
                    Booking[] bookings = (Booking[]) result.objects;
                    for (Booking booking : bookings) bookingAdapter.addItem(booking);
                    bookingAdapter.notifyDataSetChanged();
                } else {
                    SimpleAlertController.showSimpleMessage("Oops", result.message, ManageActivity.this);
                }
            }

            @Override
            public void failure(RetrofitError error) { }
        });
    }

    // list adapter

    public class BookingAdapter extends BaseAdapter {
        private List<Booking> items;
        private LayoutInflater mInflator;
        public BookingAdapter() {
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
            return items.size() + 1;
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
            if (i == items.size()) {
                view = mInflator.inflate(R.layout.listitem_new_booking, null);
                view.findViewById(R.id.addBooingButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ManageActivity.this, BookDateActivity.class);
                        Bundle b = new Bundle();
                        b.putString("session", connector.getSessionId());
                        b.putString("eid", eid);
                        b.putString("sid", sid);
                        b.putString("password", password);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                });
                return view;
            }
            ViewHolder viewHolder;
            view = mInflator.inflate(R.layout.listitem_booking, null);
            viewHolder = new ViewHolder();
            viewHolder.date = (TextView) view.findViewById(R.id.dateText);
            viewHolder.time = (TextView) view.findViewById(R.id.timeText);
            viewHolder.court = (TextView) view.findViewById(R.id.courtText);
            viewHolder.venue = (TextView) view.findViewById(R.id.venueText);
            viewHolder.payment = (TextView) view.findViewById(R.id.paymentText);
            viewHolder.delete = (ImageButton) view.findViewById(R.id.deleteButton);
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Booking booking = (Booking) v.getTag();
                    SimpleAlertController.showDestructiveMessageWithHandler("Warning",
                            String.format("Are you sure you want to cancel %s on %s?", booking.courtReadable, booking.dateReadable), "Sure",
                            ManageActivity.this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressDialog = ProgressDialog.show(ManageActivity.this, "", String.format("Cancel %s", booking.courtReadable));
                            connector.deleteBooking(eid, sid, password, booking.id, new Callback<Connector.Result>() {
                                @Override
                                public void success(Connector.Result result, Response response) {
                                    progressDialog.dismiss();
                                    if (result.success) {
                                        refresh();
                                    } else {
                                        SimpleAlertController.showSimpleMessage("Oops", result.message, ManageActivity.this);
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {

                                }
                            });
                        }
                    });
                }
            });
            Booking booking = (Booking) getItem(i);
            viewHolder.date.setText(booking.dateReadable);
            viewHolder.time.setText(booking.timeReadable);
            viewHolder.court.setText(booking.courtReadable);
            viewHolder.venue.setText(booking.venueReadable);
            viewHolder.payment.setText(booking.paymentInfo);
            viewHolder.delete.setTag(booking);
            return view;
        }

        public class ViewHolder {
            TextView date;
            TextView court;
            TextView venue;
            TextView payment;
            TextView time;
            ImageButton delete;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
        connector.checkUpdate(new Callback<Connector.Result>() {
            @Override
            public void success(Connector.Result result, Response response) {
                if (result.success) {

                    int v;
                    try {
                        v = Integer.valueOf(result.message.replaceAll("\n", ""));
                    } catch (Exception ex) {return;}
                    PackageInfo pInfo = null;
                    try {
                        pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    int versionCode = pInfo.versionCode;
                    if (versionCode < v) {
                        SimpleAlertController.showConstructiveMessageWithHandler("New update found",
                                "New version found, do you want to update? You will be reminded until updated.",
                                "Update", ManageActivity.this, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String url = "http://aahung.github.io/cityu-sports-android/download/usports.apk";
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            }
                        });
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}
