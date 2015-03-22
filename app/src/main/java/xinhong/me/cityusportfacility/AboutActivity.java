package xinhong.me.cityusportfacility;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class AboutActivity extends ActionBarActivity {

    @InjectView(R.id.githubButton)
    ImageButton githubButton;
    @InjectView(R.id.feedbackButton)
    Button feedBackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.inject(this);
        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://aahung.github.io/cityu-sports-android/"));
                startActivity(intent);
            }
        });

        feedBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"app@xinhong.me"});
                i.putExtra(Intent.EXTRA_SUBJECT, "CityU Sport Facility Android Feedback");
                i.putExtra(Intent.EXTRA_TEXT, String.format("Version: %s\n", getVersion(AboutActivity.this)));
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(AboutActivity.this, "There are no email clients installed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public static String getVersion(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String versionName = pInfo.versionName;
        int versionCode = pInfo.versionCode;
        return String.format("%s (%d)", versionName, versionCode);
    }
}
