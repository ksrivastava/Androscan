package com.example.androscan;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public abstract class BaseBrowseActivity extends ApplicationActivity {

	private static final String TAG = "BaseBrowseActivity";
	protected static final String allAuthorsURL = "&author=ALL_AUTHORS";
	protected static final String allTitlesURL = "&title=ALL_TITLES";
	protected static final String allGenresURL = "&genre=ALL_GENRES";

	ProgressDialog pd;

	public Class<?> activeClass;
	
	public Bitmap getImage(String src) {
		// TODO Auto-generated method stub
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap image = BitmapFactory.decodeStream(input);
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			try {
				Intent refresh = new Intent(this, activeClass);
				refresh.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(refresh);
				break;
			}
			catch (NullPointerException e) {
				
			}
		}
		return true;
	}

	class DownloadStreamTask extends AsyncTask<String, Void, String> {

		private Context context = null;

		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pd = ProgressDialog.show(context, "", "Please Wait", false);
		}

		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub

			try {
				loadFromNetwork(urls[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG, "finished");
			return null;
		}

		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Log.d(TAG, "About to dismiss");
			pd.dismiss();
			Log.d(TAG, "Dismissed from " + context.toString());
		}

		protected void setContext(Context context) {
			this.context = context;
		}

	}

	public void loadFromNetwork(String url) throws IOException, JSONException {
	}

}

class StableArrayAdapter extends ArrayAdapter<String> {

	HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	public StableArrayAdapter(Context context, int textViewResourceId,
			List<String> objects) {
		super(context, textViewResourceId, objects);
		for (int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i), i);
		}
	}

	@Override
	public long getItemId(int position) {
		String item = getItem(position);
		return mIdMap.get(item);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

}
