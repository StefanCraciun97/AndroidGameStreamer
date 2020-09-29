package com.example.streamerclientv2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class MachineListActivity extends AppCompatActivity {



    Thread tcpClient;
    String serverIP;
    String selectedMachineIP;
    final private int LAUNCH_STREAMING_ACTIVITY = 2;



    private class CustomAdapter extends BaseAdapter {

        Context context;
        ArrayList<Database.DatabaseRecord> data;
        private LayoutInflater inflater = null;
        public ImageView isOnline;


        public CustomAdapter(Context context, ArrayList<Database.DatabaseRecord> data){

            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null)
                view = inflater.inflate(R.layout.machine_list_item, null);
            TextView textMachineName = (TextView) view.findViewById(R.id.text11);
            TextView textMachineIP = (TextView) view.findViewById(R.id.text12);
            isOnline = (ImageView) view.findViewById(R.id.is_online);
            // remove quotes from string and add a few spaces in the beginning
            // it looks better this way
            textMachineName.setText("   "  + data.get(position).getMachine_name().replace("\"", ""));
            textMachineIP.setText("    "  + data.get(position).getCurrent_IP().replace("\"", ""));


            // set the image that shows whether machine is online or not
            if(data.get(position).isOnline().equals("1"))
                isOnline.setImageResource(R.drawable.img_online);
            else
                isOnline.setImageResource(R.drawable.img_offline);


            return view;
        }
    }




    private class AppsCustomAdapter extends BaseAdapter{

        Context context;
        ArrayList<String> data;
        private LayoutInflater inflater = null;
        private ImageView gamepadImage;

        public AppsCustomAdapter(Context context, ArrayList<String>data){

            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if(view == null)
                view = inflater.inflate(R.layout.app_list_item, null);
            TextView textAppName = (TextView) view.findViewById(R.id.text10);

            // remove quotes and .lnk extension from string
            // it looks better this way

            String contents = data.get(position).replace("\"", "");
            contents = contents.replace(".lnk", "");
            textAppName.setText("  " + contents);
            return view;
        }

    }











    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_list);

//        final EditText machineIP = (EditText) findViewById(R.id.machine_ip);
//        Button addMachine = (Button) findViewById(R.id.save_machine);


        final ListView machineList = (ListView) findViewById(R.id.machine_list);
//        TextView header = new TextView(this);
//        header.setText("merge");
//        machineList.addHeaderView(header);

        // ListView setup stuff
        final ArrayList<Database.DatabaseRecord> machines = new ArrayList<>();

//        final ArrayAdapter<Database.DatabaseRecord> arrayAdapter = new ArrayAdapter<Database.DatabaseRecord>(
//                this, android.R.layout.simple_list_item_activated_1, machines);
        final CustomAdapter arrayAdapter = new CustomAdapter(this,machines);
        machineList.setAdapter(arrayAdapter);


        final ListView appList = (ListView) findViewById(R.id.app_list);

        // ListView setup stuff again
        final ArrayList<String> apps = new ArrayList<>();
        //final ArrayAdapter<String> arrayAdapterApps = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, apps);
        final AppsCustomAdapter arrayAdapterApps = new AppsCustomAdapter(this, apps);
        appList.setAdapter(arrayAdapterApps);




        int userID = getIntent().getIntExtra("userID",-1);
        serverIP = getIntent().getStringExtra("serverIP");

        Log.d("uid", Integer.toString(userID));



        // create a new TCP connection to receive machine table
        final TCPclient tcpClientRunnable = new TCPclient(this,serverIP, 20001);
        tcpClient = new Thread(tcpClientRunnable);
        tcpClient.start();

        // download and show machine table
        Database d = tcpClientRunnable.getCurrentUserTable();
        for(Database.DatabaseRecord i: d.getAllRows())
            machines.add(i);
        arrayAdapter.notifyDataSetChanged(); // didn't update the ListView without this


        machineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                // when we click a machine, tell CentralServer what machine we selected
                // only if machine is reported to be online by the server

                if (((Database.DatabaseRecord) machineList.getItemAtPosition(position)).isOnline().equals("1")){
                    tcpClientRunnable.sendMessage("select_machine");
                    tcpClientRunnable.sendMessage(((Database.DatabaseRecord) machineList.getItemAtPosition(position)).getMachine_id());

                    //Toast.makeText(getApplicationContext(), ((Database.DatabaseRecord) machineList.getItemAtPosition(position)).getMachine_id(), Toast.LENGTH_SHORT).show();

                    // get list of apps available on Windows machine and show it in second ListView
                    tcpClientRunnable.sendMessage("app_list");
                    String appListString = tcpClientRunnable.recvMessage();
                    if( appListString.equals("error_machine_went_offline")){ // this is what server says if WindowsClient crashed on selected machine
                        Toast.makeText(getApplicationContext(), "Machine went offline in the meantime", Toast.LENGTH_SHORT).show();
                        return;
                        // should change green marker to green one
                    }


                    selectedMachineIP = ((Database.DatabaseRecord) machineList.getItemAtPosition(position)).getCurrent_IP().replace("\"", "");

                    for (String i : appListString.split(","))
                        apps.add(i);
                    arrayAdapterApps.notifyDataSetChanged();
                }

                else{
                    Toast.makeText(getApplicationContext(), "Selcted machine is offline", Toast.LENGTH_SHORT).show();
                }



            }
        });


        appList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // when user selects an app, tell server to open it on WindowsMachine
                tcpClientRunnable.sendMessage("start");
                tcpClientRunnable.sendMessage((String) appList.getItemAtPosition(position));

                String response = tcpClientRunnable.recvMessage();

                if (response.equals("error_machine_went_offline")){
                    Toast.makeText(getApplicationContext(), "Machine went offline in the meantime", Toast.LENGTH_SHORT).show();
                    return;
                }


//                Intent intent = new Intent(getApplicationContext(), StreamingActivity.class);
//                startActivity(intent);

                Intent intent = new Intent(getApplicationContext(), StreamingActivity.class);
                // give IP to next activity so it knows where to send input
                intent.putExtra("selectedMachineIP", selectedMachineIP);
                startActivityForResult(intent, LAUNCH_STREAMING_ACTIVITY);


            }
        });


//        addMachine.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                tcpClientRunnable.sendMessage("salutare");
//                Log.d("rsp", tcpClientRunnable.recvMessage());
//            }
//        });




    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == LAUNCH_STREAMING_ACTIVITY){
            if(resultCode == Activity.RESULT_OK){

                Toast.makeText(getApplicationContext(), "Gaming session ended", Toast.LENGTH_SHORT).show();

            }


        }
    }




}
