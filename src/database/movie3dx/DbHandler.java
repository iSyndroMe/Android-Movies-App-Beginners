package database.movie3dx;


import movie.moive3dx.Movie;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbHandler {

	private DbOpenHelper dbOpenHelper;

	public DbHandler(Context c) {
		dbOpenHelper = new DbOpenHelper(c);
	}

	public void insert(Movie movie) {
		// get a WRITABLE database instance:
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		// the values to insert:
		ContentValues values = new ContentValues();
		values.put(DbConstants.MOVIE_NAME, movie.getName());
		values.put(DbConstants.MOVIE_DSEC, movie.getDescription());
		values.put(DbConstants.MOVIE_YEAR, movie.getYear());
		values.put(DbConstants.MOVIE_RATING, movie.getRating());

		// insert
		db.insertOrThrow(DbConstants.TABLE_MOVIES, null, values);

		// writable databases must be closed!
		db.close();
	}

	public void update(Movie movie) {
		// get a WRITABLE database instance:
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		// the values to insert:
		ContentValues values = new ContentValues();
		values.put(DbConstants.MOVIE_NAME, movie.getName());
		values.put(DbConstants.MOVIE_DSEC, movie.getDescription());
		values.put(DbConstants.MOVIE_RATING, movie.getRating());

		// insert
		// db.update(table, values, whereClause, whereArgs)
		db.updateWithOnConflict(DbConstants.TABLE_MOVIES, values,
				DbConstants.MOVIE_ID + " =?",
				new String[] { String.valueOf(movie.getId()) },
				SQLiteDatabase.CONFLICT_REPLACE);

		// writable databases must be closed!
		db.close();
	}

	public void delete(Long id) {
		// get a WRITABLE database instance:
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		// delete
		// db.delete(table, whereClause, whereArgs)
		db.delete(DbConstants.TABLE_MOVIES, DbConstants.MOVIE_ID + "=?",
				new String[] { String.valueOf(id) });

		// writable databases must be closed!
		db.close();
	}

	public void deleteAll() {
		// get a WRITABLE database instance:
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		// delete all!
		// db.delete(table, whereClause, whereArgs)
		db.delete(DbConstants.TABLE_MOVIES, null, // no select - all items!
				null);

		// writable databases must be closed!
		db.close();
	}

	public Cursor queryAll() {
		// get a READABLE database instance:
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(DbConstants.TABLE_MOVIES, null, null,
				null, null, null, DbConstants.MOVIE_NAME
						+ " COLLATE LOCALIZED ASC");

		return cursor;
	}

	public Movie query(Long id) {
		// get a READABLE database instance:
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

		Cursor cursor = db.query(DbConstants.TABLE_MOVIES, null,
				DbConstants.MOVIE_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null);

		Movie movie = null;

		// get the data:
		if (cursor.moveToNext()) {
			String name = cursor.getString(cursor
					.getColumnIndex(DbConstants.MOVIE_NAME));
			String descrption = cursor.getString(cursor
					.getColumnIndex(DbConstants.MOVIE_DSEC));
			String rating = cursor.getString(cursor
					.getColumnIndex(DbConstants.MOVIE_RATING));
		

			movie = new Movie(name, descrption , rating);
			// remember the id :
			movie.setId(id);
			
			cursor.close();
			return movie;
			
		}else{return null;}
		
		
	}

}
