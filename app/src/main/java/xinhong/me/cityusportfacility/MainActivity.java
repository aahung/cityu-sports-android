package xinhong.me.cityusportfacility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.eid) EditText eidEdit;
    @InjectView(R.id.password) EditText passwordEdit;
    @InjectView(R.id.login) Button loginButton;
    @InjectView(R.id.githubButton) ImageButton githubButton;


    private Connector connector;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        SharedPreferences userInfo = getSharedPreferences("user", MODE_PRIVATE);
        setEid(userInfo.getString("eid", ""));
        setPassword(userInfo.getString("password", ""));
        eidEdit.addTextChangedListener(onTextChange);
        passwordEdit.addTextChangedListener(onTextChange);
        loginButton.setOnClickListener(tryLogin);
        USportApplication application = (USportApplication) getApplication();
        connector = new Connector(application.okClient);
        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://aahung.github.io/cityu-sports-android/"));
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_logout).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getEid() { return eidEdit.getText().toString(); }
    private void setEid(String value) { eidEdit.setText(value);}
    private String getPassword() { return passwordEdit.getText().toString(); }
    private void setPassword(String value) { passwordEdit.setText(value);}

    private View.OnClickListener tryLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!validateLogin()) {
                eidEdit.requestFocus();
                return;
            }
            progressDialog = ProgressDialog.show(MainActivity.this, null, "Request session");
            connector.requestSessionId(new Callback<Connector.Result>() {
                @Override
                public void success(Connector.Result result, Response response) {
                    if (result.success) {
                        connector.setSessionId(result.message);
                        progressDialog.setTitle("Log in");
                        login();
                    } else {
                        SimpleAlertController.showSimpleMessage("Oops", result.message, MainActivity.this);
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void failure(RetrofitError error) {}
            });
        }
    };

    private boolean validateLogin() {
        return (getEid().length() > 1 || getPassword().length() > 1);
    }

    private void login() {
        connector.login(getEid(), getPassword(), new Callback<Connector.Result>() {
            @Override
            public void success(Connector.Result result, Response response) {
                if (result.success) {
                    progressDialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, ManageActivity.class);
                    Bundle b = new Bundle();
                    b.putString("session", connector.getSessionId());
                    b.putString("eid", getEid());
                    b.putString("sid", result.message);
                    b.putString("password", getPassword());
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                } else {
                    SimpleAlertController.showSimpleMessage("Oops", result.message, MainActivity.this);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private TextWatcher onTextChange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            SharedPreferences userInfo = getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor edit = userInfo.edit();
            edit.clear();
            edit.putString("eid", getEid());
            edit.putString("password", getPassword());
            edit.commit();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (validateLogin()) {
            tryLogin.onClick(null);
        }
    }
}
