package com.wgt.myprayerapp.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.wgt.myprayerapp.R;
import com.wgt.myprayerapp.Utils.ValidatorUtils;
import com.wgt.myprayerapp.model.PostPrayerModelClass;
import com.wgt.myprayerapp.model.UserSingletonModelClass;
import com.wgt.myprayerapp.networking.UrlConstants;
import com.wgt.myprayerapp.networking.VolleyUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Kaiser on 25-07-2017.
 */

public class PostPrayerTextFrag extends Fragment implements View.OnClickListener, View.OnTouchListener {
    public static boolean valueofText = false;
    public static CallbackManager callbackManagertext;
    View rootView;
    TextView txt_overflow;
    ImageView img_overflow;
    int[] i = {0};
    UserSingletonModelClass _userSingletonModelClass = UserSingletonModelClass.get_userSingletonModelClass();
    PostPrayerModelClass postPrayerModelClass = new PostPrayerModelClass();
    EditText txtPrayer;
    ProgressDialog progressDialog;
    PopupMenu popup;
    SwitchCompat toggle_switch;
    LinearLayout linearLayout_btnFb;
    Button btn_post_prayer;
    ShareDialog shareDialog;
    private String prayer;
    private String receiver_email;
    private ImageView img_post_txt;
    private FacebookCallback<Sharer.Result> callback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result result) {
            Log.e("aaa@@", "Successfully posted");


            Toast.makeText(getContext(), "Successfully posted", Toast.LENGTH_SHORT).show();

            // Write some code to do some operations when you shared content successfully.
        }

        @Override
        public void onCancel() {
            Log.e("aaa@@", "User Denied");
            Toast.makeText(getContext(), "User Denied", Toast.LENGTH_SHORT).show();
            // Write some code to do some operations when you cancel sharing content.
        }

        @Override
        public void onError(FacebookException error) {
            Log.e("aaa@@", error.getMessage());
            Toast.makeText(getContext(), "Error :" + error, Toast.LENGTH_SHORT).show();
            // Write some code to do some operations when some error occurs while sharing content.
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frag_post_prayer_text, container, false);
        rootView.setOnTouchListener(this);//to detect touch on non-views

        txtPrayer = rootView.findViewById(R.id.txtPrayer);
        txt_overflow = rootView.findViewById(R.id.txt_overflow);
        img_post_txt = rootView.findViewById(R.id.img_post_txt);
        txt_overflow.setOnClickListener(this);
        img_overflow = rootView.findViewById(R.id.img_overflow);
        img_overflow.setOnClickListener(this);
        final FrameLayout frame_overflow = rootView.findViewById(R.id.frame_overflow);
        frame_overflow.setOnClickListener(this);

        btn_post_prayer = rootView.findViewById(R.id.btn_post_prayer);
        btn_post_prayer.setOnClickListener(this);
        linearLayout_btnFb = rootView.findViewById(R.id.linearLayout_btnFb);
        linearLayout_btnFb.setOnClickListener(this);

        FacebookSdk.sdkInitialize(getActivity());
        callbackManagertext = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(getActivity());
        shareDialog.registerCallback(callbackManagertext, callback);
        valueofText = true;
        PostPrayerAudioFrag.valueOfaudeo = false;
        PostPrayerVideoFrag.valueofvideo = false;


        toggle_switch = rootView.findViewById(R.id.toggle_switch);
        toggle_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv_OR = rootView.findViewById(R.id.tv_OR);
                linearLayout_btnFb = rootView.findViewById(R.id.linearLayout_btnFb);
                if (toggle_switch.isChecked()) {
                    toggle_switch.setText("Public");
                    //tv_OR.setVisibility(View.VISIBLE);
                    postPrayerModelClass.setAccessibility("Public");
                    linearLayout_btnFb.setVisibility(View.VISIBLE);
                    btn_post_prayer.setVisibility(View.GONE);
                } else {
                    toggle_switch.setText("Private");
                    //tv_OR.setVisibility(View.GONE);
                    postPrayerModelClass.setAccessibility("Private");
                    linearLayout_btnFb.setVisibility(View.GONE);
                    btn_post_prayer.setVisibility(View.VISIBLE);
                }
            }
        });

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

    @Override
    public void onClick(View v) {
        hideSoftKeyboard();
        int item = v.getId();
        switch (item) {
            case R.id.txt_overflow:
            case R.id.img_overflow:
            case R.id.frame_overflow:
                popup.show();//showing popup menu
                break;
            case R.id.btn_post_prayer:
            case R.id.linearLayout_btnFb:
                if (txtPrayer.getText().length() <= 10) {
                    txtPrayer.requestFocus();
                    txtPrayer.setError("Minimum 10 characters required for your prayer description.");
                    Toast.makeText(getContext(), "Minimum 10 characters required", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    prayer = txtPrayer.getText().toString().trim();
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

                                postTextPrayer();
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

    /*
       *Volley code for posting text prayer
        */
    public void postTextPrayer() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Posting Prayer...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UrlConstants._URL_POST_TEXT_PRAYER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (progressDialog.isShowing())
                    progressDialog.cancel();
                try {
                    JSONObject job = new JSONObject(response);
                    String status = job.getString("status");

                    if (status.equals("true")) {
//                        Toast.makeText(getActivity(), "Data posted successfully to database", Toast.LENGTH_SHORT).show();
                        txtPrayer.setText("");
                        if (postPrayerModelClass.getAccessibility().equals("Public")) {
                            Toast.makeText(getActivity(), "Opening Facebook...", Toast.LENGTH_SHORT).show();
                            postTextPrayerToFb();
                        }
                        if (postPrayerModelClass.getAccessibility().equals("Private")) {
                            Toast.makeText(getActivity(), "Posted Successfully.", Toast.LENGTH_SHORT).show();

                        }

                    } else
                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing())
                    progressDialog.cancel();
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", _userSingletonModelClass.getTxt_user_login_id());
                params.put("sender_name", _userSingletonModelClass.getTxt_fname() + " " + _userSingletonModelClass.getTxt_lname());
                params.put("sender_email", _userSingletonModelClass.getTxt_email());
                params.put("receiver_email", receiver_email);
                params.put("post_content", txtPrayer.getText().toString());
                params.put("post_description", txtPrayer.getText().toString());
                params.put("accessibility", postPrayerModelClass.getAccessibility());
                params.put("post_type", "Text");
                params.put("post_priority", postPrayerModelClass.getPost_priority());
                params.put("sender_access_token", _userSingletonModelClass.getTxt_user_access_token());
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yy h:mm a");
                String formattedDate1 = df1.format(c.getTime());
                params.put("created_date", formattedDate1);
                return params;
            }
        };
        VolleyUtils.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    //----------Volley code for posting text prayer ends------------

    private void postTextPrayerToFb() {
        // Create an object

        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                .putString("og:type", "books.book")
                .putString("og:title", "Text Prayer")
                .putString("og:description", prayer)
                .putString("books:isbn", "0-553-57340-3")
                .build();
        // Create an action
        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("books.reads")
                .putObject("book", object)
                .build();
        // Create the content
        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("book")
                .setAction(action)
                .build();
        shareDialog.show(content);


    }

    void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideSoftKeyboard();
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManagertext.onActivityResult(requestCode,
                resultCode, data);

    }

}



