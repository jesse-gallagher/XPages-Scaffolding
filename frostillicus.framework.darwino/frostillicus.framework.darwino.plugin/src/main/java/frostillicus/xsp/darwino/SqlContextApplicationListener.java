package frostillicus.xsp.darwino;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.darwino.commons.Platform;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.log.Logger;
import com.darwino.commons.platform.beans.impl.ManagedBeansServicesImpl;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Session;
import com.darwino.jsonstore.sql.impl.full.JsonDb;
import com.darwino.jsonstore.sql.impl.full.LocalFullJsonDBServerImpl;
import com.darwino.jsonstore.sql.impl.full.SqlContext;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener;

/**
 * This listener clears out any active Darwino SqlContexts in the application scope.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class SqlContextApplicationListener implements ApplicationListener {
	private static final Logger log = Activator.log;

	private static Map<String, Map<String, SqlContext>> sqlContextMap = new HashMap<String, Map<String, SqlContext>>();
	private static Map<String, Map<String, LocalFullJsonDBServerImpl>> serverMap = new HashMap<String, Map<String, LocalFullJsonDBServerImpl>>();
	private static Map<String, Map<String, Session>> sessionMap = new HashMap<String, Map<String, Session>>();
	private static Map<String, Map<String, Database>> databaseMap = new HashMap<String, Map<String, Database>>();
	
	@SuppressWarnings("resource")
	public static Database getDatabase(String connectionBeanName, String instanceId, String databaseId) throws JsonException, SQLException {
		String applicationId = ((ApplicationEx)FacesContext.getCurrentInstance().getApplication()).getApplicationId();
		Map<String, Database> databases = databaseMap.get(applicationId);
		if(!databases.containsKey(connectionBeanName + instanceId + databaseId)) {
			Session session = getSession(connectionBeanName, instanceId);
			databases.put(connectionBeanName + instanceId + databaseId, session.getDatabase(databaseId));
		}
		return databases.get(connectionBeanName + instanceId + databaseId);
	}
	
	@SuppressWarnings("resource")
	public static Session getSession(String connectionBeanName, String instanceId) throws JsonException, SQLException {
		String applicationId = ((ApplicationEx)FacesContext.getCurrentInstance().getApplication()).getApplicationId();
		Map<String, Session> sessions = sessionMap.get(applicationId);
		if(!sessions.containsKey(connectionBeanName + instanceId)) {
			LocalFullJsonDBServerImpl server = getServer(connectionBeanName);
			// TODO add user support
			Session session = server.createSystemSession(instanceId);
			sessions.put(connectionBeanName + instanceId, session);
		}
		return sessions.get(connectionBeanName + instanceId);
	}
	
	@SuppressWarnings("resource")
	public static LocalFullJsonDBServerImpl getServer(String connectionBeanName) throws SQLException {
		String applicationId = ((ApplicationEx)FacesContext.getCurrentInstance().getApplication()).getApplicationId();
		Map<String, LocalFullJsonDBServerImpl> servers = serverMap.get(applicationId);
		if(!servers.containsKey(connectionBeanName)) {
			SqlContext context = getSqlContext(connectionBeanName);
			LocalFullJsonDBServerImpl server = new LocalFullJsonDBServerImpl(context, null);
			servers.put(connectionBeanName, server);
		}
		return servers.get(connectionBeanName);
	}
	
	public static SqlContext getSqlContext(String connectionBeanName) throws SQLException {
		String applicationId = ((ApplicationEx)FacesContext.getCurrentInstance().getApplication()).getApplicationId();
		Map<String, SqlContext> sqlContexts = sqlContextMap.get(applicationId);
		if(!sqlContexts.containsKey(connectionBeanName)) {
			JsonDb connectionBean = getConnectionBean(connectionBeanName);
			sqlContexts.put(connectionBeanName, connectionBean.createSqlContext());
		}
		return sqlContexts.get(connectionBeanName);
	}
	
	private static JsonDb getConnectionBean(String connectionBeanName) {
		
		ManagedBeansServicesImpl managedBeanService = (ManagedBeansServicesImpl)Platform.getManagedBeansService();
		
		if(log.isDebugEnabled()) {
			log.debug("DominoAccessor#getConnectionBean: managed beans service is {0}", managedBeanService); //$NON-NLS-1$
			log.debug("DominoAccessor#getConnectionBean: enumerated beans are {0}", Arrays.asList(managedBeanService.enumerate("darwino/jsondb", false))); //$NON-NLS-1$ //$NON-NLS-2$
		}

		JsonDb connectionBean = (JsonDb)managedBeanService.getUnchecked("darwino/jsondb", connectionBeanName); //$NON-NLS-1$
		if(log.isDebugEnabled()) {
			log.debug("DominoAccessor#getConnectionBean: connection bean is {0}", connectionBean); //$NON-NLS-1$
		}
		return connectionBean;
	}

	@Override
	public void applicationCreated(ApplicationEx application) {
		sqlContextMap.put(application.getApplicationId(), new HashMap<String, SqlContext>());
		serverMap.put(application.getApplicationId(), new HashMap<String, LocalFullJsonDBServerImpl>());
		sessionMap.put(application.getApplicationId(), new HashMap<String, Session>());
		databaseMap.put(application.getApplicationId(), new HashMap<String, Database>());
	}

	@Override
	public void applicationDestroyed(ApplicationEx application) {
		databaseMap.remove(application.getApplicationId());
		
		Map<String, Session> sessions = sessionMap.remove(application.getApplicationId());
		if(sessions != null) {
			for(Session session : sessions.values()) {
				try { session.close(); } catch(IOException e) { }
			}
		}
		
		Map<String, LocalFullJsonDBServerImpl> servers = serverMap.remove(application.getApplicationId());
		if(servers != null) {
			for(LocalFullJsonDBServerImpl server : servers.values()) {
				try { server.close(); } catch(IOException e) { }
			}
		}
		
		Map<String, SqlContext> sqlContexts = sqlContextMap.remove(application.getApplicationId());
		if(sqlContexts != null) {
			for(SqlContext sqlContext : sqlContexts.values()) {
				sqlContext.close();
			}
		}
	}

}
