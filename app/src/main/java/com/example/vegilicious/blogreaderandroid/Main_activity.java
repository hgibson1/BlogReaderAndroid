package com.example.vegilicious.blogreaderandroid;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class Main_activity extends ListActivity {
    protected JSONObject mBlogData;
    private TextView noDataTextView;
    protected ProgressBar mProgressBar;

    protected static final int mNumberBlogPosts = 20;
    protected static final String TAG = Main_activity.class.getSimpleName();
    //static means can access without instance of ListActivity class
    //Protected variables are closed to public but available in subclasses class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        noDataTextView = (TextView) getListView().getEmptyView();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);


        if (networkIsAvailable()) {
            GetBlogPostTask getBlogPost = new GetBlogPostTask(); //creates task
            getBlogPost.execute(); //executes task
            mProgressBar.setVisibility(View.VISIBLE); //makes progress bar visible while task is executing
        } else {
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id){
        super.onListItemClick(list, view, position, id);
        //listview where item was tapped, view to display, position in listview, id of view
        try {
            JSONArray jsonPosts = mBlogData.getJSONArray("posts");
            JSONObject post = jsonPosts.getJSONObject(position);
            String postUrl = post.getString("url");

            Intent intent = new Intent(this, DetailActivity.class);
            //explicit intent, describes exactly how to use
            //can also use Uri.parse(postURL), implicit intent, which hands of to android system
            //Intent.ACTION_VIEW causes a view to open based on the url passed to it
            intent.setData(Uri.parse(postUrl)); //takes uri (can be interchanged w/ url)
            //Uri.parse() takes string and outputs uri
            startActivity(intent); //starts activity using intent
            //intent moves through android system till can find web browser to open url

        } catch(JSONException e){
            Log.e(TAG, "JSON EXCEPTION caught", e);
        }

    }

    //method that checks network connectivity
    private boolean networkIsAvailable(){
        //Creating connectivity manager. getSystemService() takes context string as Service. Must cast
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //creating network info object
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //allows use to get and analyze network info. requires access network state permission
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
        //checks that network info exists and is connected
            isAvailable = true;
        }
        return  isAvailable;
    }

    //Custom async task
    private class GetBlogPostTask extends AsyncTask<Object, Void, JSONObject> {
        //AsyncTask is generic class, must add type of input, type of Progress, type of return
        JSONObject jsonResponse = null;
         @Override
        protected JSONObject doInBackground(Object ... agr0) {
             String responseData = "";
             //Exception: java runtime system sends warning when something is wrong = throwing an exception
             //URL constructor throws MalformedURLException which must be handled by tricatch block
             try {
                 URL blogFeedURL = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=20");
                 //open connection returns generic URL connection so must be cast to Http
                 //open connection throws input/output i/o exception which must be caught. Can change catch blocks
                 HttpURLConnection connection = (HttpURLConnection) blogFeedURL.openConnection();
                 connection.connect();

                 int responseCode = connection.getResponseCode();


                 if (responseCode == HttpURLConnection.HTTP_OK) {
                     //executes if response code = 200, or there is a network connection
                     Log.d(TAG, "Network connected, response code = 200");

                     //creates input stream object and reads with the streamReader
                     InputStream inputStream = connection.getInputStream();
                     InputStreamReader reader = new InputStreamReader(inputStream);

                     //gets length of connection and initializes array to store characters
                     int contentLength = connection.getContentLength();
                     char[] charArray = new char[contentLength];
                    responseData = new String(charArray);

                     reader.read(charArray); //stream reader reads stream and stores in array

                     //JSONObject holds any object in JSON form
                     //JSONArray array of JSON objects or JSON arrays
                     //JSONObject constructor takes data string as parameter
                     jsonResponse = new JSONObject(responseData);

                 } else {
                     Log.d(TAG, "Network not connected response code = " + responseCode);
                 }

             } catch (MalformedURLException e) {
                 //code will only be executed if exception thrown. variable e catches exception
                 Log.e(TAG, "URL exception caught", e);
             } catch (IOException e) {
                 // openConnection throws IO exception
                 Log.e(TAG, "IO exception caught", e);
             } catch (Exception e) {
                 Log.e(TAG, "Generic Exception caught", e);
                 //connect() throws generic exception. Must put generic exception catch block last
             }

             Log.d(TAG, responseData);
             return jsonResponse;
         }

        //this is on user interface thread. Bridge between async task and main class
        @Override
        protected void onPostExecute(JSONObject result) {
            mBlogData = result;
             updateList();
        }
    }

    //updates list and handles error
    public void updateList() {
        mProgressBar.setVisibility(View.INVISIBLE);
        //hides progress bar when async task finishes
        if (mBlogData == null){
        //handel error
        //making a dialog
        //Builder is public class nested inside alert Dialog class. Must define to make dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.errorMessage));
            builder.setMessage(getString(R.string.noDataErrorMessage));
            //can have up to three buttons: positive, negative, neutral. Set button takes method text and clicklistener
            builder.setPositiveButton(R.string.OK, null);
            //creates and displays the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
            noDataTextView.setText(getString(R.string.emptyMessage));

        } else {
            try {
                Log.d(TAG, mBlogData.toString(2));
                JSONArray posts = mBlogData.getJSONArray("posts");

                int length = posts.length();

                //creates an array list to hold the blog posts
                ArrayList<HashMap<String, String>> blogPosts =
                        new ArrayList<HashMap<String, String>>();

                for (int i = 0; i < length; i++) {
                    JSONObject blogPost = posts.getJSONObject(i);
                    //Html converts special html characters to normal text. Returns span so must be converted to string
                    String author = Html.fromHtml(blogPost.getString("author")).toString();
                    String title = Html.fromHtml(blogPost.getString("title")).toString();

                    //creates a hashmap object
                    HashMap<String, String> post = new HashMap<String, String>();
                    post.put("title", title);
                    post.put("author", author);

                    blogPosts.add(post); //remember to put title + author in string resources
                }

            String[] keys = {"title", "author"};
            int[] ids = {android.R.id.text1, android.R.id.text2};
            //ids of text views that hold keys. Using system ids

            //Using simple adaptor which is generic class that can be configured
            //takes context, data, resource id of layout to use (simple list 2 has 2 lines of text),
            //from (hashmap keys), to (textviews to display text)
                SimpleAdapter adapter =
                        new SimpleAdapter(this, blogPosts, android.R.layout.simple_list_item_2, keys, ids);

            } catch (JSONException e){
                Log.e(TAG, "JSONException caught", e);
            }
        }
    }
}
