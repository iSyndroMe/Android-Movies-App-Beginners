package activitys.movie3dx.ariel_evso;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import movie.moive3dx.Movie;
import movie.moive3dx.MovConstants;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ariel_evso_moveisapp.R;

import database.movie3dx.DbHandler;

public class EditActivity extends Activity implements OnClickListener, OnRatingBarChangeListener {



	private DbHandler dbHandler;
	private static final String TAG = "edit_activity";
	private float rating;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);

		dbHandler = new DbHandler(this);

		Button btnSave = (Button) findViewById(R.id.btnSave);
		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		RatingBar rateBar = (RatingBar) findViewById(R.id.ratingBarMvRate);
		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		rateBar.setOnRatingBarChangeListener(this);
		
		checkWhoCallUs();
		
	}

	private void checkWhoCallUs() {
		Intent callingIntent = getIntent();
		EditText editName = (EditText) findViewById(R.id.editMovie);
		EditText editDesc = (EditText) findViewById(R.id.editDescrption);
		TextView mainTitle = (TextView) findViewById(R.id.editTitle);
		RatingBar rateBar = (RatingBar) findViewById(R.id.ratingBarMvRate);
		TableLayout tableMovies = (TableLayout) findViewById(R.id.tbMovieDetails);
		Movie movie = null;

		if (callingIntent.getAction().equals(Intent.ACTION_INSERT)) {
			Log.d(TAG, "from insert");
			String text = "Add New Movie";
			mainTitle.setText(text);
			tableMovies.setVisibility(View.GONE);

		} else if (callingIntent.getAction().equals(Intent.ACTION_EDIT)) {

			Log.d(TAG, "from edit");

			long id = callingIntent.getLongExtra("id", -1);
			movie = dbHandler.query(id);
			Log.d(TAG, "id" + id);
			
			float halfRating = Float.parseFloat(movie.getRating())/2;

			editName.setText(movie.getName());
			editDesc.setText(movie.getDescription());
			rateBar.setRating(halfRating);
			tableMovies.setVisibility(View.GONE);
		} else if (callingIntent.getAction().equals(Intent.ACTION_WEB_SEARCH)) {
			rateBar.setVisibility(View.GONE);
			new showMovieDetails().execute(MovConstants.API_URL);
		}

	}

	@Override
	public void onClick(View v) {
		
		EditText editName = (EditText) findViewById(R.id.editMovie);
		EditText editDesc = (EditText) findViewById(R.id.editDescrption);

		// get data from textViews:
		String movieName = editName.getText().toString();
		String descrption = editDesc.getText().toString();


		switch (v.getId()) {
		case R.id.btnSave:
			if (movieName.equals("") || descrption.equals("")) {
				Toast.makeText(this, "Enter moive name and descrption",
						Toast.LENGTH_SHORT).show();
			} else {
				saveChanges();
				finish();
			}
			break;
		case R.id.btnCancel:
			finish();
			break;

		}
	}

	private void saveChanges() {

		EditText editName = (EditText) findViewById(R.id.editMovie);
		EditText editDesc = (EditText) findViewById(R.id.editDescrption);

		// get data from textViews:
		String movieName = editName.getText().toString();
		String descrption = editDesc.getText().toString();

		// create a new movie object:
		Movie movie = null;

		// get the calling intent
		Intent callingIntent = getIntent();

		if (callingIntent.getAction().equals(Intent.ACTION_INSERT)) {
			movie = new Movie(movieName, descrption, String.valueOf(rating));
			// this is a new movie.
			dbHandler.insert(movie);
		} else if (callingIntent.getAction().equals(Intent.ACTION_EDIT)) {

			long id = callingIntent.getLongExtra("id", 0);
			movie = new Movie(movieName, descrption, String.valueOf(rating));

			movie.setId(id);

			dbHandler.update(movie);

		} else if (callingIntent.getAction().equals(Intent.ACTION_WEB_SEARCH)) {
			TextView textYear =(TextView) findViewById(R.id.year_movie);
			TextView textRating =(TextView) findViewById(R.id.imdbRating_movie);
			String year = textYear.getText().toString();
			String rating = textRating.getText().toString();
			movie = new Movie(movieName, descrption , null , year , rating);
			saveToInternalSorage(MovConstants.IMG_BITMAP);
			Intent resultIntent = new Intent();
			dbHandler.insert(movie);
			setResult(RESULT_OK, resultIntent);

		}

	}

	class showMovieDetails extends AsyncTask<String, Void, String> {

		Intent callingIntent = getIntent();
		String imdbID = callingIntent.getStringExtra("imdbID");

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			// start the progress dialog
			progressDialog = new ProgressDialog(EditActivity.this);
			progressDialog.setTitle("It may take few seconeds...");
			// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			BufferedReader input = null;
			HttpURLConnection connection = null;
			StringBuilder response = new StringBuilder();

			try {

				String queryString = "";
				queryString += "?i=" + URLEncoder.encode(imdbID, "utf-8");

				// prepare a URL object :
				URL url = new URL(params[0] + queryString);

				// open a connection
				connection = (HttpURLConnection) url.openConnection();

				// stream = connection.getInputStream();

				// get the length:
				int length = connection.getContentLength();
				// tell the progress dialog the length:
				// this CAN (!!) be modified outside the UI thread !!!
				progressDialog.setMax(length);

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

			try {
				progressDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}

			EditText nameMovie = (EditText) findViewById(R.id.editMovie);
			EditText plotDesc = (EditText) findViewById(R.id.editDescrption);
			TextView movieTitle = (TextView) findViewById(R.id.title_movie);
			TextView movieGenre = (TextView) findViewById(R.id.genere_movie);
			TextView movieYear = (TextView) findViewById(R.id.year_movie);
			TextView movieReleased = (TextView) findViewById(R.id.released_movie);
			TextView movieRuntime = (TextView) findViewById(R.id.runtime_movie);
			TextView movieRating = (TextView) findViewById(R.id.imdbRating_movie);

			if (result == null || result.length() == 0) {
				// no result:
				// textOutput.setText("error loading data...");
			} else {

				// parse results from json

				try {
					JSONObject movieObject = new JSONObject(result);

					String title = movieObject.getString("Title");
					String plot = movieObject.getString("Plot");
					String poster = movieObject.getString("Poster");
					String genre = movieObject.getString("Genre");
					String year = movieObject.getString("Year");
					String released = movieObject.getString("Released");
					String runTime = movieObject.getString("Runtime");
					String rating = movieObject.getString("imdbRating");

					Movie movie = new Movie(title, plot, null, year , rating);
					
					MovConstants.MOVIE_TITLE = title;
					

					/*************************************************
					 fill the table & and edit text output on the edit
					 activity XML
					 *************************************************/
					
					//edit views
					nameMovie.setText(movie.getName());
					plotDesc.setText(movie.getDescription());
					//table
					movieTitle.setText(title);
					movieGenre.setText(genre);
					movieYear.setText(year);
					movieReleased.setText(released);
					movieRuntime.setText(runTime);
					movieRating.setText(rating);
					
					//open task to get the image with the URL("poster") that we just grab from JSON
					new getMovieImage().execute(poster);

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

		}
	}

	class getMovieImage extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... params) {
			// in the background:

			// get the address from the params:
			String address = params[0];

			HttpURLConnection connection = null;
			InputStream stream = null;
			ByteArrayOutputStream outputStream = null;

			// the bitmap will go here:
			Bitmap b = null;

			try {
				// build the URL:
				URL url = new URL(address);
				// open a connection:
				connection = (HttpURLConnection) url.openConnection();

				// check the connection response code:
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					// not good..
					return null;
				}

				// the input stream:
				stream = connection.getInputStream();

				// get the length:
				int length = connection.getContentLength();
				// tell the progress dialog the length:
				// this CAN (!!) be modified outside the UI thread !!!
				// progressDialog.setMax(length);

				// a stream to hold the read bytes.
				// (like the StringBuilder we used before)
				outputStream = new ByteArrayOutputStream();

				// a byte buffer for reading the stream in 1024 bytes chunks:
				byte[] buffer = new byte[1024];

				int totalBytesRead = 0;
				int bytesRead = 0;

				// read the bytes from the stream
				while ((bytesRead = stream.read(buffer, 0, buffer.length)) != -1) {
					totalBytesRead += bytesRead;
					outputStream.write(buffer, 0, bytesRead);

					// notify the UI thread on the progress so far:
					// publishProgress(totalBytesRead);
					Log.d("TAG", "progress: " + totalBytesRead + " / " + length);
				}

				// flush the output stream - write all the pending bytes in its
				// internal buffer.
				outputStream.flush();

				// get a byte array out of the outputStream
				// theses are the bitmap bytes
				byte[] imageBytes = outputStream.toByteArray();

				// use the BitmapFactory to convert it to a bitmap
				b = BitmapFactory.decodeByteArray(imageBytes, 0, length);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					// close connection:
					connection.disconnect();
				}
				if (outputStream != null) {
					try {
						// close output stream:
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return b;
		}

		/*
		 * maybe use it later, if so don't forget change progress Void to
		 * Integer!
		 * 
		 * @Override protected void onProgressUpdate(Integer... values) { //on
		 * the UI thread
		 * 
		 * //progressDialog.setProgress(values[0]); }
		 */

		protected void onPostExecute(Bitmap result) {
			// back on the UI thread

			// close the progress dialog
			// progressDialog.dismiss();

			ImageView imageView = (ImageView) findViewById(R.id.imageMovie);

			if (result == null) {
				Toast.makeText(EditActivity.this, "Error loading image",
						Toast.LENGTH_LONG).show();
			} else {
				// set the image bitmap:
				imageView.setImageBitmap(result);
				MovConstants.IMG_BITMAP = result;
			}
		};
	}
	
	private String saveToInternalSorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
         // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, MovConstants.MOVIE_TITLE + ".jpg");

        FileOutputStream fos = null;
        try {           

            fos = new FileOutputStream(mypath);

       // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {		
		switch (ratingBar.getId()) {
		case R.id.ratingBarMvRate:
			this.rating = (rating * 2);
			break;

		default:
			break;
		}
	}
}
