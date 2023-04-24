package com.example.test;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
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

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button start = (Button) findViewById(R.id.button);
        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                sendMessage();
            }
        });
        Button warning = (Button) findViewById(R.id.button2);
        warning.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                sendMessage();
            }
        });
        Button back = (Button) findViewById(R.id.button3);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                sendMessage();
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
    private void sendMessage() {
        // Create a new thread to avoid blocking the UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Get the connected nodes on the Wear network
                Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
                try {
                    List<Node> nodes = Tasks.await(nodeListTask);
                    for (Node node : nodes) {
                        // Build the message
                        String message = "Hello!";
                        byte[] payload = message.getBytes();

                        // Send the message
                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(getApplicationContext()).sendMessage(node.getId(), "/message", payload);

                        // Add onCompleteListener to check if the message was successfully sent
                        sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                            @Override
                            public void onComplete(@NonNull Task<Integer> task) {
                                if (task.isSuccessful()) {
                                    int result = task.getResult();
                                    Log.d("from android", "Message sent to " + node.getDisplayName() + ". Result: " + result + ". message: " +message);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "성공", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Exception exception = task.getException();
                                    Log.e("from android", "Failed to send message: " + exception);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Toast 메시지 내용", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                } catch (ExecutionException exception) {
                    Log.e("from android", "Failed to send message: " + exception);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "Toast 메시지 내용", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (InterruptedException exception) {
                    Log.e("from android", "Failed to send message: " + exception);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "Toast 메시지 내용", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}

