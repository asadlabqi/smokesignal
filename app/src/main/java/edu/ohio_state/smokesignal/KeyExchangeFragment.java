package edu.ohio_state.smokesignal;

import android.app.Activity;
import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.nfc.NdefRecord.createMime;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KeyExchangeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeyExchangeFragment extends Fragment implements NfcAdapter.CreateNdefMessageCallback {

    NfcAdapter mNfcAdapter;
    private Context mContext;
    private TextView mTextView;
    public boolean mSharing;
    private static File mFile;
    private byte[] keyStream;

    private static String LOGTAG = "KeyExchangeFragment";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment KeyExchangeFragment.
     */
    public static KeyExchangeFragment newInstance(Uri keyUri) {
        KeyExchangeFragment fragment = new KeyExchangeFragment();
        Bundle args = new Bundle();
        if(keyUri != null) {
            args.putString("uri", keyUri.getPath());
            Log.d(LOGTAG, "A key was shared at:" + args.getString("uri"));
            mFile = new File(keyUri.getPath());
            Log.d(LOGTAG, mFile.toString());
        }
        fragment.setArguments(args);
        return fragment;
    }

    public KeyExchangeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_key_exchange, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        mTextView = (TextView) v.findViewById(R.id.key_exchange_text);

        String line = "";

        try {
            FileInputStream fis = getContext().openFileInput(mFile.getName());
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            line = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        keyStream = line.getBytes();

        Log.d(LOGTAG, "Key Stream was: " + keyStream.toString());


        final Button shareKeyButton = (Button) v.findViewById(R.id.keyExchangeButton);
        shareKeyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSharing = true;

                mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
                if (mNfcAdapter == null) {
                    Log.d("Main Activity","NFC NOT AVAILABLE");
                    return;
                }
                // Register callback
                mNfcAdapter.setNdefPushMessageCallback(KeyExchangeFragment.this, getActivity());
                mTextView.setText(R.string.sharing_NFC);
            }
        });

    }

    //NFC METHODS
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        return new NdefMessage(
                new NdefRecord[] { createMime("application/edu.ohio_state.smokesignal", keyStream)});
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
        Activity activity = (Activity) context;
        ((MainActivity) activity).onSectionAttached(2);
        Log.d("KeyExchangeFragment", "IN ON ATTACH");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
