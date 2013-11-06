package net.zetetic.tests.spatialite;

import java.util.List;

import net.zetetic.tests.SQLCipherTest;

public class SpatialiteTestSuiteRunner extends net.zetetic.tests.TestSuiteRunner {

	public SpatialiteTestSuiteRunner() {
		super();
	}

	@Override
	protected List<SQLCipherTest> getTestsToRun() {
		List<SQLCipherTest> tests;
		
		tests = super.getTestsToRun();
		
		// tests.clear();
		tests.add(new DatabaseVersionTest());
		tests.add(new RTreeTest());
		tests.add(new GeoFunctionTest());
		
		return tests;
	}

}
