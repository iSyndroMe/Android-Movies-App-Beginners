package activitys.movie3dx.ariel_evso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import movie.moive3dx.Movie;
import movie.moive3dx.MovConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.ariel_evso_moveisapp.R;








import android.app.Fragment.SavedState;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SeacrhActivity extends ListActivity implements OnClickListener,
		OnItemClickListener {

	// private DbHandler dbHandler;
	private MovieAdapter adapter;

	List<Movie> moviesList = new ArrayList<Movie>();

	public String myResult = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_seacrh);

		ListView listView = getListView();
		
		adapter = new MovieAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);


		Button btnGo = (Button) findViewById(R.id.btnGo);
		Button btnCancel = (Button) findViewById(R.id.btnCancelSearch);
		btnGo.setOnClickListener(this);
		btnCancel.setOnClickListener(this);

	}
	
	public void onSaveInstanceState(Bundle savedState) {

	    super.onSaveInstanceState(savedState);

	    savedState.putString("myRes", myResult);

	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(state);
		
		myResult = state.getString("myRes");
		if(myResult != null){
		parseJSON(myResult);
		}

	}
	
	
	
	class getMovies extends AsyncTask<String, Void, String> {

		ProgressDialog dialog;
		EditText editSearch = (EditText) findViewById(R.id.editSearch);
		String textSearch = editSearch.getText().toString();

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(SeacrhActivity.this);
			dialog.setTitle("loading");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			BufferedReader input = null;
			HttpURLConnection connection = null;
			StringBuilder response = new StringBuilder();

			try {

				String queryString = "";
				queryString += "?s=" + URLEncoder.encode(textSearch, "utf-8");
				queryString += "&limit=" + MovConstants.RESULTS_LIMIT;

				// prepare a URL object :
				URL url = new URL(params[0] + queryString);

				// open a connection
				connection = (HttpURLConnection) url.openConnection();

				// check the result status of the conection:
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					// not good.
					return null;
				}

				// get the input stream from the connection
				// and make it into a buffered char stream.
				input = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));

				// read from the buffered stream line by line:
				String line = "";
				while ((line = input.readLine()) != null) {
					// append to the string builder:
					response.append(line + "\n");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				// close the stream if it exists:
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				// close the connectin if it exists:
				if (connection != null) {
					connection.disconnect();
				}
			}

			// get the string from the string builder:
			return response.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();

			myResult = result;
			
			parseJSON(result);
			
			
		}

	}
	
	private void parseJSON(String result) {
		moviesList.clear();

		if (result == null || result.length() == 0) {
			return;
		} else {

			// parse results from json

			try {
				JSONObject responseObject = new JSONObject(result);
				JSONArray resultsArray = responseObject
						.getJSONArray("Search");

				// Iterate over the JSON array:
				for (int i = 0; i < resultsArray.length(); i++) {
					// the JSON object in position i
					JSONObject movieObject = resultsArray.getJSONObject(i);

					// get the primitive values in the object
					String title = movieObject.getString("Title");
					String imdbID = movieObject.getString("imdbID");
					String year = movieObject.getString("Year");

					moviesList.add(new Movie(title, null, imdbID, year, null));

				}
			} catch (JSONException e) {
				Toast.makeText(
						SeacrhActivity.this,
						"Error Movie not found!" + "\n"
								+ "check if you enterd valid movie name",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}

			// notify/refresh adapter:
			adapter.notifyDataSetChanged();
		}
		
	}

	class MovieAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return moviesList.size();
		}

		@Override
		public Object getItem(int pos) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int pos) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				// well... inflate a new view
				view = getLayoutInflater().inflate(R.layout.list_movies,
						parent, false);
			}

			Movie movie = moviesList.get(position);

			TextView movieTitle = (TextView) view
					.findViewById(R.id.textMovieName);
			TextView movieYear = (TextView) view
					.findViewById(R.id.textMovieYear);

			movieTitle.setText(movie.getName());
			movieYear.setText(movie.getYear());

			return view;
		}

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btnGo:
			searchRequest();
			break;

		case R.id.btnCancelSearch:
			finish();
			break;

		}
	}

	private void searchRequest() {
		EditText editSerach = (EditText) findViewById(R.id.editSearch);

		String textSerach = editSerach.getText().toString();

		if (textSerach.equals("")) {
			Toast.makeText(this, "Please add text for search",
					Toast.LENGTH_SHORT).show();
		} else {
			new getMovies().execute(MovConstants.API_URL);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int pos, long id) {
		Movie movie = moviesList.get(pos);

		Intent intent = new Intent(this, EditActivity.class);
		intent.setAction(Intent.ACTION_WEB_SEARCH);
		intent.putExtra("imdbID", movie.getImdbID());

		startActivityForResult(intent, MovConstants.REQUEST_CODE_WEB);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case MovConstants.REQUEST_CODE_WEB:
			finish();
			break;

		}
	}

}
