package com.wgt.myprayerapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.wgt.myprayerapp.R;
import com.wgt.myprayerapp.Utils.CustomUtils;
import com.wgt.myprayerapp.model.CountryModel;
import com.wgt.myprayerapp.model.StateModel;
import com.wgt.myprayerapp.model.UserSingletonModelClass;
import com.wgt.myprayerapp.networking.UrlConstants;
import com.wgt.myprayerapp.networking.VolleyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;

/**
 * Created by Kaiser on 25-07-2017.
 */

public class EditOneFrag extends Fragment implements View.OnTouchListener {

    public static EditText txt_fname, txt_lname, txt_email, txt_addr1, txt_addr2, txt_city, txt_phone;
    public static Spinner spinner_state, spinner_country;
    public static String txt_country_id1, txt_country_name, txt_country_shortname, txt_state_id, txt_state_name, txt_country_id2;
    View rootView;
    TextView nav_header_username;
    UserSingletonModelClass _userSingletonModelClass = UserSingletonModelClass.get_userSingletonModelClass();

    /*
   *Taking variables and arraylist for spinner
    */
    private boolean check = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frag_edit_one, container, false);
        rootView.setOnTouchListener(this);//to detect touch on non-views


        //_userSingletonModelClass.setList_classes_attended(0);

        txt_fname = rootView.findViewById(R.id.txt_fname);
        txt_lname = rootView.findViewById(R.id.txt_lname);
        txt_email = rootView.findViewById(R.id.txt_email);
        txt_addr1 = rootView.findViewById(R.id.txt_addr1);
        txt_addr2 = rootView.findViewById(R.id.txt_addr2);
        txt_city = rootView.findViewById(R.id.txt_city);
        txt_phone = rootView.findViewById(R.id.txt_phone);
        spinner_state = rootView.findViewById(R.id.spinner_state);
        spinner_country = rootView.findViewById(R.id.spinner_country_name);
        setCustomDesign();


        return rootView;
    }

    private void setCustomDesign() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Typeface regular_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Regular.ttf");

        ((TextView) rootView.findViewById(R.id.tv_regn_one)).setTypeface(regular_font);
        ((TextView) rootView.findViewById(R.id.tv_regn_one)).setText("Edit Profile");
        ((TextView) rootView.findViewById(R.id.tv_regn_step1)).setTypeface(regular_font);
        ((TextView) rootView.findViewById(R.id.tv_regn_step1)).setText("Step (1/3)");
        ((TextView) rootView.findViewById(R.id.txt_fname)).setTypeface(regular_font);
        ((TextView) rootView.findViewById(R.id.txt_lname)).setTypeface(regular_font);
        ((TextView) rootView.findViewById(R.id.txt_email)).setTypeface(regular_font);

//        ((TextView)findViewById(R.id.txt_country)).setTypeface(regular_font);
        ((TextView) rootView.findViewById(R.id.txt_addr1)).setTypeface(regular_font);
        ((TextView) rootView.findViewById(R.id.txt_addr2)).setTypeface(regular_font);
        // ((TextView)findViewById(R.id.txt_city)).setTypeface(regular_font);

        ((TextView) rootView.findViewById(R.id.txt_city)).setTypeface(regular_font);
        ((TextView) rootView.findViewById(R.id.txt_phone)).setTypeface(regular_font);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        load_ProfileDetails();
        addItemsOnCountrySpinner();

    }

    /*
  Volley code for spinner
   */
    public void addItemsOnCountrySpinner() {
        StringRequest stringRequest = new StringRequest(UrlConstants._URL_COUNTRY_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showjson_to_spinner(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        VolleyUtils.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    public void showjson_to_spinner(String response_str) {
        final CountryModel countryModel = new CountryModel();

        try {
            JSONObject jsonObject = new JSONObject(response_str);
            JSONObject jsonObject_country = jsonObject.getJSONObject("data");

            countryModel.setCountry_id(jsonObject_country.getString("id"));
            countryModel.setCountry_name(jsonObject_country.getString("name"));
            countryModel.setCountry_short_name(jsonObject_country.getString("sortname"));

            JSONArray jsonArrayStates = jsonObject_country.getJSONArray("state");
            for (int i = 0; i < jsonArrayStates.length(); i++) {
                JSONObject jsonObject_state = jsonArrayStates.getJSONObject(i);

                StateModel stateModel = new StateModel();
                stateModel.setState_id(jsonObject_state.getString("id"));
                stateModel.setState_name(jsonObject_state.getString("name"));
                stateModel.setState_country_id(jsonObject_state.getString("country_id"));

                countryModel.addStateModel(stateModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        final ArrayList<String> arraylist_country_name = new ArrayList<>();
        // arraylist_country_name.add("Select Country");
        arraylist_country_name.add(countryModel.getCountry_name());

        ArrayList<String> arrayList_state_name = new ArrayList<>();
        arrayList_state_name.add("Select State");
        final int[] pos_selected_state = {0};
        for (StateModel temp_sModel : countryModel.getStateModelList()) {

            if (temp_sModel.getState_name().equals(_userSingletonModelClass.getTxt_state_name()))
                pos_selected_state[0] = arrayList_state_name.size();

            arrayList_state_name.add(temp_sModel.getState_name());

        }
        try {
            spinner_country.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, arraylist_country_name));
            spinner_state.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, arrayList_state_name));
            spinner_country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    txt_country_id1 = countryModel.getCountry_id();
                    txt_country_name = countryModel.getCountry_name();
                    txt_country_shortname = countryModel.getCountry_short_name();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinner_country.setSelection(0);

            spinner_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        txt_state_id = "-1";
                        txt_state_name = "-1";
                        txt_country_id2 = "-1";
                    } else {
                        position -= 1;
                        txt_state_id = countryModel.getStateModelList().get(position).getState_id();
                        txt_state_name = countryModel.getStateModelList().get(position).getState_name();
                        txt_country_id2 = countryModel.getStateModelList().get(position).getState_country_id();
                    }

                    pos_selected_state[0] = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinner_state.setSelection(pos_selected_state[0]);
        } catch (Exception e) {
            //Toast.makeText(getActivity(), "Couldn't load countries", Toast.LENGTH_SHORT).show();
            return;
        }
    }
//----------------Volley code for spinner ends---------------------------

    void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideSoftKeyboard();
        return false;
    }

    public void load_ProfileDetails() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading data...");
        progressDialog.setCancelable(false);
        if (!(progressDialog.isShowing()))
            progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UrlConstants._URL_USER_REGISTRATION_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject job = new JSONObject(response);
                    String status = job.getString("status");

                    if (status.equals("true")) {
                        JSONObject jobdata = job.getJSONObject("data");
                        String access_token = jobdata.getString("access_token");
                        _userSingletonModelClass.setTxt_user_access_token(access_token);
                        JSONArray userjsonarray = jobdata.getJSONArray("user");
                        for (int i = 0; i < userjsonarray.length(); i++) {
                            JSONObject jobuser = userjsonarray.getJSONObject(i);

                            _userSingletonModelClass.setTxt_user_login_id(jobuser.getString("id"));
                            _userSingletonModelClass.setTxt_fname(jobuser.getString("first_name"));
                            _userSingletonModelClass.setTxt_lname(jobuser.getString("last_name"));
                            _userSingletonModelClass.setTxt_email(jobuser.getString("email"));

                            _userSingletonModelClass.setTxt_country_id(jobuser.getString("country_id"));
                            _userSingletonModelClass.setTxt_country(jobuser.getString("country_name"));
                            _userSingletonModelClass.setTxt_addr1(jobuser.getString("address1"));
                            _userSingletonModelClass.setTxt_addr2(jobuser.getString("address2"));
                            _userSingletonModelClass.setTxt_city(jobuser.getString("city"));
                            _userSingletonModelClass.setTxt_state_id(jobuser.getString("state_id"));
                            _userSingletonModelClass.setTxt_state_name(jobuser.getString("state_name"));
                            _userSingletonModelClass.setTxt_phone(jobuser.getString("phone"));
                            _userSingletonModelClass.setChurch_id(jobuser.getString("church_id"));

                            String classes_attended[] = jobuser.getString("classes").split(";");
                            List<String> classes_attended_list = new ArrayList<>();
                            for (String str :
                                    classes_attended) {
                                if (str.length() > 0)
                                    classes_attended_list.add(str);
                            }
                            if (classes_attended_list.size() > 0)
                                _userSingletonModelClass.setList_classes_attended(classes_attended_list);

                            _userSingletonModelClass.setTxt_mission_trip_countries(jobuser.getString("mission_trip"));
                            _userSingletonModelClass.setTxt_mission_trip_participation_status(jobuser.getString("mission_trip_status"));
                            _userSingletonModelClass.setTxt_newto_mission(jobuser.getString("mission_concept"));
                            load_ProfileDetails_afterVolley();
                        }
                    } else if (status.equals("false")) {
                        Toast.makeText(getContext(), "NO DATA UPDATED .", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (progressDialog.isShowing())
                    progressDialog.cancel();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing())
                    progressDialog.cancel();
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", _userSingletonModelClass.getTxt_user_login_id());
                params.put("access_token", _userSingletonModelClass.getTxt_user_access_token());

                return params;
            }
        };
        VolleyUtils.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    public void load_ProfileDetails_afterVolley() {
        _userSingletonModelClass.setProfileCompleted(true);//default

        txt_fname.setText(_userSingletonModelClass.getTxt_fname());
        txt_lname.setText(_userSingletonModelClass.getTxt_lname());
        txt_email.setText(_userSingletonModelClass.getTxt_email());

        String addr1 = _userSingletonModelClass.getTxt_addr1();
        if (addr1.equals(null) || addr1.length() == 0 || addr1.equals("null"))
            addr1 = "";

        String addr2 = _userSingletonModelClass.getTxt_addr2();
        if (addr2.equals(null) || addr2.length() == 0 || addr2.equals("null"))
            addr2 = "";

        String phone = _userSingletonModelClass.getTxt_phone();
        if (phone.equals(null) || phone.length() == 0 || phone.equals(null) || phone.equals("null"))
            phone = "";

        String country = _userSingletonModelClass.getTxt_country();
        if (country.equals(null) || country.length() == 0 || country.equals("null") || country.equals("Select Country"))
            country = "";

        //check for mandatory details
        String state = _userSingletonModelClass.getTxt_state_name();
        if (state.equals(null) || state.length() == 0 || state.equals("null") || state.equals("Select State") || state.equals("-1")) {
            state = " ";
            _userSingletonModelClass.setProfileCompleted(false);
        }
        String city = _userSingletonModelClass.getTxt_city();
        if (city.equals(null) || city.length() == 0 || city.equals("null")) {
            city = "";
            _userSingletonModelClass.setProfileCompleted(false);
        }

        String church_id = _userSingletonModelClass.getChurch_id();
        if (church_id.equals(null) || church_id.length() == 0 || church_id.equals("null") || church_id.equals("")) {
            church_id = "";

            _userSingletonModelClass.setProfileCompleted(false);
        }

        if (_userSingletonModelClass.getList_classes_attended().size() == 0) {
            _userSingletonModelClass.setProfileCompleted(false);

        }

        txt_addr1.setText(addr1);
        txt_addr2.setText(addr2);
        txt_city.setText(city);
        txt_phone.setText(phone);

        if (!_userSingletonModelClass.isProfileCompleted()) {
            CustomUtils.alert(getContext(), "Warning", "Please fill up all mandatory fields via Edit Profile Steps");
        }
//        addItemsOnCountrySpinner();
    }
}
