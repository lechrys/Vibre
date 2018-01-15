package fr.myapplication.dc.myapplication.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.view.composer.VibrationSingleView;
import util.LoggerHelper;

/**
 * Created by Crono on 07/04/17.
 */

public class VibrationSingleFragment extends Fragment {

    VibrationSingleView singleView;
    LinearLayout globalLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerHelper.info(this.getClass().getName(),"IN onCreate");
        singleView = new VibrationSingleView(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LoggerHelper.info(this.getClass().getName(),"IN onCreateView");

        globalLayout = (LinearLayout) inflater.inflate(R.layout.empty,null);

        LinearLayout contentLayout = (LinearLayout) globalLayout.findViewById(R.id.contentLayout);

        singleView = new VibrationSingleView(getActivity());

        contentLayout.addView(singleView);

        return globalLayout;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LoggerHelper.info(this.getClass().getName(),"onDestroy");
        singleView.stopBackgroundTasks();
    }

    @Override
    public void onDestroyView(){
        super.onDestroy();
        LoggerHelper.info(this.getClass().getName(),"onDestroyView");
        singleView.stopBackgroundTasks();
    }

    @Override
    public void onPause() {
        super.onPause();
        LoggerHelper.info(this.getClass().getName(),"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        LoggerHelper.info(this.getClass().getName(),"onStop");
    }
}
