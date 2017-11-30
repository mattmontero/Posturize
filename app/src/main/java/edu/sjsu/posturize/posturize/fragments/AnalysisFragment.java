package edu.sjsu.posturize.posturize.fragments;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import edu.sjsu.posturize.posturize.R;
import edu.sjsu.posturize.posturize.data.FirebaseHelper;
import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;
import edu.sjsu.posturize.posturize.visualizations.GraphManager;

import static android.R.attr.id;

public class AnalysisFragment extends Fragment implements View.OnClickListener {

    private static ArrayList<String> dailyAnalysis;
    private Button syncButton;
    private TextView analysis;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_analysis, container, false);

        this.syncButton = (Button) rootView.findViewById(R.id.forceAnalysis);
        this.analysis = (TextView) rootView.findViewById(R.id.analysisText);
        if(dailyAnalysis != null && !dailyAnalysis.isEmpty()) {
            this.analysis.setText(dailyAnalysis.get(0));
        }
        this.syncButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.forceAnalysis:

                FirebaseHelper firestoreHelper = FirebaseHelper.getInstance();
                PostureManager postureManager = new PostureManager(getContext());
                postureManager.openDB();

                Toast.makeText(getContext(), "sync-ing data...", Toast.LENGTH_LONG).show();
                // call AsynTask to perform network operation on separate thread

                ArrayList<Date> times = new ArrayList<>();
                ArrayList<Double> slouches = new ArrayList<>();
                String id = GoogleAccountInfo.getInstance().getId();
                ArrayList<DataPoint> postures = postureManager.get(id, Calendar.getInstance());
                for(DataPoint p : postures){
                    times.add(new Date((long) p.getX()));
                    slouches.add(p.getY());
                }
                HashMap<String, Object> data = new HashMap<>();
                data.put("times", times);
                data.put("slouches", slouches);

                Log.i("DailySync", "updated firestore");
                firestoreHelper.addSlouchesToFirestoreForUser(id, data);
                postureManager.closeDB();

                Log.i("Daily Sync","scheduler job finished");

                Toast.makeText(getContext(), "request analysis...", Toast.LENGTH_LONG).show();
                StringBuilder url = new StringBuilder();
                url.append("http://default-environment.ypcxjmsjbd.us-west-2.elasticbeanstalk.com/home/forceAnalysis/");
                url.append(id);
                new HttpAsyncTask().execute(url.toString());
                break;
        }
    }

    public static AnalysisFragment newInstance() {
        
        Bundle args = new Bundle();
        
        AnalysisFragment fragment = new AnalysisFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void setAnalysis(ArrayList<String> list){
        dailyAnalysis = list;
    }

    public static String POST(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls){
            return POST(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
