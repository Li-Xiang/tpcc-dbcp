package org.littlestar.tpcc;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.dbcp2.BasicDataSource;

import org.littlestar.helper.DataSourceHelper.DBMS;

public class TpccContext implements TpccConstants {
	private static volatile TpccContext context;
	
	private volatile int warehouses = 1;
	//Read-Only.
	//private final ReentrantLock dataSourceLock = new ReentrantLock();
	private final BasicDataSource dataSource;
	private final DBMS dbms;
	
	//public static volatile boolean activateTransaction = true;
	public static AtomicBoolean transactionOn = new AtomicBoolean(true);
	public static AtomicBoolean CountingOn = new AtomicBoolean(false);
	
	private TpccContext(BasicDataSource dataSource, DBMS dbms)  {
		this.dataSource = dataSource;
		this.dbms = dbms;
	}

	/**
	 * 初始化Tpcc测试上下文, 只能程序初始化时初始化一次。
	 * @param warehouses
	 * @param dataSource
	 * @param dbms
	 */
	public static void initial(BasicDataSource dataSource, DBMS dbms) {
		if (context == null) {
			synchronized (TpccContext.class) {
				if (context == null) {
					context = new TpccContext(dataSource, dbms);
				}
			}
		}
	}
	
	public static TpccContext getContext() {
		if (context == null) {
			throw new NullPointerException("Tpcc context was not initialized ...");
		}
		return context;
	}
	
	public int getWarehouses() {
		return warehouses;
	}
	
	/**
	 * 注意! 线程不安全, 程序执行过程仓库数量需要保持一致, 只能再初始化过程调用.
	 * @param count
	 */
	public void setWarehouses(int count) {
		warehouses = count;
	}
	
	/**
	 * 获取数据源的类型.
	 * 
	 * @return 数据源的类型.
	 */
	public DBMS getDBMS() {
		return dbms;
	}
	
	public Connection getConnection() throws Throwable {
		Connection connection = null;
		//dataSourceLock.lock();
		try {
			connection = dataSource.getConnection();
		} catch (Throwable e) {
			throw e;
		} finally {
			//dataSourceLock.unlock();
		}
		return connection;
	}
	
	public void shutdown() {
		try {
			dataSource.close();
		} catch (Throwable e) {
		}
	}
}
