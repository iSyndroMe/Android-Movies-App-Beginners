package activitys.movie3dx.ariel_evso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import movie.moive3dx.HandleSorage;
import movie.moive3dx.Movie;
import movie.moive3dx.MovConstants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ariel_evso_moveisapp.R;

import database.movie3dx.DbConstants;
import database.movie3dx.DbHandler;

public class MainActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private DbHandler dbHandler;
	private MoviesAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dbHandler = new DbHandler(this);
		Cursor cursor = dbHandler.queryAll();
		startManagingCursor(cursor);
		
		

		Button btnSettings = (Button) findViewById(R.id.btnSettings);
		Button btnAdd = (Button) findViewById(R.id.btnAdd);

		btnSettings.setOnClickListener(this);
		btnAdd.setOnClickListener(this);

		GridView gw = (GridView) findViewById(R.id.gridView1);

		adapter = new MoviesAdapter(this, cursor);

		gw.setAdapter(adapter);

		gw.setOnItemClickListener(this);

		registerForContextMenu(gw);
	}

	class MoviesAdapter extends CursorAdapter {

		public MoviesAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View v, Context context, Cursor cursor) {

			String movieName = cursor.getString(cursor
					.getColumnIndex(DbConstants.MOVIE_NAME));
			String movieYear = cursor.getString(cursor
					.getColumnIndex(DbConstants.MOVIE_YEAR));
			String movieRating = cursor.getString(cursor
					.getColumnIndex(DbConstants.MOVIE_RATING));
			
		
			

			TextView textMovieName = (TextView) v
					.findViewById(R.id.textMovieName);
			TextView textMovieYear = (TextView) v
					.findViewById(R.id.textMovieYear);
			ImageView imageView = (ImageView) v
					.findViewById(R.id.imageMovieFav);
			TextView textRating = (TextView) v
					.findViewById(R.id.textRating_movie);
			
			LinearLayout ly = (LinearLayout) v.findViewById(R.id.LinearLayoutGWmovies);
			ImageView imgLike = (ImageView) v.findViewById(R.id.imageViewLike);
			
			float rating = Float.parseFloat(movieRating);
			
			RatingBar rt = (RatingBar) v.findViewById(R.id.ratingBar);
			
			rt.setRating(rating/2);
			
			if(rating < 7.0f){
				//ly.setBackgroundColor(Color.rgb( 246 , 84 , 106 ));
				imgLike.setImageResource(R.drawable.no_32);
			}else{
				imgLike.setImageResource(R.drawable.yes_32);
			}

			File file = getDir("imageDir", Context.MODE_PRIVATE);
			String path = file.toString();
			
			Bitmap bitmap = loadImageFromStorage(path , movieName);
			

			textMovieName.setText(movieName);
			textMovieYear.setText(movieYear);
			textRating.setText(movieRating);
			imageView.setImageBitmap(bitmap);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.list_favorite_movies,
					parent, false);
		}

	}

	public Bitmap StringToBitMap(String encodedString) {
		try {
			byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
					encodeByte.length);
			return bitmap;
		} catch (Exception e) {
			e.getMessage();
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actionDeleteAll:
			// check if there is any items in the list
			if (adapter.getCount() != 0) {
				alertDeleteAll();
			} else {
				Toast.makeText(this, "No Items in the list", Toast.LENGTH_SHORT)
						.show();
			}
			break;

		case R.id.actionExit:
			onBackPressed();
			break;

		}

		return true;
	}

	private void alertDeleteAll() {

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					dbHandler.deleteAll();
					File dir = getDir("imageDir", Context.MODE_PRIVATE);
					HandleSorage.deleteDir(dir);				
					refreshTheOutput();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// do nothing
					break;
				}
			}

		};

		// the dialog builder:
		Builder builder = new AlertDialog.Builder(this);

		// set the dialog properties:
		builder.setTitle("Wait!").setMessage("You sure, delete all movies?")
				.setPositiveButton("Yes", listener)
				.setNegativeButton("No", listener);

		// create the actual dialog:
		AlertDialog dialog = builder.create();

		// show the dialog:
		dialog.show();

	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// trick: get the id in the list:
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		long id = info.id;
		
		Movie mv = dbHandler.query(id);

		Intent intent;
		// handle action:
		switch (item.getItemId()) {

		case R.id.action_edit:
			// intent to open EditActivity
			intent = new Intent(this, EditActivity.class);
			// the action will be ACTION_EDIT
			intent.setAction(Intent.ACTION_EDIT);

			// send the movie id:
			intent.putExtra("id", id);

			// open activity
			startActivity(intent);
			break;

		case R.id.action_delete:
			// delete movie:
			dbHandler.delete(id);
			deleteFromStorage(mv.getName());
			refreshTheOutput();
			break;
		}

		return true;

	}

	private void deleteFromStorage(String n) {
		
		try {
			File dir = getDir("imageDir", Context.MODE_PRIVATE);
			File file = new File(dir, n + ".jpg");
			boolean deleted = file.delete();
			
			if(deleted){
				Toast.makeText(this, "Movie was successful deleted", Toast.LENGTH_SHORT).show();
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btnSettings:
			openOptionsMenu();
			break;

		case R.id.btnAdd:
			alertHowToAdd();
			break;
		}

	}

	private void alertHowToAdd() {
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent;
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					intent = new Intent(MainActivity.this, EditActivity.class);
					intent.setAction(Intent.ACTION_INSERT);
					startActivity(intent);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					intent = new Intent(MainActivity.this, SeacrhActivity.class);
					startActivity(intent);
					break;
				}
			}

		};

		// the dialog builder:
		Builder builder = new AlertDialog.Builder(this);

		// set the dialog properties:
		builder.setTitle("How to Add?").setMessage("Choose one of the options")
				.setPositiveButton("Manual", listener)
				.setNegativeButton("Web Search", listener);

		// create the actual dialog:
		AlertDialog dialog = builder.create();

		// show the dialog:
		dialog.show();

	}

	@Override
	public void onItemClick(AdapterView<?> listView, View itemView, int pos,
			long id) {
		Intent intent = new Intent(this, EditActivity.class);
		intent.setAction(Intent.ACTION_EDIT);
		intent.putExtra("id", id);
		startActivity(intent);

	}

	@Override
	public void onBackPressed() {
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					finish();
					break;

				case DialogInterface.BUTTON_NEGATIVE:

					break;
				}
			}

		};

		// the dialog builder:
		Builder builder = new AlertDialog.Builder(this);

		// set the dialog properties:
		builder.setTitle("Wait!").setMessage("Dont leave :(")
				.setPositiveButton("Leave", listener)
				.setNegativeButton("Stay", listener);

		// create the actual dialog:
		AlertDialog dialog = builder.create();

		// show the dialog:
		dialog.show();

	}
	
	private Bitmap loadImageFromStorage(String path , String file){
		
	    try {
	        File f = new File(path, file + ".jpg");
	        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
	        return b;
	    } 
	    catch (FileNotFoundException e) 
	    {
	        e.printStackTrace();
	    }
		return null;

	}

	private void refreshTheOutput() {

		Cursor oldCursor = adapter.getCursor();

		Cursor newCursor = dbHandler.queryAll();
		startManagingCursor(newCursor);

		adapter.changeCursor(newCursor);

		stopManagingCursor(oldCursor);
		oldCursor.close();
	}

}
