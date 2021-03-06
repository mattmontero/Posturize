package edu.sjsu.posturize.posturize.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.sjsu.posturize.posturize.R;
import edu.sjsu.posturize.posturize.visualizations.GraphManager;

/**
 * Fragment for the Graph View
 */

public class GraphFragment extends Fragment {

    static private GraphManager gm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_graph, container, false);

        gm = new GraphManager(rootView);
        gm.show();

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        gm.startObserving();
    }

    @Override
    public void onPause(){
        super.onPause();
        gm.stopObserving();
    }

    public static GraphFragment newInstance() {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

}
