package ca.ualberta.angrybidding.ui.activity.main;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.slouple.android.AdvancedFragment;
import com.slouple.android.widget.adapter.DummyAdapter;

import java.util.ArrayList;

import ca.ualberta.angrybidding.ElasticSearchNotification;
import ca.ualberta.angrybidding.ElasticSearchUser;
import ca.ualberta.angrybidding.R;
import ca.ualberta.angrybidding.notification.BidAddedNotification;
import ca.ualberta.angrybidding.notification.NotificationCallback;
import ca.ualberta.angrybidding.notification.NotificationConnection;
import ca.ualberta.angrybidding.notification.NotificationFactory;
import ca.ualberta.angrybidding.notification.NotificationService;
import ca.ualberta.angrybidding.notification.NotificationWrapper;
import ca.ualberta.angrybidding.ui.view.NotificationView;

/**
 * Created by SarahS on 2018/03/29.
 */

public class NotificationFragment extends AdvancedFragment implements IMainFragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList<NotificationWrapper> notifications = new ArrayList<>();
    private ElasticSearchNotification.ListNotificationListener listener;

    private static final int COMMENT_VIEW_TYPE = 1;
    private static final int RATE_VIEW_TYPE = 2;

    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    @Override
    public AppBarLayout getAppBarLayout(ViewGroup rootView, LayoutInflater inflater) {
        if (appBarLayout == null) {
            appBarLayout = (AppBarLayout) inflater.inflate(R.layout.notification_fragment_toolbar, rootView, false);
        }
        return appBarLayout;

    }

    @Override
    public Toolbar getToolbar(ViewGroup rootView, LayoutInflater inflater) {
        if (toolbar == null) {
            toolbar = (Toolbar) getAppBarLayout(rootView, inflater).findViewById(R.id.notification_fragment_toolbar);
        }
        return toolbar;
    }

    @Override
    public void onActionBarAdded(ActionBar actionBar) {

    }

    @Override
    public boolean shouldOffsetForToolbar() {
        return true;
    }

    @Override
    public void onVisible() {

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    //Need to add
    private NotificationConnection connection = new NotificationConnection() {
        @Override
        public NotificationWrapper onDeserializeNotification(String className, String json) {
            Class<?> classType;
            if(className.equals("ca.ualberta.angrybidding.notification.BidAddedNotification")){
                classType = BidAddedNotification.class;
            }else{
                return null;
            }
            Gson gson = new Gson();
            return (NotificationWrapper) gson.fromJson(json, classType);
        }

        @Override
        public void onReceivedNotification(NotificationWrapper notificationWrapper) {
            if(notificationWrapper != null){
                //Need Sorting?
                //addNotification(notification);
                //notifications.add(notification);
                recyclerView.getAdapter().notifyDataSetChanged();
                final MainActivity mainActivity = (MainActivity) getContext();
                if(mainActivity.getCurrentFragmentID() != R.id.nav_notification) {
                    Snackbar.make(getView(), "New Notification", Snackbar.LENGTH_LONG)
                            .setAction("View", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mainActivity.setCurrentFragment(R.id.nav_notification);
                                }
                            }).show();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startNotificationService();
    }

    //Notification view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.fragment_notification, container, false);
        //ViewPager viewPager = layout.findViewById(R.id.notificationViewPager);
        swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.notificationsSwipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                NotificationFragment.this.onRefresh();
            }
        });

        recyclerView = (RecyclerView) layout.findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new DummyAdapter<NotificationWrapper, NotificationView>(notifications) {
            @Override
            public NotificationView createView(int viewType) {
                return createTaskView();
            }

            @Override
            public void onBindView(NotificationView view, NotificationWrapper item) {
                NotificationFragment.this.onBindView(view, item);
            }

            @Override
            public void onReachingLastItem(int i) {

            }

        });

        refresh();

        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void refresh() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    public void clear() {
        notifications.clear();
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void onRefresh() {
        clear();
        ElasticSearchNotification.listNotificationByUsername(getContext(), ElasticSearchUser.getMainUser(getContext()).getUsername(), new ElasticSearchNotification.ListNotificationListener() {
            @Override
            public void onResult(ArrayList<ElasticSearchNotification> newNotification) {
                NotificationFactory noti = new NotificationFactory<ElasticSearchNotification>();
                ArrayList<NotificationWrapper> newNotifications = noti.parseNotifications(newNotification);
                for (final NotificationWrapper notificationWrapper: newNotifications) {
                    notificationWrapper.onReceived(getContext(), new NotificationCallback(){
                        @Override
                        public void callBack(){
                            notifications.add(notificationWrapper);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
                finishRefresh();
            }

            /*
             * Show message when a error occurs
             */
            @Override
            public void onError(VolleyError error) {
                Log.e("NotificationFragment", error.getMessage(), error);
            }
        });
    }

    public void finishRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    protected void onBindView(NotificationView notificationView, final NotificationWrapper noti) {
        notificationView.setNotification(noti);
    }

    protected NotificationView createTaskView() {
        NotificationView notificationView = new NotificationView(getContext());
        return notificationView;
    }

    @Override
    public void onStart(){
        super.onStart();
        NotificationManager notificationManager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        startNotificationService();
        getContext().bindService(new Intent(getContext(), NotificationService.class), connection, 0);
    }

    public void startNotificationService(){
        if(!getContext().isServiceRunning(NotificationService.class)){
            Intent serviceIntent = new Intent(getContext(), NotificationService.class);
            getContext().startService(serviceIntent);
            Log.d("NotificationsFragment", "Starting NotificationService");
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        connection.disconnect();
        getContext().unbindService(connection);
        Log.d("NotificationsFragment", "Stopped NotificationService");
    }

    /*public void removeNotification(Notification notification, boolean removeFromServer){
        if(notifications.remove(notification)){
            sortNotifications();
            recyclerView.getAdapter().notifyDataSetChanged();

            if(removeFromServer){
                PostAPIRequest request = new PostAPIRequest("DeleteNotification", new APIJsonResponseListener() {
                    @Override
                    public void onSuccess(HashMap<String, String> values) {
                        Snackbar.make(getView(), R.string.notificationRemoved, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {

                    }
                }, getContext());
                request.getParams().put("entryID", String.valueOf(notification.getEntryID()));
                request.setUser(User.getMainUserSession(getContext()));
                request.submit();
            }
        }
    }*/

}