package xinhong.me.cityusportfacility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by aahung on 3/21/15.
 */
public class SimpleAlertController {
    public static void showSimpleMessage(String title, String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setNeutralButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showSimpleMessageWithHandler(String title, String message, Context context, DialogInterface.OnClickListener handler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setNeutralButton("Ok", handler);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showDestructiveMessageWithHandler(String title, String message, String buttonText, Context context, DialogInterface.OnClickListener handler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setNegativeButton(buttonText, handler)
                .setNeutralButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showConstructiveMessageWithHandler(String title, String message, String buttonText, Context context, DialogInterface.OnClickListener handler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setNeutralButton("Cancel", null)
                .setPositiveButton(buttonText, handler);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
