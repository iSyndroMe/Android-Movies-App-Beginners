package database.movie3dx;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

	public static String DB_NAME = "movies.db";

	public static int DB_VERSION = 1;

	public DbOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String sql = "" 
				+ "CREATE TABLE " + DbConstants.TABLE_MOVIES + "("
				+ DbConstants.MOVIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DbConstants.MOVIE_NAME + " TEXT,"
				+ DbConstants.MOVIE_DSEC + " TEXT,"
				+ DbConstants.MOVIE_YEAR + " TEXT,"
				+ DbConstants.MOVIE_RATING + " TEXT" + ")";
		
		db.execSQL(sql);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		String sql = "DROP TABLE IF EXISTS " + DbConstants.TABLE_MOVIES;
		db.execSQL(sql);
		onCreate(db);

	}

}
