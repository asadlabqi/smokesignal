package edu.ohio_state.smokesignal;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KeyBankFragment.OnKeySharedListener} interface
 * to handle interaction events.
 * Use the {@link KeyBankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeyBankFragment extends Fragment {

    final private static String LOGTAG = "KeyBankFragment";

    private List<String> fileList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private Runnable update;
    private AlertDialog dialog;
    private String listItemName;

    private SecureRandom mSecureRandom;
    private byte[] keyStream = new byte[16];

    private OnKeySharedListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment KeyBankFragment.
     */
    public static KeyBankFragment newInstance(byte[] key) {
        KeyBankFragment fragment = new KeyBankFragment();
        Bundle args = new Bundle();
        if(key != null) {
            args.putByteArray("key", key);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public KeyBankFragment() {
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
        return inflater.inflate(R.layout.fragment_key_bank, container, false);
    }

    @Override
    public void onViewCreated(final View v, final Bundle savedInstanceState) {
        ListView keyBank = (ListView) v.findViewById(R.id.key_bank_list);

        if(savedInstanceState.getByteArray("key") == null) {
            addFile(savedInstanceState.getByteArray("key"));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.fragment_rename_dialog);
        builder.setMessage(R.string.rename)
                .setPositiveButton(R.string.submitrename, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText uInput = (EditText)  ((AlertDialog) dialog).findViewById(R.id.newname);
                        String newName = uInput.getText().toString();
                        rename(listItemName, newName);
                        uInput.setText("");
                        getActivity().runOnUiThread(update);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        dialog = builder.create();

        update = new Runnable() {
            @Override
            public void run() {
                arrayAdapter.notifyDataSetChanged();
            }
        };

        String path = getActivity().getFilesDir().getPath();
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i=0; i < file.length; i++)
        {
            fileList.add(i, file[i].getName());
        }

        // Create the ArrayAdapter to work with the ListView.
        arrayAdapter = new ArrayAdapter<>(
                getActivity().getApplicationContext(), R.layout.blacktestlist, fileList);
        keyBank.setAdapter(arrayAdapter);
        registerForContextMenu(keyBank);

        final Button generateButton = (Button) v.findViewById(R.id.generateButton);
        generateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSecureRandom = new SecureRandom();
                mSecureRandom.nextBytes(keyStream);
                addFile(keyStream);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.key_bank_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(fileList.get(info.position));
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int selected = item.getItemId();
        listItemName = fileList.get(info.position);

        if (selected == 0) {
            // The user selected Rename.
            dialog.show();
        } else if (selected == 1) {
            // The user selected Share.
            File dir = getActivity().getFilesDir();
            File file = new File(dir, listItemName);
            Uri keyUri = Uri.fromFile(file);
            mListener.OnKeyShared(keyUri);
        } else {
            // The user selected Delete.
            delete(listItemName);
        }

        getActivity().runOnUiThread(update);

        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnKeySharedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnKeySharedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*
        The user has decided to share a specific key in the NFC view. Send this key back to the
        activity and pass it on to Key Exchange.
    */
    public interface OnKeySharedListener {
        void OnKeyShared(Uri uri);
    }

    private boolean delete(String itemName) {
        File dir = getActivity().getFilesDir();
        File file = new File(dir, itemName);

        fileList.remove(itemName);
        boolean deleted = file.delete();

        Log.d(LOGTAG, "Delete Result - " + deleted);

        return deleted;
    }

    private void rename(String itemName, String newName) {
        File dir = getActivity().getFilesDir();
        File file = new File(dir, itemName);
        File newfile = new File(dir, newName);
        boolean result = file.renameTo(newfile);
        Log.d(LOGTAG, "Rename Result - " + result);

        fileList.remove(itemName);
        fileList.add(fileList.size() - 1, newName);
    }

    private void addFile(byte[] keyStream) {
        FileOutputStream outputStream;
        Context cxt = getContext();

        Calendar c = Calendar.getInstance();
        String filename = "KEY-" + c.get(Calendar.DATE) + "-" + c.get(Calendar.HOUR) + "-" + c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND);

        try {
            outputStream = cxt.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(keyStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add the file to the fileList.
        fileList.add(fileList.size() - 1, filename);
        getActivity().runOnUiThread(update);
    }

}
