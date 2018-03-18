package ca.ualberta.angrybidding.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;

import ca.ualberta.angrybidding.Bid;
import ca.ualberta.angrybidding.ElasticSearchTask;
import ca.ualberta.angrybidding.ElasticSearchUser;
import ca.ualberta.angrybidding.R;
import ca.ualberta.angrybidding.User;
import ca.ualberta.angrybidding.elasticsearch.UpdateResponseListener;
import ca.ualberta.angrybidding.ui.activity.main.AllTaskFragment;
import ca.ualberta.angrybidding.ui.activity.main.MainActivity;
import ca.ualberta.angrybidding.ui.view.TaskView;

public class AddBidActivity extends AngryBiddingActivity {
    private ElasticSearchTask elasticSearchTask;
    private User user;
    private String price;
    private String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bid);
        Intent i = getIntent();
        String taskJson = i.getStringExtra("task");
        id = i.getStringExtra("ID");
        elasticSearchTask = new Gson().fromJson(taskJson, ElasticSearchTask.class);
        user = ElasticSearchUser.getMainUser(this);

    }

    public void BidTaskClick(View view){
        price =((EditText) findViewById(R.id.amount_of_bid)).getText().toString();
        double price1 = Double.parseDouble(price);
        elasticSearchTask.getBids().add(new Bid(user, price1));
//        elasticSearchTask.setBids(new Bid(user, price1));
        ElasticSearchTask.updateTask(this, id, elasticSearchTask, new UpdateResponseListener() {
            @Override
            public void onCreated(String id) {

            }

            @Override
            public void onUpdated(int version) {
                finish();
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        finish();
    }
}
