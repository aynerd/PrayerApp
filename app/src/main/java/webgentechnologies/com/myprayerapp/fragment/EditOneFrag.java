package webgentechnologies.com.myprayerapp.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import webgentechnologies.com.myprayerapp.R;
import webgentechnologies.com.myprayerapp.model.CountryModel;
import webgentechnologies.com.myprayerapp.model.StateModel;
import webgentechnologies.com.myprayerapp.model.UserSingletonModelClass;
import webgentechnologies.com.myprayerapp.networking.UrlConstants;
import webgentechnologies.com.myprayerapp.networking.VolleyUtils;

/**
 * Created by Kaiser on 25-07-2017.
 */

public class EditOneFrag extends Fragment //implements TextWatcher
{

    View rootView;
    public static EditText txt_fname, txt_lname, txt_email, txt_addr1, txt_addr2, txt_city, txt_phone;
    Spinner spinner_state, spinner_country;

    /*
   *Taking variables and arraylist for spinner
    */

    public static String txt_editone_country_id1, txt_editone_country_name, txt_editone_country_sortname, txt_editone_state_id, txt_editone_state_name, txt_editone_country_id2;
    UserSingletonModelClass userclass = UserSingletonModelClass.get_userSingletonModelClass();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frag_edit_one, container, false);
        txt_fname = (EditText) rootView.findViewById(R.id.txt_fname);
        txt_lname = (EditText) rootView.findViewById(R.id.txt_lname);
        txt_email = (EditText) rootView.findViewById(R.id.txt_email);
        txt_addr1 = (EditText) rootView.findViewById(R.id.txt_addr1);
        txt_addr2 = (EditText) rootView.findViewById(R.id.txt_addr2);
        txt_city = (EditText) rootView.findViewById(R.id.txt_city);
        txt_phone = (EditText) rootView.findViewById(R.id.txt_phone);
        spinner_state = (Spinner) rootView.findViewById(R.id.spinner_state);
        spinner_country = (Spinner) rootView.findViewById(R.id.spinner_country_name);
        setCustomDesign();
        // addItemsOnStateSpinner();
        // setCustomClickListeners();
//        showPopUp();
        sendrequest_to_spinner();

        /*
        As Textwatcher(addTextChangedListener would not be used further that's why I have commented below codes
         */
      /*  txt_fname.addTextChangedListener(this);
        txt_lname.addTextChangedListener(this);
        // txt_email.addTextChangedListener(this);
        txt_addr1.addTextChangedListener(this);
        txt_addr2.addTextChangedListener(this);
        txt_city.addTextChangedListener(this);
        txt_phone.addTextChangedListener(this);*/
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
        //((TextView) rootView.findViewById(R.id.txt_email)).setTypeface(regular_font);

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
        txt_fname.setText(userclass.getTxt_fname());
        txt_lname.setText(userclass.getTxt_lname());
        txt_email.setText(userclass.getTxt_email());
        txt_addr1.setText(userclass.getTxt_addr1());
        txt_addr2.setText(userclass.getTxt_addr2());
        txt_city.setText(userclass.getTxt_city());
        txt_phone.setText(userclass.getTxt_phone());
        //volley request
    }
    //
//    private void setCustomClickListeners() {
////
//    }


    /*
  Volley code for spinner
   */
    public void sendrequest_to_spinner() {
        StringRequest stringRequest = new StringRequest(UrlConstants._URL_GET_COUNTRY_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showjson_to_spinner(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
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
        }

        final ArrayList<String> arraylist_country_name = new ArrayList<>();
        arraylist_country_name.add(countryModel.getCountry_name());

        ArrayList<String> arrayList_state_name = new ArrayList<>();
        for (StateModel temp_sModel :
                countryModel.getStateModelList()) {
            arrayList_state_name.add(temp_sModel.getState_name());
        }

        spinner_country.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item,arraylist_country_name ));
        spinner_state.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, arrayList_state_name));
        spinner_country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txt_editone_country_id1 = countryModel.getCountry_id();
                txt_editone_country_name = countryModel.getCountry_name();
                txt_editone_country_sortname = countryModel.getCountry_short_name();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txt_editone_state_id = countryModel.getStateModelList().get(position).getState_id();
                txt_editone_state_name = countryModel.getStateModelList().get(position).getState_name();
                txt_editone_country_id2 = countryModel.getStateModelList().get(position).getState_country_id();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
//----------------Volley code for spinner ends---------------------------


}
