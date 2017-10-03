package com.reelsonar.ibobber.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;
import com.reelsonar.ibobber.LoginActivity;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.model.Intro;
import com.reelsonar.ibobber.model.UserAuth.UserAuth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.text.TextUtils.isEmpty;
import static com.facebook.FacebookSdk.getApplicationContext;
import static com.reelsonar.ibobber.util.RestConstants.NETFISH_ADS_FLAG;
import static com.reelsonar.ibobber.util.RestConstants.NETFISH_PACKAGE;
import static com.reelsonar.ibobber.util.RestConstants.USER_INFO;


public class AppUtils {
    private static long animationDuration = 600;
    public static final String BASE_LIVE_URL = "https://netfish.reelsonar-services.com/api/v9/";
    public static final String BASE_TEST_URL = "http://netfish.reviewprototypes.com/api/v4/";


    public static final String CLIENT_ID = "c096eab751fd4446a07ddaf0eee668c2";
    public static final String CLIENT_SECRET = "ba6676e322a3439282079ddacfe79c48";
    public static final String REDIRECT_URI = "http://www.google.com";

    public static boolean IS_DEBUGGABLE = true;

    // TODO: 23/8/17 LOGIN STATUS
    public static final Integer USERTYPE_APP = 1;
    public static final Integer USERTYPE_FACEBOOK = 2;
    public static final Integer USERTYPE_TWITTER = 3;
    public static final Integer USERTYPE_INSTAGRAM = 4;

    // Device Type : iOS = 1 , android =  2
    public static final String DEVICE_TYPE = "2";

    public static void logout(Context context) {
        UserAuth auth = getUserInfo(context);
        String userType = auth.getData().getUserType();
        clearSharedPreference(context);
        if (userType.equalsIgnoreCase("1")) {

        } else if (userType.equalsIgnoreCase("2")) {
            LoginManager.getInstance().logOut();
        } else if (userType.equalsIgnoreCase("3")) {
            logOutOfTwitter(context);
        } else if (userType.equalsIgnoreCase("4")) {
            CookieManager cookieManager = CookieManager.getInstance();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.createInstance(context);
            }
            cookieManager.setAcceptCookie(true);
            cookieManager.removeAllCookie();
        }
        Intent in = new Intent(context, LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(in);
    }


    public static void logOutOfTwitter(Context ctx) {
        SharedPreferences sharedPrefs = ctx.getSharedPreferences(
                RestConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPrefs.edit();
        e.putString(RestConstants.PREF_KEY_TOKEN, null);
        e.putString(RestConstants.PREF_KEY_SECRET, null);
        e.commit();
    }

    //
    public static String SHA1(String text)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    protected static UserAuth getUserInfo(Context context) {
        UserAuth auth = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userInfoStr = prefs.getString(USER_INFO, "");
        auth = new Gson().fromJson(userInfoStr, UserAuth.class);
        return auth;
    }

    protected final void storeUserInfo(UserAuth auth, Context context) {
        String userInfoStr = new Gson().toJson(auth, UserAuth.class);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_INFO, userInfoStr);
        editor.apply();
    }

    public static AlertDialog showDialog(Context ctx, String title, String msg,
                                         String btn1, String btn2,
                                         OnClickListener listener1,
                                         OnClickListener listener2) {
        if (ctx != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(title);
            builder.setMessage(msg).setCancelable(true)
                    .setPositiveButton(btn1, listener1);
            if (btn2 != null) {
                builder.setNegativeButton(btn2, listener2);
            }
            AlertDialog alert = builder.create();
            alert.show();
            return alert;
        }
        return null;
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static void showToast(final Activity activity, final String message) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    View view =
                            LayoutInflater.from(activity).inflate(R.layout.custom_toast_layout, null);
                    TextView customToastText = (TextView) view.findViewById(R.id.tvCustomToast);
                    customToastText.setText(message);
                    Toast toast = new Toast(activity);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
                    toast.setView(view);
                    toast.show();
                }
            });
        }
    }

    public static void showToast(final Context activity, final String message) {
        if (activity != null) {
            View view =
                    LayoutInflater.from(activity).inflate(R.layout.custom_toast_layout, null);
            TextView customToastText = (TextView) view.findViewById(R.id.tvCustomToast);
            customToastText.setText(message);
            Toast toast = new Toast(activity);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
            toast.setView(view);
            toast.show();
        }
    }


    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        } else
            return false;
    }

    public static void logd(String name, String value) {
        Log.d(name, value);
    }

    public static String getText(TextView textView) {
        return textView.getText().toString().trim();
    }


    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(animationDuration);
        v.startAnimation(a);
    }

    public static void expandAnimation(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) + 200);
        v.startAnimation(a);
        v.requestLayout();
    }

//    public static void expand(final View v, final onExpandAnimationEnd animationEnd,
//                              final boolean showkeyBoard) {
//        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        final int targetHeight = (int) (getScreenHeight((Activity) v.getContext()));
//        v.getLayoutParams().height = 0;
//        v.setVisibility(View.VISIBLE);
//        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, targetHeight);
//        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                v.getLayoutParams().height = (int) animation.getAnimatedValue();
//                v.requestLayout();
//            }
//        });
//
//        valueAnimator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                if (animationEnd != null || showkeyBoard) {
//                    if (animationEnd != null) {
//                        animationEnd.onExpandAnimationEnd();
//                    }
//                }
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//        valueAnimator.setInterpolator(new DecelerateInterpolator());
//        valueAnimator.setDuration(animationDuration);
//        valueAnimator.start();
//    }
//
//    public static void collapse(final View v, final onExpandAnimationEnd animationEnd) {
//        if (v != null) {
//            final int targetHeight = v.getMeasuredHeight();
//            ValueAnimator valueAnimator = ValueAnimator.ofInt(targetHeight, 0);
//            valueAnimator.setInterpolator(new DecelerateInterpolator());
//            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    v.getLayoutParams().height = (int) animation.getAnimatedValue();
//                    v.requestLayout();
//                }
//            });
//
//            valueAnimator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animator) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animator) {
//                    if (animationEnd != null) {
//                        animationEnd.onCollapseAnimationEnd();
//                    }
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animator) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animator) {
//
//                }
//            });
//            valueAnimator.setInterpolator(new DecelerateInterpolator());
//            valueAnimator.setDuration(animationDuration);
//            valueAnimator.start();
//        }
//    }

    public static void startFromBottomToUp(Activity activity) {
        activity.overridePendingTransition(R.anim.trans_bottom_up, R.anim.no_animation);
    }

    public static void finishFromUpToBottom(Activity activity) {
        activity.overridePendingTransition(R.anim.no_animation, R.anim.trans_up_bottom);
    }

    public static void finishFromRightToLeft(Activity activity) {
        activity.overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    public static void hideKeyboard(Activity ctx) {
        if (ctx != null) {
            if (ctx.getCurrentFocus() != null) {
                InputMethodManager imm =
                        (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ctx.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    public static void openKeyboard(Activity activity, EditText editText) {
        if (activity != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(
                    editText.getApplicationWindowToken(),
                    InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getImeiNo(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static void openUrl(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "video/*");
        context.startActivity(Intent.createChooser(intent, "Complete action using"));
    }

    public static int getScreenWidth(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    public static int getScreenHeight(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }

    public static String getStringWithoutSpace(String originalString) {
        return originalString.replaceAll("\\s+", "");
    }

    public static int dpToPx(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static String getVideoDuration(long durationInMillis) {

        final int HOUR = 60 * 60 * 1000;
        final int MINUTE = 60 * 1000;
        final int SECOND = 1000;

        int durationHour = (int) (durationInMillis / HOUR);
        int durationMint = (int) ((durationInMillis % HOUR) / MINUTE);
        int durationSec = (int) ((durationInMillis % MINUTE) / SECOND);

        String durationMinLabel;
        String durationSecLabel;

        if (durationHour > 0) {
            if (durationMint < 10) {
                durationMinLabel = "0" + durationMint;
            } else {
                durationMinLabel = String.valueOf(durationMint);
            }
            if (durationSec < 10) {
                durationSecLabel = "0" + durationSec;
            } else {
                durationSecLabel = String.valueOf(durationSec);
            }
            return String.valueOf(durationHour)
                    + ":"
                    + String.valueOf(durationMinLabel)
                    + ":"
                    + String.valueOf(durationSecLabel);
        } else if (durationMint > 0) {
            if (durationSec < 10) {
                durationSecLabel = "0" + durationSec;
            } else {
                durationSecLabel = String.valueOf(durationSec);
            }
            return String.valueOf(durationMint) + ":" + String.valueOf(durationSecLabel);
        } else {
            if (durationSec < 10) {
                durationSecLabel = "0" + durationSec;
            } else {
                durationSecLabel = String.valueOf(durationSec);
            }
            return "0" + ":" + durationSecLabel;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {

        if (listView.getAdapter() == null) {
            return;
        }

        int totalHeight;
        int items = listView.getAdapter().getCount();
        int rows;

        View listItem = listView.getAdapter().getView(0, null, listView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();

        float x;

        x = items;
        rows = (int) (x);
        totalHeight *= rows;

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

//    public static boolean showNetFishAds(Context context) {
//        if (!AppUtils.isAppInstalled(context, NETFISH_PACKAGE)) {
//            if (getIntegerSharedpreference(context, NETFISH_ADS_FLAG) < NETFISH_REPEAT_ADS_COUNT) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
//    }

    /**
     * @param context
     * NETFISH_ADS_FLAG = 0 at initial , First time it redirect to netfish directly
     * and then next time redirect to NetFishAdsActivity.
     * @return Couunt of NETFISH_ADS
     */
    public static int showNetFishAds(Context context) {
        if (!AppUtils.isAppInstalled(context, NETFISH_PACKAGE)) {
            return getIntegerSharedpreference(context, NETFISH_ADS_FLAG);
        } else {
            return -1;
        }
    }

    // String value sharedpreference
    public static void storeSharedPreference(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // Integer value sharedpreference
    public static void storeSharedPreference(Context context, String key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    // Boolean value sharedpreference
    public static void storeSharedPreference(Context context, String key, Boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // Get String from SharedPreference
    public static String getStringSharedpreference(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, "");
    }

    public static Integer getIntegerSharedpreference(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(key, 0);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

//    public static String getUpdatedImageUrl(String imagePath, int width, int height, String fitType) {
//        String basePath = RestClient.IMAGE_BASE_Path;
//        basePath += imagePath + "&w=" + width + "&h=" + height + "&fit=" + fitType;
//        return basePath;
//    }
//
//    public static void showGuestUserDialog(final Activity activity) {
//        String title = activity.getString(R.string.guest_user);
//        String message = activity.getString(R.string.guest_user_message);
//        final GuestDialog guestDialog = new GuestDialog(activity, title, message, "", "");
//        guestDialog.setClickHandler(new GuestDialog.DialogClickHandler() {
//            @Override
//            public void positiveClick() {
//                guestDialog.dismiss();
//                AppUtils.clearNotifications();
//                Intent intent = new Intent(activity, LoginSignUpActivity.class);
//                activity.startActivity(intent);
//                activity.overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
//                activity.finish();
////                Intent intent = new Intent(activity, LoginSignUpActivity.class);
////                intent.putExtra(Constants.IS_GUEST_USER, true);
////                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                activity.startActivity(intent);
////                activity.overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
//
//            }
//
//            @Override
//            public void negativeClick() {
//                guestDialog.dismiss();
//            }
//        });
//        guestDialog.show();
//    }

//    public static void showReportCatchDialog(final Activity activity,
//                                             final OnReportCatchOptionSelection onReportCatchOptionSelection) {
//        final ReportCatchDialog reportCatchDialog = new ReportCatchDialog(activity);
//        reportCatchDialog.setClickHandler(new ReportCatchDialog.DialogClickHandler() {
//            @Override
//            public void positiveClick(String selectedOp, String otherText) {
//                onReportCatchOptionSelection.onReportCatchOptionSelect(selectedOp, otherText);
//                reportCatchDialog.dismiss();
//            }
//
//            @Override
//            public void negativeClick() {
//                reportCatchDialog.dismiss();
//            }
//        });
//        reportCatchDialog.show();
//    }

//    public static void showBlockUserDialog(final Activity activity, String title, final OnBlockOptionSelected onBlockOptionSelected) {
//        final BlockUserDialog blockUserDialog = new BlockUserDialog(activity, title);
//        blockUserDialog.setClickHandler(new BlockUserDialog.DialogClickHandler() {
//            @Override
//            public void positiveClick(String selectedOp) {
//                onBlockOptionSelected.onBlockOptionSelect(selectedOp);
//                blockUserDialog.dismiss();
//            }
//
//            @Override
//            public void negativeClick() {
//                blockUserDialog.dismiss();
//            }
//        });
//        blockUserDialog.show();
//    }

    public static void startFromRightToLeft(Context context) {
        ((Activity) context).overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    public static void FinishFromLeftToRight(Context context) {
        ((Activity) context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

//    public static boolean isGuestUser(Context context) {
//        String userData = getPrefs(context).getString(PreferenceConstants.USERDATA, "");
//        if (isEmpty(userData)) {
//            return true;
//        }
//        return false;
//    }
//
//    public static UserDataResponse getUserObject(Context context) {
//        UserDataResponse userDataResponse = null;
//        String userData = getPrefs(context).getString(PreferenceConstants.USERDATA, "");
//        if (!isEmpty(userData)) {
//            userDataResponse = new Gson().fromJson(userData, UserDataResponse.class);
//        }
//        return userDataResponse;
//    }
//
//    public static void clearRealmTables() {
//        Realm realm = Realm.getDefaultInstance();
//        realm.beginTransaction();
//        //realm.deleteAll();
//        realm.delete(PreviousSearch.class);
//        realm.delete(Baits.class);
//        realm.delete(Brands.class);
//        realm.delete(HardBaits.class);
//        realm.delete(JigRings.class);
//        realm.delete(SaltwaterHardbaits.class);
//        realm.delete(SaltwaterSoftbaits.class);
//        realm.delete(SoftBaits.class);
//        realm.delete(SwimBaits.class);
//
//        realm.commitTransaction();
//    }

    static String leftUnit, centerUnit, rightUnit;

//    public static void showWheelDialog(Context context, int wheelType, final TextView tvLabelView) {
//
//        View view = LayoutInflater.from(context).inflate(R.layout.wheel_picker_activity, null);
//        final Dialog mBottomSheetDialog = new Dialog(context, R.style.MyAnimation_Window);
//        mBottomSheetDialog.setContentView(view);
//        TextView tvDone = (TextView) view.findViewById(R.id.tvDone);
//        WheelPicker mainWheelLeft = (WheelPicker) view.findViewById(R.id.main_wheel_left);
//        WheelPicker mainWheelCenter = (WheelPicker) view.findViewById(R.id.main_wheel_center);
//        WheelPicker mainWheelRight = (WheelPicker) view.findViewById(R.id.main_wheel_right);
//
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        Window window = mBottomSheetDialog.getWindow();
//        lp.copyFrom(window.getAttributes());
//
//        lp.width = AppUtils.getScreenWidth((Activity) context);
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        window.setAttributes(lp);
//        //window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
//        mainWheelLeft.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(WheelPicker picker, Object data, int position) {
//                leftUnit = data.toString();
//            }
//        });
//
//        mainWheelCenter.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(WheelPicker picker, Object data, int position) {
//                centerUnit = data.toString();
//            }
//        });
//
//        mainWheelRight.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(WheelPicker picker, Object data, int position) {
//                rightUnit = data.toString();
//            }
//        });
//
//        List<String> leftWheelList = new ArrayList<>();
//        List<String> centerWheelList = new ArrayList<>();
//        List<String> rightWheelList = new ArrayList<>();
//        for (int i = 0; i <= 2000; i++) {
//            leftWheelList.add(String.valueOf(i));
//        }
//        for (int i = 0; i <= 99; i++) {
//            centerWheelList.add("." + String.valueOf(i));
//        }
//        if (wheelType > -1) {
//            if (wheelType == Constants.WHEEL_TYPE_LENGTH) {
//                String[] lengthArray = context.getResources().getStringArray(R.array.length_units);
//                for (int i = 0; i < lengthArray.length; i++) {
//                    rightWheelList.add(lengthArray[i]);
//                }
//            } else if (wheelType == Constants.WHEEL_TYPE_WEIGHT) {
//                String[] weightArray = context.getResources().getStringArray(R.array.weight_units);
//                for (int i = 0; i < weightArray.length; i++) {
//                    rightWheelList.add(weightArray[i]);
//                }
//            }
//            mainWheelLeft.setData(leftWheelList);
//            mainWheelCenter.setData(centerWheelList);
//            mainWheelRight.setData(rightWheelList);
//
//            leftUnit = leftWheelList.get(0);
//            centerUnit = centerWheelList.get(0);
//            rightUnit = rightWheelList.get(0);
//
//            mBottomSheetDialog.setCancelable(true);
//            mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
//            mBottomSheetDialog.show();
//            tvDone.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (!(leftUnit.equalsIgnoreCase("0") && centerUnit.equalsIgnoreCase("0"))) {
//                        String pickerValue = leftUnit + centerUnit + " " + rightUnit;
//                        tvLabelView.setText(pickerValue);
//                    }
//                    mBottomSheetDialog.dismiss();
//                }
//            });
//        }
//    }

    public static Bitmap getBitmapFromPosition(View shareView) {
        Bitmap bitmap;
        /*shareView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        shareView.layout(0, 0, shareView.getMeasuredWidth(), shareView.getMeasuredHeight());*/
        shareView.setDrawingCacheEnabled(true);
    bitmap =Bitmap.createBitmap(shareView.getDrawingCache());
        shareView.setDrawingCacheEnabled(false);
        return bitmap;
}

    public static void createVideoURL(Activity activity, String videoUrl) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(new File(videoUrl)));
        sendIntent.setType("video/*");
        activity.startActivity(Intent.createChooser(sendIntent, "Share With"));
    }

    public static int getHeight(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return view.getMeasuredHeight();
    }

    public static int getSoftButtonsBarHeight(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight) {
                return realHeight - usableHeight;
            } else {
                return 0;
            }
        }
        return 0;
    }

    public static void showSettingDialog(final Activity activity, String title) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startInstalledAppDetailsActivity(activity);
                    }
                }).create();
        dialog.show();
    }

    public static void startInstalledAppDetailsActivity(Activity activity) {
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + activity.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        activity.startActivity(i);
    }

    public static int getActionBarHeight(Context context) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    context.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    public static Long convertStringToTimestamp(String str_date) {

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            // you can change format of date
            Date date = formatter.parse(str_date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.getTimeInMillis() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getFormattedDate(String date) {
        String formattedBirthDate = "";
        if (!isEmpty(date)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(date) * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            formattedBirthDate = sdf.format(calendar.getTime());
        }
        return formattedBirthDate;
    }

    public static String getFormattedDate1(String date) {
        String formattedBirthDate = "";
        if (!isEmpty(date)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(date));
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            formattedBirthDate = sdf.format(calendar.getTime());
        }
        return formattedBirthDate;
    }

    public static Bitmap getWaterMarkedImage(Bitmap src, Bitmap watermark, Point location) {
        //get source image width and height
        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
        //create canvas object
        Canvas canvas = new Canvas(result);
        //draw bitmap on canvas
        canvas.drawBitmap(src, 0, 0, null);
        //create paint object
        Paint paint = new Paint();
        //        //apply color
        //        paint.setColor(color);
        //        //set transparency
        //        paint.setAlpha(alpha);
        //        //set text size
        //        paint.setTextSize(size);
        paint.setAntiAlias(true);
        //set should be underlined or not
        //paint.setUnderlineText(underline);
        //draw text on given location
        Log.e("x", String.valueOf(location.x));
        Log.e("y", String.valueOf(location.y));

        canvas.drawBitmap(watermark, location.x, location.y, paint);

        return result;
    }

    public static void clearNotifications() {
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(
                Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    public static void setTextNotNull(TextView textView, String text) {
        setTextNotNull(textView, text, "-");
    }

    public static void setTextNotNull(TextView textView, String text, String nullChar) {
        textView.setText(TextUtils.isEmpty(text) ? nullChar : text);
    }

    public static String getTempratureSymbol(Activity activity) {
        return " °F";
    }

    public static void showAlertDialog(final Activity activity, String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

//    public static boolean isSubscribed(Context context) {
//        UserDataResponse userDataResponse = AppUtils.getUserObject(context);
//        if (userDataResponse != null) {
//            InAppData inAppData = userDataResponse.getUserModel().getInAppData();
//            if (inAppData != null) {
//                return !isEmpty(inAppData.getActivation_date());
//            }
//        }
//        return false;
//    }

//    public static int getSubscriptionStatus(Context context) {
//        UserDataResponse userDataResponse = AppUtils.getUserObject(context);
//        if (userDataResponse != null) {
//            InAppData inAppData = userDataResponse.getUserModel().getInAppData();
//            if (inAppData != null) {
//                return inAppData.getSubscriptionStatus();
//            }
//        }
//        return 0;
//    }

    public static float distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float) (earthRadius * c);
    }

    public static int calculateDiffBetweenDate(long createdDate) {
        Calendar calendar = Calendar.getInstance();
        long currentDate = calendar.getTimeInMillis();
        long diffMillis = Math.abs(currentDate - createdDate);
        return (int) TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
    }

//    public static void showFreeSubscriptionDialog(Context context, int remainingFreeSubscriptionDays, final FreeSubscriptionListener freeSubscriptionListener) {
//        String title = context.getString(R.string.free_subscription);
//        String message;
//        remainingFreeSubscriptionDays = 7 - remainingFreeSubscriptionDays;
//        if (remainingFreeSubscriptionDays > 1)
//            message = "You can access this service only for " + remainingFreeSubscriptionDays + " days for free";
//        else
//            message = "You can access this service only for " + remainingFreeSubscriptionDays + " day for free";
//
//        final GuestDialog guestDialog = new GuestDialog(context, title, message, "", "");
//        guestDialog.setClickHandler(new GuestDialog.DialogClickHandler() {
//            @Override
//            public void positiveClick() {
//                guestDialog.dismiss();
//                freeSubscriptionListener.onFreeSubscriptionPositive();
//            }
//
//            @Override
//            public void negativeClick() {
//                guestDialog.dismiss();
//            }
//        });
//        guestDialog.show();
//    }

    public static String getWorkingDirectory() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "NetFish");
        if (!file.exists())
            file.mkdir();

        return file.getAbsolutePath() + File.separator;
    }

    public static String getCatchDirectory() {
        return String.valueOf(getApplicationContext().getCacheDir());
    }

    public static AnimationSet setFadeInFadeOutAnimation() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new LinearOutSlowInInterpolator());
        fadeIn.setDuration(600);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(600);
        scaleAnimation.setInterpolator(new LinearOutSlowInInterpolator());


        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(scaleAnimation);
        animation.addAnimation(fadeIn);
        return animation;
    }

    public static Drawable getBubbleDrawable(final Context context) {
        return new Drawable() {

            private static final int OFFSET = 15;
            int OFFSET_BOTTOM = 10;
            Paint whitePaint = new android.graphics.Paint();

            @Override
            public void setColorFilter(ColorFilter cf) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setAlpha(int alpha) {
                // TODO Auto-generated method stub

            }

            @Override
            public int getOpacity() {
                // TODO Auto-generated method stub
                return android.graphics.PixelFormat.OPAQUE;
            }

            @Override
            public void draw(@NonNull Canvas canvas) {
                Rect r = getBounds();
                Path path = new Path();
                Paint whitePaint = new Paint();

                path.moveTo(OFFSET, OFFSET);
                path.arcTo(new RectF(r.right - OFFSET * 2, OFFSET, r.right, OFFSET * 3), 270, 90);

                path.lineTo(r.width(), 0);

                path.lineTo(r.width(), r.height() - OFFSET_BOTTOM);

                path.arcTo(new RectF(r.right - OFFSET * 2, (r.bottom - OFFSET_BOTTOM) - OFFSET * 2, r.right, r.bottom - OFFSET_BOTTOM), 0, 90);

                path.lineTo((r.width() / 2) + 3, r.height() - OFFSET_BOTTOM);
                path.lineTo((r.width() / 2), r.height());

                path.lineTo((r.width() / 2), r.height());
                path.lineTo((r.width() / 2) - 3, r.height() - OFFSET_BOTTOM);

                path.arcTo(new RectF(0, (r.bottom - OFFSET_BOTTOM) - OFFSET * 2, OFFSET * 2, r.bottom - OFFSET_BOTTOM), 90, 90);


                path.lineTo(0, r.height() - OFFSET_BOTTOM);

                path.lineTo(0, 0);
                path.arcTo(new RectF(0, OFFSET, OFFSET * 2, OFFSET * 3), 180, 90);

                path.close();
                whitePaint.setColor(ContextCompat.getColor(context, R.color.dart_gray));
                whitePaint.setStyle(Paint.Style.FILL);
                whitePaint.clearShadowLayer();
                canvas.drawPath(path, whitePaint);
            }

        };

    }

    public static void moveImageToNetFishFolder(String srcDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(getWorkingDirectory() + "Uploads/", src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                for (String file : files) {
                    String src1 = (new File(src, file).getPath());
                    moveImageToNetFishFolder(src1);
                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

//    public static void makeTextViewResizable(final int position,
//                                             final TextView tv, final int maxLine, final String expandText, final boolean viewMore,
//                                             final OnCommentViewMoreClickLinear viewMoreClickLinear) {
//
//        if (tv.getTag() == null) {
//            tv.setTag(tv.getText());
//        }
//        ViewTreeObserver vto = tv.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//
//            @SuppressWarnings("deprecation")
//            @Override
//            public void onGlobalLayout() {
//
//                ViewTreeObserver obs = tv.getViewTreeObserver();
//                obs.removeGlobalOnLayoutListener(this);
//                if (maxLine == 0) {
//                    int lineEndIndex = tv.getLayout().getLineEnd(0);
//                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
//                    tv.setText(text);
//                    tv.setMovementMethod(LinkMovementMethod.getInstance());
//                    tv.setText(
//                            addClickablePartTextViewResizable(position, Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
//                                    viewMore, viewMoreClickLinear), TextView.BufferType.SPANNABLE);
//                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
//                    int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
//                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
//                    tv.setText(text);
//                    tv.setMovementMethod(LinkMovementMethod.getInstance());
//                    tv.setText(
//                            addClickablePartTextViewResizable(position, Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
//                                    viewMore, viewMoreClickLinear), TextView.BufferType.SPANNABLE);
//                } else {
//                    int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
//                    String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
//                    tv.setText(text);
//                    tv.setMovementMethod(LinkMovementMethod.getInstance());
//                    tv.setText(
//                            addClickablePartTextViewResizable(position, Html.fromHtml(tv.getText().toString()), tv, lineEndIndex, expandText,
//                                    viewMore, viewMoreClickLinear), TextView.BufferType.SPANNABLE);
//                }
//            }
//        });
//
//    }

//    public static SpannableStringBuilder addClickablePartTextViewResizable(final int position, final Spanned strSpanned, final TextView tv,
//                                                                           final int maxLine, final String spanableText, final boolean viewMore,
//                                                                           final OnCommentViewMoreClickLinear viewMoreClickLinear) {
//        String str = strSpanned.toString();
//        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);
//
//        if (str.contains(spanableText)) {
//
//
//            ssb.setSpan(new MySpannable(false) {
//                @Override
//                public void onClick(View widget) {
//                    if (viewMore) {
////                        tv.setLayoutParams(tv.getLayoutParams());
////                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
////                        tv.invalidate();
//                        // makeTextViewResizable(tv, -1, "", false);
//                    } else {
////                        tv.setLayoutParams(tv.getLayoutParams());
////                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
////                        tv.invalidate();
//                        // makeTextViewResizable(tv, 3, "...more", true);
//
//                    }
//                    viewMoreClickLinear.onCommentViewMoreClick(position);
//                }
//            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);
//
//        }
//        return ssb;
//
//    }

    public static String getDateString(long miliis, String mDateFormate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTimeInMillis(miliis);
        SimpleDateFormat dateFormatter = new SimpleDateFormat(mDateFormate, Locale.ENGLISH);
        return dateFormatter.format(calendar.getTime());
    }

    public static long getGMT(long millis) {
        return millis * 1000 - TimeZone.getDefault().getRawOffset() - TimeZone.getDefault().getDSTSavings();
    }

    public static String getDate(long milliSeconds,
                                 String dateFormat) {// Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        //  formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Create a calendar object that will convert the date and time value in millisecondsto date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getDateFromTime(long mTimestamp, String mDateFormate) {
        String date;
        SimpleDateFormat dateFormatter = new SimpleDateFormat(mDateFormate);
        dateFormatter.setTimeZone(TimeZone.getDefault());

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(mTimestamp);

        date = dateFormatter.format(cal.getTime());
        return date;
    }

    public static boolean moveCacheFile(File cacheFile, String internalStorageName) {

        boolean ret = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(cacheFile);

            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int read = -1;
            while ((read = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.close();
            fis.close();

            fos = new FileOutputStream(internalStorageName);
            baos.writeTo(fos);
            fos.close();

            // delete cache
            cacheFile.delete();

            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e(TAG, "Error saving previous rates!");
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
            }
        }

        return ret;
    }

    public static boolean isBadWordEntered(Context context, String string) {
        String[] androidStrings = context.getResources().getStringArray(R.array.bad_words);
        boolean isBadWordEntered = false;
        for (String s : androidStrings) {
            if (string.toLowerCase().trim().contains(s.toLowerCase())) {
                isBadWordEntered = true;
                break;
            }
        }
        return isBadWordEntered;
    }
//
//    public static String getShareImageUrl(String imageUrl) {
//        return RestClient.WATERMARK_URL + "watermark.php?url=" + imageUrl + "&mark=logo.png&markfit=fill&markh=150&markw=150&markpad=5";
//        //return "https://www.drawingtutorials101.com/drawing-tutorials/Anime-and-Manga/Shin-Chan/shin-chan-cartoon/how-to-draw-shin-chan.jpg";
//    }
//
//    public static void shareCatchOnBranchIo(final Context context, final String title, String description,
//                                            String shareImageUrl, String id, final boolean isFromCatch) {
//
//        final BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
//                .setCanonicalIdentifier("item/12345")
//                .setTitle(title)
//                .setContentDescription(description)
//                .setContentImageUrl(getShareImageUrl(shareImageUrl))
//                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC);
//        if (isFromCatch) {
//            branchUniversalObject.addContentMetadata("property1", "catch_id=" + id);
//            branchUniversalObject.addContentMetadata("property2", "red");
//        } else {
//            branchUniversalObject.addContentMetadata("property1", "post_id=" + id);
//            branchUniversalObject.addContentMetadata("property2", "red");
//        }
//
//        final LinkProperties linkProperties = new LinkProperties()
//                .setFeature("sharing")
//                .addControlParameter("$canonical_url", shareImageUrl)
//                .addControlParameter("$og_type", "application");
//        if (isFromCatch) {
//            linkProperties.addControlParameter("$data", "catch_id=" + id);
//            linkProperties.addControlParameter("$ios_deeplink_path", "open?Catch_id=" + id);
//        } else {
//            linkProperties.addControlParameter("$data", "post_id=" + id);
//            linkProperties.addControlParameter("$ios_deeplink_path", "open?Post_id=" + id);
//        }
//
//
//        branchUniversalObject.generateShortUrl(context,
//                linkProperties, new Branch.BranchLinkCreateListener() {
//                    @Override
//                    public void onLinkCreate(String url, BranchError error) {
//                        if (error == null) {
//                            Log.e("MyApp", "got my Branch link to share: " + url);
//                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//                            sharingIntent.setType("text/plain");
//                            /*sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri);*/
//                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
//                            if (isFromCatch)
//                                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Review my catch " + title + " in NetFish App. " + url);
//                            else
//                                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Review my post " + title + " in NetFish App. " + url);
//                            context.startActivity(Intent.createChooser(sharingIntent, "Share using"));
//                        }
//                    }
//                });
//    }
//
//    public static void fireGoogleAnalyticsScreen(Context context, String screenName) {
//        UserDataResponse userDataResponse = getUserObject(context);
//        Tracker tracker = BaseApplication.getInstance().getGoogleAnalyticsTracker();
//        if (tracker != null) {
//            if (userDataResponse != null && userDataResponse.getUserModel() != null)
//                tracker.set("&uid", userDataResponse.getUserModel().getUser_id());
//            tracker.setScreenName(screenName);
//            tracker.send(new HitBuilders.ScreenViewBuilder().build());
//        }
//    }

    public static void openAppOnPlayStore(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    public static void openAppOnPlayStore(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

//    public static String getGAClientID() {
//        String gaClientId = "";
//        Tracker tracker = BaseApplication.getInstance().getGoogleAnalyticsTracker();
//        if (tracker != null) {
//            Log.e("GA Tracker Id", tracker.get("&cid"));
//            gaClientId = tracker.get("&cid");
//        }
//        return gaClientId;
//    }

    public static String decodeString(String messgae) {
        try {
            byte[] data = Base64.decode(messgae, Base64.DEFAULT);
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encodeString(String message) {
        try {
            byte[] data = message.getBytes("UTF-8");

            return Base64.encodeToString(data, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void clearSharedPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().clear().commit();
    }

    public static ArrayList<Intro> getIntroList(Context context) {
        ArrayList<Intro> arrayList = new ArrayList<>();
        String[] intro_text = context.getResources().getStringArray(R.array.intro_screens_text);
        String[] intro_title = context.getResources().getStringArray(R.array.intro_screens_title);
        TypedArray intro_img = context.getResources().obtainTypedArray(R.array.intro_screens);
        int[] selected_num = context.getResources().getIntArray(R.array.intro_selected_number);

        for (int i = 0; i < intro_text.length; i++) {
            Intro introDetail = new Intro(intro_text[i], intro_title[i], intro_img.getResourceId(i, 0), selected_num[i]);
            arrayList.add(introDetail);
        }

        return arrayList;
    }

}
