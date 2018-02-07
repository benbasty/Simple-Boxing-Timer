package layout;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import wintersun.crane_timer.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static RelativeLayout settingsLayout, helpLayout;
    private static boolean isLayoutSettings = true;

    //spinners
    public Spinner roundSpinner, restSpinner;

    //variables
    int roundTime;
    int restTime;

    CheckBox CbEndRound, CbEndRest, CbHalfRound, CbInterval, CbVibrate;

    //list for spinners
    List<String> roundLengths;
    List<String> restLengths;

    //listadapters for spinners
    ArrayAdapter<String> mAdapter;
    ArrayAdapter<String> mAdapter2;

    //data passer to main activity
    OnDataPass dataPasser;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_settings, container, false);

        //find layouts
        settingsLayout = (RelativeLayout)view.findViewById(R.id.settings_layout);
        helpLayout = (RelativeLayout)view.findViewById(R.id.help_layout);

        helpLayout.setVisibility(View.GONE);

        //find checkboxes
        CbEndRound = (CheckBox)view.findViewById(R.id.checkBox_endRound);
        CbEndRest = (CheckBox)view.findViewById(R.id.checkBox_restWarning);
        CbHalfRound = (CheckBox)view.findViewById(R.id.checkBox_halfRound);
        CbInterval = (CheckBox)view.findViewById(R.id.checkBox_intervals);
        CbVibrate = (CheckBox)view.findViewById(R.id.checkBox_vibrate);

        //set onClickListener for checkboxes
        CbEndRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CbEndRound.isChecked())
                    passData("1ce");
                else
                    passData("0ce");
            }
        });

        CbEndRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CbEndRest.isChecked()){
                    passData("1cr");
                }
                else
                    passData("0cr");
            }
        });

        CbHalfRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CbHalfRound.isChecked())
                    passData("1ch");
                else
                    passData("0ch");
            }
        });

        CbInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CbInterval.isChecked())
                    passData("1ci");
                else
                    passData("0ci");
            }
        });

        CbVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CbVibrate.isChecked())
                    passData("1vib");
                else
                    passData("0vib");
            }
        });

        //find spinners
        roundSpinner = (Spinner)view.findViewById(R.id.spinner_roundtime);
        restSpinner = (Spinner)view.findViewById(R.id.spinner_resttime);

        //spinner drop down elements

        roundLengths = new ArrayList<String>(5);
        restLengths = new ArrayList<String>(3);

        roundLengths.add("1 minute");
        roundLengths.add("2 minutes");
        roundLengths.add("3 minutes");
        roundLengths.add("4 minutes");
        roundLengths.add("5 minutes");

        restLengths.add("30 seconds");
        restLengths.add("60 seconds");
        restLengths.add("90 seconds");

        //adapter for spinner

        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, roundLengths);

        mAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mAdapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, restLengths);

        mAdapter2.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //set adapters
        roundSpinner.setAdapter(mAdapter);
        restSpinner.setAdapter(mAdapter2);
        //set spinner listener
        roundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        roundTime = 60 * 1000;
                        passData("ro60");
                        break;
                    case 1:
                        roundTime = 120 * 1000;
                        passData("ro120");
                        break;
                    case 2:
                        roundTime = 180 * 1000;
                        passData("ro180");
                        break;
                    case 3:
                        roundTime = 240 * 1000;
                        passData("ro240");
                        break;
                    case 4:
                        roundTime = 300 * 1000;
                        passData("ro300");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        restSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        restTime = 30 * 1000;
                        passData("re30");
                        break;
                    case 1:
                        roundTime = 60 * 1000;
                        passData("re60");
                        break;
                    case 2:
                        roundTime = 90 * 1000;
                        passData("re90");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        dataPasser = (OnDataPass) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static boolean getIsLayoutSettings(){
        return isLayoutSettings;
    }

    public static void switchSettingsHelp(){
        if(isLayoutSettings){
            settingsLayout.setVisibility(View.GONE);
            helpLayout.setVisibility(View.VISIBLE);

            isLayoutSettings = false;
        }else if(!isLayoutSettings){
            settingsLayout.setVisibility(View.VISIBLE);
            helpLayout.setVisibility(View.GONE);

            isLayoutSettings = true;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    //interface to pass data
    public interface OnDataPass {
        public void onDataPass(String data);
    }
    //method using interface to pass data
    public void passData(String data) {
        dataPasser.onDataPass(data);
    }
}


