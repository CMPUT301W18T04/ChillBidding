package ca.ualberta.angrybidding.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.slouple.android.AdvancedActivity;
import com.slouple.android.widget.adapter.DummyAdapter;

import ca.ualberta.angrybidding.Bid;
import ca.ualberta.angrybidding.ElasticSearchTask;
import ca.ualberta.angrybidding.ElasticSearchUser;
import ca.ualberta.angrybidding.R;
import ca.ualberta.angrybidding.Task;
import ca.ualberta.angrybidding.User;
import ca.ualberta.angrybidding.elasticsearch.UpdateResponseListener;
import ca.ualberta.angrybidding.ui.view.BidView;

public class ViewTaskDetailActivity extends AdvancedActivity {
    private ElasticSearchTask elasticSearchTask;
    private User user;
    private String id;

    private TextView titleTextView;
    private TextView ownerTextView;
    private TextView descriptionTextView;
    private TextView bidsLable;
    private RecyclerView bidRecyclerView;
    /**
     * Creates ViewTaskDetailActivity
     * Gets task object from Intent using Gson
     * Assigns members to according view objects
     *
     * @param savedInstanceState State of saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task_detail);

        Intent intent = getIntent();
        String taskJson = intent.getStringExtra("task");
        id = intent.getStringExtra("id");
        elasticSearchTask = new Gson().fromJson(taskJson, ElasticSearchTask.class);
        user = ElasticSearchUser.getMainUser(this);

        titleTextView = findViewById(R.id.taskDetailTitle);
        ownerTextView = findViewById(R.id.taskDetailOwner);
        descriptionTextView = findViewById(R.id.taskDetailDescription);
        bidsLable = findViewById(R.id.taskDetailBidsLabel);
        bidRecyclerView = findViewById(R.id.taskDetailBids);

        titleTextView.setText(elasticSearchTask.getTitle());
        ownerTextView.setText(elasticSearchTask.getUser().getUsername());
        descriptionTextView.setText(elasticSearchTask.getDescription());


        if (elasticSearchTask.getBids().size() < 1) {
            bidsLable.setVisibility(View.GONE);
            bidRecyclerView.setVisibility(View.GONE);
        } else {
            bidRecyclerView.setAdapter(new DummyAdapter<Bid, BidView>(elasticSearchTask.getBids()) {
                @Override
                public BidView createView(int i) {
                    return new BidView(ViewTaskDetailActivity.this);

                }

                /** Sets the list view of Bids
                 *  Checks if Task detail view user is owner or not and displays popup menu accordingly
                 *
                 * @param bidView view of bid list
                 * @param bid The bid in bid list
                 */
                @Override
                public void onBindView(BidView bidView, final Bid bid) {
                    bidView.setBid(bid);
                    if (elasticSearchTask.getUser().equals(user)) {
                        bidView.useBidPopupMenu(bid, new BidView.OnBidActionListener() {
                            @Override
                            public void onDecline() {
                                ViewTaskDetailActivity.this.onDecline(bid);
                            }

                            @Override
                            public void onAccept() {
                                ViewTaskDetailActivity.this.onAccept(bid);
                            }
                        });
                    }
                }

                @Override
                public void onReachingLastItem(int i) {

                }
            });
            bidRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    /**Removes or Declines selected bid
     *
     * @param bid The selected bid
     */
    public void onDecline (Bid bid) {
        elasticSearchTask.getBids().remove(bid);
        updateFinish();

    }

    /**Removes bids from bid list except Accepted(chosen) bid
     *
     * @param bid The selected bid
     */
    public void onAccept (Bid bid) {
        elasticSearchTask.setChosenBid(bid);
        updateFinish();
    }

    /**
     * Updates the change
     */
    public void updateFinish() {
        ElasticSearchTask.updateTask(this, id, elasticSearchTask, new UpdateResponseListener() {
            @Override
            public void onCreated(String id) {

            }

            @Override
            public void onUpdated(int version) {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

}
