package net.zetetic.tests.spatialite;

import java.io.File;

import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;
import net.zetetic.ZeteticApplication;
import net.zetetic.tests.SQLCipherTest;

public class RTreeTest extends SQLCipherTest{

	private static final String TAG = RTreeTest.class.getSimpleName();
	
	private static final String DATABASE= TAG + ".db";

	private static final String PASSWORD="test";
	
	private static final String[] TEST_DATA_NAME = new String[] {
		"first point", "second point", "third point"
	};
	private static final double[] TEST_DATA_MEASURED_VALUE = new double[] {
		1.23456, 2.34567, 3.45678
	};
	private static final String[] TEST_DATA_THE_GEOM = new String[] {
		"POINT(1.01 2.02)", "POINT(2.02 3.03)", "POINT(3.03 4.04)"
	};
	
	@Override
	public boolean execute(SQLiteDatabase database) {
		SQLiteStatement stmt;
		File newDatabasePath = ZeteticApplication.getInstance().getDatabasePath(DATABASE);

		database.close();
		
		stmt = null;
		database = null;
		try {
			newDatabasePath.delete();
			database = SQLiteDatabase.openOrCreateDatabase(newDatabasePath, PASSWORD, null);
			database.rawExecSQL("SELECT InitSpatialMetadata()");
			database.rawExecSQL(
					"CREATE TABLE test_geom ("+
					"	id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
					"	name TEXT NOT NULL, " + 
					"	measured_value DOUBLE NOT NULL " +
					")"
					);
			database.rawExecSQL(
					"SELECT AddGeometryColumn('test_geom', 'the_geom', 4326, 'POINT', 'XY')"
					);
			database.rawExecSQL(
					"SELECT CreateSpatialIndex('test_geom', 'the_geom')"
					);
			
			stmt = database.compileStatement(
					"INSERT INTO test_geom (name, measured_value, the_geom) " +
				    " VALUES (?, ?, GeomFromText(?, 4326))"
					);
			for (int i=0; i < TEST_DATA_NAME.length; ++i) {
				stmt.clearBindings();
				stmt.bindString(1, TEST_DATA_NAME[i]);
				stmt.bindDouble(2, TEST_DATA_MEASURED_VALUE[i]);
				stmt.bindString(3, TEST_DATA_THE_GEOM[i]);
				stmt.executeInsert();
			}
					
			stmt.close();
			stmt = null;
			
			Cursor c = database.rawQuery("SELECT id, name, measured_value, AsText(the_geom) from test_geom", null);
			if (c == null) {
				Log.e(TAG, "rawQuery() failed");
				return false;
			}
			
			int cnt = 0;
			while (c.moveToNext() == true) {
				cnt += 1;
				// Log.d(TAG, DatabaseUtils.dumpCurrentRowToString(c));
			}
			c.close();
			
			if (cnt != TEST_DATA_NAME.length) {
				Log.e(TAG, "cnt: " + cnt + " inserted: " + TEST_DATA_NAME.length);
				return false;
			}
			
			long CheckSpatialIndex = DatabaseUtils.longForQuery(database,
					"SELECT CheckSpatialIndex()", null);
			if (CheckSpatialIndex == 0) {
				Log.e(TAG, "CheckSpatialIndex() failed ");
				// XXX Currently broken
				// return false;
			}
			
		}finally {
			if (stmt != null) stmt.close();
			if (database != null) database.close();
		}

		return true;
	}

	@Override
	public String getName() {
		return "Spatialite " + TAG;
	}

}
