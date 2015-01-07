package com.example.vegilicious.blogreaderandroid;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.example.vegilicious.blogreaderandroid.R;

import java.net.URI;

public class DetailActivity extends ActionBarActivity {

    protected String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Gets intent and Uri
        Intent intent = getIntent();
        Uri blogUri = intent.getData();
        mUrl = blogUri.toString();
        //gets webview and loads with blog uri
        WebView webView = (WebView) findViewById(R.id.WebView);
        webView.loadUrl(mUrl);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //checks if item was tapped
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

     private void sharePost() {
    Intent sharedIntent = new Intent(Intent.ACTION_SEND);
         sharedIntent.setType("text/plain"); //MimeTypes, mimics
         sharedIntent.putExtra(Intent.EXTRA_TEXT, mUrl);//value added as Extra. Becomes key-value pair
         //EXTRA_TEXT is a special key of intent class that allows to share text

         startActivity(Intent.createChooser(sharedIntent, "How do you want to share?"));
         //create chooser allows user to share which app they want to handel the intent
         //forces android to always display chooser, instead of default, or error message

    }

}
