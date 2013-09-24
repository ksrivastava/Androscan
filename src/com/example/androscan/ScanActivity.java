package com.example.androscan;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ScanActivity extends BaseBrowseActivity implements OnClickListener {

	private static final String TAG = "ScanActivity";
	private static final String Access_Key = "XP4RYYCE";
	private String ISBN_URL = "http://isbndb.com/api/books.xml?access_key="
			+ Access_Key + "&results=subjects&index1=isbn&value1=";

	private String GOOGLE_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
	private String projection = "&projection=full";

	Button scanBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);

		scanBtn = (Button) findViewById(R.id.btnScan);
		scanBtn.setOnClickListener(this);

	}

	void callScanner() {
		try {

			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "ISBN");
			startActivityForResult(intent, 0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "ERROR:" + e,
					Toast.LENGTH_LONG).show();

		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {

			if (resultCode == RESULT_OK) {
				String isbn = intent.getStringExtra("SCAN_RESULT");
				Log.d(TAG, isbn);

				String isbnUrl = ISBN_URL + isbn;
				String googleUrl = GOOGLE_URL + isbn + projection;
				String[] url = { googleUrl, isbnUrl };

				load(isbn, url); // Load both from Google and ISBNdb

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(), "Scan cancelled",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	void load(String isbn, String... urls) {
		DownloadXml download = new DownloadXml();
		download.isbn = isbn;
		download.setContext(ScanActivity.this);
		download.execute(urls);
	}

	private class DownloadXml extends DownloadStreamTask {

		Book book;
		String isbn;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pd.dismiss(); // Maybe have automatic update?
			book = new Book();

		}

		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			try {
				loadJsonNetwork(urls[0], book);
				if (book.full() == false)
					loadXmlNetwork(urls[1], book);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		public void pause() {
			if (this.getStatus().equals(Status.RUNNING)) {
				try {
					Log.d("Async", "Pause");
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void resume() {
			if (this.getStatus().equals(Status.PENDING)) {
				Log.d("Async", "Resume");
				this.notify();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			book.isbn = isbn;
			book.print();
			try {
			
				book.insertBook();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refresh();
		}
	}

	private void loadXmlNetwork(String url, Book book) throws IOException,
			XmlPullParserException {
		String result = downloadUrl(url);
		parseXml(result, book);
	}

	private void loadJsonNetwork(String url, Book book) throws IOException,
			JSONException {
		String result = downloadUrl(url);

		JSONObject jObject = new JSONObject(result);
		JSONArray itemsArray = jObject.optJSONArray("items");
		JSONObject item = itemsArray.getJSONObject(0);

		JSONObject volume = item.getJSONObject("volumeInfo");

		if (book.title == null && volume.has("title")) {
			book.title = volume.getString("title");
		}

		if (book.author.isEmpty() && volume.has("authors")) {
			JSONArray authorArray = volume.getJSONArray("authors");
			for (int i = 0; i < authorArray.length(); i++) {
				String str = authorArray.getString(i);
				filterAndAdd(str, book.author, "AUTHOR");
			}
		}

		if (book.genre.isEmpty() && volume.has("categories")) {
			JSONArray genreArray = volume.getJSONArray("categories");
			for (int i = 0; i < genreArray.length(); i++) {
				String str = genreArray.getString(i);
				filterAndAdd(str, book.genre, "AUTHOR"); // Because we are not
															// dealing with
															// " -- ", but with
															// commas, "and",
															// and &
			}
		}

		if (volume.has("imageLinks")) {
			JSONObject imageLinks = volume.getJSONObject("imageLinks");
			book.url = imageLinks.getString("thumbnail");
		}

	}

	private void parseXml(String stream, Book book)
			throws XmlPullParserException, IOException {

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();

		xpp.setInput(new StringReader(stream));
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String item = xpp.getName();
				if (item.equals("Title") && book.title == null) {
					eventType = xpp.next();
					if (eventType == XmlPullParser.TEXT) {
						book.title = xpp.getText();

					}
				}

				else if (item.equals("AuthorsText") && book.author.isEmpty()) {
					eventType = xpp.next();
					if (eventType == XmlPullParser.TEXT) {
						filterAndAdd(xpp.getText(), book.author, "AUTHOR");
					}
				}

				else if (item.equals("Subject") && book.genre.isEmpty()) {
					eventType = xpp.next();
					if (eventType == XmlPullParser.TEXT) {
						filterAndAdd(xpp.getText(), book.genre, "GENRE");
					}
				}
			}

			eventType = xpp.next();
		}
	}

	private void filterAndAdd(String text, HashSet<String> set, String type) {
		// TODO Auto-generated method stub
		if (type.equals("AUTHOR")) {
			String regex = "((\\s)(and|&)(\\s))|((,)(\\s))";
			String[] authors = text.split(regex);
			for (String str : authors) {
				set.add(str);
			}
		}

		else if (type.equals("GENRE")) {
			String[] genres = text.split("(\\s)(--)(\\s)");
			for (String str : genres) {
				set.add(str);
			}
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		callScanner();
	}

}