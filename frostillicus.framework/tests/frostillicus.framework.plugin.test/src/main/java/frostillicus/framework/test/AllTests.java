package frostillicus.framework.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.ibm.commons.Platform;
import com.ibm.commons.util.io.StreamUtil;

import frostillicus.framework.test.models.TestModels;
import frostillicus.xsp.util.FrameworkUtils;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import org.openntf.domino.Session;
import org.openntf.domino.utils.Factory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestModels.class
})
public class AllTests {

	public static String MODELS_DB_PATH;

	public static lotus.domino.Session lotusSession;
	public static Session session;

	@BeforeClass
	public static void init() throws Exception {
		// MUST use sysout because the log handler crashes when the Notes thread has not been initialized
		System.out.println("Init " + AllTests.class.getName()); //$NON-NLS-1$

		NotesThread.sinitThread();
		Factory.startup();
		Factory.initThread(Factory.PERMISSIVE_THREAD_CONFIG);

		lotusSession = NotesFactory.createSession();
		session = Factory.getWrapperFactory().fromLotus(lotusSession, Session.SCHEMA, null);
		FrameworkUtils.setFallbackSession(session);

		MODELS_DB_PATH = instantiateDb("models");
	}

	@AfterClass
	public static void term() throws Exception {
		lotusSession.recycle();
		Factory.termThread();
		Factory.shutdown();
		NotesThread.stermThread();

		deleteDb("models");

		// MUST use sysout because the log handler crashes when the Notes thread has not been initialized
		System.out.println("Term " + AllTests.class.getName()); //$NON-NLS-1$
	}

	private static String instantiateDb(final String basename) throws IOException {
		File dbFile = File.createTempFile(basename, ".nsf"); //$NON-NLS-1$
		Platform.getInstance().log("{0} location is {1}", basename, dbFile.getAbsolutePath()); //$NON-NLS-1$
		FileOutputStream fos = new FileOutputStream(dbFile);
		InputStream is = AllTests.class.getResourceAsStream("/" + basename + ".nsf"); //$NON-NLS-1$ //$NON-NLS-2$
		StreamUtil.copyStream(is, fos);
		fos.flush();
		StreamUtil.close(fos);
		StreamUtil.close(is);
		return dbFile.getAbsolutePath();
	}

	private static void deleteDb(final String dbName) {
		try {
			File testDB = new File(dbName);
			if(testDB.exists()) {
				testDB.delete();
			}
		} catch(Exception e) { }
	}

}
