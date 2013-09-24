package com.example.androscan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class ApplicationActivity extends Activity {

	private static final String TAG = "ApplicationActivity";

	protected final String SERVER_URL = "http://23.23.176.254/book.php";
	protected final String searchUrl = "http://23.23.176.254/book.php?method=search";
	protected final String insertUrl = "http://23.23.176.254/book.php?method=insert";

	static boolean loadTitle = false;
	static boolean loadAuthor = false;
	static boolean loadGenre = false;

	public class Book {
		String title;
		ArrayList<String> authorList;
		ArrayList<String> genreList;
		HashSet<String> author;
		HashSet<String> genre;
		String authorString, genreString;
		String url;
		String isbn;

		Book() {
			author = new HashSet<String>();
			genre = new HashSet<String>();
			authorList = new ArrayList<String>();
			genreList = new ArrayList<String>();
			title = null;
			authorString = null;
			genreString = null;
			url = "http://books.google.com.ng/googlebooks/images/no_cover_thumb.gif";
			isbn = null;
		}

		boolean full() {
			return (title != null) && !(genre.isEmpty()) && !(author.isEmpty());
		}

		void print() {
			Log.d(TAG, "Title: " + title);
			Log.d(TAG, "Authors: ");
			for (String str : author) {
				Log.d(TAG, "    " + str);
			}
			Log.d(TAG, "Genre: ");
			for (String str : genre) {
				Log.d(TAG, "    " + str);
			}
			// Log.d(TAG, "URL: " + url);
		}

		void insertBook() throws UnsupportedEncodingException {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(SERVER_URL);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						6);

				nameValuePairs.add(new BasicNameValuePair("method", "insert"));
				nameValuePairs.add(new BasicNameValuePair("title", title));
				nameValuePairs.add(new BasicNameValuePair("author",
						addArguments("", author)));
				nameValuePairs.add(new BasicNameValuePair("genre",
						addArguments("", genre)));
				nameValuePairs.add(new BasicNameValuePair("imageURL", url));
				nameValuePairs.add(new BasicNameValuePair("ISBN", isbn));

				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						nameValuePairs);
				httpPost.addHeader(entity.getContentType());
				httpPost.setEntity(entity);

				HttpResponse response = httpClient.execute(httpPost);

				Log.d(TAG, "Server response is "
						+ response.getStatusLine().getStatusCode());

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		void insertBook2() throws IOException {
			String dbUrl = getDbUrl();
			String result = downloadUrl(dbUrl);
			Log.d(TAG, result);
			if (result.length() > 0) {
				Log.d(TAG, result);
			}
		}

		private String getDbUrl() {
			// TODO Auto-generated method stub
			String titleArg = "&title=" + title;
			String authorArg = addArguments("&author=", author);
			String genreArg = addArguments("&genre=", genre);

			String dbUrl = insertUrl + titleArg + authorArg + genreArg;
			dbUrl = dbUrl.replaceAll("\\s", "+");
			Log.d(TAG, "URL: " + dbUrl);
			return dbUrl;
		}

		private String addArguments(String arg, HashSet<String> set) {
			// TODO Auto-generated method stub
			for (String str : set) {
				arg += ("\"" + str + "\"" + ",");
			}
			if (arg.equals(""))
				return arg;
			Log.d(TAG, arg.substring(0, arg.length() - 1));
			return arg.substring(0, arg.length() - 1);
		}

		public void complete(ScanActivity context) {
			// TODO Auto-generated method stub
			title = null;
			if (title == null) {
				getAlertInfo("Incomplete Info",
						"Please fill in the missing information", context);
				while (title == null) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Log.d("Complete", "Done");
			}

		}

		public void getAlertInfo(String prompt, String message,
				ScanActivity context) {
			// TODO Auto-generated method stub
			Log.d("Alert", "alert 1");
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
			alert.setTitle(prompt);
			alert.setMessage(message);

			// Set an EditText view to get user input
			final EditText input = new EditText(context);
			alert.setView(input);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							Log.d("Alert", value);
						}

					});

			/*
			 * alert.setNegativeButton("Cancel", new
			 * DialogInterface.OnClickListener() { public void
			 * onClick(DialogInterface dialog, int whichButton) { Log.d("Book",
			 * "Cancelled Alert"); } });
			 */
			Log.d("Alert", "alert 2");
			alert.show();
			Log.d("Alert", "alert 3");
		}
	}

	protected String downloadUrl(String url) throws IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		// Log.d(TAG, url);
		String result = null;
		String stream = "";
		try {
			HttpResponse response = client.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				while ((result = br.readLine()) != null) {
					// Log.d(TAG, result);
					stream += result;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream;
	}

	void refresh() {
		// TODO Auto-generated method stub
		loadTitle = true;
		loadAuthor = true;
		loadGenre = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_application, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
		case R.id.menu_titles:
			startActivity(new Intent(this, TitleActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.menu_authors:
			startActivity(new Intent(this, AuthorActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.menu_genres:
			startActivity(new Intent(this, GenreActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.menu_insert:
			startActivity(new Intent(this, ScanActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.menu_refresh:
			refresh();
			break;
		}

		return true;
	}
}
