package fr.myapplication.dc.myapplication.activity.notification;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.data.model.VibrationMessage;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.view.adapter.MessageListAdapter;
import util.Constants;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by Crono on 10/08/16.
 * This class performs a vibration when notification is received
 * will eventually display some graphical representation of the vibrations
 */
public class NotificationReaderListActivity extends AppCompatActivity implements BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
    }

    @Override
    public void setViews() {
    }

    @Override
    public void setButtonsListeners() {
    }

}
