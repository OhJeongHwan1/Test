package com.example.test;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.test.databinding.ActivityMainBinding;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkConnectedNodes();

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button start = (Button) findViewById(R.id.button);
        start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                    sendData("start");
                }
        });
        Button warning = (Button) findViewById(R.id.button2);
        warning.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                    sendData("warning");
                }
        });
        Button back = (Button) findViewById(R.id.button3);
        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                sendData("back");
            }
        });

    }
@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
private void sendData(String data) {
    new Thread(new Runnable() {
        @Override
        public void run() {
            String path = "/data";
            String message = data;
            byte[] messageData = message.getBytes(StandardCharsets.UTF_8);

            Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            nodeListTask.addOnSuccessListener(new OnSuccessListener<List<Node>>() {
                @Override
                public void onSuccess(List<Node> nodes) {
                    for (Node node : nodes) {
                        Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this)
                                .sendMessage(node.getId(), path, messageData);

                        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                Log.d("from android", "성공적 전송: " + message);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "성공", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                        sendMessageTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e("from android", "Failed to send data: " + exception);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Toast 메시지 내용", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }).start();
}
    private void checkConnectedNodes() {
        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(this).getConnectedNodes();
        nodeListTask.addOnSuccessListener(new OnSuccessListener<List<Node>>() {
            @Override
            public void onSuccess(List<Node> nodes) {
                if (nodes != null && !nodes.isEmpty()) {
                    for (Node node : nodes) {
                        Log.d("MainActivity", "연결된 노드: " + node.getDisplayName());
                    }
                } else {
                    Log.d("MainActivity", "연결된 노드가 없습니다.");
                }
            }
        });
        nodeListTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("MainActivity", "Failed to get connected nodes", exception);
            }
        });
    }
}

