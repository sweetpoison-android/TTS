package com.company.tts;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
TextToSpeech tts;
EditText et,et1;
Button bt;
Spinner speedSpinner,pitchSpinner;
String speed="normal";

TextView tv;
ImageView img;

   boolean flashlightstatus=false;  //use in flashlight function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = findViewById(R.id.edittext);
        et1=findViewById(R.id.edittext1);////////////voice search edittext
        bt = findViewById(R.id.button);
        tv=findViewById(R.id.voicetext);
        img=findViewById(R.id.imageview);
        speedSpinner=findViewById(R.id.spinner);

        loadSpinnerData();

        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                speed=adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getApplicationContext(),"you selected"+speed,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tts = new TextToSpeech(this, this);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //searchONyoutube();
                getVoiceInput();


            }
        });

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceoutput();
                setspeed();
            }
        });
    }

            @Override
            public void onInit(int i)
        {
             if(i==TextToSpeech.SUCCESS)
             {
                 int result=tts.setLanguage(Locale.ENGLISH);
                 if(result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED)
                 {
                     Toast.makeText(MainActivity.this,"Language not Supported",Toast.LENGTH_SHORT).show();
                 }
             else
                 {
                     bt.setEnabled(true);
                     voiceoutput();
                 }
             }
             else
             {
                 Toast.makeText(MainActivity.this,"Initialising Failed !", Toast.LENGTH_SHORT).show();
             }
            }
    @Override
    protected void onDestroy() {
        if(tts !=null)
        {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            ArrayList<String> ar = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String s=ar.get(0);
            tv.setText(s);

            ////////////// torch on and off    ///////////////////////////////////////////////

            boolean hascameraflash=getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if(ar.get(0).toString().equalsIgnoreCase("torch on"))
            {
                flashligh_on();
            }
            else if(ar.get(0).toString().equalsIgnoreCase("torch off"))
            {
               flashlight_OFF();
            }

            //  calling /////

            Uri readContactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            Cursor cursor = getContentResolver().query(readContactsUri, null, null, null, null);

            if(cursor!=null) {
                cursor.moveToFirst();

                // Loop in the phone contacts cursor to add each contacts in phoneContactsList.
                do {
                    // Get contact display name.
                    int displayNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    String userDisplayName = cursor.getString(displayNameIndex);

                    // Get contact phone number.
                    int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String phoneNumber = cursor.getString(phoneNumberIndex);

                    if (ar.get(0).toString().equalsIgnoreCase("call to "+userDisplayName)) {
                        Intent call = new Intent(Intent.ACTION_CALL);

                            call.setData(Uri.parse("tel:" + phoneNumber));

                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(getApplicationContext(), "please grant permission", Toast.LENGTH_LONG).show();
                            } else {
                                startActivity(call);
                            }
                        }



                }while (cursor.moveToNext());

            }



            /////////////  open app  ///////////////////////


            PackageManager manager=getPackageManager();

            Intent in=new Intent(Intent.ACTION_MAIN,null);
            in.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> availableactivities=manager.queryIntentActivities(in,0);
            for(ResolveInfo ri : availableactivities) {
                if(ar.get(0).toString().equalsIgnoreCase("open "+ri.loadLabel(manager).toString()))   //get app name
                {
                    try {
                        Intent openapp = getApplicationContext().getPackageManager().getLaunchIntentForPackage( ri.activityInfo.packageName);   //get package name
                        startActivity(openapp);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "App not found", Toast.LENGTH_LONG).show();
                    }
                }


            }



           /* if (ar.get(0).toString().equals("open camera")) {
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(camera);
            } else if (ar.get(0).toString().equals("open gallery")) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivity(gallery);
            } else if (ar.get(0).toString().equals("open setting")) {
                Intent setting = new Intent(Settings.ACTION_SETTINGS);
                startActivity(setting);
            }
            else if (ar.get(0).toString().equalsIgnoreCase("open facebook")) {
                Intent facebook = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
                startActivity(facebook);
            }*/


            if (ar.get(0).toString().equalsIgnoreCase("open contact")) {
                //Intent contact=new Intent(Intent.ACTION_PICK);   //for choose contact from whatsapp
                Intent contact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); //for choose contact from phone
                startActivity(contact);
            }


            //////////////////////////////////  search on browser  //////////////////////////////////////////////////////
            else if (ar.get(0).toString().equalsIgnoreCase("search"+s.substring((s.indexOf(" ")),s.indexOf("on"))+"on twitter")) {
                try {
                    String ss = "twitter://user?screen_name=" +  s.substring((s.indexOf(" ")),s.indexOf("on")) + "";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ss));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "app not found", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/search?q=" + et1.getText().toString() + "&src=typed_query")));
                }
            }

            else if (ar.get(0).toString().equalsIgnoreCase("search"+s.substring((s.indexOf(" ")),s.indexOf("on"))+"on youtube")) {

                String ss = "https://www.youtube.com/results?search_query=" + s.substring((s.indexOf(" ")),s.indexOf("on")) + "";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ss)));
            }

            else if (ar.get(0).toString().equalsIgnoreCase("search"+s.substring((s.indexOf(" ")),s.indexOf("on"))+"on google")) {
                String ss = "https://www.google.com/search?q="+  s.substring((s.indexOf(" ")),s.indexOf("on")) +"&oq=" +  s.substring((s.indexOf(" ")),s.indexOf("on")) + "&aqs=chrome.0.0l6.3964j0j8&sourceid=chrome&ie=UTF-8";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ss)));
            }

            else if (ar.get(0).toString().equalsIgnoreCase("search"+s.substring((s.indexOf(" ")),s.indexOf("on"))+"on facebook")) {
                String ss = "https://www.facebook.com/search/top/?q=" +  s.substring((s.indexOf(" ")),s.indexOf("on")) + "";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ss)));
                                /*try {
                                    getApplicationContext().getPackageManager().getPackageInfo("com.facebook.katana",0);
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://https://www.facebook.com/search/top/?q="+et1.getText().toString()+"")));
                                }catch (Exception e)
                                {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/search/top/?q="+et1.getText().toString()+"")));
                                }*/
            }

            /////////////////////////  Action     //////////////////////////

            /*else if(ar.get(0).toString().equalsIgnoreCase("call this"))
            {
                Intent call=new Intent(Intent.ACTION_CALL);
                String telnum=et1.getText().toString();
                /*if(telnum.trim().isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"please enter number",Toast.LENGTH_LONG).show();
                }
                else
                {
                    call.setData(Uri.parse("tel:"+telnum));

                if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(getApplicationContext(),"please grant permission",Toast.LENGTH_LONG).show();
                }
                else
                {
                    startActivity(call);
                }
            }*/

                    super.onActivityResult(requestCode, resultCode, data);
        }
    }

/////////////////////voice recognisation
    public  void voiceoutput()
    {
        CharSequence s=et.getText();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"id1");
        }
    }
    ///////////////voice recognisation
    public void getVoiceInput()
    {
        Intent in=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        in.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        in.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
        in.putExtra(RecognizerIntent.EXTRA_PROMPT,"what you want .........");
        try
            {
                startActivityForResult(in,1);
            }
        catch (ActivityNotFoundException e)
        {

        }
    }
    ///////////voice speed
    public void loadSpinnerData()
    {
        List<String> labels=new ArrayList<String>();
        labels.add("Very Slow");
        labels.add("Slow");
        labels.add("Normal");
        labels.add("Fast");
        labels.add("Very Fast");
        ArrayAdapter<String> ardp=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,labels);
        ardp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedSpinner.setAdapter(ardp);
    }
    public void setspeed()
    {
        if(speed.equalsIgnoreCase("Very Slow"))
        {
            tts.setSpeechRate(0.1f);
        }
        if(speed.equalsIgnoreCase("Slow"))
        {
            tts.setSpeechRate(0.5f);
        }
        if(speed.equalsIgnoreCase("Normal"))
        {
            tts.setSpeechRate(1.0f);
        }
        if(speed.equalsIgnoreCase("Fast"))
        {
            tts.setSpeechRate(1.5f);
        }
        if(speed.equalsIgnoreCase("Very Fast"))
        {
            tts.setSpeechRate(2.0f);
        }
    }




   /* public  void loadApps()
    {
        PackageManager manager;



        manager=getPackageManager();
        apps=new ArrayList<>();

        Intent in=new Intent(Intent.ACTION_MAIN,null);
        in.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableactivities=manager.queryIntentActivities(in,0);
        for(ResolveInfo ri : availableactivities)
        {
            Item app=new Item();
            app.label=ri.activityInfo.packageName;         //get app packagename
            app.name=ri.loadLabel(manager);          //  get app name
            app.icon=ri.loadIcon(manager);      //get app icon

            apps.add(app);
        }
    }*/

   ///////////////////          flasl light            ////////////////////////////////////////

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void flashligh_on() {
        /*CameraManager mngr=(CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraid=mngr.getCameraIdList()[0];
            mngr.setTorchMode(cameraid,true);
            flashlightstatus=true;
          //  img.setImageResource(R.drawable.ic_bulb_on);
            bt.setText("Touch for flash light oFF");
        }catch (Exception e)
        {

        }*/
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            CameraManager mngr=(CameraManager)getSystemService(Context.CAMERA_SERVICE);
            String cameraId=null;
            try {
                cameraId=mngr.getCameraIdList()[0];
                mngr.setTorchMode(cameraId,true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void flashlight_OFF()
    {
        /*CameraManager mngr=(CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraid=mngr.getCameraIdList()[0];
            mngr.setTorchMode(cameraid,false);
            flashlightstatus=false;
           // img.setImageResource(R.drawable.ic_bulb_off);
            bt.setText("Touch for flash light ON");
        }catch (Exception e)
        {

        }*/

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            CameraManager mngr=(CameraManager)getSystemService(Context.CAMERA_SERVICE);
            String cameraId=null;
            try {
                cameraId=mngr.getCameraIdList()[0];
                mngr.setTorchMode(cameraId,false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
