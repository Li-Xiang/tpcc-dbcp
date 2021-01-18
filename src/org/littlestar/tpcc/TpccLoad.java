package org.littlestar.tpcc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.littlestar.helper.DataSourceHelper;
import org.littlestar.helper.RandomHelper;

public class TpccLoad implements TpccConstants {
	private static final Log logger = LogFactory.getLog(TpccLoad.class);
	/**
	 * 多线程加载。
	 * @param wareCount warehouses number.
	 * @param threads load threads number.
	 */
	public static void load(int wareCount, int threads) {
		final LocalDateTime loadBeginTime = LocalDateTime.now();  
		final ExecutorService executor = Executors.newFixedThreadPool(threads);
		LinkedList<Future<Long>> futures = new LinkedList<Future<Long>>();
		Future<Long> future;
		TpccHelper.output("Loading Tpcc's data with Fixed-Thread-Pool (" + threads + ")... ");
		///// Item Loader thread.
		final Callable<Long> itemThread = new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				long rows = 0l;
				final LocalDateTime beginTime = LocalDateTime.now();
				TpccHelper.output("Loading Item ... ");
				try {
					rows = item();
				} catch (Throwable e) {
					logger.fatal("Loading Item failed, abort.", e);
					System.exit(201);
				}
				final LocalDateTime endTime = LocalDateTime.now();
				final Duration duration = Duration.between(beginTime, endTime);
				final long runTime = duration.toMillis();
				long rps = (runTime > 0) ? (rows * 1000l) / runTime : 0l;
				TpccHelper.output("Item done, " + rows + " rows, elapsed " + runTime + " ms. (" + rps + " rows/sec)");
				return rows;
			}
		};
		future = executor.submit(itemThread);
		futures.add(future);
		
		///// Warehouse Loader thread.
		final Callable<Long> warehousesThread = new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				long rows = 0l;
				final LocalDateTime beginTime = LocalDateTime.now();
				TpccHelper.output("Loading Warehouse ...");
				try {
					rows = warehouse(wareCount);
				} catch (Throwable e) {
					logger.fatal("Loading Warehouse failed, abort.", e);
					System.exit(202);
				}
				final LocalDateTime endTime = LocalDateTime.now();
				final Duration duration = Duration.between(beginTime, endTime);
				final long runTime = duration.toMillis();
				long rps = (runTime > 0) ? (rows * 1000l) / runTime : 0l;
				TpccHelper.output("Warehouse done, " + rows + " rows, elapsed " + runTime + " ms. (" + rps + " rows/sec)");
				return rows;
			}
		};
		future = executor.submit(warehousesThread);
		futures.add(future);
		
		for (int w = 1; w <= wareCount; w++) {
			int w_id = w;
			///// Stock Loader threads.
			Callable<Long> stockThread = new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					long rows = 0l;
					final LocalDateTime beginTime = LocalDateTime.now();
					TpccHelper.output("Loading Stock Wid=" + w_id + " of " + wareCount + " ...");
					try {
						rows = stock(w_id);
					} catch (Throwable e) {
						logger.fatal("Loading Stock Wid=" + w_id + " failed, abort.", e);
						System.exit(203);
					}
					final LocalDateTime endTime = LocalDateTime.now();
					final Duration duration = Duration.between(beginTime, endTime);
					final long runTime = duration.toMillis();
					long rps = (runTime > 0) ? (rows * 1000l) / runTime : 0l;
					TpccHelper.output("Stock Wid=" + w_id + " of " + wareCount + " done, " + rows + " rows, elapsed " + runTime + " ms. (" + rps + " rows/sec)");
					return rows;
				}
			};
			future = executor.submit(stockThread);
			futures.add(future);
			
			///// District Loader threads.
			Callable<Long> districtThread = new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					long rows = 0l;
					final LocalDateTime beginTime = LocalDateTime.now();
					TpccHelper.output("Loading District Wid=" + w_id + " ... ");
					try {
						rows = district(w_id);
					} catch (Throwable e) {
						logger.fatal("Loading District Wid=" + w_id + " failed, abort.", e);
						System.exit(204);
					}
					final LocalDateTime endTime = LocalDateTime.now();
					final Duration duration = Duration.between(beginTime, endTime);
					final long runTime = duration.toMillis();
					long rps = (runTime > 0) ? (rows * 1000l) / runTime : 0l;
					TpccHelper.output("District Wid=" + w_id + " done, " + rows + " rows, elapsed " + runTime + " ms. (" + rps + " rows/sec)");
					return rows;
				}
			};
			future = executor.submit(districtThread);
			futures.add(future);
			
			for (int d = 1; d <= DIST_PER_WARE; d++) { // 每个仓库下的每个销售区域;
				final int d_id = d;
				///// Customer & History Loader threads.
				Callable<Long> customerThread = new Callable<Long>() { //Loading Customer for Did="++", Wid=%ld\n"
					@Override
					public Long call() throws Exception {
						long rows = 0l;
						final LocalDateTime beginTime = LocalDateTime.now();
						TpccHelper.output("Loading Customer, History for Did=" + d_id + ", Wid=" + w_id + " ... ");
						try {
							rows = customer(d_id, w_id);
						} catch (Throwable e) {
							logger.fatal("Loading Customer, History for Did=" + d_id + ", Wid=" + w_id + " failed, abort.", e);
							System.exit(205);
						}
						final LocalDateTime endTime = LocalDateTime.now();
						final Duration duration = Duration.between(beginTime, endTime);
						final long runTime = duration.toMillis();
						long rps = (runTime > 0) ? (rows * 1000l) / runTime : 0l;
						TpccHelper.output("Customer, History for Did=" + d_id + ", Wid=" + w_id + " done, " + rows + " rows, elapsed " +  runTime + " ms. (" + rps + " rows/sec)");
						return rows;
					}
				};
				future = executor.submit(customerThread);
				futures.add(future);
				
				///// Orders & New-Orders & Order-Line Loader threads.
				Callable<Long> ordersThread = new Callable<Long>() {
					@Override
					public Long call() throws Exception {
						long rows = 0l;
						final LocalDateTime beginTime = LocalDateTime.now();
						TpccHelper.output("Loading Orders, New-Orders, Order-Line for Did=" + d_id + ", Wid=" + w_id + " ... ");
						try {
							rows = orders(d_id, w_id);
						} catch (Throwable e) {
							logger.fatal("Loading Orders, New-Orders, Order-Line for Did=" + d_id + ", Wid=" + w_id + " failed, abort.", e);
							System.exit(206);
						}
						final LocalDateTime endTime = LocalDateTime.now();
						final Duration duration = Duration.between(beginTime, endTime);
						final long runTime = duration.toMillis();
						long rps = (runTime > 0) ? (rows * 1000l) / runTime : 0l;
						TpccHelper.output("Orders, New-Orders, Order-Line for Did=" + d_id + ", Wid=" + w_id + " done, "+rows+" rows, elapsed " +  runTime + " ms. (" + rps + " rows/sec)");
						return rows;
					}
				};
				future = executor.submit(ordersThread);
				futures.add(future);
			}
		}
		executor.shutdown();
		
		long waitCount = 1;
		double rows = 0d;
		while (true) {
			int threadCount = futures.size();
			int doneCount = 0;
			for (Future<?> f : futures) {
				if (f.isDone()) {
					doneCount++;
				}
			}
			//避免线程执行时间过长, 让前端误以为线程挂死, 定期输出信息. 
			if (waitCount % 6000L == 0) {
				TpccHelper.output("Please wait... " + doneCount + " of " + threadCount + " threads done.");
			}

			if (doneCount == threadCount) {
				for (Future<Long> f : futures) {
					try {
						rows += f.get();
					} catch (Throwable e) {
					}
				}
				break;
			}
			try {
				TimeUnit.MICROSECONDS.sleep(100);
			} catch (InterruptedException e) {
			}
			waitCount++;
		}
		final LocalDateTime loadEndTime = LocalDateTime.now();
		final Duration duration = Duration.between(loadBeginTime, loadEndTime);
		final long loadRunTime = duration.getSeconds();
		Double rps = (loadRunTime > 0) ? (rows/ loadRunTime) : 0l;
		TpccHelper.output("......  All data is done! " + rows + " rows, elapsed " + loadRunTime + " secs. ( "+rps.longValue()+" rows/sec ) ......");
	}
	
	
	/**
	 * 商品条目表 - Item
	 * 
	 * 100,000 rows in the ITEM table with:
	 *   I_ID unique within [100,000]
	 *   I_IM_ID random within [1 .. 10,000]
	 *   I_NAME random a-string [14 .. 24]
	 *   I_PRICE random within [1.00 .. 100.00]
	 *   I_DATA random a-string [26 .. 50]. 
	 *     For 10% of the rows, selected at random, the string "original" must be held by 8 consecutive 
	 *     characters starting at a random position within I_DATA.
	 * 
	 * @throws Throwable
	 */
	private static long item() throws Throwable {
		int i_id = 0;           // Item ID
		int i_im_id = 0;        // Image ID associated to Item
		String i_name = null;   // Item Name, varchar(24)
		double i_price = 0;     // Item price, decimal(5,2)
		String i_data = null;   // Brand information, varchar(50)
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.loadItemsStmt();
		long rows = 0l;
		// 创建一个包含10%行数随机置位的位图, 置位的索引对应的行的'i_idata'字段, 需要在随机位置包含"original"字符串。
		// 和参考实现的思路是相似的，只不过参考实现使用的是一个整型数值实现的位图.
		BitSet origBitmap = RandomHelper.randomBitMap(MAX_ITEMS / 10, MAX_ITEMS);
		try {
			final PreparedStatement stmt = connection.prepareStatement(sqlText);
			for (i_id = 1; i_id <= MAX_ITEMS; i_id++) {
				i_im_id = RandomHelper.randomInt(1, 10000);
				i_name = RandomHelper.randomString(14, 24);
				i_price = RandomHelper.randomDecimal(2, 1.00, 100.00);
				i_data = RandomHelper.randomString(26, 50);
				if (origBitmap.get(i_id - 1)) { // 通过替换方式, 实现随机位置包含"original"。
					int pos = RandomHelper.randomInt(0, i_data.length() - 8); // "original".length() => 8
					i_data = replaceString(i_data, pos, "original");
				}
				stmt.setInt(1, i_id);
				stmt.setInt(2, i_im_id);
				stmt.setString(3, i_name);
				stmt.setDouble(4, i_price);
				stmt.setString(5, i_data);
				stmt.addBatch();
				rows ++;
				if (i_id % SQL_BATCH_SIZE == 0) { // MySQL连接属性需要设置: rewriteBatchedStatements=true;
					stmt.executeBatch();
				}
			}
			stmt.executeBatch();
			stmt.close();
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	/**
	 * 仓库表 - Warehouse
	 * 1 row in the WAREHOUSE table for each configured warehouse with:
	 *   W_ID unique within [number_of_configured_warehouses]
	 *   W_NAME random a-string [6 .. 10]
	 *   W_STREET_1 random a-string [10 .. 20] 
	 *   W_STREET_2 random a-string [10 .. 20] 
	 *   W_CITY random a-string [10 .. 20] 
	 *   W_STATE random a-string of 2 letters
	 *   W_ZIP generated according to Clause 4.3.2.7 
	 *   W_TAX random within [0.0000 .. 0.2000]
	 *   W_YTD = 300,000.00
	 * 
	 * @param wareCount
	 * @throws Throwable
	 */
	private static long warehouse(int wareCount) throws Throwable {
		int w_id = 1;
		String w_name = null;
		String w_street_1 = null;
		String w_street_2 = null;
		String w_city = null;
		String w_state = null;
		String w_zip = null;
		double w_tax = 0;
		double w_ytd = 0;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.loadWarehouseStmt();
		long rows = 0l;
		try {
			final PreparedStatement stmt = connection.prepareStatement(sqlText);
			for (; w_id <= wareCount; w_id++) {
				w_name = RandomHelper.randomString(6, 10);
				w_street_1 = RandomHelper.randomString(10, 20);
				w_street_2 = RandomHelper.randomString(10, 20);
				w_city = RandomHelper.randomString(10, 20);
				w_state = RandomHelper.randomString(2);
				w_zip = RandomHelper.randomNumberString(9); //这里没有实现TPC-C 4.3.2.7的定义, 仅为定长数字类型字符串;
				w_tax = RandomHelper.randomDecimal(4, 0.0000 , 0.2001);
				w_ytd = 300000.00D;
				stmt.setInt(1, w_id);
				stmt.setString(2, w_name);
				stmt.setString(3, w_street_1);
				stmt.setString(4, w_street_2);
				stmt.setString(5, w_city);
				stmt.setString(6, w_state);
				stmt.setString(7, w_zip);
				stmt.setDouble(8, w_tax);
				stmt.setDouble(9, w_ytd);
				stmt.executeUpdate();
				rows ++;
			}
			stmt.close();
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	} 
	
	/**
	 * 库存表 - stock 
	 * For each row in the WAREHOUSE table: 
	 * 100,000 rows in the STOCK table with:
	 *   S_I_ID unique within [100,000]
	 *   S_W_ID = W_ID
	 *   S_QUANTITY random within [10 .. 100]
	 *   S_DIST_01 random a-string of 24 letters
	 *   S_DIST_02 random a-string of 24 letters
	 *   S_DIST_03 random a-string of 24 letters
	 *   S_DIST_04 random a-string of 24 letters
	 *   S_DIST_05 random a-string of 24 letters
	 *   S_DIST_06 random a-string of 24 letters
	 *   S_DIST_07 random a-string of 24 letters
	 *   S_DIST_08 random a-string of 24 letters
	 *   S_DIST_09 random a-string of 24 letters
	 *   S_DIST_10 random a-string of 24 letters
	 *   S_YTD = 0
	 *   S_ORDER_CNT = 0
	 *   S_REMOTE_CNT = 0
	 *   S_DATA random a-string [26 .. 50]. For 10% of the rows, selected at random, the string 	
	 *     "original" must be held by 8 consecutive characters starting at a random position within 	
	 *     S_DATA
	 *   
	 * @param w_id
	 * @throws Throwable 
	 */
	
	private static long stock(final int w_id) throws Throwable {
		int s_i_id = 1;
		int s_w_id = w_id;
		int s_quantity;
		String s_dist_01;
		String s_dist_02;
		String s_dist_03;
		String s_dist_04;
		String s_dist_05;
		String s_dist_06;
		String s_dist_07;
		String s_dist_08;
		String s_dist_09;
		String s_dist_10;
		int s_ytd = 0;
		int s_order_cnt = 0;
		int s_remote_cnt = 0;
		String s_data;
		final BitSet origBitmap = RandomHelper.randomBitMap(MAX_ITEMS / 10, MAX_ITEMS);
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.loadStockStmt();
		long rows = 0l;
		try {
			PreparedStatement stmt = connection.prepareStatement(sqlText);
			for (s_i_id = 1; s_i_id <= MAX_ITEMS; s_i_id++) {
				s_quantity = RandomHelper.randomInt(10, 100);
				s_dist_01 = RandomHelper.randomString(24);
				s_dist_02 = RandomHelper.randomString(24);
				s_dist_03 = RandomHelper.randomString(24);
				s_dist_04 = RandomHelper.randomString(24);
				s_dist_05 = RandomHelper.randomString(24);
				s_dist_06 = RandomHelper.randomString(24);
				s_dist_07 = RandomHelper.randomString(24);
				s_dist_08 = RandomHelper.randomString(24);
				s_dist_09 = RandomHelper.randomString(24);
				s_dist_10 = RandomHelper.randomString(24);
				s_data = RandomHelper.randomString(26, 50);
				if (origBitmap.get(s_i_id - 1)) {
					int pos = RandomHelper.randomInt(0, s_data.length() - 8); 
					s_data = replaceString(s_data, pos, "original");
				}
				stmt.setInt(1, s_i_id);
				stmt.setInt(2, s_w_id);
				stmt.setInt(3, s_quantity);
				stmt.setString(4, s_dist_01);
				stmt.setString(5, s_dist_02);
				stmt.setString(6, s_dist_03);
				stmt.setString(7, s_dist_04);
				stmt.setString(8, s_dist_05);
				stmt.setString(9, s_dist_06);
				stmt.setString(10, s_dist_07);
				stmt.setString(11, s_dist_08);
				stmt.setString(12, s_dist_09);
				stmt.setString(13, s_dist_10);
				stmt.setInt(14, s_ytd);
				stmt.setInt(15, s_order_cnt);
				stmt.setInt(16, s_remote_cnt);
				stmt.setString(17, s_data);
				stmt.addBatch();
				rows ++;
				if (s_i_id % SQL_BATCH_SIZE == 0) { // MySQL连接属性需要设置: rewriteBatchedStatements=true;
					stmt.executeBatch();
				}
			}
			stmt.executeBatch();
			stmt.close();
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}

	/**
	 * 区域表 - District
	 * 
	 * 10 rows in the DISTRICT table  with:
	 *   D_ID unique within [10]
	 *   D_W_ID = W_ID
	 *   D_NAME random a-string [6 .. 10]
	 *   D_STREET_1 random a-string [10 .. 20]
	 *   D_STREET_2 random a-string [10 .. 20]
	 *   D_CITY random a-string [10 .. 20]
	 *   D_STATE random a-string of 2 letters
	 *   D_ZIP generated according to Clause 4.3.2.7
	 *   D_TAX random within [0.0000 .. 0.2000]
	 *   D_YTD = 30,000.00
	 *   D_NEXT_O_ID = 3,001
	 * 
	 * @param d_id
	 * @param w_id
	 * @throws Throwable
	 */
	
	private static long district(int w_id) throws Throwable {
		int d_id = 1;
		int d_w_id = w_id;
		String d_name;
		String d_street_1;
		String d_street_2;
		String d_city;
		String d_state;
		String d_zip;
		double d_tax;
		double d_ytd = 30000.0d;
		int d_next_o_id = 3001;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.loadDistrictStmt();
		long rows = 0l;
		try {
			final PreparedStatement stmt = connection.prepareStatement(sqlText);
			for (d_id = 1; d_id <= DIST_PER_WARE; d_id++) {
				d_name = RandomHelper.randomString(6, 10);
				d_street_1 = RandomHelper.randomString(10, 20);
				d_street_2 = RandomHelper.randomString(10, 20);
				d_city = RandomHelper.randomString(10, 20);
				d_state = RandomHelper.randomString(2);
				d_zip = RandomHelper.randomNumberString(9);
				d_tax = RandomHelper.randomDecimal(4, 0.0000, 0.2001);
				stmt.setInt(1, d_id);
				stmt.setInt(2, d_w_id);
				stmt.setString(3, d_name);
				stmt.setString(4, d_street_1);
				stmt.setString(5, d_street_2);
				stmt.setString(6, d_city);
				stmt.setString(7, d_state);
				stmt.setString(8, d_zip);
				stmt.setDouble(9, d_tax);
				stmt.setDouble(10, d_ytd);
				stmt.setInt(11, d_next_o_id);
				int ret = stmt.executeUpdate();
				rows += ret;
				/*
				if(logger.isTraceEnabled()) {
					final StringBuilder msg = new StringBuilder();
					msg.append("District Wid1 = ").append(w_id).append(": ").append(sqlText).append("; ").append(d_id)
							.append(", ").append(d_w_id).append(", ").append(d_name).append(", ").append(d_street_1)
							.append(", ").append(d_street_2).append(", ").append(d_city).append(", ").append(d_state)
							.append(", ").append(d_zip).append(", ").append(d_tax).append(", ").append(d_ytd)
							.append(", ").append(d_next_o_id).append(" (  ").append(ret).append(" )");
					logger.trace(msg.toString());
				}*/
			}
			stmt.close();
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	/**
	 * For each row in the DISTRICT table:
	 * 3,000 rows in the CUSTOMER table with:
	 *   C_ID unique within [3,000]
	 *   C_D_ID = D_ID
	 *   C_W_ID = D_W_ID
	 *   C_LAST generated according to Clause 4.3.2.3, iterating through the range of  [0 .. 999] for the first 1,000 customers, 
	 *          and generating a non-uniform random number using the function NURand(255,0,999) for each of the remaining 2,000 
	 *          customers. The run-time constant C (see Clause 2.1.6) used for the database population must be randomly chosen 
	 *          independently from the test run(s).
	 *   C_MIDDLE = "OE"
	 *   C_FIRST random a-string [8 .. 16]
	 *   C_STREET_1 random a-string [10 .. 20] 
	 *   C_STREET_2 random a-string [10 .. 20] 
	 *   C_CITY random a-string [10 .. 20] 
	 *   C_STATE random a-string of 2 letters
	 *   C_ZIP generated according to Clause 4.3.2.7 
	 *   C_PHONE random n-string of 16 numbers
	 *   C_SINCE date/time given by the operating system when the CUSTOMER table was populated.
	 *   C_CREDIT = "GC". For 10% of the rows, selected at random, C_CREDIT = "BC"
	 *   C_CREDIT_LIM = 50,000.00
	 *   C_DISCOUNT random within [0.0000 .. 0.5000]
	 *   C_BALANCE = -10.00
	 *   C_YTD_PAYMENT = 10.00
	 *   C_PAYMENT_CNT = 1
	 *   C_DELIVERY_CNT = 0
	 *   C_DATA random a-string [300 .. 500]
	 *  
	 * For each row in the CUSTOMER table:
	 * 1 row in the HISTORY table with:
	 *   H_C_ID = C_ID
	 *   H_C_D_ID = H_D_ID = D_ID
	 *   H_C_W_ID = H_W_ID = W_ID
	 *   H_DATE current date and time
	 *   H_AMOUNT = 10.00
	 *   H_DATA random a-string [12 .. 24]
	 *   
	 * @param d_id
	 * @param w_id
	 * @throws Throwable
	 */
	
	private static long customer(int d_id, int w_id) throws Throwable {
		int c_id = 1;
		int c_d_id = d_id;
		int c_w_id = w_id;
		String c_first;
		String c_middle = "OE";
		String c_last;
		String c_street_1;
		String c_street_2;
		String c_city;
		String c_state;
		String c_zip;
		String c_phone;
		Timestamp c_since;
		String c_credit ;
		long c_credit_lim = 50000;
		double c_discount;
		double c_balance = -10.0d;
		double c_ytd_payment = 10.0d;
		int c_payment_cnt = 1;
		int c_delivery_cnt = 0;
		String c_data = null;
		
		int h_c_id; 
		int h_c_d_id; 
		int h_c_w_id;
		int h_d_id;
		int h_w_id;
		Timestamp h_date;
		double h_amount;
		String h_data;
		
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText1 = TpccStatements.loadCustomerStmt();  //insert into customer ...
		final String sqlText2 = TpccStatements.loadHistoryStmt();   //insert into history ...
		
		long rows = 0l;
		try {
			final PreparedStatement stmt1 = connection.prepareStatement(sqlText1);
			final PreparedStatement stmt2 = connection.prepareStatement(sqlText2);
			for (; c_id <= CUST_PER_DIST; c_id++) {
				c_first = RandomHelper.randomString(8, 16);
				// c_last
				if (c_id <= 1000) {
					c_last = RandomHelper.lastName(c_id - 1);
				} else {
					c_last = RandomHelper.lastName(RandomHelper.nuRand(255, 0, 999));
				}
				
				c_street_1 = RandomHelper.randomString(10, 20);
				c_street_2 = RandomHelper.randomString(10, 20);
				c_city = RandomHelper.randomString(10, 20);
				c_state = RandomHelper.randomString(2);
				c_zip = RandomHelper.randomNumberString(9);
				c_phone = RandomHelper.randomNumberString(16);
				c_since = TpccHelper.getTimestamp();
				
				// c_credit
				if (RandomHelper.randomBoolean()) {
					c_credit = "BC";
				} else {
					c_credit = "GC";
				}
				
				c_discount = RandomHelper.randomDecimal(4, 0.0000, 0.5001);
				c_data = RandomHelper.randomString(300, 500);
				
				stmt1.setInt(1, c_id);
				stmt1.setInt(2, c_d_id);
				stmt1.setInt(3, c_w_id);
				stmt1.setString(4, c_first);
				stmt1.setString(5, c_middle);
				stmt1.setString(6, c_last);
				stmt1.setString(7, c_street_1);
				stmt1.setString(8, c_street_2);
				stmt1.setString(9, c_city);
				stmt1.setString(10, c_state);
				stmt1.setString(11, c_zip);
				stmt1.setString(12, c_phone);
				stmt1.setTimestamp(13, c_since);
				stmt1.setString(14, c_credit);
				stmt1.setLong(15, c_credit_lim);
				stmt1.setDouble(16, c_discount);
				stmt1.setDouble(17, c_balance);
				stmt1.setDouble(18, c_ytd_payment);
				stmt1.setInt(19, c_payment_cnt);
				stmt1.setInt(20, c_delivery_cnt);
				stmt1.setString(21, c_data);
				stmt1.addBatch();
				rows ++;
				
				h_c_id = c_id;
				h_c_d_id = d_id;
				h_c_w_id = w_id;
				h_d_id = d_id;
				h_w_id = w_id;
				h_date = TpccHelper.getTimestamp();
				h_amount = 10.00D;
				h_data = RandomHelper.randomString(12, 24);
				stmt2.setInt(1, h_c_id);
				stmt2.setInt(2, h_c_d_id);
				stmt2.setInt(3, h_c_w_id);
				stmt2.setInt(4, h_d_id);
				stmt2.setInt(5, h_w_id);
				stmt2.setTimestamp(6, h_date);
				stmt2.setDouble(7, h_amount);
				stmt2.setString(8, h_data);
				stmt2.addBatch();
				rows ++;
				if (c_id % SQL_BATCH_SIZE == 0) { 
					stmt1.executeBatch();
					stmt2.executeBatch();
				}
			}
			stmt1.executeBatch();
			stmt2.executeBatch();
			stmt1.close();
			stmt2.close();
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	/**
	 * Population orders, new_orders, order_line tables.
	 * 
	 * 3,000 rows in the ORDER table with:
	 *   O_ID unique within [3,000]
	 *   O_C_ID selected sequentially from a random permutation of [1 .. 3,000]
	 *   O_D_ID = D_ID
	 *   O_W_ID = W_ID
	 *   O_ENTRY_D current date/time given by the operating system
	 *   O_CARRIER_ID random within [1 .. 10] if O_ID < 2,101,  null otherwise
	 *   O_OL_CNT random within [5 .. 15]
	 *   O_ALL_LOCAL = 1
	 * 
	 * 900 rows in the NEW-ORDER table corresponding to the last 900 rows in the 
	 * ORDER table for that district (i.e., with NO_O_ID between 2,101 and 3,000), with: 
	 *   NO_O_ID = O_ID
	 *   NO_D_ID = D_ID
	 *   NO_W_ID = W_ID
	 *   
	 * A number of rows in the ORDER-LINE table equal to O_OL_CNT, generated according to 
	 * the rules for input data generation of the New-Order transaction (see Clause 2.4.1) with:
	 *   OL_O_ID = O_ID
	 *   OL_D_ID =  D_ID
	 *   OL_W_ID = W_ID
	 *   OL_NUMBER unique within [O_OL_CNT]
	 *   OL_I_ID random within [1 .. 100,000]
	 *   OL_SUPPLY_W_ID = W_ID
	 *   OL_DELIVERY_D = O_ENTRY_D if OL_O_ID < 2,101,  null otherwise
	 *   OL_QUANTITY = 5
	 *   OL_AMOUNT = 0.00 if OL_O_ID < 2,101, random within [0.01 .. 9,999.99] otherwise
	 *   OL_DIST_INFO random a-string of 24 letters
	 * 
	 * Flow:
	 * 
	 * for(ord_per_dist) {
	 *   if(o_id > 2100) {
	 *     insert into new_orders ...
	 *     insert into orders ...
	 *   } else {
	 *     insert into orders ...
	 *   }
	 *   for(o_ol_cnt) {
	 *     if(o_id > 2100) {
	 *       insert into order_line with
	 *          ol_quantity = null,  
	 *          ol_amount = random within [0.01 .. 99.99];
	 *     } else {
	 *       insert into order_line with 
	 *       ol_quantity = o_entry_d,
	 *       al_amount = 0.0;  
	 *     }
	 *   }
	 * }
	 * 
	 * @param d_id
	 * @param w_id
	 * @throws Throwable 
	 */
	
	public static long orders(int d_id, int w_id) throws Throwable {
		int o_id;
		int o_d_id = d_id;
		int o_w_id = w_id;
		int o_c_id;
		Timestamp o_entry_d;
		Integer o_carrier_id; //
		int o_ol_cnt;
		int o_all_local = 1;
		int[] nums = RandomHelper.randomPermutation(ORD_PER_DIST);
		
		int no_o_id ;
		int no_d_id = d_id;
		int no_w_id = w_id;
		
		final Connection connection1 = TpccContext.getContext().getConnection();
		final String sqlText1 = TpccStatements.loadOrdersStmt();   // insert into orders ...
		final String sqlText2 = TpccStatements.loadNewOrdersStmt(); // insert into new_orders ...
		final String sqlText3 = TpccStatements.loadOrderLineStmt(); // insert into new_line ...
		
		long rows = 0l;
		try {
			final PreparedStatement stmt1 = connection1.prepareStatement(sqlText1);
			final PreparedStatement stmt2 = connection1.prepareStatement(sqlText2);
			final PreparedStatement stmt3 = connection1.prepareStatement(sqlText3);
			for (o_id = 1; o_id <= ORD_PER_DIST; o_id++) {
				o_c_id = nums[o_id - 1]; // array 's index start with 0
				o_entry_d = TpccHelper.getTimestamp();
				o_ol_cnt = RandomHelper.randomInt(5, 15);
				o_all_local = 1;
				//
				if (o_id > 2100) { // the last 900 orders have not been delivered, o_carrier_id = null;
					//o_carrier_id = null;
					no_o_id = o_id;
					stmt1.setNull(6, Types.INTEGER); 
					stmt2.setInt(1, no_o_id);
					stmt2.setInt(2, no_d_id);
					stmt2.setInt(3, no_w_id);
					stmt2.addBatch();
					rows ++;
				} else {
					o_carrier_id = RandomHelper.randomInt(1, 10);
					stmt1.setInt(6, o_carrier_id);
				}
				
				stmt1.setInt(1, o_id);
				stmt1.setInt(2, o_d_id);
				stmt1.setInt(3, o_w_id);
				stmt1.setInt(4, o_c_id);
				stmt1.setTimestamp(5, o_entry_d);
				//stmt.setInt(6, o_carrier_id); see above ...
				stmt1.setInt(7, o_ol_cnt);
				stmt1.setInt(8, o_all_local);
				stmt1.addBatch();
				rows ++;
				
				//-> order_line
				int ol_o_id = o_id;
				int ol_d_id = d_id;
				int ol_w_id = w_id;
				int ol_number = 1;
				int ol_i_id;
				int ol_supply_w_id = o_w_id;
				Timestamp ol_delivery_d = null;
				int ol_quantity = 5;
				double ol_amount = 0.0d;
				String ol_dist_info;
				
				for (ol_number = 1; ol_number <= o_ol_cnt; ol_number++) {
					ol_i_id= RandomHelper.randomInt(1, MAX_ITEMS);
					ol_dist_info = RandomHelper.randomString(24);
					// OL_DELIVERY_D = O_ENTRY_D if OL_O_ID < 2,101,  null otherwise;
					// OL_AMOUNT = 0.00 if OL_O_ID < 2,101, random within [0.01 .. 9,999.99] otherwise;
					if (ol_o_id > 2100) {
						stmt3.setNull(7, Types.TIMESTAMP);
						ol_amount = RandomHelper.randomDecimal(2, 0.01, 100.00);
					} else {
						ol_delivery_d = TpccHelper.getTimestamp();
						stmt3.setTimestamp(7, ol_delivery_d);
						ol_amount = 0.0D;
					}
					stmt3.setInt(1, ol_o_id);
					stmt3.setInt(2, ol_d_id);
					stmt3.setInt(3, ol_w_id);
					stmt3.setInt(4, ol_number);
					stmt3.setInt(5, ol_i_id);
					stmt3.setInt(6, ol_supply_w_id);
					//stmt3.setTimestamp(7, ol_delivery_d);
					stmt3.setInt(8, ol_quantity);
					stmt3.setDouble(9, ol_amount);
					stmt3.setString(10, ol_dist_info);
					//stmt3.execute();
					stmt3.addBatch();
					/*if(logger.isTraceEnabled()) {
						final StringBuilder msg = new StringBuilder();
						msg.append("Order_Line Wid1 = ").append(w_id).append(": ").append(sqlText3).append("; ")
							.append(ol_o_id).append(", ")
							.append(ol_d_id).append(", ")
							.append(ol_w_id).append(", ")
							.append(ol_number).append(", ")
							.append(ol_i_id).append(", ")
							.append(ol_supply_w_id).append(", ")
							.append(ol_delivery_d).append(", ")
							.append(ol_quantity).append(", ")
							.append(ol_amount).append(", ")
							.append(ol_dist_info);
						logger.trace(msg.toString());
					}*/
					rows ++;
				}
				
				if (o_id % SQL_BATCH_SIZE == 0) {
					stmt1.executeBatch();
					stmt2.executeBatch();
					stmt3.executeBatch();
				}
			}
			stmt1.executeBatch();
			stmt2.executeBatch();
			stmt3.executeBatch();
			stmt1.close();
			stmt2.close();
			stmt3.close();
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection1);
		}
		return rows;
	}
	
	private static String replaceString(String target, int startPos,  String replacement) {
		int originalLength = target.length();
		if (startPos < 0 | startPos > originalLength) {
			return target;
		}
		String newString = target.substring(0, startPos);
		newString += replacement;

		startPos = newString.length();
		if (startPos == originalLength) {
			return newString;
		} else if (startPos > originalLength) {
			return newString.substring(0, originalLength);
		} else {
			newString += target.substring(startPos, originalLength);
		}
		return newString;
	}
}
