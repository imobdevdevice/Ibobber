package com.reelsonar.ibobber.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.reelsonar.ibobber.R;


/**
 * Created by Darshna Desai on 23/5/17.
 */

public class SimpleCustomDialog extends Dialog {

//    @BindView(R.id.tvDialogTitle)
    TextView tvDialogTitle;
//    @BindView(R.id.ivClose)
    TextView ivClose;
//    @BindView(R.id.dialog_message)
    TextView dialogMessage;
//    @BindView(R.id.txt_ok)
    TextView txtOk;
//    @BindView(R.id.dialog)
    RelativeLayout dialog;

    private Context mContext;

    public SimpleCustomDialog(Context context, String title, String message, String spannableText) {
        super(context, R.style.ThemeDialogCustomGuest);
        mContext = context;
        init(context, title, message, spannableText);
    }

    private void init(Context context, String title, String message, String spannableText) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_simple_custom, null);
//        DialogSimpleCustomBinding.

        tvDialogTitle.setText(title);
        dialogMessage.setText(message);
        if (!spannableText.equalsIgnoreCase("")) {
            dialogMessage.setMovementMethod(LinkMovementMethod.getInstance());
//            dialogMessage.setText(addClickablePart(message, spannableText), TextView.BufferType.SPANNABLE);
            dialogMessage.setText(message);


            Spannable spannable = new SpannableString(dialogMessage.getText());
            /*To change the specific font style to bold*/
            spannable.setSpan(new RelativeSizeSpan(1f)
                    , message.indexOf(spannableText)
                    , message.indexOf(spannableText)
                            + spannableText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            dialogMessage.setText(spannable);
        }
        setContentView(view);
        setCancelable(true);
    }

//    @OnClick({R.id.ivClose, R.id.txt_ok})
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.ivClose:
//                dismiss();
//                break;
//            case R.id.txt_ok:
//                dismiss();
//                break;
//        }
//    }

//    private Spannable addClickablePart(String str, String clickableText) {
//        Spannable ssb = new SpannableStringBuilder(str);
//        ssb.setSpan(new ClickableSpan() {
//                        @Override
//                        public void onClick(View widget) {
//                            showTermsPrivacyDialog();
//                            dismiss();
//                        }
//
//                        @Override
//                        public void updateDrawState(TextPaint ds) {
//                            // super.updateDrawState(ds);
//                            ds.setUnderlineText(true);
//                        }
//                    }, str.indexOf(clickableText)
//                , str.indexOf(clickableText) + clickableText.length()
//                , Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
//
//        return ssb;
//    }
//
//    private void showTermsPrivacyDialog() {
//        WebViewDialog webViewDialog = new WebViewDialog(getContext(), mContext.getString(R.string.terms_of_use), "file:///android_asset/terms_condition.html") {
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.ivClose:
//                        dismiss();
//                        break;
//                    case R.id.tvDialogNext:
//                        if (isPrivacyDialog()) {
//                            dismiss();
//                        } else {
//                            setNewContent(mContext.getString(R.string.privacy_policy_), "file:///android_asset/policy.html");
//                        }
//                        break;
//                }
//            }
//        };
//        webViewDialog.show();
//    }
}

