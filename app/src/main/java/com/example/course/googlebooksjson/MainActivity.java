package com.example.course.googlebooksjson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
	
	private TextView text = null;
	
	//messages from background thread contain data for UI
	Handler handler = new Handler(Looper.getMainLooper()){
		public void handleMessage(Message msg) {
			String title =(String) msg.obj;
			text.append(title + "\n" +"\n");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		text=(TextView)findViewById(R.id.texter);
		
		Thread t = new Thread(background);
		t.start();
	}

	//thread connects to Google Book Api, gets response code, JSON search results,
	//places data into Log and sends messages to display data on UI
	Runnable background = new Runnable() {
		public void run(){
			
			StringBuilder builder = new StringBuilder();

			String Url = "https://www.googleapis.com/books/v1/volumes?q=flowers+inauthor:keyes";
			//String Url = "https://www.googleapis.com/books/v1/volumes?q=java+inauthor:savitch";
			//String Url = "https://www.googleapis.com/books/v1/volumes?q=isbn:9780134802213";

			InputStream is = null;

			try {
				URL url = new URL(Url);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				// Starts the query
				conn.connect();
				int response = conn.getResponseCode();
				Log.e("JSON", "The response is: " + response);
				//if response code not 200, end thread
				if (response != 200) return;
				is = conn.getInputStream();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				// Makes sure that the InputStream is closed after the app is
				// finished using it.
			}	catch(IOException e) {}
			 finally {
				if (is != null) {
					try {
						is.close();
					} catch(IOException e) {}
				}
			}

			//convert StringBuilder to String
			String readJSONFeed = builder.toString();
			Log.e("JSON", readJSONFeed);
			
			//decode JSON
			try {		
				JSONObject obj = new JSONObject(readJSONFeed);
				String kind = obj.getString("kind");
				Log.i("JSON", "kind " + kind);
				String total = obj.getString("totalItems");				
				Log.i("JSON", "totalItems " + total);
				JSONArray jsonArray = new JSONArray();
				jsonArray = obj.getJSONArray("items");
				Log.i("JSON",
						"Number of entries " + jsonArray.length());
				
				//for each array item get title and date
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					JSONObject volumeInfo = jsonObject.getJSONObject("volumeInfo");
					String title = volumeInfo.getString("title");
					
				    Message msg = handler.obtainMessage();
				    msg.obj = title;
				    handler.sendMessage(msg);
				    
					Log.i("JSON", title);
					String date = volumeInfo.getString("publishedDate");
					Log.i("JSON", date);
				}
			} catch (JSONException e) {e.getMessage();
				e.printStackTrace();
			}
		}
	
	};
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
