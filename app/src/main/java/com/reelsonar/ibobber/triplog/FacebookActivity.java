package com.reelsonar.ibobber.triplog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.reelsonar.ibobber.R;


public class FacebookActivity extends FragmentActivity {

    private Boolean postRequested = false;
    private ShareDialog shareDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        String postName = "";
        String postBody = "";

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            postName = bundle.getString(Intent.EXTRA_SUBJECT);
            postBody = bundle.getString(Intent.EXTRA_TEXT);
            postBody = postBody.replaceAll("\n",  "     ");
        }
        shareDialog = new ShareDialog(this);
        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                .putString("og:title", "Sample Course")
                .putString("og:description", "This is a sample course.")
                .build();
        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("fitness.runs")
                .putObject("fitness:course", object)
                .build();
        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("fitness:course")
                .setAction(action)
                .build();
        shareDialog.show(content);

    //        ShareLinkContent content = new ShareLinkContent.Builder()
//                .setContentUrl(Uri.parse("https://developers.facebook.com"))
//                .set
//                .build();

    //        FacebookDialog.ShareDialogBuilder shareDialogBuilder = new FacebookDialog.ShareDialogBuilder(this)
//            .setName(postName)
//            .setApplicationName("iBobber")
//            .setDescription(postBody)
//            .setLink("http://www.reelsonar.com");
//        FacebookDialog shareDialog = shareDialogBuilder.build();
//        shareDialog.present();

        postRequested = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (postRequested == true) finish();
    }

}
