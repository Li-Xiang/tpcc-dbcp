package org.littlestar.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
/**
 * DataSourceHelper是一个Apache DBCP助手类，简化BasicDataSource的创建。DataSourceHelper实例维护一个配置集合, 集合可以包含多个数据源配置, 
 * 每个数据源配置通过唯一编号访问。如果不指定编号, 使用默认的数据源编号DEFAULT_CONFIG_ID. 
 * 
 * @author LiXiang
 *
 */
public class DataSourceHelper {
	
	public static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
	public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
	public static final String DERBY_EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver"; 
	public static final String DERBY_CLIENT_DRIVER = "org.apache.derby.jdbc.ClientDriver"; 
	public static final String MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static final String DB2_DRIVE = "com.ibm.db2.jcc.DB2Driver";;
	public static final String APACHE_IGNITE_DRIVE = "org.apache.ignite.IgniteJdbcThinDriver";
	
	public static final String DEFAULT_CONFIG_ID = "default-data-source";
	public static final String USERNAME_PARAMETER = "username";
	public static final String PASSWORD_PARAMETER = "password";
	public static final String URL_PARAMETER = "url";
	public static final String DRIVER_CLASSNAME_PARAMETER = "driverClassName";
	public static final String CONNECTION_PROPERTIES_PARAMETER = "connectionProperties";
	public static final String DEFAULT_AUTOCOMMIT_PARAMETER = "defaultAutoCommit";
	public static final String DEFAULT_READONLY_PARAMETER = "defaultReadOnly";
	public static final String DEFAULT_TRANSACTION_ISOLATION_PARAMETER = "defaultTransactionIsolation";
	public static final String DEFAULT_CATALOG_PARAMETER = "defaultCatalog";
	public static final String CACHE_STATE_PARAMETER = "cacheState";
	public static final String DEFAULT_QUERY_TIMEOUT_PARAMETER = "defaultQueryTimeout";
	public static final String ENABLE_AUTO_COMMIT_ON_RETURN_PARAMETER = "enableAutoCommitOnReturn";
	public static final String ROLLBACK_ON_RETURN_PARAMETER = "rollbackOnReturn";
	public static final String INITIAL_SIZE_PARAMETER = "initialSize";
	public static final String MAX_TOTAL_PARAMETER = "maxTotal";
	public static final String MAX_IDLE_PARAMETER = "maxIdle";
	public static final String MIN_IDLE_PARAMETER = "minIdle";
	public static final String MAX_WAIT_MILLIS_PARAMETER = "maxWaitMillis";
	public static final String VALIDATION_QUERY_PARAMETER = "validationQuery";
	public static final String VALIDATION_QUERY_TIMEOUT_PARAMETER = "validationQueryTimeout";
	public static final String TEST_ON_CREATE_PARAMETER = "testOnCreate";
	public static final String TEST_ON_BORROW_PARAMETER = "testOnBorrow";
	public static final String TEST_ON_RETURN_PARAMETER = "testOnReturn";
	public static final String TEST_WHILE_IDLE_PARAMETER = "testWhileIdle";
	public static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS_PARAMETER = "timeBetweenEvictionRunsMillis";
	public static final String NUM_TESTS_PER_EVICTION_RUN_PARAMETER = "numTestsPerEvictionRun";
	public static final String MIN_EVICTABLE_IDLE_TIME_MILLIS_PARAMETER = "minEvictableIdleTimeMillis";
	public static final String SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS_PARAMETER = "softMinEvictableIdleTimeMillis";
	public static final String MAX_CONN_LIFETIME_MILLIS_PARAMETER = "maxConnLifetimeMillis";
	public static final String LOG_EXPIRED_CONNECTIONS_PARAMETER = "logExpiredConnections";
	public static final String CONNECTION_INIT_SQLS_PARAMETER = "connectionInitSqls";
	public static final String LIFO_PARAMETER = "lifo";
	public static final String POOL_PREPARED_STATEMENTS_PARAMETER = "poolPreparedStatements";
	public static final String MAX_OPEN_PREPARED_STATEMENTS_PARAMETER = "maxOpenPreparedStatements";
	public static final String REMOVE_ABANDONED_ON_MAINTENANCE_PARAMETER = "removeAbandonedOnMaintenance";
	public static final String REMOVE_ABANDONED_ON_BORROW_PARAMETER = "removeAbandonedOnBorrow";
	public static final String REMOVE_ABANDONED_TIMEOUT_PARAMETER = "removeAbandonedTimeout";
	public static final String LOG_ABANDONED_PARAMETER = "logAbandoned";
	public static final String ABANDONED_USAGE_TRACKING_PARAMETER = "abandonedUsageTracking";
	public static final String FAST_FAIL_VALIDATION_PARAMETER = "fastFailValidation";
	public static final String DISCONNECTION_SQL_CODES_PARAMETER = "disconnectionSqlCodes";
	public static final String JMX_NAME_PARAMETER = "jmxName";
	
	private final Map<String, Map<String, String>> configurationSet;
	
	/**
	 * 构造函数, 创建一个空配置的DataSourceHelper实例。
	 */
	public DataSourceHelper() {
		configurationSet = new ConcurrentHashMap<String, Map<String, String>>();
	}
	
	/**
	 * 构造函数, 创建一个DataSourceHelper实例, 并从指定的配置文件中加载数据源配置:
	 *   
	 * @param configFile  配置文件的路径。
	 * @throws Throwable
	 */
	public DataSourceHelper(File configFile) throws Throwable {
		this();
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		Gson gson = new Gson();
		Map<String, Map<String, String>> configurationsFromJson = gson.fromJson(reader,
				new TypeToken<Map<String, Map<String, String>>>() {
				}.getType());
		if (configurationsFromJson != null) {
			Set<Map.Entry<String, Map<String, String>>> jsonConfigSet = configurationsFromJson.entrySet();
			for (Entry<String, Map<String, String>> config : jsonConfigSet) {
				String dsId = config.getKey();
				Map<String, String> parameters = config.getValue();
				String password = parameters.get(PASSWORD_PARAMETER);
				if (password != null) {
					password = password.trim();
				}
				configurationSet.put(dsId, parameters);
			}
		}
	}
	
	/**
	 * 构造函数, 创建一个DataSourceHelper实例, 并添加一个数据源配置。
	 * @param dsId 数据源编号。
	 * @param config 数据源配置。
	 */
	
	public DataSourceHelper(String dsId, Map<String, String> config) {
		this();
		configurationSet.put(dsId, config);
	}
	
	/**
	 * 构造函数, 创建一个DataSourceHelper实例, 并添加默数据源配置。
	 * @param config 数据源配置。
	 */
	
	public DataSourceHelper(Map<String, String> config) {
		this(DEFAULT_CONFIG_ID, config);
	}

	/**
	 * 设置数据源配置，如果数据源配置已经存在，则进行合并，覆盖同名的配置。
	 * @param dsId 数据源编号。
	 * @param config 数据源配置。
	 */
	public void putDsConfig(String dsId, Map<String, String> config) {
		Map<String, String> exist = configurationSet.get(dsId);
		if (exist != null) {
			exist.putAll(config);
		} else {
			configurationSet.put(dsId, config);
		}
	}
	
	public void putDsConfig(Map<String, String> config) {
		putDsConfig(DEFAULT_CONFIG_ID, config);
	}
	
	public Map<String, String> getDsConfig(String dsId) {
		return configurationSet.get(dsId);
	}

	public Map<String, String> getDsConfig() {
		return getDsConfig(DEFAULT_CONFIG_ID);
	}

	public Set<String> getConfigKeySet() {
		return configurationSet.keySet();
	}
	
	/**
	 * 获取指定的参数值。
	 * @param dsId 指定数据源的编号.
	 * @param parameterName 指定需要获取的参数名.
	 * @return parameterName的参数值.
	 */
	public String getParameter(String dsId, String parameterName) {
		Map<String, String> config = configurationSet.get(dsId);
		return config.get(parameterName);
	}
	
	/**
	 * 获取默认数据源的指定的参数值。
	 * @param parameterName 指定需要获取的参数名.
	 * @return parameterName的参数值
	 */
	public String getParameter(String parameterName) {
		return getParameter(DEFAULT_CONFIG_ID, parameterName);
	}
	
	/**
	 * 设置一个指定数据源(dsId)配置的产数值。
	 * @param dsId 指定数据源的编号.
	 * @param parameterName 参数名。
	 * @param parameterValue 参数值。
	 */
	public void setParameter(String dsId, String parameterName, String parameterValue) {
		Map<String, String> config = configurationSet.get(dsId);
		if (config == null) {
			config = new HashMap<String, String>();
			configurationSet.put(dsId, config);
		}
		config.put(parameterName, parameterValue);
	}
	
	public void setParameter(String parameterName, String parameterValue) {
		setParameter(DEFAULT_CONFIG_ID, parameterName, parameterValue);
	}
	
	/**
	 * 根据数据源配置创建一个数据源。
	 * @param dsId 数据源配置的编号。
	 * @return 返回一个BasicDataSource实例。 
	 * @throws Throwable
	 */
	public BasicDataSource getDataSource(String dsId) throws Throwable {
		Map<String, String> dsConfig = configurationSet.get(dsId);
		if(dsConfig == null) {
			throw new Throwable("could not find datasource configuration '"+dsId+"'. ");
		}
		return newDataSource(dsConfig);
	}

	/**
	 * 根据默认数据源配置创建一个数据源。
	 * @return 返回默认数据源配置的BasicDataSource实例。 
	 * @throws Throwable
	 */
	public BasicDataSource getDataSource() throws Throwable {
		String dsId = DEFAULT_CONFIG_ID;
		return getDataSource(dsId);
	}
	
	/**
	 * 将当前的数据源配置集合保存到一个Json格式的配置文件。
	 * @param jsonFile 指定Json文件的路径。
	 * @throws Throwable
	 */
	public void toJsonFile(File jsonFile) throws Throwable {
		toJsonFile(jsonFile, configurationSet);
	}
	
	/**
	 * 获取当前配置集合。
	 * @return 返回当前的源配置集，Json格式字符串。
	 */
	public String toJsonString() {
		return toJsonString(configurationSet);
	}
	
	public enum DBMS {MySQL, Oracle, Derby, SQLite, MSSQL, PostgreSQL, DB2, IGNITE, Unknown;}
	
	public DBMS getDbms(String dsId) {
		String drive = getParameter(dsId, DRIVER_CLASSNAME_PARAMETER);
		if (drive == null)
			drive = "";
		switch (drive) {
		case MYSQL_DRIVER:
			return DBMS.MySQL;
		case ORACLE_DRIVER:
			return DBMS.Oracle;
		case SQLITE_DRIVER:
			return DBMS.SQLite;
		case DERBY_EMBEDDED_DRIVER:
			return DBMS.Derby;
		case DERBY_CLIENT_DRIVER:
			return DBMS.Derby;
		case MSSQL_DRIVER:
			return DBMS.MSSQL;
		case DB2_DRIVE:
			return DBMS.DB2;
		case APACHE_IGNITE_DRIVE:
			return DBMS.IGNITE;
		default:
			return DBMS.Unknown;
		}
	}
	
	public DBMS getDbms() {
		return getDbms(DEFAULT_CONFIG_ID);
	}
	
	public static BasicDataSource newDataSource(Map<String, String> config) throws Throwable {
		BasicDataSource dataSource = new BasicDataSource();
		if (config == null)
			return dataSource;
		Set<Map.Entry<String, String>> entrySet = config.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.equalsIgnoreCase(USERNAME_PARAMETER)) {
				dataSource.setUsername(value);
			} else if (key.equalsIgnoreCase(PASSWORD_PARAMETER)) {
				dataSource.setPassword(value);
			} else if (key.equalsIgnoreCase(URL_PARAMETER)) {
				dataSource.setUrl(value);
			} else if (key.equalsIgnoreCase(DRIVER_CLASSNAME_PARAMETER)) {
				dataSource.setDriverClassName(value);
			} else if (key.equalsIgnoreCase(CONNECTION_PROPERTIES_PARAMETER)) {
				dataSource.setConnectionProperties(value);
			} else if (key.equalsIgnoreCase(DEFAULT_AUTOCOMMIT_PARAMETER)) {
				dataSource.setDefaultAutoCommit(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(DEFAULT_READONLY_PARAMETER)) {
				dataSource.setDefaultReadOnly(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(DEFAULT_TRANSACTION_ISOLATION_PARAMETER)) {
				try {
					int transactionIsolation = Integer.parseInt(value);
					dataSource.setDefaultTransactionIsolation(transactionIsolation);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(DEFAULT_CATALOG_PARAMETER)) {
				dataSource.setDefaultCatalog(value);
			} else if (key.equalsIgnoreCase(CACHE_STATE_PARAMETER)) {
				dataSource.setCacheState(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(DEFAULT_QUERY_TIMEOUT_PARAMETER)) {
				try {
					int queryTimeoutSeconds = Integer.parseInt(value);
					dataSource.setDefaultQueryTimeout(queryTimeoutSeconds);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(ENABLE_AUTO_COMMIT_ON_RETURN_PARAMETER)) {
				dataSource.setAutoCommitOnReturn(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(ROLLBACK_ON_RETURN_PARAMETER)) {
				dataSource.setRollbackOnReturn(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(INITIAL_SIZE_PARAMETER)) {
				try {
					int initialSize = Integer.parseInt(value);
					dataSource.setInitialSize(initialSize);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(MAX_TOTAL_PARAMETER)) {
				try {
					int maxTotal = Integer.parseInt(value);
					dataSource.setMaxTotal(maxTotal);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(MAX_IDLE_PARAMETER)) {
				try {
					int maxIdle = Integer.parseInt(value);
					dataSource.setMaxIdle(maxIdle);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(MIN_IDLE_PARAMETER)) {
				try {
					int minIdle = Integer.parseInt(value);
					dataSource.setMinIdle(minIdle);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(MAX_WAIT_MILLIS_PARAMETER)) {
				int maxWaitMillis = Integer.parseInt(value);
				dataSource.setMaxWaitMillis(maxWaitMillis);
			} else if (key.equalsIgnoreCase(VALIDATION_QUERY_PARAMETER)) {
				dataSource.setValidationQuery(value);
			} else if (key.equalsIgnoreCase(VALIDATION_QUERY_TIMEOUT_PARAMETER)) {
				try {
					int validationQueryTimeoutSeconds = Integer.parseInt(value);
					dataSource.setValidationQueryTimeout(validationQueryTimeoutSeconds);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(TEST_ON_CREATE_PARAMETER)) {
				dataSource.setTestOnCreate(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(TEST_ON_BORROW_PARAMETER)) {
				dataSource.setTestOnBorrow(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(TEST_ON_RETURN_PARAMETER)) {
				dataSource.setTestOnReturn(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(TEST_WHILE_IDLE_PARAMETER)) {
				dataSource.setTestWhileIdle(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(TIME_BETWEEN_EVICTION_RUNS_MILLIS_PARAMETER)) {
				try {
					long timeBetweenEvictionRunsMillis = Long.parseLong(value);
					dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(NUM_TESTS_PER_EVICTION_RUN_PARAMETER)) {
				try {
					int numTestsPerEvictionRun = Integer.parseInt(value);
					dataSource.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(MIN_EVICTABLE_IDLE_TIME_MILLIS_PARAMETER)) {
				try {
					long minEvictableIdleTimeMillis = Long.parseLong(value);
					dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS_PARAMETER)) {
				try {
					long softMinEvictableIdleTimeMillis = Long.parseLong(value);
					dataSource.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(MAX_CONN_LIFETIME_MILLIS_PARAMETER)) {
				try {
					long maxConnLifetimeMillis = Long.parseLong(value);
					dataSource.setMaxConnLifetimeMillis(maxConnLifetimeMillis);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(LOG_EXPIRED_CONNECTIONS_PARAMETER)) {
				dataSource.setLogExpiredConnections(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(CONNECTION_INIT_SQLS_PARAMETER)) {
				String[] sqlArray = value.split(";");
				List<String> connectionInitSqls = Arrays.asList(sqlArray);
				dataSource.setConnectionInitSqls(connectionInitSqls);
			} else if (key.equalsIgnoreCase(LIFO_PARAMETER)) {
				dataSource.setLifo(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(POOL_PREPARED_STATEMENTS_PARAMETER)) {
				dataSource.setPoolPreparedStatements(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(MAX_OPEN_PREPARED_STATEMENTS_PARAMETER)) {
				try {
					int maxOpenStatements = Integer.parseInt(value);
					dataSource.setMaxOpenPreparedStatements(maxOpenStatements);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(REMOVE_ABANDONED_ON_MAINTENANCE_PARAMETER)) {
				dataSource.setRemoveAbandonedOnMaintenance(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(REMOVE_ABANDONED_ON_BORROW_PARAMETER)) {
				dataSource.setRemoveAbandonedOnBorrow(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(REMOVE_ABANDONED_TIMEOUT_PARAMETER)) {
				try {
					int removeAbandonedTimeout = Integer.parseInt(value);
					dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
				} catch (Throwable e) {
					throw e;
				}
			} else if (key.equalsIgnoreCase(LOG_ABANDONED_PARAMETER)) {
				dataSource.setLogAbandoned(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(ABANDONED_USAGE_TRACKING_PARAMETER)) {
				dataSource.setAbandonedUsageTracking(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(FAST_FAIL_VALIDATION_PARAMETER)) {
				dataSource.setFastFailValidation(Boolean.parseBoolean(value));
			} else if (key.equalsIgnoreCase(DISCONNECTION_SQL_CODES_PARAMETER)) {
				String[] codeArray = value.split(";");
				List<String> disconnectionSqlCodes = Arrays.asList(codeArray);
				dataSource.setDisconnectionSqlCodes(disconnectionSqlCodes);
			} else if (key.equalsIgnoreCase(JMX_NAME_PARAMETER)) {
				dataSource.setJmxName(value);
			}
		}
		return dataSource;
	}
	
	public static Map<String, String> getDataSourceParameters(BasicDataSource dataSource) {
		if (dataSource == null)
			return null;
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(USERNAME_PARAMETER, dataSource.getUsername());
		parameters.put(PASSWORD_PARAMETER, dataSource.getPassword());
		parameters.put(URL_PARAMETER, dataSource.getUrl());
		parameters.put(DRIVER_CLASSNAME_PARAMETER, dataSource.getDriverClassName());
		parameters.put(CONNECTION_PROPERTIES_PARAMETER, toConnectionProperties(getConnectionProperties(dataSource)));
		parameters.put(DEFAULT_AUTOCOMMIT_PARAMETER, dataSource.getDefaultAutoCommit() + "");
		parameters.put(DEFAULT_READONLY_PARAMETER, dataSource.getDefaultReadOnly() + "");
		parameters.put(DEFAULT_TRANSACTION_ISOLATION_PARAMETER, Integer.toString(dataSource.getDefaultTransactionIsolation()));
		parameters.put(DEFAULT_CATALOG_PARAMETER, dataSource.getDefaultCatalog());
		parameters.put(CACHE_STATE_PARAMETER, Boolean.toString(dataSource.getCacheState()));
		parameters.put(DEFAULT_QUERY_TIMEOUT_PARAMETER, dataSource.getDefaultQueryTimeout() + "");
		parameters.put(ENABLE_AUTO_COMMIT_ON_RETURN_PARAMETER, Boolean.toString(dataSource.getAutoCommitOnReturn()));
		parameters.put(ROLLBACK_ON_RETURN_PARAMETER, Boolean.toString(dataSource.getRollbackOnReturn()));
		parameters.put(INITIAL_SIZE_PARAMETER, Integer.toString(dataSource.getInitialSize()));
		parameters.put(MAX_TOTAL_PARAMETER, Integer.toString(dataSource.getMaxTotal()));
		parameters.put(MAX_IDLE_PARAMETER, Integer.toString(dataSource.getMaxIdle()));
		parameters.put(MIN_IDLE_PARAMETER, Integer.toString(dataSource.getMinIdle()));
		parameters.put(MAX_WAIT_MILLIS_PARAMETER, Long.toString(dataSource.getMaxWaitMillis()));
		parameters.put(VALIDATION_QUERY_PARAMETER, dataSource.getValidationQuery());
		parameters.put(VALIDATION_QUERY_TIMEOUT_PARAMETER, Integer.toString(dataSource.getValidationQueryTimeout()));
		parameters.put(TEST_ON_CREATE_PARAMETER, Boolean.toString(dataSource.getTestOnCreate()));
		parameters.put(TEST_ON_BORROW_PARAMETER, Boolean.toString(dataSource.getTestOnBorrow()));
		parameters.put(TEST_ON_RETURN_PARAMETER, Boolean.toString(dataSource.getTestOnReturn()));
		parameters.put(TEST_WHILE_IDLE_PARAMETER, Boolean.toString(dataSource.getTestWhileIdle()));
		parameters.put(TIME_BETWEEN_EVICTION_RUNS_MILLIS_PARAMETER, Long.toString(dataSource.getTimeBetweenEvictionRunsMillis()));
		parameters.put(NUM_TESTS_PER_EVICTION_RUN_PARAMETER, Integer.toString(dataSource.getNumTestsPerEvictionRun()));
		parameters.put(MIN_EVICTABLE_IDLE_TIME_MILLIS_PARAMETER, Long.toString(dataSource.getMinEvictableIdleTimeMillis()));
		parameters.put(SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS_PARAMETER, Long.toString(dataSource.getSoftMinEvictableIdleTimeMillis()));
		parameters.put(MAX_CONN_LIFETIME_MILLIS_PARAMETER, Long.toString(dataSource.getMaxConnLifetimeMillis()));
		parameters.put(LOG_EXPIRED_CONNECTIONS_PARAMETER, Boolean.toString(dataSource.getLogExpiredConnections()));
		parameters.put(CONNECTION_INIT_SQLS_PARAMETER, String.join(";", dataSource.getConnectionInitSqls()));
		parameters.put(LIFO_PARAMETER, Boolean.toString(dataSource.getLifo()));
		parameters.put(POOL_PREPARED_STATEMENTS_PARAMETER, Boolean.toString(dataSource.isPoolPreparedStatements()));
		parameters.put(MAX_OPEN_PREPARED_STATEMENTS_PARAMETER, Integer.toString(dataSource.getMaxOpenPreparedStatements()));
		parameters.put(REMOVE_ABANDONED_ON_MAINTENANCE_PARAMETER, Boolean.toString(dataSource.getRemoveAbandonedOnMaintenance()));
		parameters.put(REMOVE_ABANDONED_ON_BORROW_PARAMETER, Boolean.toString(dataSource.getRemoveAbandonedOnBorrow()));
		parameters.put(REMOVE_ABANDONED_TIMEOUT_PARAMETER, Integer.toString(dataSource.getRemoveAbandonedTimeout()));
		parameters.put(LOG_ABANDONED_PARAMETER, Boolean.toString(dataSource.getLogAbandoned()));
		parameters.put(ABANDONED_USAGE_TRACKING_PARAMETER, Boolean.toString(dataSource.getAbandonedUsageTracking()));
		parameters.put(FAST_FAIL_VALIDATION_PARAMETER, Boolean.toString(dataSource.getFastFailValidation()));
		parameters.put(DISCONNECTION_SQL_CODES_PARAMETER, String.join(";", dataSource.getDisconnectionSqlCodes()));
		parameters.put(JMX_NAME_PARAMETER, dataSource.getJmxName());
		return parameters;
	}
	
	public static Map<String, String> getDataSourceInfo(BasicDataSource dataSource) {
		if (dataSource == null)
			return null;
		Map<String, String> info = new HashMap<String, String>();
		info.put("DriverClassName", dataSource.getDriverClassName());
		info.put("Url",dataSource.getUrl());
		info.put("InitialSize", Integer.toString(dataSource.getInitialSize()));
		info.put("MaxTotal", Integer.toString(dataSource.getMaxTotal()));
		info.put("NumActive", Integer.toString(dataSource.getNumActive()));
		info.put("NumIdle", Integer.toString(dataSource.getNumIdle()));
		info.put("MaxWaitMillis", Long.toString(dataSource.getMaxWaitMillis()));
		return info;
	}
	
	/**
	 * 将配置以Json的格式，写到指定文件中。
	 */
	public static void toJsonFile(File jsonFile, Map<String, Map<String, String>> configuration)
			throws Throwable {
		String json = toJsonString(configuration);
		BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
		writer.write(json);
		writer.close();
	}
	
	/**
	 * 将configurationSet转换成json格式.
	 */
	public static String toJsonString(Map<String, Map<String, String>> configurationSet) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		String json = gson.toJson(configurationSet);
		return json;
	}
	
	/**
	 * 获取BasicDataSource的json格式的参数信息。
	 * 
	 * */
	public static String toJsonString(BasicDataSource dataSource) {
		Map<String, String> parameters = getDataSourceParameters(dataSource);
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		String json = gson.toJson(parameters);
		return json;
	}
	
	/**
	 * 因为BasicDataSource.getConnectionProperties()的访问修饰符(access modifiers)为默认值也就是protected,
	 * 非继承类无法通过getConnectionProperties()获取BasicDataSource实例的连接属性.
	 * 通过该方法，可以利用Java的反射机制来获取连接属性(BasicDataSource.connectionProperties属性)。
	 */
	public static Properties getConnectionProperties(BasicDataSource dataSource) {
		Properties properties = null;
		try {
			Field field = BasicDataSource.class.getDeclaredField("connectionProperties");
			field.setAccessible(true);
			properties = (Properties) field.get(dataSource);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return properties;
	}
	
	/**
	 * 将指定的properties转换为BasicDataSource。setConnectionProperties(final String connectionProperties)方法
	 * 能够解析的字符串(key1=value1;key2=value2;...)。
	 */
	public static String toConnectionProperties(Properties properties) {
		StringBuilder propertiesString = new StringBuilder();
		if (properties != null) {
			String prefix = "";
			for (final String name : properties.stringPropertyNames()) {
				propertiesString.append(prefix);
				prefix = ";";
				propertiesString.append(name + "=" + properties.getProperty(name));
			}
		}
		return propertiesString.toString();
	}
	
	public static Map<String, String> getMySQLTemplate() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(DRIVER_CLASSNAME_PARAMETER, MYSQL_DRIVER);
		parameters.put(INITIAL_SIZE_PARAMETER, "1");
		parameters.put(MAX_IDLE_PARAMETER, "4");
		parameters.put(MAX_TOTAL_PARAMETER, "8");
		parameters.put(MAX_WAIT_MILLIS_PARAMETER, "60000");
		parameters.put(LOG_ABANDONED_PARAMETER, "true");
		parameters.put(REMOVE_ABANDONED_ON_MAINTENANCE_PARAMETER, "true"); /* 连接池回收不使用的连接  */
		parameters.put(REMOVE_ABANDONED_ON_BORROW_PARAMETER, "true");
		parameters.put(REMOVE_ABANDONED_TIMEOUT_PARAMETER, "180");
		parameters.put(VALIDATION_QUERY_PARAMETER, "select 1");
		parameters.put(TEST_ON_BORROW_PARAMETER, "true");
		parameters.put(TEST_ON_RETURN_PARAMETER, "false");
		parameters.put(TEST_WHILE_IDLE_PARAMETER, "false");
		parameters.put(CONNECTION_PROPERTIES_PARAMETER,
				"useSSL=false;useUnicode=true;characterEncoding=UTF-8;autoReconnect=true");
		return parameters;
	}
	
	public static Map<String, String> getOracleTemplate() {
		String oracleHome = System.getenv("ORACLE_HOME");
		String tnsAdmin = oracleHome + File.separator + "network" + File.separator + "admin";
		System.setProperty("oracle.net.tns_admin", tnsAdmin);
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(DRIVER_CLASSNAME_PARAMETER, ORACLE_DRIVER);
		parameters.put(INITIAL_SIZE_PARAMETER, "1");
		parameters.put(MAX_IDLE_PARAMETER, "4");
		parameters.put(MAX_TOTAL_PARAMETER, "8");
		parameters.put(MAX_WAIT_MILLIS_PARAMETER, "60000");
		parameters.put(LOG_ABANDONED_PARAMETER, "true");
		parameters.put(REMOVE_ABANDONED_ON_MAINTENANCE_PARAMETER, "true");
		parameters.put(REMOVE_ABANDONED_ON_BORROW_PARAMETER, "true");
		parameters.put(REMOVE_ABANDONED_TIMEOUT_PARAMETER, "180");
		parameters.put(VALIDATION_QUERY_PARAMETER, "select 1 from dual");
		parameters.put(TEST_ON_BORROW_PARAMETER, "true");
		parameters.put(TEST_ON_RETURN_PARAMETER, "false");
		parameters.put(TEST_WHILE_IDLE_PARAMETER, "true");
		return parameters;
	}
	
	public static Map<String, String> getSQLiteTemplate() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(DRIVER_CLASSNAME_PARAMETER, SQLITE_DRIVER);
		parameters.put(INITIAL_SIZE_PARAMETER, "0");
		parameters.put(MAX_IDLE_PARAMETER, "1");
		parameters.put(MAX_TOTAL_PARAMETER, "8");
		parameters.put(MAX_WAIT_MILLIS_PARAMETER, "60000");
		parameters.put(LOG_ABANDONED_PARAMETER, "true");
		parameters.put(REMOVE_ABANDONED_ON_MAINTENANCE_PARAMETER, "true");
		parameters.put(REMOVE_ABANDONED_ON_BORROW_PARAMETER, "true");
		parameters.put(REMOVE_ABANDONED_TIMEOUT_PARAMETER, "180");
		parameters.put(VALIDATION_QUERY_PARAMETER, "select 1");
		parameters.put(TEST_ON_BORROW_PARAMETER, "true");
		parameters.put(TEST_ON_RETURN_PARAMETER, "false");
		parameters.put(TEST_WHILE_IDLE_PARAMETER, "true");
		parameters.put(CONNECTION_PROPERTIES_PARAMETER,
				"journal_mode=WAL;wal_autocheckpoint=6000;synchronous=NORMAL;cache_size=8000");
		return parameters;
	}
	
	public static void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (Throwable e) {
		}
	}
}
