package edu.ohio_state.smokesignal;

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

import java.security.SecureRandom;

import static android.nfc.NdefRecord.createMime;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KeyExchangeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KeyExchangeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeyExchangeFragment extends Fragment implements NfcAdapter.CreateNdefMessageCallback {

    NfcAdapter mNfcAdapter;
    private Context mContext;
    private TextView mTextView;
    public boolean mSharing;

    private static String LOGTAG = "KeyExchangeFragment";

    private OnFragmentInteractionListener mListener;

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

        final Button shareKeyButton = (Button) v.findViewById(R.id.keyExchangeButton);
        shareKeyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSharing = true;
                //mTextView.setText(getActivity().toString());

                mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
                if (mNfcAdapter == null) {
                    Log.d("Main Activity","NFC NOT AVAILABLE");
                    return;
                }
                // Register callback
                mNfcAdapter.setNdefPushMessageCallback(KeyExchangeFragment.this, getActivity());
                mTextView.setText("Sharing key via NFC");
            }
        });

    }

    //NFC METHODS
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        Log.d("KeyExchangeFragment", "IN ON ATTACH");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
