package fr.myapplication.dc.myapplication.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.myapplication.dc.myapplication.view.composer.VibrationComposerView;

/**
 * Created by Crono on 07/04/17.
 */

public class VibrationComposerFragment extends Fragment{

    VibrationComposerView composerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        composerView = new VibrationComposerView(getActivity());
        return composerView;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        composerView.stopBackgroundTasks();
    }
}
