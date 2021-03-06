package edu.aku.hassannaqvi.nns2018_teamleadersapp.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.aku.hassannaqvi.nns2018_teamleadersapp.R;
import edu.aku.hassannaqvi.nns2018_teamleadersapp.contracts.FormsContract;
import edu.aku.hassannaqvi.nns2018_teamleadersapp.contracts.SerialContract;
import edu.aku.hassannaqvi.nns2018_teamleadersapp.contracts.VersionAppContract;
import edu.aku.hassannaqvi.nns2018_teamleadersapp.core.AndroidDatabaseManager;
import edu.aku.hassannaqvi.nns2018_teamleadersapp.core.DatabaseHelper;
import edu.aku.hassannaqvi.nns2018_teamleadersapp.core.MainApp;
import edu.aku.hassannaqvi.nns2018_teamleadersapp.databinding.ActivityEndingBinding;

public class MainActivity extends MenuActivity {

    private final String TAG = "MainActivity";

    String dtToday = new SimpleDateFormat("dd-MM-yy HH:mm").format(new Date().getTime());
    @BindView(R.id.adminsec)
    LinearLayout adminsec;
    @BindView(R.id.lblheader)
    TextView lblheader;
    @BindView(R.id.recordSummary)
    TextView recordSummary;
    @BindView(R.id.syncStatus)
    TextView syncStatus;

    @BindView(R.id.syncDevice)
    Button syncDevice;

    @BindView(R.id.spAreas)
    Spinner spAreas;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    AlertDialog.Builder builder;
    String m_Text = "";
    ProgressDialog mProgressDialog;
    ArrayList<String> lablesAreas;
    Map<String, String> AreasMap;
    VersionAppContract versionAppContract;
    private ProgressDialog pd;
    private Boolean exit = false;
    private String rSumText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        lblheader.setText("Welcome! You're assigned to block ' " + MainApp.userName);

        /*TagID Start*/
        sharedPref = getSharedPreferences("tagName", MODE_PRIVATE);
        editor = sharedPref.edit();

        builder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog dialog = builder.create();

        ImageView img = new ImageView(getApplicationContext());
        img.setImageResource(R.drawable.tagimg);
        img.setPadding(0, 15, 0, 15);
        builder.setCustomTitle(img);

        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.requestFocus();
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                if (!m_Text.equals("")) {
                    editor.putString("tagName", m_Text);
                    editor.commit();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        if (sharedPref.getString("tagName", null) == "" || sharedPref.getString("tagName", null) == null) {
            builder.show();
        }
        /*TagID End*/


//        Binding setting
        ActivityEndingBinding mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainBinding.setCallback(this);

        DatabaseHelper db = new DatabaseHelper(this);


        mainBinding.adminsec.setVisibility(View.VISIBLE);

        Collection<FormsContract> todaysForms = db.getTodayForms();
        Collection<FormsContract> unsyncedForms = db.getUnsyncedForms();

        rSumText += "TODAY'S RECORDS SUMMARY\r\n";

        rSumText += "=======================\r\n";
        rSumText += "\r\n";
        rSumText += "Total Forms Today: " + todaysForms.size() + "\r\n";
        rSumText += "\r\n";
        if (todaysForms.size() > 0) {
            rSumText += "\tFORMS' LIST: \r\n";
            String iStatus;
            rSumText += "--------------------------------------------------\r\n";
            rSumText += "[Cluster No.] \t[ House No. ] \t[Form Status] \t[Sync Status]----------\r\n";
            rSumText += "--------------------------------------------------\r\n";

            for (FormsContract fc : todaysForms) {
                if (fc.getIstatus() != null) {
                    switch (fc.getIstatus()) {
                        case "1":
                            iStatus = "\tComplete";
                            break;
                        case "2":
                            iStatus = "\tIncomplete";
                            break;
                        case "3":
                            iStatus = "\tRefused";
                            break;
                        case "4":
                            iStatus = "\tRefused";
                            break;
                        default:
                            iStatus = "\tN/A";
                    }
                } else {
                    iStatus = "\tN/A";
                }

                //rSumText += fc.getDSSID();

                rSumText += fc.getClusterNo() + " \t";
                rSumText += " " + fc.getHhNo() + " \t";
                rSumText += " " + iStatus + " \t";

                rSumText += (fc.getSynced().equals("") ? "\t\tNot Synced" : "\t\tSynced");
                rSumText += "\r\n";
                rSumText += "--------------------------------------------------\r\n";
            }
        }

        SharedPreferences syncPref = getSharedPreferences("SyncInfo", Context.MODE_PRIVATE);
        rSumText += "Last Data Download: \t" + syncPref.getString("LastDownSyncServer", "Never Updated");
        rSumText += "\r\n";
        rSumText += "Last Data Upload: \t" + syncPref.getString("LastUpSyncServer", "Never Synced");
        rSumText += "\r\n";
        rSumText += "\r\n";
        rSumText += "Unsynced Forms: \t" + unsyncedForms.size();
        rSumText += "\r\n";

        Log.d(TAG, "onCreate: " + rSumText);
        mainBinding.recordSummary.setText(rSumText);

//        Fill spinner

        /*lablesAreas = new ArrayList<>();
        AreasMap = new HashMap<>();
        lablesAreas.add("Select Area..");


        spAreas.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lablesAreas));

        spAreas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (spAreas.getSelectedItemPosition() != 0) {
                    MainApp.areaCode = Integer.valueOf(AreasMap.get(spAreas.getSelectedItem().toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/

        /*Add data in Serial date wrt date*/
        MainApp.sc = db.getSerialWRTDate(new SimpleDateFormat("dd-MM-yy").format(new Date()).toString());

        if (MainApp.sc.getDeviceid() == null) {
            db.addSerialForm(new SerialContract(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID),
                    new SimpleDateFormat("dd-MM-yy").format(new Date()).toString(),
                    "0"));

            MainApp.sc = db.getSerialWRTDate(new SimpleDateFormat("dd-MM-yy").format(new Date()).toString());
        }

//        Version Checking
        versionAppContract = db.getVersionApp();
        if (versionAppContract.getVersioncode() != null) {
            if (MainApp.versionCode < Integer.valueOf(versionAppContract.getVersioncode())) {
                mainBinding.lblAppVersion.setVisibility(View.VISIBLE);
                mainBinding.lblAppVersion.setText("New Version Available");
            } else {
                mainBinding.lblAppVersion.setVisibility(View.GONE);
                mainBinding.lblAppVersion.setText(null);
            }
        }

//        Testing visibility
        if (Integer.valueOf(MainApp.versionName.split("\\.")[0]) > 0) {
            mainBinding.testing.setVisibility(View.GONE);
        } else {
            mainBinding.testing.setVisibility(View.VISIBLE);
        }

    }

    public void openForm() {

//        if (spAreas.getSelectedItemPosition() != 0) {
        final Intent oF = new Intent(MainActivity.this, SectionA1Activity.class);

        if (sharedPref.getString("tagName", null) != "" && sharedPref.getString("tagName", null) != null && !MainApp.userName.equals("0000")) {
            //   startActivity(oF);
        } else {

            builder = new AlertDialog.Builder(MainActivity.this);
            ImageView img = new ImageView(getApplicationContext());
            img.setImageResource(R.drawable.tagimg);
            img.setPadding(0, 15, 0, 15);
            builder.setCustomTitle(img);

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);


            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text = input.getText().toString();
                    if (!m_Text.equals("")) {
                        editor.putString("tagName", m_Text);
                        editor.commit();

                        if (!MainApp.userName.equals("0000")) {
                            startActivity(oF);
                        }
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
//        } else {
//            Toast.makeText(getApplicationContext(), "Please select data from combobox!!", Toast.LENGTH_LONG).show();
//        }
    }








    /*public void openD(View v) {
        Intent iD = new Intent(this, SectionDActivity.class);
        startActivity(iD);
    }

    public void openE(View v) {
        Intent iE = new Intent(this, SectionEActivity.class);
        startActivity(iE);
    }

    public void openF(View v) {
        Intent iF = new Intent(this, SectionFActivity.class);
        startActivity(iF);
    }

    public void openG(View v) {
        Intent iG = new Intent(this, SectionGActivity.class);
        startActivity(iG);
    }

    public void openI(View v) {
        Intent iI = new Intent(this, SectionIActivity.class);
        startActivity(iI);
    }

    public void openJ(View v) {
        Intent iJ = new Intent(this, SectionJActivity.class);
        startActivity(iJ);
    }

    public void openK(View v) {
        Intent iK = new Intent(this, SectionKActivity.class);
        startActivity(iK);
    }

    public void openL(View v) {
        Intent iL = new Intent(this, SectionLActivity.class);
        startActivity(iL);
    }

    public void openM(View v) {
        Intent iM = new Intent(this, SectionMActivity.class);
        startActivity(iM);
    }


    public void openHA(View v) {
        Intent iB = new Intent(this, SectionHAActivity.class);
        startActivity(iB);
    }

    public void openHB(View v) {
        Intent iB = new Intent(this, SectionHBActivity.class);
        startActivity(iB);
    }*/


    public void testGPS(View v) {

        SharedPreferences sharedPref = getSharedPreferences("GPSCoordinates", Context.MODE_PRIVATE);
        Log.d("MAP", "testGPS: " + sharedPref.getAll().toString());
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("Map", entry.getKey() + ": " + entry.getValue().toString());
        }

    }

    public void updateApp(View v) {
        v.setBackgroundColor(Color.GREEN);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);
        alertDialogBuilder
                .setMessage("Are you sure to download new app??")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // this is how you fire the downloader
                                try {
                                    URL url = new URL(MainApp._UPDATE_URL + "app-debug.apk");
                                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                                    c.setRequestMethod("GET");
                                    c.setDoOutput(true);
                                    c.connect();

                                    String PATH = Environment.getExternalStorageDirectory() + "/download/";
                                    File file = new File(PATH);
                                    file.mkdirs();
                                    File outputFile = new File(file, "app.apk");
                                    FileOutputStream fos = new FileOutputStream(outputFile);

                                    InputStream is = c.getInputStream();

                                    byte[] buffer = new byte[1024];
                                    int len1 = 0;
                                    while ((len1 = is.read(buffer)) != -1) {
                                        fos.write(buffer, 0, len1);
                                    }
                                    fos.close();
                                    is.close();//till here, it works fine - .apk is download to my sdcard in download file

                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "app.apk")), "application/vnd.android.package-archive");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                } catch (IOException e) {
                                    Toast.makeText(getApplicationContext(), "Update error!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void openDB(View v) {
        Intent dbmanager = new Intent(getApplicationContext(), AndroidDatabaseManager.class);
        startActivity(dbmanager);
    }

    public void CheckCluster(View v) {

    }

    /*public void syncServer(View view) {

        // Require permissions INTERNET & ACCESS_NETWORK_STATE
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            DatabaseHelper db = new DatabaseHelper(this);
            syncStatus.setText(null);
            Toast.makeText(getApplicationContext(), "Syncing Forms", Toast.LENGTH_SHORT).show();
            new SyncAllData(
                    this,
                    "Forms",
                    "updateSyncedForms",
                    FormsContract.class,
                    MainApp._HOST_URL + FormsContract.FormsTable._URL,
                    db.getUnsyncedForms(), this.findViewById(R.id.syncStatus)
            ).execute();

            Toast.makeText(getApplicationContext(), "Syncing Family Members", Toast.LENGTH_SHORT).show();
            new SyncAllData(
                    this,
                    "Family Members",
                    "updateSyncedFamilyMembers",
                    FamilyMembersContract.class,
                    MainApp._HOST_URL + FamilyMembersContract.familyMembers._URL,
                    db.getUnsyncedFamilyMembers(), this.findViewById(R.id.syncStatus)
            ).execute();

            Toast.makeText(getApplicationContext(), "Syncing WRAs", Toast.LENGTH_SHORT).show();
            new SyncAllData(
                    this,
                    "WRAs",
                    "updateSyncedMWRAForm",
                    MWRAContract.class,
                    MainApp._HOST_URL + MWRAContract.MWRATable._URL,
                    db.getUnsyncedMWRA(), this.findViewById(R.id.syncStatus)
            ).execute();

            Toast.makeText(getApplicationContext(), "Syncing Children", Toast.LENGTH_SHORT).show();
            new SyncAllData(
                    this,
                    "Children",
                    "updateSyncedChildForm",
                    ChildContract.class,
                    MainApp._HOST_URL + ChildContract.ChildTable._URL,
                    db.getUnsyncedChildForms(), this.findViewById(R.id.syncStatus)
            ).execute();

            Toast.makeText(getApplicationContext(), "Syncing Eligibles", Toast.LENGTH_SHORT).show();
            new SyncAllData(
                    this,
                    "Eligibles",
                    "updateSyncedEligibles",
                    EligibleMembersContract.class,
                    MainApp._HOST_URL + EligibleMembersContract.eligibleMembers._URL,
                    db.getUnsyncedEligbleMembers(), this.findViewById(R.id.syncStatus)
            ).execute();

            Toast.makeText(getApplicationContext(), "Syncing Outcomes", Toast.LENGTH_SHORT).show();
            new SyncAllData(
                    this,
                    "Outcomes",
                    "updateSyncedOutcomeForm",
                    OutcomeContract.class,
                    MainApp._HOST_URL + OutcomeContract.outcomeTable._URL,
                    db.getUnsyncedOutcome(), this.findViewById(R.id.syncStatus)
            ).execute();

            Toast.makeText(getApplicationContext(), "Syncing Recepients", Toast.LENGTH_SHORT).show();
            new SyncAllData(
                    this,
                    "Recepients",
                    "updateSyncedRecepientsForm",
                    RecipientsContract.class,
                    MainApp._HOST_URL + RecipientsContract.RecipientsTable._URL,
                    db.getUnsyncedRecipients(), this.findViewById(R.id.syncStatus)
            ).execute();

            Toast.makeText(getApplicationContext(), "Syncing Nutrition", Toast.LENGTH_SHORT).show();
            new SyncAllData(
                    this,
                    "Nutrition",
                    "updateSyncedNutrition",
                    NutritionContract.class,
                    MainApp._HOST_URL + NutritionContract.NutritionTable._URL,
                    db.getUnsyncedNutrition(), this.findViewById(R.id.syncStatus)
            ).execute();

            Toast.makeText(getApplicationContext(), "Syncing Deceased", Toast.LENGTH_SHORT).show();
            new SyncAllData(
                    this,
                    "Deceased",
                    "updateSyncedDeceasedForm",
                    DeceasedContract.class,
                    MainApp._HOST_URL + DeceasedContract.DeceasedTable._URL,
                    db.getUnsyncedDeceasedMembers(), this.findViewById(R.id.syncStatus)
            ).execute();

            SharedPreferences syncPref = getSharedPreferences("SyncInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = syncPref.edit();

            editor.putString("LastUpSyncServer", dtToday);

            editor.apply();

        } else {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
        }

    }*/

    public void syncDevice(View view) {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            // Sync Random
            /*new GetBLRandom(this).execute();*/

            SharedPreferences syncPref = getSharedPreferences("SyncInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = syncPref.edit();

            editor.putString("LastDownSyncServer", dtToday);

            editor.apply();
        } else {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity

            startActivity(new Intent(this, LoginActivity.class));

        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }

}