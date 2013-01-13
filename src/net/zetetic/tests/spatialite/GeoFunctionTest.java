package net.zetetic.tests.spatialite;

import java.io.File;
import java.io.IOException;

import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase;
import net.zetetic.ZeteticApplication;
import net.zetetic.tests.SQLCipherTest;

public class GeoFunctionTest extends SQLCipherTest  {

	private static final String TAG = GeoFunctionTest.class.getSimpleName();
	
	@Override
	public boolean execute(SQLiteDatabase database) {

		File databaseFile = ZeteticApplication.getInstance().getDatabasePath(ZeteticApplication.SPATIALITE_DATABASE);

		databaseFile.mkdirs();
		databaseFile.delete();

		try {
			database.close();
			ZeteticApplication.getInstance().extractAssetToDatabaseDirectory(ZeteticApplication.SPATIALITE_DATABASE);
			database =  SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), "", null, SQLiteDatabase.OPEN_READONLY);

			if (testGeometryRepresentation01(database) == false) return false;
			if (testGeometryRepresentation02(database) == false) return false;
			if (testGeometryRepresentation03(database) == false) return false;
			if (testGeometryClass(database) == false) return false;
			if (testGeometryEnvelope(database) == false) return false;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally {
			databaseFile.delete();
		}
		
		return true;
	}

	@Override
	public String getName() {
		return "Spatialite " + TAG;
	}
	
	private boolean testGeometryRepresentation01(SQLiteDatabase database) {
		Cursor c;
		String name, geometry;
		long peoples;
		double x, y;
		final String[] selectionArgs = new String[] { "350000" };

		c = database.rawQuery(
				"SELECT name, peoples, HEX(Geometry) from Towns where peoples > ? order by peoples DESC", 
				selectionArgs);
		if (c == null) return false;

		if (c.moveToFirst() == false) {
			c.close();
			return false;
		}
		name = c.getString(0);
		peoples = c.getLong(1);
		geometry = c.getString(2);
		
		c.close();
		
		if (!TextUtils.equals(name, "Roma")
				|| (peoples != 2546804)
				|| (!TextUtils.equals(geometry, "0001787F00003D0AD723BF11284133333313C1B851413D0AD723BF11284133333313C1B851417C010000003D0AD723BF11284133333313C1B85141FE"))
				) {
			Log.e(TAG, "Query failed. name/peoples/geometry: "
					+ name + "/" + String.valueOf(peoples) + "/" + geometry);
			return false;
		}
		
		c = database.rawQuery(
				"SELECT name, peoples, AsText(Geometry) from Towns where peoples > ? order by peoples DESC",
				selectionArgs);
		if (c == null) return false;

		if (c.moveToFirst() == false) {
			c.close();
			return false;
		}
		name = c.getString(0);
		peoples = c.getLong(1);
		geometry = c.getString(2);
		
		c.close();
		if (!TextUtils.equals(name, "Roma")
				|| (peoples != 2546804)
				|| (!TextUtils.equals(geometry, "POINT(788703.57 4645636.3)"))
				) {
			Log.e(TAG, "Query failed. name/peoples/geometry: "
				+ name + "/" + String.valueOf(peoples) + "/" + geometry);
			return false;
		}
		
		c = database.rawQuery(
				"SELECT name, X(Geometry), Y(Geometry) from Towns where peoples > ? order by peoples DESC",
				selectionArgs);
		if (c == null) return false;

		if (c.moveToFirst() == false) {
			c.close();
			return false;
		}
		
		name = c.getString(0);
		x = c.getDouble(1);
		y = c.getDouble(2);
		if (!TextUtils.equals(name, "Roma")
				|| (Double.compare(x, 788703.57) != 0)
				|| (Double.compare(y, 4645636.3) != 0)) {
			Log.e(TAG, "Query failed. Name/x/y: " + name + "/" + String.valueOf(x) + "/" + String.valueOf(y));
			return false;
		}
		
		return true;
	}
	
	private boolean testGeometryRepresentation02(SQLiteDatabase database) {
		String geom;
		final String selectionArgs[] = new String[] { "POINT(10 20)" };
	
		geom = DatabaseUtils.stringForQuery(database, "SELECT HEX(GeomFromText(?))",
				selectionArgs);
		
		if (!TextUtils.equals(geom, "00010000000000000000000024400000000000003440000000000000244000000000000034407C0100000000000000000024400000000000003440FE")) {
			Log.e(TAG, "HEX(GeomFromText()) failed. geom: " + geom);
			return false;
		}
		
		geom = DatabaseUtils.stringForQuery(database, "SELECT HEX(AsBinary(GeomFromText(?)))",
				selectionArgs);
		if (!TextUtils.equals(geom, "010100000000000000000024400000000000003440")) {
			Log.e(TAG, "HEX(AsBinary(GeomFromText())) failed. geom: " + geom);
			return false;
		}
		
		geom = DatabaseUtils.stringForQuery(database, "SELECT AsText(GeomFromWKB(X'010100000000000000000024400000000000003440'))",
				null);
		if (!TextUtils.equals(geom, "POINT(10 20)")) {
			Log.e(TAG, "GeomFromWKB() failed. geom: " + geom);
			return false;
		}
		
		return true;
	}
	
	private boolean testGeometryRepresentation03(SQLiteDatabase database) {
		String geom;

		geom = DatabaseUtils.stringForQuery(database,
				"SELECT AsKml(Geometry) FROM Highways WHERE PK_UID = 2",
				null);
		if (!TextUtils.equals(geom, "<LineString><coordinates>11.13099600000591,43.82077199999232 11.13146800000592,43.82066499999232</coordinates></LineString>")) {
			Log.e(TAG, "AsKml() failed. geom: " + geom);
			return false;
		}
		
		geom = DatabaseUtils.stringForQuery(database,
				"SELECT AsGeoJSON(Geometry, 2) FROM Highways WHERE PK_UID = 2",
				null);
		if (!TextUtils.equals(geom, "{\"type\":\"LineString\",\"coordinates\":[[671365.87,4854173.77],[671404.13,4854162.86]]}")) {
			Log.e(TAG, "AsGeoJSON() failed. geom: " + geom);
			return false;
		}
		
		return true;
	}
	
	private boolean testGeometryClass(SQLiteDatabase database) {
		Cursor c;
		
		/* Statement 1 */
		{
			c = database.rawQuery(
					"SELECT PK_UID, AsText(Geometry) FROM HighWays WHERE PK_UID = 2", 
					null);
			if (c == null) return false;

			if (c.moveToFirst() == false) {
				c.close();
				return false;
			}

			long pkUid = c.getLong(0);
			String geom = c.getString(1);

			c.close();

			if ( (pkUid != 2) || 
					!TextUtils.equals(geom, "LINESTRING(671365.867442 4854173.770802, 671404.13073 4854162.864623)") ) {
				Log.e(TAG, "AsText() failed. pkUid: " + pkUid + " geom: " + geom);
				return false;
			}
		}
		
		/* Statement 2 */
		{
			c = database.rawQuery(
					"SELECT PK_UID, NumPoints(Geometry), GLength(Geometry), Dimension(Geometry), " + 
							"GeometryType(Geometry) " +
							"FROM HighWays " + 
							"ORDER BY NumPoints(Geometry) DESC " + 
							"LIMIT 1", 
							null);
			if (c == null) return false;

			if (c.moveToFirst() == false) {
				c.close();
				return false;
			}

			long pkUid = c.getLong(0);
			long numPoints = c.getLong(1);
			double gLength = c.getDouble(2);
			long dimension = c.getLong(3);
			String geometryType = c.getString(4);

			c.close();
			
			if ( (pkUid != 774)
					|| (numPoints != 6758)
					|| (Double.compare(gLength, 94997.87213441564) != 0)
					|| (dimension != 1)
					|| (!TextUtils.equals(geometryType, "LINESTRING"))
					) {
				Log.e(TAG, "ORDER BY NumPoints() query failed." +
					" pkUid: " + String.valueOf(pkUid) + " numPoints: " + String.valueOf(numPoints) +
					" gLength: " + String.valueOf(gLength) + " dimension:" + String.valueOf(dimension) +
					" geometryType: " + geometryType
					);
				return false;
			}
		}
		
		/* Statement 3 */
		{
			String name = DatabaseUtils.stringForQuery(database,
					"SELECT name, AsText(Geometry) FROM Regions WHERE PK_UID = 52",
					null);
			if (!TextUtils.equals(name, "EMILIA-ROMAGNA")) {
				Log.e(TAG, "AsText(Geometry) FROM Regions query failed. name:" + name);
				return false;
			}
		}
		
		/* Statement 4 */
		{
			c = database.rawQuery(
					"SELECT PK_UID, NumInteriorRings(Geometry), NumPoints(ExteriorRing(Geometry)), " + 
							"NumPoints(InteriorRingN(Geometry, 1)) " + 
					"FROM regions " + 
					"ORDER BY NumInteriorRings(Geometry) DESC " + 
					"LIMIT 5",
							null);
			if (c == null) return false;

			if (c.moveToFirst() == false) {
				c.close();
				return false;
			}
			
			long pkUid = c.getLong(0);
			long numRings = c.getLong(1);
			long numExtPoints = c.getLong(2);
			long numIntPoints = c.getLong(3);

			c.close();
			
			if ( (pkUid != 55) || (numRings != 1) || (numExtPoints != 602) || (numIntPoints != 9)) {
				Log.e(TAG, "NumInteriorRings() query failed. pkUid:" + String.valueOf(pkUid) +
						" numRings: " + String.valueOf(numRings) + " numExtPoints: " + 
						String.valueOf(numExtPoints) + " numIntPoints: " + 
						String.valueOf(numIntPoints));
				return false;
			}
		}
		
		/* Statement 5 */
		{
			c = database.rawQuery(
					"SELECT Round(GLength(Geometry)), IsClosed(Geometry), NumPoints(Geometry) " + 
					"FROM Highways " +
					"WHERE PK_UID = 2",
					null);
			if (c == null) return false;

			if (c.moveToFirst() == false) {
				c.close();
				return false;
			}
			
			double gLength =  c.getDouble(0);
			int isClosed = c.getInt(1);
			long numPoints = c.getLong(2);
			
			c.close();
			
			if ((Double.compare(gLength, 40.0) != 0) || (isClosed != 0) || (numPoints != 2)) {
				Log.e(TAG, "Highways(2) query failed. gLength:" + String.valueOf(gLength) +
						" isClosed: " + String.valueOf(isClosed) + " numPoints: " + 
						String.valueOf(numPoints));
				return false;
			}
		}
		
		return true;
	}
	
	private boolean testGeometryEnvelope(SQLiteDatabase database) {
		Cursor c = database.rawQuery(
				"SELECT Name, AsText(Envelope(Geometry)) FROM Regions LIMIT 5",
				null);
		if (c == null) return false;

		if (c.moveToFirst() == false) {
			c.close();
			return false;
		}
		
		String name = c.getString(0);
		String geom = c.getString(1);
		
		c.close();
		
		if (!TextUtils.equals(name, "VENETO")
				|| !TextUtils.equals(geom, "POLYGON((752912.250297 5027429.54477, 753828.826422 5027429.54477, 753828.826422 5028928.677375, 752912.250297 5028928.677375, 752912.250297 5027429.54477))")
				) {
			Log.e(TAG, "testGeometryEnvelope query failed. name:" + name + " geom: " + geom);
			return false;
		}
		
		return true;
	}

}
