package net.zetetic.tests.spatialite;

import android.text.TextUtils;
import android.util.Log;
import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase;
import net.zetetic.tests.SQLCipherTest;

public class DatabaseVersionTest extends SQLCipherTest {

	private static final String TAG = DatabaseVersionTest.class.getSimpleName();

	@Override
	public boolean execute(SQLiteDatabase database) {
		
		if (checkVersion(database, "spatialite_version()") == false) {
			return false;
		}
		
		if (checkVersion(database, "proj4_version()") == false) {
			return false;
		}
		
		if (checkVersion(database, "geos_version()") == false) {
			return false;
		}
		
		return true;
	}

	private boolean checkVersion(SQLiteDatabase database, String sqliteExpr) {
		String version;

		version = DatabaseUtils.stringForQuery(database, "SELECT " + sqliteExpr, null);

		if (TextUtils.isEmpty(version)) {
			Log.e(TAG, sqliteExpr + " failed");
		}else {
			Log.i(TAG, sqliteExpr + ": " + version);
		}

		return !TextUtils.isEmpty(version);
	}
	
	@Override
	public String getName() {
		return "Spatialite DatabaseVersionTest";
	}

}
