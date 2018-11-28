package com.job.darasalecturer.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.job.darasalecturer.util.Constants.KEY_UUID;


/**
 * Created by Job on Friday : 8/3/2018.
 */
public class DoSnack {

    private Context context;
    private Activity activity;

    public DoSnack(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     * @deprecated (use the static method)
     */
    @Deprecated
    public void showSnackbar(final int mainTextStringId, final int actionStringId,
                             android.view.View.OnClickListener listener) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                activity.getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(activity.getString(actionStringId), listener).show();
    }

    /**
     * @deprecated (use the static method)
     * */
    @Deprecated
    public void showSnackbar(final String mainTextStringId, final String actionStringId,
                             View.OnClickListener listener) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                mainTextStringId,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionStringId, listener).show();
    }

    public static void showSnackbar(Activity activity, final String mainTextStringId, final String actionStringId,
                             View.OnClickListener listener) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                mainTextStringId,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionStringId, listener).show();
    }

    /**
     * @deprecated (use the static method)
     * */
    @Deprecated
    public void showSnackbar(final String mainTextStringId) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                mainTextStringId,
                Snackbar.LENGTH_INDEFINITE)
                .show();
    }

    public static void showSnackbar(Activity activity,final String mainTextStringId) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                mainTextStringId,
                Snackbar.LENGTH_INDEFINITE)
                .show();
    }

    public void showSnackbarDissaper(final String mainTextStringId, final String actionStringId,
                             View.OnClickListener listener) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                mainTextStringId,
                Snackbar.LENGTH_LONG)
                .setAction(actionStringId, listener).show();
    }

    /**
     * @deprecated (use the static method)
     * */
    @Deprecated
    public void showShortSnackbar(final String mainTextStringId) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                mainTextStringId,
                Snackbar.LENGTH_LONG)
                .show();
    }

    public static void showShortSnackbar(Activity activity,final String mainTextStringId) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                mainTextStringId,
                Snackbar.LENGTH_LONG)
                .show();
    }

    public static void UserAuthToastExceptions(Activity activity,@NonNull Task<AuthResult> authtask) {
        String error = "";
        try {
            throw authtask.getException();
        } catch (FirebaseAuthWeakPasswordException e) {
            error = "Weak Password!";
        } catch (FirebaseAuthInvalidCredentialsException e) {
            error = "Invalid email";
        } catch (FirebaseAuthUserCollisionException e) {
            error = "Existing Account";
        } catch (Exception e) {
            error = "Unknown Error Occured";
            e.printStackTrace();
        }
        //Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
        errorPrompt(activity,"Oops...", error);
    }

    public static void errorPrompt(Activity activity,String title, String message) {

        new SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .show();
    }

    public void errorPrompt(String title, String message) {

        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .show();
    }

    private String theDay(int day) {
        switch (day) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
                return "";
        }
    }

    private void dialogue(){
      /*  final SweetAlertDialog pDialog = new SweetAlertDialog(, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#FF5521"));
        pDialog.setTitleText("Saving Changes...");
        pDialog.setCancelable(false);

        pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();

            }
        });
        pDialog.show();*/
    }

    private void errorPrompt() {
        /*new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText("Something went wrong!")
                .show();*/
    }

    public static int setColor(Context context, @ColorRes int color){
        return ContextCompat.getColor(context, color);
    }

    public static Drawable setDrawable(Context context, @DrawableRes int drawable){
        return ContextCompat.getDrawable(context, drawable);
    }

    /**
     * Creates a UUID and saves it to {@link SharedPreferences}. The UUID is added to the published
     * message to avoid it being undelivered due to de-duplication. See {@link DeviceMessage} for
     * details.
     */
    public static String getUUID(SharedPreferences sharedPreferences) {
        String uuid = sharedPreferences.getString(KEY_UUID, "");
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(KEY_UUID, uuid).apply();
        }
        return uuid;
    }
}
