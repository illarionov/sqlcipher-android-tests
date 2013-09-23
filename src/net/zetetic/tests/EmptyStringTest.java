package net.zetetic.tests;

import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

public class EmptyStringTest extends SQLCipherTest {

	@Override
	public boolean execute(SQLiteDatabase database) {
		String res = null;

		Cursor cursor = database.rawQuery("select ''", null);
		if (cursor == null) return false;
		if (cursor.moveToFirst()) {
			res = cursor.getString(0);
		}
		cursor.close();
		
		return "".equals(res);
	}

	@Override
	public String getName() {
		return "Select empty string test";
	}

}
