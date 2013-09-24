package com.example.androscan;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class BookDisplayActivity extends BaseBrowseActivity {

	private static final String TAG = "Book Activity Display";
	String title;
	ImageView image;
	TextView authorView, genreView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_display);
		
		title = getIntent().getExtras().getString("Title");
		setTitle(title);

		image = (ImageView) findViewById(R.id.imageBook);
		authorView = (TextView) findViewById(R.id.textAuthor);
		genreView = (TextView) findViewById(R.id.textGenre);
		load();
	}

	private void load() {
		// TODO Auto-generated method stub
		String search = title.replaceAll("\\s", "+");
		search = search.replaceAll("&", "%26");
		String url = searchUrl + "&title=" + search;
		DownloadJson download = new DownloadJson();
		download.setContext(BookDisplayActivity.this);
		download.execute(url);
	}

	private class DownloadJson extends DownloadStreamTask {

		private Book book;

		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			try {
				this.book = loadBookFromNetwork(urls[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(book == null) return;
			
			String authorString = "";
			String genreString = "";
			for (String a : book.authorList) {
				authorString += (a + "\n"); 
			}
			
			Log.d(TAG, authorString);
			Log.d(TAG, "Done");
			for (String g : book.genreList) {
				genreString += (g + "\n"); 
			}
			
			Log.d(TAG, genreString);
			
			image.setImageBitmap(getImage(book.url));
			authorView.setText(authorString);
			genreView.setText(genreString);
			if (book.genreList.size() > 10) {
				genreView.setMovementMethod(ScrollingMovementMethod.getInstance());
			}
			if (book.authorList.size() > 10) {
				authorView.setMovementMethod(ScrollingMovementMethod.getInstance());
			}
			
		}

	}

	public Book loadBookFromNetwork(String url)
			throws IOException, JSONException {

		String result = downloadUrl(url);
		Log.d(TAG, result);
		Book book = new Book();
		JSONObject jObject = new JSONObject(result);
		JSONArray authorArray = jObject.optJSONArray("Authors");

		for (int i = 0; i < authorArray.length(); i++) {
			JSONObject item = authorArray.getJSONObject(i);
			String author = item.getString("Name");
			book.authorList.add(author);
		}
		
		JSONArray genreArray = jObject.optJSONArray("Genres");

		for (int i = 0; i < genreArray.length(); i++) {
			JSONObject item = genreArray.getJSONObject(i);
			String genre = item.getString("Genre");
			book.genreList.add(genre);
		}
		
		JSONArray image = jObject.getJSONArray("Image");
		book.url = image.getJSONObject(0).getString("URL");

		return book;
	}

}
