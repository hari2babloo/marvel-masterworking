package ServiceAPI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.opencv.cultoftheunicorn.marvel.R;

/**
 * Created by kumarkis on 03/27/2018.
 */
public class AlertUtil {

    public static void showDialog(Context context, String title, String message, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setIcon(R.drawable.applogog);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton("OK", onClickListener);
        //dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        dialog.show();
    }
}
