/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brett.beam;

import android.app.Activity;
import android.content.Intent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brett.beam.Adapters.MessageAdapter;
import com.brett.beam.models.Messsage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class Beam extends Activity implements CreateNdefMessageCallback,
        OnNdefPushCompleteCallback {
    NfcAdapter mNfcAdapter;
    TextView mInfoText;
    private static final int MESSAGE_SENT = 1;
    private static final int PICK_IMAGE_REQUEST = 1;
    ListView listView;
    //adapter pour la listview
    MessageAdapter adapter;
    ArrayList<Messsage> listMessages;
    EditText etMessage;
    boolean isImage=false;
    Bitmap bitmap;
    MenuItem miMenuPhoto;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        etMessage=(EditText) findViewById(R.id.message);

       // Check for available NFC Adapter
       mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
          etMessage.setText("NFC is not available on this device.");
        }
        // Register callback to set NDEF message
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // Register callback to listen for message-sent success
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

        listMessages = new ArrayList<>();
        adapter = new MessageAdapter(this,listMessages);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }


    /**
     * Implementation for the CreateNdefMessageCallback interface
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        Time time = new Time();
        time.setToNow();
        String text ;
        NdefMessage msg;
        if(isImage) {
            text = ("une image" + "_" +
                    "Beam Time: " + time.format("%H:%M:%S"));
            listMessages.add(new Messsage("self", bitmap, time.format("%H:%M:%S")));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,baos);
            byte[] b = baos.toByteArray();
            msg = new NdefMessage(
                    new NdefRecord[] {
                            createMimeRecord("application/com.itransition.dolbik.android.beam", b),
                            createMimeRecord("application/com.example.android.beam", text.getBytes())

                    });
        }
        else {
            text = (etMessage.getText() + "_" +
                    "Beam Time: " + time.format("%H:%M:%S"));
            listMessages.add(new Messsage("self", etMessage.getText().toString(), time.format("%H:%M:%S")));
            msg = new NdefMessage(
                    new NdefRecord[] { createMimeRecord(
                            "application/com.example.android.beam", text.getBytes())

                    });
        }

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                etMessage.setText("");
            }
        });

        return msg;
    }

    /**
     * Implementation for the OnNdefPushCompleteCallback interface
     */
    @Override
    public void onNdefPushComplete(NfcEvent arg0) {
        // A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
    }

    /** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SENT:
                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
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
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        if(msg.getRecords().length>1){
            Bitmap bmp= BitmapFactory.decodeByteArray(msg.getRecords()[0].getPayload(), 0, msg.getRecords()[0].getPayload().length);
            Bitmap bitmapModifiable = bmp.copy(Bitmap.Config.ARGB_8888, true);
            String str=new String(msg.getRecords()[1].getPayload());
            String[] strTab=str.split("_");
            listMessages.add(new Messsage("Other",bitmapModifiable,strTab[1]));
        }
        else
        {
            String str=new String(msg.getRecords()[0].getPayload());
            String[] strTab=str.split("_");
            listMessages.add(new Messsage("Other",strTab[0],strTab[1]));
        }


        adapter.notifyDataSetChanged();
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     *
     * @param mimeType
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // If NFC is not available, we won't be needing this menu
        if (mNfcAdapter == null) {
            return super.onCreateOptionsMenu(menu);
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);

        miMenuPhoto = menu.findItem(R.id.menu_photo);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
                startActivity(intent);
                return true;
            case R.id.menu_photo:
                if(!isImage) {
                    Intent iPhoto = new Intent();
                    iPhoto.setType("image/*");
                    iPhoto.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(iPhoto, "Choisir une image"), PICK_IMAGE_REQUEST);
                }
                else
                {
                    isImage=false;
                    miMenuPhoto.setIcon(R.drawable.photo);
                    etMessage.setText("");
                    etMessage.setEnabled(true);

                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                isImage=true;
                try {
                    miMenuPhoto.setIcon(R.drawable.note);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                etMessage.setText("Image selectionnée");
                etMessage.setEnabled(false);
                //ImageView imageView = (ImageView) findViewById(R.id.imageView);
                //imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
