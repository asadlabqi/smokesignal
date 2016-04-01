package edu.ohio_state.smokesignal;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EncryptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EncryptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EncryptionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    byte[] text;
    byte[] cText;
    byte[] keyStream;

    private List<String> fileList = new ArrayList<>();

    //Array for converting byte array to printable format
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView mTextView;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EncryptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EncryptionFragment newInstance() {
        EncryptionFragment fragment = new EncryptionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public EncryptionFragment() {
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

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_encryption, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        final TextView messageView = (TextView) v.findViewById((R.id.inputView));
        final TextView inputView = (TextView) v.findViewById((R.id.input));
        final TextView outputView = (TextView) v.findViewById((R.id.output));
        final TextView decView = (TextView) v.findViewById((R.id.decView));

        // TODO: Consider just receiving a fileList from the KeyBank.
        String path = getActivity().getFilesDir().getPath();
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i=0; i < file.length; i++)
        {
            fileList.add(i, file[i].getName());
        }

        // Logic handling for the key selection dropdown.
        Spinner dropdown = (Spinner) v.findViewById(R.id.key_bank_dropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, fileList);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                String keyFile = fileList.get(position);
                String line = "";

                try {
                    FileInputStream fis = getContext().openFileInput(keyFile);
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing!

            }
        });

        // Encrypt the text in the EditText window.
        final Button encryptButton = (Button) v.findViewById(R.id.encryptButton);
        encryptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String plaintext = messageView.getText().toString();
                text = plaintext.getBytes();
                inputView.setText(bytesToHex(text));
                cText = encrypt(keyStream, text);
                outputView.setText(bytesToHex(cText));

            }
        });

        // Decrypt the text in the EditText window.
        final Button decryptButton = (Button) v.findViewById(R.id.decryptButton);
        decryptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                byte[] decrypted;
                decrypted = decrypt(keyStream, cText);
                decView.setText(bytesToHex(decrypted));
            }
        });


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

    private byte[] encrypt(byte[] keyStream, byte[] plaintext) {
        int i;
        int mLength = plaintext.length;
        byte[] ciphertext = new byte[mLength];

        for (i = 0; i < mLength ; i++) {
            ciphertext[i] = (byte) (plaintext[i]^keyStream[i]);
        }

        return ciphertext;
    }

    private byte[] decrypt(byte[] keyStream, byte[] ciphertext) {
        int i;
        int cLength = ciphertext.length;
        byte[] plaintext = new byte[cLength];

        for (i = 0; i < cLength ; i++) {
            plaintext[i] = (byte) (ciphertext[i]^keyStream[i]);
        }

        return plaintext;
    }

    //Converts byte array to a string of hex digits while keeping leading zeroes
    //Source: http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
