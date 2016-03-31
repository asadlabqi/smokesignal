package edu.ohio_state.smokesignal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.security.SecureRandom;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, NfcAdapter.CreateNdefMessageCallback
 {

     NfcAdapter mNfcAdapter;
     private String TAG = "Main Activity";
     private NavigationDrawerFragment mNavigationDrawerFragment;
     private CharSequence mTitle;
     private String payload;

    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Log.d("Main Activity","NFC NOT AVAILABLE");
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);

        Log.d("MainActivity", "nfc adapter success!!");

    }

     //NAVIGATION DRAWER
     @Override
     public void onNavigationDrawerItemSelected(int position) {
         // update the main content by replacing fragments
         FragmentManager fragmentManager = getSupportFragmentManager();

         Log.d("MA", "Position: " + position);

         Fragment fragment = PlaceholderFragment.newInstance(position + 1);
         if (position == 0) {
             fragment = EncryptionFragment.newInstance();
         }else if(position == 1){
             fragment = KeyExchangeFragment.newInstance();
         }else if(position == 2){
             // Messages Fragment
         }
         else if(position == 3){
             fragment = KeyBankFragment.newInstance();
         }
         else {
             // Settings Fragment
         }

         fragmentManager.beginTransaction()
                 .replace(R.id.container, fragment).commit();

     }

     public void onSectionAttached(int number) {
         switch (number) {
             case 1:
                 mTitle = getString(R.string.encryption);
                 break;
             case 2:
                 mTitle = getString(R.string.share_key);
                 break;
             case 3:
                 mTitle = getString(R.string.messages);
                 break;
             case 4:
                 mTitle = getString(R.string.settings);
                 break;
         }
     }

     public void restoreActionBar() {
         ActionBar actionBar = getSupportActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
         actionBar.setDisplayShowTitleEnabled(true);
         actionBar.setTitle(mTitle);
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

     @Override
     public NdefMessage createNdefMessage(NfcEvent event) {
         String text = ("Beam me up, Android!\n\n" +
                 "Beam Time: " + System.currentTimeMillis());
         NdefMessage msg = new NdefMessage(
                 new NdefRecord[] { createMime(
                         "application/edu.ohio_state.smokesignal", text.getBytes())
                         /**
                          * The Android Application Record (AAR) is commented out. When a device
                          * receives a push with an AAR in it, the application specified in the AAR
                          * is guaranteed to run. The AAR overrides the tag dispatch system.
                          * You can add it back in to guarantee that this
                          * activity starts when receiving a beamed message. For now, this code
                          * uses the tag dispatch system.
                          */
                         //,NdefRecord.createApplicationRecord("com.example.android.beam")
                 });
         return msg;
     }

     @Override
     public void onNewIntent(Intent intent) {
         // onResume gets called after this to handle the intent
         setIntent(intent);
     }

     /**
      * Parses the NDEF Message from the intent and prints to the TextView
      */
     void processIntent(Intent intent) {
         mTextView = (TextView) findViewById(R.id.key_exchange_text);
         Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                 NfcAdapter.EXTRA_NDEF_MESSAGES);
         // only one message sent during the beam
         NdefMessage msg = (NdefMessage) rawMsgs[0];
         // record 0 contains the MIME type, record 1 is the AAR, if present
         mTextView.setText(new String(msg.getRecords()[0].getPayload()));
     }

     @Override
     public void onResume() {
         super.onResume();
         // Check to see that the Activity started due to an Android Beam
         if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
             processIntent(getIntent());
         }
     }

     /**
      * A placeholder fragment containing a simple view.
      */
     public static class PlaceholderFragment extends Fragment {
         /**
          * The fragment argument representing the section number for this
          * fragment.
          */
         private static final String ARG_SECTION_NUMBER = "section_number";

         /**
          * Returns a new instance of this fragment for the given section
          * number.
          */
         public static PlaceholderFragment newInstance(int sectionNumber) {
             PlaceholderFragment fragment = new PlaceholderFragment();
             Bundle args = new Bundle();
             args.putInt(ARG_SECTION_NUMBER, sectionNumber);
             fragment.setArguments(args);
             return fragment;
         }

         public PlaceholderFragment() {
         }

         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_main, container, false);
             return rootView;
         }

         @Override
         public void onAttach(Activity activity) {
             super.onAttach(activity);
             ((MainActivity) activity).onSectionAttached(
                     getArguments().getInt(ARG_SECTION_NUMBER));
         }
     }

}

