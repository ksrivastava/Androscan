package com.example.androscan;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class DisplayActivity extends BaseBrowseActivity {

	String type = null;
	String data = null;

	private static final String TAG = "DisplayActivity";

	class Thumbnail {
		String title;
		String url;
		Bitmap image;

		Thumbnail(String title, String url) {
			this.title = title;
			this.url = url;
			image = getImage(this.url);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);
		
		type = getIntent().getExtras().getString("Type");
		data = getIntent().getExtras().getString("Data");

		Log.d(TAG, type);
		Log.d(TAG, data);

		setTitle(data);

		String search = data.replaceAll("\\s", "+");
		search = search.replaceAll("&", "%26");

		String attr = null;
		if (type.equals("TITLE")) {
			attr = "&title=" + search;
			// load(data);
		}

		else if (type.equals("AUTHOR")) {
			attr = "&author=" + search;
			load(attr, "AUTHOR");
		}

		else if (type.equals("GENRE")) {
			attr = "&genre=" + search;
			load(attr, "GENRE");
		}

	}

	void load(String search, String type) {

		String url = searchUrl + search;

		DownloadJson download = new DownloadJson();
		download.setContext(DisplayActivity.this);
		download.type = type;
		download.execute(url);
	}

	private class DownloadJson extends DownloadStreamTask {
		ArrayList<Thumbnail> images;
		GridView gridview;

		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			try {
				images = loadFromNetwork(urls[0], type);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		String type;

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			for (Thumbnail i : images) {
				Log.d(TAG, i.title + " " + i.url);
			}

			gridview = (GridView) findViewById(R.id.gridview);
			gridview.setAdapter(new BookAdapter(DisplayActivity.this, images));

			gridview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {

					String title = images.get(position).title;
					Intent displayIntent = new Intent(getApplicationContext(),
							BookDisplayActivity.class);
					displayIntent.putExtra("Title", title);
					startActivity(displayIntent);
				}
			});
		}

	}

	public ArrayList<Thumbnail> loadFromNetwork(String url, String type)
			throws IOException, JSONException {

		String result = downloadUrl(url);
		Log.d(TAG, result);
		JSONObject jObject = new JSONObject(result);
		JSONArray titlesArray = jObject.optJSONArray("Titles");

		ArrayList<Thumbnail> list = new ArrayList<Thumbnail>();
		for (int i = 0; i < titlesArray.length(); i++) {
			JSONObject item = titlesArray.getJSONObject(i);
			String title = item.getString("Title");
			String imageUrl = item.getString("URL");
			Thumbnail thumb = new Thumbnail(title, imageUrl);
			list.add(thumb);
		}

		return list;
	}

	class BookAdapter extends BaseAdapter {

		private static final String TAG = "Book Adapter";
		private Context context;
		private Bitmap[] images;

		public BookAdapter(Context c, ArrayList<Thumbnail> thumbs) {
			this.context = c;
			ArrayList<Bitmap> bits = new ArrayList<Bitmap>();
			for (Thumbnail t : thumbs) {
				bits.add(t.image);
			}
			images = new Bitmap[bits.size()];
			images = bits.toArray(images);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return images.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				imageView = new ImageView(context);
				imageView.setLayoutParams(new GridView.LayoutParams(120, 180));
				// imageView.setAdjustViewBounds(true);
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}
			Log.d(TAG, "" + position);
			imageView.setImageBitmap(images[position]);
			// imageView.setImageResource(mThumbIds[position]);
			return imageView;
		}

	}
}