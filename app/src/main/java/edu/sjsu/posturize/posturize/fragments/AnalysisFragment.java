package edu.sjsu.posturize.posturize.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.sjsu.posturize.posturize.R;
import edu.sjsu.posturize.posturize.visualizations.GraphManager;

public class AnalysisFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_analysis, container, false);

        return rootView;
    }

    public static AnalysisFragment newInstance() {
        
        Bundle args = new Bundle();
        
        AnalysisFragment fragment = new AnalysisFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
