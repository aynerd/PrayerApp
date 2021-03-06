package com.wgt.myprayerapp.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.wgt.myprayerapp.R;
import com.wgt.myprayerapp.Utils.FileUtils;
import com.wgt.myprayerapp.Utils.ValidatorUtils;
import com.wgt.myprayerapp.model.PostPrayerModelClass;
import com.wgt.myprayerapp.model.UserSingletonModelClass;
import com.wgt.myprayerapp.networking.AndroidMultiPartEntity;
import com.wgt.myprayerapp.networking.UrlConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;

/**
 * Created by Kaiser on 25-07-2017.
 */

public class PostPrayerAudioFrag extends Fragment implements View.OnClickListener, View.OnTouchListener {
    public static final int RequestPermissionCode = 1;
    public static CallbackManager callbackManager;
    public static boolean valueOfaudeo = false;
    View rootView;
    ImageView audio_record;
    MediaRecorder mediaRecorder;
    UserSingletonModelClass userclass = UserSingletonModelClass.get_userSingletonModelClass();
    FileUtils fileUtils = new FileUtils();
    PostPrayerModelClass postPrayerModelClass = new PostPrayerModelClass();
    TextView txt_overflow;
    ImageView img_overflow;
    EditText txt_Prayer;
    Button btn_post_prayer;
    long totalSize = 0;
    String receiver_email;
    PopupMenu popup;
    TextView tv_audio_timer;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    ShareDialog shareDialog;
    boolean recording_status = false;
    private boolean sucess = false;
    private ProgressDialog progressDialog;
    private Uri fileUri; // file url to store image/video
    private long startHTime = 0L;
    private Handler customHandler = new Handler();
    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            if (tv_audio_timer != null)
                tv_audio_timer.setText("" + String.format("%02d", mins) + "m:"
                        + String.format("%02d", secs) + "s");
            customHandler.postDelayed(this, 0);
        }

    };
    private FacebookCallback<Sharer.Result> callback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result result) {
            Log.e("aaa@@", "Successfully posted");
            sucess = true;
            Toast.makeText(getContext(), "Successfully posted", Toast.LENGTH_SHORT).show();
            // Write some code to do some operations when you shared content successfully.
        }

        @Override
        public void onCancel() {
            Log.e("aaa@@", "Cancel occurred");
            Toast.makeText(getContext(), "User Denied", Toast.LENGTH_SHORT).show();
            // Write some code to do some operations when you cancel sharing content.
        }

        @Override
        public void onError(FacebookException error) {
            Log.e("aaa@@", error.getMessage());
            // Write some code to do some operations when some error occurs while sharing content.
        }
    };

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                FileUtils.FILE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
              /*  Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");*/
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
       /* if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } */
        if (type == MEDIA_TYPE_AUDIO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "AUDIO_" + timeStamp + ".mp3");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.frag_post_prayer_audio, container, false);
        rootView.setOnTouchListener(this);//to detect touch on non-views

        txt_Prayer = rootView.findViewById(R.id.txt_Prayer);
        tv_audio_timer = rootView.findViewById(R.id.audio_timer);
        audio_record = rootView.findViewById(R.id.audio_record);
        audio_record.setOnClickListener(this);

        FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(getActivity());
        shareDialog.registerCallback(callbackManager, callback);
        valueOfaudeo = true;
        PostPrayerTextFrag.valueofText = false;


        progressDialog = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        final SwitchCompat toggle_switch = rootView.findViewById(R.id.toggle_switch);
        toggle_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv_OR = rootView.findViewById(R.id.tv_OR);
                LinearLayout linearLayout_btnFb = rootView.findViewById(R.id.linearLayout_btnFb);
                if(toggle_switch.isChecked()){
                    toggle_switch.setText("Public");
                    postPrayerModelClass.setAccessibility("Public");
                    //tv_OR.setVisibility(View.VISIBLE);
                    linearLayout_btnFb.setVisibility(View.VISIBLE);
                    btn_post_prayer.setVisibility(View.GONE);
                }
                else{
                    toggle_switch.setText("Private");
                    postPrayerModelClass.setAccessibility("Private");
                    //tv_OR.setVisibility(View.GONE);
                    linearLayout_btnFb.setVisibility(View.GONE);
                    btn_post_prayer.setVisibility(View.VISIBLE);
                }
            }
        });

        txt_overflow = rootView.findViewById(R.id.txt_overflow);
        txt_overflow.setOnClickListener(this);
        img_overflow = rootView.findViewById(R.id.img_overflow);
        img_overflow.setOnClickListener(this);

        final FrameLayout frame_overflow = rootView.findViewById(R.id.frame_overflow);
        frame_overflow.setOnClickListener(this);

        btn_post_prayer = rootView.findViewById(R.id.btn_post_prayer);
        btn_post_prayer.setOnClickListener(this);
        LinearLayout linearLayout_btnFb = rootView.findViewById(R.id.linearLayout_btnFb);
        linearLayout_btnFb.setOnClickListener(this);

        postPrayerModelClass.setAccessibility("Private");
        postPrayerModelClass.setPost_priority("Medium");

        //Creating the instance of PopupMenu
        popup = new PopupMenu(rootView.getContext(), rootView.findViewById(R.id.img_overflow));
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.post_priority_menu, popup.getMenu());
        popup.getMenu().getItem(1).setChecked(true);
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                postPrayerModelClass.setPost_priority(item.getTitle().toString());
                item.setChecked(true);
                return true;
            }
        });
        return rootView;
    }

    private void resetTimer() {
        startHTime = 0L;
        customHandler = new Handler();
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updatedTime = 0L;
    }

    //------------------Code for audio recording----------------
    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(fileUri.getPath());
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (!StoragePermission && RecordPermission) {
                        Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getActivity(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onClick(View v) {
        hideSoftKeyboard();
        int item = v.getId();
        switch (item) {
            case R.id.audio_record:
                if (!recording_status) {
                    if (checkPermission()) {
                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_AUDIO);
                        fileUtils.setAudio_filepath(fileUri.getPath());
                        MediaRecorderReady();
                        try {
                            audio_record.setBackgroundResource(R.drawable.red_button);
                            resetTimer();
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                            recording_status = true;
                            startHTime = SystemClock.uptimeMillis();
                            customHandler.postDelayed(updateTimerThread, 0);
                            Toast.makeText(getContext(), "Recording...", Toast.LENGTH_SHORT).show();
                            btn_post_prayer.setEnabled(false);
                        } catch (IllegalStateException e) {
                            btn_post_prayer.setEnabled(true);
                            Toast.makeText(getContext(), "Err:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        } catch (IOException e) {
                            btn_post_prayer.setEnabled(true);
                            Toast.makeText(getContext(), "Err:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        btn_post_prayer.setEnabled(true);
                        Toast.makeText(getContext(), "Record permission failed.Please try again.", Toast.LENGTH_SHORT).show();
                        requestPermission();
                    }
                } else {
                    try {
                        mediaRecorder.stop();
                        recording_status = false;
                        audio_record.setBackgroundResource(R.drawable.mic);
                        timeSwapBuff += timeInMilliseconds;
                        customHandler.removeCallbacks(updateTimerThread);
                        Toast.makeText(getActivity(), "Recording Completed", Toast.LENGTH_SHORT).show();
                        btn_post_prayer.setEnabled(true);
                    } catch (Exception e) {
                        btn_post_prayer.setEnabled(true);
                        Toast.makeText(getContext(), "Couldn't stop.Please try stopping again. Err:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }


                break;
            case R.id.txt_overflow:
            case R.id.img_overflow:
            case R.id.frame_overflow:
                popup.show();//showing popup menu
                break;
            case R.id.btn_post_prayer:
            case R.id.linearLayout_btnFb:
                txt_Prayer.setError(null);
                if (txt_Prayer.getText().length() < 10) {
                    txt_Prayer.requestFocus();
                    txt_Prayer.setError("Minimum 10 characters required for your prayer description.");
                    return;
                } else if (tv_audio_timer.getText().toString().length() == 0 || tv_audio_timer.getText().toString().equals("00m:00s")) {
                    tv_audio_timer.requestFocus();
                    try {
                        File sourceFile = new File(fileUtils.getAudio_filepath());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Please record an audio", Toast.LENGTH_SHORT).show();
                    }
                    return;
                } else {
                    LayoutInflater li = LayoutInflater.from(getContext());
                    final View promptsView = li.inflate(R.layout.verify_email_dialog, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());// set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);
                    // set dialog message
                    alertDialogBuilder.setCancelable(false);
                    final AlertDialog alertDialog = alertDialogBuilder.create();// create alert dialog
                    alertDialog.show();

                    final TextView txt_title = promptsView.findViewById(R.id.tv_email_dialog_title);
                    txt_title.setText("Enter email id of Church Admin");
                    txt_title.setTextSize(15);
                    final EditText txt = promptsView.findViewById(R.id.txt_otp);
                    txt.setHint("Enter Email");
                    txt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                    Button btn_verify = promptsView.findViewById(R.id.btn_verify);
                    btn_verify.setText("Submit");
                    btn_verify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            receiver_email = txt.getText().toString();
                            txt.setError(null);

                            if (receiver_email.length() == 0)
                                txt.setError("Email can't be empty");
                            else if (!ValidatorUtils.isValidEmail(receiver_email))
                                txt.setError("Email Format is invalid");
                            else {
                                alertDialog.cancel();
                                new UploadAudioFileToServer().execute();
                            }
                        }
                    });

                    Button btn_back = promptsView.findViewById(R.id.btn_back);
                    btn_back.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.cancel();
                        }
                    });
                }
                break;
        }
    }

    /**
     * Method to show alert dialog
     */

    //------------------Json upload code ends------------------
    void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideSoftKeyboard();
        return false;
    }

    /**
     * Creating file uri to store image/video
     */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private void postAudioPrayerToFb(String responseLink) {
        if (ShareDialog.canShow(ShareLinkContent.class)) {

            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(responseLink))
                    .setQuote("Audio: " + txt_Prayer.getText().toString())
                    .build();
            shareDialog.show(content);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,
                resultCode, data);

    }

    /**
     * Uploading the file to server
     */
    private class UploadAudioFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            if (progressDialog != null)
                progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected String doInBackground(Void... params) {
            //  Toast.makeText(getActivity(), "Posted Successfully.", Toast.LENGTH_SHORT).show();
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(UrlConstants._URL_POST_AUDIO_PRAYER);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                entity.addPart("user_id", new StringBody(userclass.getTxt_user_login_id()));
                entity.addPart("sender_name", new StringBody(userclass.getTxt_fname() + " " + userclass.getTxt_lname()));
                entity.addPart("sender_email", new StringBody(userclass.getTxt_email()));
                entity.addPart("receiver_email", new StringBody(receiver_email));
                File sourceFile = new File(fileUtils.getAudio_filepath());

                // Adding file data to http body
                entity.addPart("audiofile", new FileBody(sourceFile));

                entity.addPart("post_description", new StringBody(txt_Prayer.getText().toString()));
                String accessibility = postPrayerModelClass.getAccessibility();
                entity.addPart("accessibility", new StringBody(accessibility));
                entity.addPart("post_type", new StringBody("Audio"));
                entity.addPart("post_priority", new StringBody(postPrayerModelClass.getPost_priority()));
                String accessToken = userclass.getTxt_user_access_token();
                entity.addPart("user_access_token", new StringBody(accessToken));
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yy h:mm a");
                String formattedDate1 = df1.format(c.getTime());
                entity.addPart("created_date", new StringBody(formattedDate1));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = "Err:" + e.toString();
            } catch (IOException e) {
                responseString = "Err:" + e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog.isShowing())
                progressDialog.cancel();
            if (result.startsWith("Err"))
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            else

            if (postPrayerModelClass.getAccessibility().equals("Public")) {
                Toast.makeText(getActivity(), "Opening Facebook...", Toast.LENGTH_SHORT).show();
                JSONObject job = null;
                try {
                    job = new JSONObject(result);
                    String response_url = job.getString("data");
                    postAudioPrayerToFb(response_url);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            if (postPrayerModelClass.getAccessibility().equals("Private")) {
                Toast.makeText(getContext(), "Successfully posted", Toast.LENGTH_SHORT).show();
            }
            tv_audio_timer.setText("");
            txt_Prayer.setText("");

            super.onPostExecute(result);

        }

    }

}


