package com.example.androscan;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class TitleActivity extends BaseBrowseActivity {

	private static final String TAG = "TitleActivity";
	ListView browseList;

	ArrayList<String> list;
	StableArrayAdapter adapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_title);
		activeClass = TitleActivity.class;
		list = new ArrayList<String>();
		browseList = (ListView) findViewById(R.id.TitleBrowseList);
		browseList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               String clickedTitle =  browseList.getItemAtPosition(position).toString();
               Intent displayIntent = new Intent(getApplicationContext(), BookDisplayActivity.class);
    
               displayIntent.putExtra("Title", clickedTitle);
               
               startActivity(displayIntent);
            }
        });
		
		setTitle("Browse by Titles");
		load();

	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (loadTitle == true) {
			list.clear();
			load();
		}
			
	}
	

	void load() {
		loadTitle = false;
		String url = searchUrl + allTitlesURL;
		DownloadJson download = new DownloadJson();
		download.setContext(TitleActivity.this);
		download.execute(url);
	}

	private class DownloadJson extends DownloadStreamTask {
	
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			if (adapter != null)
				browseList.setAdapter(adapter);

		}

	}

	public void loadFromNetwork(String url) throws IOException,
			JSONException {

		String result = downloadUrl(url);
		Log.d(TAG, result);
		JSONObject jObject = new JSONObject(result);
		JSONArray titlesArray = jObject.optJSONArray("Titles");

		for (int i = 0; i < titlesArray.length(); i++) {
			JSONObject item = titlesArray.getJSONObject(i);
			String title = item.getString("Title");
			Log.d(TAG, "Before add");
			if (list == null) Log.d(TAG, "List is null");
			list.add(title);
			Log.d(TAG, "After add");
		}
		Log.d(TAG, "Before adapter");
		adapter = new StableArrayAdapter(this,
				android.R.layout.simple_list_item_1, list);
		Log.d(TAG, "After adapter");
	}

}
