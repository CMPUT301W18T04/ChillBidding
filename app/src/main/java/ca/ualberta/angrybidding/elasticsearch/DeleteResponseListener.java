package ca.ualberta.angrybidding.elasticsearch;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class DeleteResponseListener extends ElasticSearchResponseListener {

    @Override
    public void onResponse(JSONObject response) {
        try {
            String result = response.getString("result");
            if (result.equals("not_found")) {
                onNotFound();
            } else if (result.equals("deleted")) {
                onDeleted(response.getString("_id"));
            }
        } catch (JSONException e) {
            onErrorResponse(new VolleyError(e));
        }
    }

    public abstract void onDeleted(String id);

    public abstract void onNotFound();
}
