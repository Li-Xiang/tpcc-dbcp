package org.littlestar.tpcc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.littlestar.helper.DataSourceHelper;
import org.littlestar.helper.RandomHelper;
import org.littlestar.tpcc.dbo.CustomerDbo;
import org.littlestar.tpcc.dbo.DistrictDbo;
import org.littlestar.tpcc.dbo.ItemDbo;
import org.littlestar.tpcc.dbo.NewOrdersDbo;
import org.littlestar.tpcc.dbo.OrderLineDbo;
import org.littlestar.tpcc.dbo.OrdersDbo;
import org.littlestar.tpcc.dbo.StockDbo;
import org.littlestar.tpcc.dbo.WarehouseDbo;

public class TpccTransaction implements TpccConstants {
	public static Log logger = LogFactory.getLog(TpccTransaction.class);
	public static void run(int threads, long warmup, int reportPeriod, int measureTime) throws Throwable {
		final ExecutorService benchmarkExecutor = Executors.newCachedThreadPool();
		final ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(10);
		final LinkedList<TpccThread> tpccThreads = new LinkedList<TpccThread>();
		////// 创建并启动测试线程。
		for (int i = 0; i < threads; i++) {
			TpccThread task = new TpccThread();
			benchmarkExecutor.execute(task);
			tpccThreads.add(task);
		}
		
		////// 热身, 延迟warmup时间开启事务计数器标志.
		if (warmup > 0) {
			TpccHelper.output("Ramp-up time. (" + warmup + " sec.) ");
			TimeUnit.SECONDS.sleep(warmup);
		}
		
		TpccContext.CountingOn.set(true);  // 开启事务计数器
		TpccHelper.output("Measuring start.");
		TpccHelper.reportHeader();
		
		if (reportPeriod > 0) {  
			// 创建定时任务, 输出结果.
			final ReportThread reportTask = new ReportThread(tpccThreads, reportPeriod);
			scheduleExecutor.scheduleAtFixedRate(reportTask, reportPeriod, reportPeriod, TimeUnit.SECONDS);
		}
		
		final Runnable shutdownThread = new Runnable() {
			@Override
			public void run() {
				TpccContext.CountingOn.set(false);    // 置事务计数器标记为关闭.
				TpccContext.transactionOn.set(false); // 置事务执行标记为关闭.
				benchmarkExecutor.shutdown();         // 接收新任务, 关闭线程池。
				scheduleExecutor.shutdown();
			}
		};
		scheduleExecutor.schedule(shutdownThread, measureTime, TimeUnit.SECONDS);
		while (true) {
			if (!scheduleExecutor.awaitTermination(measureTime + 10, TimeUnit.SECONDS)) {
				TpccHelper.output("Stoping threads ...");
				scheduleExecutor.shutdownNow();
			}
			TpccHelper.reportFooter(tpccThreads, measureTime);
			break;
		}
	}
	
	/**
	 * 
	 * 2.4	The New-Order Transaction
	 *   --> 2.4.2	Transaction Profile
	 *   --> 2.4.3	Terminal I/O --> not implemented! 
	 *     TPCC规范中, 规定了每个事务终端(Terminal I/O)需要显示的信息, 但这里是没有实现, 只是从数据库中查询出需要的字段. 
	 * A.1	The New-Order Transaction
	 * 
	 * @param w_id warehouse id
	 * @param d_id district id
	 * @param c_id customer id
	 * @param o_ol_cnt number of items
	 * @param o_all_local are all order lines local 
	 * @param itemid ids of items to be ordered
	 * @param supware warehouses supplying items
	 * @param qty quantity of each item
	 * @return true: transaction success, otherwise failure.
	 * @throws Throwable 
	 */
	
	public static boolean newOrder(final int w_id, final int d_id, final int c_id, final int o_ol_cnt,
			final int o_all_local, final int itemid[], final int supware[], final int qty[]) throws Throwable {
		
		final Connection connection = TpccContext.getContext().getConnection();
		try {
			// Start transaction.
			connection.setAutoCommit(false); 
			
			///// proceed = 1;
			double c_discount ;
			String c_last;
			String c_credit;
			double w_tax;
			final String sqlText1 = TpccStatements.newOrderStmt1();
			final PreparedStatement stmt1 = connection.prepareStatement(sqlText1);
			stmt1.setInt(1, w_id);
			stmt1.setInt(2, d_id);
			stmt1.setInt(3, c_id);
			final ResultSet rs1 = stmt1.executeQuery();
			if (rs1.next()) {
				c_discount = rs1.getDouble(CustomerDbo.CN_C_DISCOUNT);
				c_last = rs1.getString(CustomerDbo.CN_C_LAST);
				c_credit = rs1.getString(CustomerDbo.CN_C_CREDIT);
				w_tax = rs1.getDouble(WarehouseDbo.CN_W_TAX);
			} else {
				rs1.close();
				stmt1.close();
				throw new NoDataFoundException("NEW-ORDER.1: " + sqlText1 + "; " + w_id + ", " + w_id + ", " + d_id + ", " + c_id
						+ " (NO_DATA_FOUND)");
			}
			if(logger.isTraceEnabled()) {
				logger.trace("NEW-ORDER.1: " + sqlText1 + "; " + w_id + ", " + w_id + ", " + d_id + ", " + c_id
						+" ( c_discount= " + c_discount +"; c_last= "+ c_last+ "; c_credit="+c_credit+"; w_tax="+w_tax+" )");
			}
			rs1.close();
			stmt1.close();
			
			///// proceed = 2;
			int d_next_o_id = 0;
			double d_tax;
			final String sqlText2 = TpccStatements.newOrderStmt2();
			final PreparedStatement stmt2 = connection.prepareStatement(sqlText2);
			stmt2.setInt(1, d_id);
			stmt2.setInt(2, w_id);
			final ResultSet rs2 = stmt2.executeQuery();
			if (rs2.next()) {
				d_next_o_id = rs2.getInt(DistrictDbo.CN_D_NEXT_O_ID);
				d_tax = rs2.getDouble(DistrictDbo.CN_D_TAX);
			} else {
				rs2.close();
				stmt2.close();
				throw new NoDataFoundException("NEW-ORDER.2: "+ sqlText2 +"; "+ d_id+ ", "+ w_id + " (NO_DATA_FOUND)");
			}
			if(logger.isTraceEnabled()) {
				logger.trace("NEW-ORDER.2: " + sqlText2 +"; "+ d_id+ ", "+ w_id +" ( d_next_o_id= " + d_next_o_id +"; d_tax= "+ d_tax+" )");
			}
			rs2.close();
			stmt2.close();
			
			///// proceed = 3;
			final String sqlText3 = TpccStatements.newOrderStmt3();
			final PreparedStatement stmt3 = connection.prepareStatement(sqlText3);
			stmt3.setInt(1, d_next_o_id);
			stmt3.setInt(2, d_id);
			stmt3.setInt(3, w_id);
			int updates = stmt3.executeUpdate();
			if(updates != 1) {
				throw new NoDataFoundException("NEW-ORDER.3: "+sqlText3 + "; " + d_next_o_id + ", " + d_id + ", " + w_id + " (NO_DATA_FOUND)");
			}
			if(logger.isTraceEnabled()) {
				logger.trace("NEW-ORDER.3: " + sqlText3 + "; " + d_next_o_id + 1 + ", " + d_id + ", " + w_id + " ( "+ updates + " )");
			}
			stmt3.close();
			
			///// proceed = 4;
			int o_id = d_next_o_id;
			Timestamp o_entry_d = TpccHelper.getTimestamp();
			final String sqlText4 = TpccStatements.newOrderStmt4();
			final PreparedStatement stmt4 = connection.prepareStatement(sqlText4);
			stmt4.setInt(1, o_id);
			stmt4.setInt(2, d_id);
			stmt4.setInt(3, w_id);
			stmt4.setInt(4, c_id);
			stmt4.setTimestamp(5, o_entry_d);
			stmt4.setInt(6, o_ol_cnt);
			stmt4.setInt(7, o_all_local);
			
			updates = stmt4.executeUpdate();
			if(logger.isTraceEnabled()) {
				logger.trace("NEW-ORDER.4: " + sqlText4 + "; " + o_id + ", " + d_id + ", " + w_id + ", " + c_id + ", " + o_entry_d + ", " + o_ol_cnt + ", " + o_all_local 
						+ " ( " + updates + " )");
			}
			stmt4.close();
			
			///// proceed = 5;
			final String sqlText5 = TpccStatements.newOrderStmt5();
			final PreparedStatement stmt5 = connection.prepareStatement(sqlText5);
			stmt5.setInt(1, o_id);
			stmt5.setInt(2, d_id);
			stmt5.setInt(3, w_id);
			stmt5.executeUpdate();
			if(logger.isTraceEnabled()) {
				logger.trace("NEW-ORDER.5: " + sqlText5 +"; "+ o_id+ ", "+d_id+ ", "+w_id+ " ( "+updates+" )");
			}
			stmt5.close();
			
			for (int ol_number = 1; ol_number <= o_ol_cnt; ol_number++) {
				int ol_supply_w_id = supware[ol_number - 1];
				int ol_i_id = itemid[ol_number - 1];
				int ol_quantity = qty[ol_number - 1];

				//// proceed = 6;
				double i_price;
				String i_name;
				String i_data;
				final String sqlText6 = TpccStatements.newOrderStmt6();
				final PreparedStatement stmt6 = connection.prepareStatement(sqlText6);
				stmt6.setInt(1, ol_i_id);
				final ResultSet rs6 = stmt6.executeQuery();
				if (rs6.next()) {
					i_price = rs6.getDouble(ItemDbo.CN_I_PRICE);
					i_name = rs6.getString(ItemDbo.CN_I_NAME);
					i_data = rs6.getString(ItemDbo.CN_I_DATA);
				} else {
					rs6.close();
					stmt6.close();
					connection.rollback();
					return true; // return(0); 随机1%的rollback.
				}
				if(logger.isTraceEnabled()) {
					logger.trace("NEW-ORDER.6: " + sqlText6 +"; "+ ol_i_id +" ( i_price= " + i_price +"; i_name= "+ i_name+"; i_data= "+ i_data+" )");
				}
				rs6.close();
				stmt6.close();
				
				//// proceed = 7;
				int s_quantity;
				String s_data;
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

				final String sqlText7 = TpccStatements.newOrderStmt7();
				final PreparedStatement stmt7 = connection.prepareStatement(sqlText7);
				stmt7.setInt(1, ol_i_id);
				stmt7.setInt(2, ol_supply_w_id);
				final ResultSet rs7 = stmt7.executeQuery();
				if (rs7.next()) {
					s_quantity = rs7.getInt(StockDbo.CN_S_QUANTITY);
					s_data = rs7.getString(StockDbo.CN_S_DATA);
					s_dist_01 = rs7.getString(StockDbo.CN_S_DIST_01);
					s_dist_02 = rs7.getString(StockDbo.CN_S_DIST_02);
					s_dist_03 = rs7.getString(StockDbo.CN_S_DIST_03);
					s_dist_04 = rs7.getString(StockDbo.CN_S_DIST_04);
					s_dist_05 = rs7.getString(StockDbo.CN_S_DIST_05);
					s_dist_06 = rs7.getString(StockDbo.CN_S_DIST_06);
					s_dist_07 = rs7.getString(StockDbo.CN_S_DIST_07);
					s_dist_08 = rs7.getString(StockDbo.CN_S_DIST_08);
					s_dist_09 = rs7.getString(StockDbo.CN_S_DIST_09);
					s_dist_10 = rs7.getString(StockDbo.CN_S_DIST_10);
				} else {
					rs7.close();
					stmt7.close();
					throw new NoDataFoundException("NEW-ORDER.7: "+ sqlText7 + "; " + ol_i_id + ", " + ol_supply_w_id + " (NO_DATA_FOUND)");
				}
				if(logger.isTraceEnabled()) {
					logger.trace("NEW-ORDER.7: " + sqlText7 + "; " + ol_i_id + ", " + ol_supply_w_id 
							+" ( s_quantity= " + s_quantity + "; s_data= " + s_data + "; s_dist_01= " + s_dist_01 + " )");
				}
				rs7.close();
				stmt7.close();

				//// proceed = 8;
				if (s_quantity > ol_quantity) {
					s_quantity = s_quantity - ol_quantity;
				} else {
					s_quantity = s_quantity - ol_quantity + 91;
				}

				final String sqlText8 = TpccStatements.newOrderStmt8();
				final PreparedStatement stmt8 = connection.prepareStatement(sqlText8);
				stmt8.setInt(1, s_quantity);
				stmt8.setInt(2, ol_i_id);
				stmt8.setInt(3, ol_supply_w_id);
				updates = stmt8.executeUpdate();
				if(logger.isTraceEnabled()) {
					logger.trace("NEW-ORDER.8: " + sqlText8 + "; " + s_quantity + ", " + ol_i_id + ", " + ol_supply_w_id + " ( " + updates + " )");
				}
				stmt8.close();
				
				//// proceed = 9;
				double ol_amount = ol_quantity * i_price * (1 + w_tax + d_tax) * (1 - c_discount);
				
				String ol_dist_info = "";
				switch (ol_supply_w_id) {
				case 1: ol_dist_info = s_dist_01; break;
				case 2: ol_dist_info = s_dist_02; break;
				case 3: ol_dist_info = s_dist_03; break;
				case 4: ol_dist_info = s_dist_04; break;
				case 5: ol_dist_info = s_dist_05; break;
				case 6: ol_dist_info = s_dist_06; break;
				case 7: ol_dist_info = s_dist_07; break;
				case 8: ol_dist_info = s_dist_08; break;
				case 9: ol_dist_info = s_dist_09; break;
				case 10: ol_dist_info = s_dist_10; break;
				default:
					throw new IllegalArgumentException("Illegal ol_supply_w_id = " + ol_supply_w_id + ", must in [1..10].");
				}
				
				final String sqlText9 = TpccStatements.newOrderStmt9();
				final PreparedStatement stmt9 = connection.prepareStatement(sqlText9);
				stmt9.setInt(1, o_id);
				stmt9.setInt(2, d_id);
				stmt9.setInt(3, w_id);
				stmt9.setInt(4, ol_number);
				stmt9.setInt(5, ol_i_id);
				stmt9.setInt(6, ol_supply_w_id);
				stmt9.setInt(7, ol_quantity);
				stmt9.setDouble(8, ol_amount);
				stmt9.setString(9, ol_dist_info);
				stmt9.executeUpdate();
				if(logger.isTraceEnabled()) {
					logger.trace("NEW-ORDER.9: " + sqlText9 + "; " + o_id + ", " + d_id + ", " + w_id + "," + ol_number + ", " + ol_i_id + ", " + ol_supply_w_id + ", " + ol_quantity + ", " + ol_amount + ", " + ol_dist_info 
							+ " ( " + updates + " )");
				}
				stmt9.close();
			}
			connection.commit();  // end transaction.
		} catch (Throwable e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true); 
			DataSourceHelper.closeConnection(connection);
		}
		return true;
	}
	
	/**
	 * 2.5	The Payment Transaction
	 *   --> 2.5.2	Transaction Profile
	 * A.2	The Payment Transaction
	 * 
	 * @param w_id warehouse id
	 * @param d_id district id
	 * @param byname select by c_id or c_last?
	 * @param c_w_id
	 * @param c_d_id
	 * @param c_id customer id
	 * @param c_last customer last name
	 * @param h_amount  payment amount
	 * @throws Throwable
	 */
	public static boolean payment(final int w_id, final int d_id, final boolean byname, final int c_w_id,
			final int c_d_id, final int c_id, final String c_last, final int h_amount) throws Throwable {
		final Connection connection = TpccContext.getContext().getConnection();
		try {
			// Start transaction.
			connection.setAutoCommit(false);
			// proceed = 1;
			final String sqlText1 = TpccStatements.paymentStmt1();
			final PreparedStatement stmt1 = connection.prepareStatement(sqlText1);
			stmt1.setInt(1, h_amount);
			stmt1.setInt(2, w_id);
			int updates = stmt1.executeUpdate();
			logger.trace("PAYMENT.1: " + sqlText1 + "; " + h_amount + ", " + w_id + "( " + updates + " )");
			stmt1.close();

			// proceed = 2;
			final String sqlText2 = TpccStatements.paymentStmt2();
			final PreparedStatement stmt2 = connection.prepareStatement(sqlText2);
			stmt2.setInt(1, w_id);
			final ResultSet rs2 = stmt2.executeQuery();
			String w_street_1, w_street_2, w_city, w_state, w_zip, w_name;
			if (rs2.next()) {
				w_street_1 = rs2.getString(WarehouseDbo.CN_W_STREET_1);
				w_street_2 = rs2.getString(WarehouseDbo.CN_W_STREET_2);
				w_city = rs2.getString(WarehouseDbo.CN_W_CITY);
				w_state = rs2.getString(WarehouseDbo.CN_W_STATE);
				w_zip = rs2.getString(WarehouseDbo.CN_W_ZIP);
				w_name = rs2.getString(WarehouseDbo.CN_W_NAME);
				if(logger.isTraceEnabled()) {
					logger.trace("PAYMENT.2: " + sqlText2 + "; " + w_id 
							+ " ( w_street_1= " + w_street_1 + "; w_street_2= " + w_street_2 + "; w_city=" + w_city + "; w_state=" + w_state + "; w_zip=" + w_zip + "; w_name=" + w_name + " )");
				}
			} else {
				rs2.close();
				stmt2.close();
				throw new NoDataFoundException("PAYMENT.2: " + sqlText2 + "; " + w_id + " (NO_DATA_FOUND)");
			}
			rs2.close();
			stmt2.close();

			//proceed = 3;
			final String sqlText3 = TpccStatements.paymentStmt3();
			final PreparedStatement stmt3 = connection.prepareStatement(sqlText3);
			stmt3.setInt(1, h_amount);
			stmt3.setInt(2, w_id);
			stmt3.setInt(3, d_id);
			updates = stmt3.executeUpdate();
			if(logger.isTraceEnabled()) {
				logger.trace("PAYMENT.3: " + sqlText3 + "; " + h_amount + ", " + w_id + ", " + d_id 
						+ "( " + updates + " )");
			}
			stmt3.close();
			
			// proceed = 4;
			final String sqlText4 = TpccStatements.paymentStmt4();
			final PreparedStatement stmt4 = connection.prepareStatement(sqlText4);
			stmt4.setInt(1, w_id);
			stmt4.setInt(2, d_id);
			final ResultSet rs4 = stmt4.executeQuery();
			String d_street_1, d_street_2, d_city, d_state, d_zip, d_name;
			if (rs4.next()) {
				d_street_1 = rs4.getString(DistrictDbo.CN_D_STREET_1);
				d_street_2 = rs4.getString(DistrictDbo.CN_D_STREET_2);
				d_city = rs4.getString(DistrictDbo.CN_D_CITY);
				d_state = rs4.getString(DistrictDbo.CN_D_STATE);
				d_zip = rs4.getString(DistrictDbo.CN_D_ZIP);
				d_name = rs4.getString(DistrictDbo.CN_D_NAME);
				if(logger.isTraceEnabled()) {
					logger.trace("PAYMENT.4: " + sqlText4 + "; " + w_id + ", " + d_id 
						+ " ( d_street_1= " + d_street_1 + "; d_street_2= " + d_street_2 + "; d_city=" + d_city + "; d_state=" + d_state + "; d_zip=" + d_zip + "; d_name=" + d_name + " )");
				}
			} else {
				rs4.close();
				stmt4.close();
				throw new NoDataFoundException("PAYMENT.4: "+ sqlText4 + "; " + w_id + ", " + d_id + " (NO_DATA_FOUND)");
			}
			rs4.close();
			stmt4.close();
			
			int c_id_6, c_credit_lim;
			double c_discount, c_balance = 0.0;
			String c_first, c_middle, c_street_1, c_street_2, c_city, c_state, c_zip, c_phone, c_credit = null;
			Timestamp c_since;
			if (byname) {
				// proceed = 5;
				int namecnt = 0;
				final String sqlText5 = TpccStatements.paymentStmt5();
				final PreparedStatement stmt5 = connection.prepareStatement(sqlText5);
				stmt5.setString(1, c_last);
				stmt5.setInt(2, c_d_id);
				stmt5.setInt(3, c_w_id);
				final ResultSet rs5 = stmt5.executeQuery();
				if (rs5.next()) {
					namecnt = rs5.getInt(1);
					if(logger.isTraceEnabled()) {
						logger.trace("PAYMENT.5: " + sqlText5 + "; " + c_last + "," + c_d_id + ", " + c_w_id 
							+ "( namecnt= " + namecnt + " )");
					}
				} else {
					rs5.close();
					stmt5.close();
					throw new NoDataFoundException("PAYMENT.5: " + sqlText5 + "; " + c_last + "," + c_d_id + ", " + c_w_id + " (NO_DATA_FOUND)");
				}
				rs5.close();
				stmt5.close();
				
				// proceed = 6;
				if (namecnt % 2 == 1)
					namecnt++;
				int fetchcnt = namecnt / 2 - 1;
				int n = 1;
				final String sqlText6 = TpccStatements.paymentStmt6();
				final PreparedStatement stmt6 = connection.prepareStatement(sqlText6);
				stmt6.setInt(1, c_w_id);
				stmt6.setInt(2, c_d_id);
				stmt6.setString(3, c_last);
				final ResultSet rs6 = stmt6.executeQuery();
				while (rs6.next()) {
					c_first = rs6.getString(CustomerDbo.CN_C_FIRST);
					c_middle = rs6.getString(CustomerDbo.CN_C_MIDDLE);
					c_id_6 = rs6.getInt(CustomerDbo.CN_C_ID);
					c_street_1 = rs6.getString(CustomerDbo.CN_C_STREET_1);
					c_street_2 = rs6.getString(CustomerDbo.CN_C_STREET_2);
					c_city = rs6.getString(CustomerDbo.CN_C_CITY);
					c_state = rs6.getString(CustomerDbo.CN_C_STATE);
					c_zip = rs6.getString(CustomerDbo.CN_C_ZIP);
					c_phone = rs6.getString(CustomerDbo.CN_C_PHONE);
					c_credit = rs6.getString(CustomerDbo.CN_C_CREDIT);
					c_credit_lim = rs6.getInt(CustomerDbo.CN_C_CREDIT_LIM);
					c_discount = rs6.getDouble(CustomerDbo.CN_C_DISCOUNT);
					c_balance = rs6.getDouble(CustomerDbo.CN_C_BALANCE);
					c_since = rs6.getTimestamp(CustomerDbo.CN_C_SINCE);
					if(logger.isTraceEnabled()) {
						logger.trace("PAYMENT.6: " + sqlText6 + "; " + c_d_id + ", " + c_w_id + "," + c_last 
							+ "( c_first= " + c_first + ", c_middle= " + c_middle + ", c_id= " + c_id_6 + ", c_street_1= " + c_street_1 + ", c_street_2= " + c_street_2 + ", c_city= " + c_city + ", c_state= " + c_state + ", c_zip= " + c_zip + ", c_phone= " + c_phone + ", c_credit= " + c_credit + ", c_credit_lim= " + c_credit_lim + ", c_discount= " + c_discount + ", c_balance= " + c_balance + ", c_since= " + c_since.toString() + " )");
					}
					if (n > fetchcnt)
						break;
					n++;
				}
				rs6.close();
				stmt6.close();
			} else {
				// proceed = 7;
				final String sqlText7 = TpccStatements.paymentStmt7();
				final PreparedStatement stmt7 = connection.prepareStatement(sqlText7);
				stmt7.setInt(1, c_w_id);
				stmt7.setInt(2, c_d_id);
				stmt7.setInt(3, c_id);
				final ResultSet rs7 = stmt7.executeQuery();
				while(rs7.next()) {
					c_first = rs7.getString(CustomerDbo.CN_C_FIRST);
					c_middle = rs7.getString(CustomerDbo.CN_C_MIDDLE);
					c_street_1 = rs7.getString(CustomerDbo.CN_C_STREET_1);
					c_street_2 = rs7.getString(CustomerDbo.CN_C_STREET_2);
					c_city = rs7.getString(CustomerDbo.CN_C_CITY);
					c_state = rs7.getString(CustomerDbo.CN_C_STATE);
					c_zip = rs7.getString(CustomerDbo.CN_C_ZIP);
					c_phone = rs7.getString(CustomerDbo.CN_C_PHONE);
					c_credit = rs7.getString(CustomerDbo.CN_C_CREDIT);
					c_credit_lim = rs7.getInt(CustomerDbo.CN_C_CREDIT_LIM);
					c_discount = rs7.getDouble(CustomerDbo.CN_C_DISCOUNT);
					c_balance = rs7.getDouble(CustomerDbo.CN_C_BALANCE);
					c_since = rs7.getTimestamp(CustomerDbo.CN_C_SINCE);
					if(logger.isTraceEnabled()) {
						logger.trace("PAYMENT.7: " + sqlText7 + "; " + c_d_id + ", " + c_w_id + "," + c_last 
							+ "( c_first= " + c_first + ", c_middle= " + c_middle + ", c_street_1= " + c_street_1  + ", c_street_2= " + c_street_2 + ", c_city= " + c_city + ", c_state= " + c_state + ", c_zip= " + c_zip + ", c_phone= " + c_phone + ", c_credit= " + c_credit + ", c_credit_lim= " + c_credit_lim + ", c_discount= " + c_discount + ", c_balance= " + c_balance + ", c_since= " + c_since.toString() + " )");
					}
				}
				rs7.close();
				stmt7.close();
			}
			
			c_balance += h_amount;
			if (c_credit != null) {
				if (c_credit.contains("BC")) {
					// proceed = 8;
					final String sqlText8 = TpccStatements.paymentStmt8();
					final PreparedStatement stmt8 = connection.prepareStatement(sqlText8);
					stmt8.setInt(1, c_w_id);
					stmt8.setInt(2, c_d_id);
					stmt8.setInt(3, c_id);
					String c_data = "";
					final ResultSet rs8 = stmt8.executeQuery();
					if (rs8.next()) {
						c_data = rs8.getString(CustomerDbo.CN_C_DATA);
						if(logger.isTraceEnabled()) {
							logger.trace("PAYMENT.8: " + sqlText8 + "; " + c_w_id + ", " + c_d_id + ", " + c_id + "( " + c_data + " )");
						}
					} else {
						rs8.close();
						stmt8.close();
						throw new NoDataFoundException("PAYMENT.8: " + sqlText8 + "; " + c_w_id + ", " + c_d_id + ", " + c_id + " (NO_DATA_FOUND)");
					}
					rs8.close();
					stmt8.close();
					
					String c_new_data = String.format("| %d %d %d %d %d $%d %s %s", c_id, c_d_id, c_w_id, d_id, w_id, h_amount, new Date().toString(), c_data);
					c_new_data = substring(c_new_data, 500);
					
					//proceed = 9;
					final String sqlText9 = TpccStatements.paymentStmt9();
					final PreparedStatement stmt9 = connection.prepareStatement(sqlText9);
					stmt9.setDouble(1, c_balance);
					stmt9.setString(2, c_new_data);
					stmt9.setInt(3, c_w_id);
					stmt9.setInt(4, c_d_id);
					stmt9.setInt(5, c_id);
					updates = stmt9.executeUpdate();
					if(logger.isTraceEnabled()) {
						logger.trace("PAYMENT.9: " + sqlText9 + "; " + c_balance + ", " + c_new_data + ", " + c_w_id + ", " + c_d_id + ", " + c_id + " ( " + updates + " )");
					}
					stmt9.close();
				} else {
					// proceed = 10;
					final String sqlText10 = TpccStatements.paymentStmt10();
					final PreparedStatement stmt10 = connection.prepareStatement(sqlText10);
					stmt10.setDouble(1, c_balance);
					stmt10.setInt(2, c_w_id);
					stmt10.setInt(3, c_d_id);
					stmt10.setInt(4, c_id);
					updates = stmt10.executeUpdate();
					if(logger.isTraceEnabled()) {
						logger.trace("PAYMENT.10: " + sqlText10 + "; " + c_balance  + ", " + c_w_id + ", " + c_d_id + ", " + c_id + " ( " + updates + " )");
					}
					stmt10.close();
				}
				
				String h_data = substring(w_name, 10);
				h_data = h_data + ' ' + substring(d_name, 10) + ' ' + ' ' + ' ' + ' ';
				Timestamp h_date = TpccHelper.getTimestamp();
				final String sqlText11 = TpccStatements.paymentStmt11();
				final PreparedStatement stmt11 = connection.prepareStatement(sqlText11);
				stmt11.setInt(1, c_d_id);
				stmt11.setInt(2, c_w_id);
				stmt11.setInt(3, c_id);
				stmt11.setInt(4, d_id);
				stmt11.setInt(5, w_id);
				stmt11.setTimestamp(6, h_date);
				stmt11.setDouble(7, h_amount);
				stmt11.setString(8, h_data);
				updates = stmt11.executeUpdate();
				if(logger.isTraceEnabled()) {
					logger.trace("PAYMENT.11: " + sqlText11 + "; " + c_d_id  + ", " + c_w_id + ", " + c_id + ", " + d_id + ", " + w_id+ ", " + h_date + ", " + h_amount + ", " + h_data 
						+" ( " + updates + " )");
				}
				stmt11.close();
			}
			connection.commit(); 
		} catch (Throwable e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
			connection.close();
		}
		return true;
	}

	/**
	 * 
	 * 2.6	The Order-Status Transaction
	 *   --> 2.6.2	Transaction Profile
	 * 
	 * @param w_id  warehouse id
	 * @param d_id district id
	 * @param byname select by c_id or c_last?
	 * @param c_id customer id
	 * @param c_last customer last name, format?
	 * @throws Throwable 
	 */
	public static boolean ordstat(final int w_id, final int d_id, final boolean byname, final int c_id, final String c_last) throws Throwable {
		final int c_d_id = d_id;
		final int c_w_id = w_id;
		
		final Connection connection = TpccContext.getContext().getConnection();
		try {
			// Start transaction.
			connection.setAutoCommit(false);
			
			if (byname) {
				// proceed = 1
				final String sqlText1 = TpccStatements.orderStatusStmt1();
				final PreparedStatement stmt1 = connection.prepareStatement(sqlText1);
				stmt1.setString(1, c_last);
				stmt1.setInt(2, d_id);
				stmt1.setInt(3, w_id);
				final ResultSet rs1 = stmt1.executeQuery();
				int namecnt = 0;
				if (rs1.next()) {
					namecnt = rs1.getInt(1);
					if(logger.isTraceEnabled()) {
						logger.trace("ORDER-STATUS.1: " + sqlText1 + "; " + c_last + ", " + d_id + "," + w_id + " ( namecnt= " + namecnt + " )");
					}
				} else {
					rs1.close();
					stmt1.close();
					throw new NoDataFoundException("ORDER-STATUS.1: " + sqlText1 + "; " + c_last + ", " + d_id + "," + w_id + " (NO_DATA_FOUND)");
				}
				rs1.close();
				stmt1.close();
				
				// proceed = 2
				if (namecnt % 2 == 1)
					namecnt++;
				int fetchcnt = namecnt / 2 - 1;
				int n = 1;
				final String sqlText2 = TpccStatements.orderStatusStmt2();
				final PreparedStatement stmt2 = connection.prepareStatement(sqlText2);
				stmt2.setString(1, c_last);
				stmt2.setInt(2, d_id);
				stmt2.setInt(3, w_id);
				final ResultSet rs2 = stmt2.executeQuery();
				double c_balance;
				String c_first;
				String c_middle;
				while (rs2.next()) {
					c_balance = rs2.getDouble(CustomerDbo.CN_C_BALANCE);
					c_first = rs2.getString(CustomerDbo.CN_C_FIRST);
					c_middle = rs2.getString(CustomerDbo.CN_C_MIDDLE);
					int _c_id = rs2.getInt(CustomerDbo.CN_C_ID);
					if(logger.isTraceEnabled()) {
						logger.trace("ORDER-STATUS.2: " + sqlText2 + "; " + c_last + ", " + d_id + "," + w_id
							+ " ( c_balance= " + c_balance + " c_first= " + c_first + " c_middle= " + c_middle + " c_id= " + _c_id + " )");
					}
					if (n > fetchcnt)
						break;
					n++;
				}
				rs2.close();
				stmt2.close();
			} else {
				// proceed = 3
				final String sqlText3 = TpccStatements.orderStatusStmt3();
				final PreparedStatement stmt3 = connection.prepareStatement(sqlText3);
				stmt3.setInt(1, c_id);
				stmt3.setInt(2, d_id);
				stmt3.setInt(3, w_id);
				final ResultSet rs3 = stmt3.executeQuery();
				if (rs3.next()) {
					double _c_balance = rs3.getDouble(CustomerDbo.CN_C_BALANCE);
					String _c_first = rs3.getString(CustomerDbo.CN_C_FIRST); 
					String _c_middle = rs3.getString(CustomerDbo.CN_C_MIDDLE); 
					String _c_last =rs3.getString(CustomerDbo.CN_C_LAST) ;
					if(logger.isTraceEnabled()) {
						logger.trace("ORDER-STATUS.3: " + sqlText3 + "; " + c_last + ", " + d_id + "," + w_id 
							+ " ( c_balance= " + _c_balance + " c_first= " + _c_first + " c_middle= " + _c_middle + " c_last= " + _c_last + " )");
					}
				} else {
					rs3.close();
					stmt3.close();
					throw new NoDataFoundException("ORDER-STATUS.3: " + sqlText3 + "; " + c_last + ", " + d_id + "," + w_id + " (NO_DATA_FOUND)");
				}
				rs3.close();
				stmt3.close();
			}
			
			/* proceed = 4 -> Find the most recent order for this customer.
			 * 
			 * The row in the ORDER table with matching O_W_ID (equals C_W_ID), O_D_ID (equals C_D_ID), O_C_ID (equals C_ID), 
			 * and with the largest existing O_ID, is selected. This is the most recent order placed by that customer. 
			 * O_ID, O_ENTRY_D, and O_CARRIER_ID are retrieved.
			 */
			final String sqlText4 = TpccStatements.orderStatusStmt4();
			final PreparedStatement stmt4 = connection.prepareStatement(sqlText4);
			stmt4.setInt(1, c_w_id);
			stmt4.setInt(2, c_d_id);
			stmt4.setInt(3, c_id);
			stmt4.setInt(4, c_w_id);
			stmt4.setInt(5, c_d_id);
			stmt4.setInt(6, c_id);
			final ResultSet rs4 = stmt4.executeQuery();
			
			int o_id = 0;
			Timestamp o_entry_d = null;
			int o_carrier_id;
			if (rs4.next()) {
				o_id = rs4.getInt(OrdersDbo.CN_O_ID);
				o_entry_d = rs4.getTimestamp(OrdersDbo.CN_O_ENTRY_D);
				o_carrier_id = rs4.getInt(OrdersDbo.CN_O_CARRIER_ID);
				if(logger.isTraceEnabled()) {
					logger.trace("ORDER-STATUS.4: " + sqlText4 + "; " + c_w_id + ", " + c_d_id + "," + c_id + "," + c_w_id + "," + c_d_id + "," + c_id
						+ " ( o_id= " + o_id + " o_entry_d= " + o_entry_d + " o_carrier_id= " + o_carrier_id+ " )");
				}
			} else {
				rs4.close();
				stmt4.close();
				throw new NoDataFoundException("ORDER-STATUS.4: " + sqlText4 + "; " + c_w_id + ", " + c_d_id + "," + c_id + "," + c_w_id + "," + c_d_id + "," + c_id + " (NO_DATA_FOUND)");
			}
			rs4.close();
			stmt4.close();
			
			/* proceed = 5
			 * 
			 * All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID), OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID) 
			 * are selected and the corresponding sets of OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, and OL_DELIVERY_D are retrieved.
			 */
			final String sqlText5 = TpccStatements.orderStatusStmt5();
			final PreparedStatement stmt5 = connection.prepareStatement(sqlText5);
			stmt5.setInt(1, o_id);
			stmt5.setInt(2, d_id);
			stmt5.setInt(3, w_id);
			final ResultSet rs5 = stmt5.executeQuery();
			 while (rs5.next()) {
				int ol_i_id = rs5.getInt(OrderLineDbo.CN_OL_I_ID);
				int ol_supply_w_id = rs5.getInt(OrderLineDbo.CN_OL_SUPPLY_W_ID);
				int ol_quantity = rs5.getInt(OrderLineDbo.CN_OL_QUANTITY);
				double ol_amount  = rs5.getDouble(OrderLineDbo.CN_OL_AMOUNT);
				Timestamp ol_delivery_d = rs5.getTimestamp(OrderLineDbo.CN_OL_DELIVERY_D);
				if(logger.isTraceEnabled()) {
					logger.trace("ORDER-STATUS.5: " + sqlText5 + "; " + o_id + ", " + d_id + "," + w_id
						+ "( ol_i_id= " + ol_i_id + " ol_supply_w_id= " + ol_supply_w_id + " ol_quantity= " + ol_quantity +  " ol_amount= " + ol_amount +  " ol_delivery_d= " + ol_delivery_d + " )");
				}
			}
			rs5.close();
			stmt5.close();
			connection.commit(); // end transaction.
		} catch (Throwable e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
			connection.close();
		}
		return true;
	}
	
	/**
	 * 
	 * 2.7.4	Transaction Profile
	 *   -> 
	 * @param w_id
	 * @param o_carrier_id
	 * @throws Throwable 
	 */
	public static boolean delivery(final int w_id, final int o_carrier_id) throws Throwable {
		final Connection connection = TpccContext.getContext().getConnection();
		try {
			connection.setAutoCommit(false);
			for (int d_id = 1; d_id <= DIST_PER_WARE; d_id++) {
				
				/* proceed = 1
				 * 
				 * The row in the NEW-ORDER table with matching NO_W_ID (equals W_ID) and NO_D_ID (equals D_ID) 
				 * and with the lowest NO_O_ID value is selected. This is the oldest undelivered order of that 
				 * district. NO_O_ID, the order number, is retrieved. 
				 */
				final String sqlText1 = TpccStatements.deliveryStmt1();
				final PreparedStatement stmt1 = connection.prepareStatement(sqlText1);
				stmt1.setInt(1, d_id);
				stmt1.setInt(2, w_id);
				final ResultSet rs1 = stmt1.executeQuery();
				int no_o_id;
				if (rs1.next()) {
					no_o_id = rs1.getInt(NewOrdersDbo.CN_NO_O_ID);
					if(logger.isTraceEnabled()) {
						logger.trace("DELIVERY.1: " + sqlText1 + "; " + d_id + ", " + w_id + " ( no_o_id= " + no_o_id + " )");
					}
				} else {
					rs1.close();
					stmt1.close();
					throw new NoDataFoundException("DELIVERY.1: " + sqlText1 + "; " + d_id + ", " + w_id + " (NO_DATA_FOUND)");
				}
				rs1.close();
				stmt1.close();
				
				/*
				 * proceed = 2
				 * 
				 * The selected row in the NEW-ORDER table is deleted.
				 */
				final String sqlText2 = TpccStatements.deliveryStmt2();
				final PreparedStatement stmt2 = connection.prepareStatement(sqlText2);
				stmt2.setInt(1, d_id);
				stmt2.setInt(2, w_id);
				stmt2.setInt(3, no_o_id);
				int updates = stmt2.executeUpdate();
				if(logger.isTraceEnabled()) {
					logger.trace("DELIVERY.2: " + sqlText2 + "; " + d_id + ", " + w_id + ", " + no_o_id +" ( " + updates + " )");
				}
				stmt2.close();
				
				/*
				 * proceed = 3, 4
				 * The row in the ORDER table with matching O_W_ID (equals W_ ID), O_D_ID (equals D_ID), and O_ID (equals NO_O_ID) is selected, 
				 * O_C_ID, the customer number, is retrieved, and O_CARRIER_ID is updated.
				 * 
				 */
				final String sqlText3 = TpccStatements.deliveryStmt3();
				final PreparedStatement stmt3 = connection.prepareStatement(sqlText3);
				stmt3.setInt(1, no_o_id);
				stmt3.setInt(2, d_id );
				stmt3.setInt(3, w_id );
				final ResultSet rs3 = stmt3.executeQuery();
				int c_id = 0;
				if (rs3.next()) {
					c_id = rs3.getInt(OrdersDbo.CN_O_C_ID);
					if(logger.isTraceEnabled()) {
						logger.trace("DELIVERY.3: " + sqlText3 + "; " + no_o_id + ", " + d_id + ", " + w_id + " ( o_c_id= " + c_id + " )");
					}
				} else {
					rs3.close();
					stmt3.close();
					throw new NoDataFoundException("DELIVERY.3: " + sqlText3 + "; " + no_o_id + ", " + d_id + ", " + w_id + " (NO_DATA_FOUND)");
				}
				rs3.close();
				stmt3.close();
				
				// proceed = 4
				final String sqlText4 = TpccStatements.deliveryStmt4();
				final PreparedStatement stmt4 = connection.prepareStatement(sqlText4);
				stmt4.setInt(1, o_carrier_id); //o_carrier_id
				stmt4.setInt(2, no_o_id);
				stmt4.setInt(3, d_id);
				stmt4.setInt(4, w_id);
				updates = stmt4.executeUpdate();
				if(logger.isTraceEnabled()) {
					logger.trace("DELIVERY.4: " + sqlText4 + "; " + o_carrier_id + ", " + no_o_id + ", " + d_id + ", "+ w_id 
						+ " ( " + updates + " )");
				}
				stmt4.close();
				
				/*
				 * proceed = 5, 6
				 * All rows in the ORDER-LINE table with matching OL_W_ID (equals O_W_ID), OL_D_ID (equals O_D_ID), and OL_O_ID (equals O_ID) are selected. 
				 * All OL_DELIVERY_D, the delivery dates, are updated to the current system time as returned by the operating system and the sum of all 
				 * OL_AMOUNT  is retrieved.
				 */
				final String sqlText5 = TpccStatements.deliveryStmt5();
				final PreparedStatement stmt5 = connection.prepareStatement(sqlText5);
				Timestamp ol_delivery_d  = TpccHelper.getTimestamp();
				stmt5.setTimestamp(1, ol_delivery_d );
				stmt5.setInt(2, no_o_id);
				stmt5.setInt(3, d_id);
				stmt5.setInt(4, w_id);
				updates = stmt5.executeUpdate();
				if(logger.isTraceEnabled()) {
					logger.trace("DELIVERY.5: " + sqlText5 + "; " + ol_delivery_d + ", " + no_o_id + ", " + d_id + ", " + w_id 
						+ " ( " + updates + " )");
				}
				stmt5.close();
				
				// proceed = 6;
				final String sqlText6 = TpccStatements.deliveryStmt6();
				final PreparedStatement stmt6 = connection.prepareStatement(sqlText6);
				stmt6.setInt(1, no_o_id);
				stmt6.setInt(2, d_id);
				stmt6.setInt(3, w_id);
				final ResultSet rs6 = stmt6.executeQuery();
				int ol_total = 0;
				if (rs6.next()) {
					ol_total = rs6.getInt(1);
					if(logger.isTraceEnabled()) {
						logger.trace("DELIVERY.6: " + sqlText6 + "; " + no_o_id + ", " + d_id + ", " + w_id
							+ " ( ol_total = " + ol_total + " )");
					}
				} else {
					rs6.close();
					stmt6.close();
					throw new NoDataFoundException("DELIVERY.6: " + sqlText6 + "; " + no_o_id + ", " + d_id + ", " + w_id + " (NO_DATA_FOUND)");
				}
				rs6.close();
				stmt6.close();
				
				/*
				 * procced = 7
				 * The row in the CUSTOMER table with matching C_W_ID (equals W_ID), C_D_ID (equals D_ID), and C_ID (equals O_C_ID) is selected 
				 * and C_BALANCE is increased by the sum of all order-line amounts (OL_AMOUNT) previously retrieved. C_DELIVERY_CNT is incremented 
				 * by 1.
				 */
				final String sqlText7 = TpccStatements.deliveryStmt7();
				final PreparedStatement stmt7 = connection.prepareStatement(sqlText7);
				stmt7.setInt(1, ol_total);
				stmt7.setInt(2, c_id);
				stmt7.setInt(3, d_id);
				stmt7.setInt(4, w_id);
				updates = stmt7.executeUpdate();
				if(logger.isTraceEnabled()) {
					logger.trace("DELIVERY.7: " + sqlText7 + "; " + ol_total + ", " + c_id + ", " + d_id + ", " + w_id
						+ " ( " + updates + " )");
				}
				stmt7.close();
			}
			connection.commit(); // end transaction.
		} catch (Throwable e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
			connection.close();
		}
		return true;
	}
	

	/**
	 * 2.8	The Stock-Level Transaction
	 *   -> 2.8.2	Transaction Profile
	 * A.5	The Stock-Level Transaction
	 *   
	 * @param w_id  warehouse id
	 * @param d_id  district id
	 * @param level stock level
	 * @return
	 * @throws Throwable 
	 */
	
	public static boolean slev(final int w_id, final int d_id, final int level) throws Throwable {
		final Connection connection = TpccContext.getContext().getConnection();
		try {
			connection.setAutoCommit(false);
			
			/*
			 * proceed = 1
			 * 
			 * The row in the DISTRICT table with matching D_W_ID and D_ID is selected and D_NEXT_O_ID is retrieved.
			 */
			final String sqlText1 = TpccStatements.stockLevelStmt1();
			final PreparedStatement stmt1 = connection.prepareStatement(sqlText1);
			stmt1.setInt(1, w_id);
			stmt1.setInt(2, d_id);
			final ResultSet rs1 = stmt1.executeQuery();
			int d_next_o_id;
			if (rs1.next()) {
				d_next_o_id = rs1.getInt(DistrictDbo.CN_D_NEXT_O_ID);
				if(logger.isTraceEnabled()) {
					logger.trace("STOCK-LEVEL.1: " + sqlText1 + "; " + w_id + ", " + d_id 
						+ " ( d_next_o_id = " + d_next_o_id + " )");
				}
			} else {
				rs1.close();
				stmt1.close();
				throw new NoDataFoundException( "STOCK-LEVEL.1: " + sqlText1 + "; " + w_id + ", " + d_id + " (NO_DATA_FOUND)");
			}
			rs1.close();
			stmt1.close();
			
			/*
			 * All rows in the ORDER-LINE table with matching OL_W_ID (equals W_ID), OL_D_ID (equals D_ID), 
			 * and OL_O_ID (lower than D_NEXT_O_ID and greater than or equal to D_NEXT_O_ID minus 20) are 
			 * selected. They are the items for 20 recent orders of the district. 
			 * 
			 * All rows in the STOCK table with matching S_I_ID (equals OL_I_ID) and S_W_ID (equals W_ID) 
			 * from the list of distinct item numbers and with S_QUANTITY lower than threshold are counted 
			 * (giving low_stock).
			 * 
			 * Comment: Stocks must be counted only for distinct items. Thus, items that have been ordered 
			 * more than once in the 20 selected orders must be aggregated into a single summary count for 
			 * that item.
			 * 
			 */
			
			final String sqlText2 = TpccStatements.stockLevelStmt2();
			final PreparedStatement stmt2 = connection.prepareStatement(sqlText2);
			stmt2.setInt(1, w_id);
			stmt2.setInt(2, d_id);
			stmt2.setInt(3, d_next_o_id);
			stmt2.setInt(4, d_next_o_id);
			stmt2.setInt(5, w_id);
			stmt2.setInt(6, level);
			final ResultSet rs2 = stmt2.executeQuery();
			int stock_count = 0;
			while (rs2.next()) {
				stock_count = rs2.getInt(1);
				if(logger.isTraceEnabled()) {
					logger.trace("STOCK-LEVEL.2: " + sqlText2 + "; " + w_id + ", " + d_id + ", " + d_next_o_id + ", " + d_next_o_id + ", " + w_id + ", " + level 
						+ " ( stock_count = " + stock_count + " )");
				}
			}
			rs2.close();
			stmt2.close();
			connection.commit(); // end transaction.
		} catch (Throwable e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
			connection.close();
		}
		return true;
	}
	

	
	public static String substring(String src, int maxLen) {
		if (src == null) return src;
		if (maxLen < 1)  return "";
		int len = src.length();
		if (len < 1) return src;

		int endIndex = (len > maxLen) ? maxLen : len;
		return src.substring(0, endIndex);
	}
	
	public static int otherWare(int homeWare, int numWare) {
		if (numWare == 1) 
			return homeWare; // 只有1个仓库;
		
		while (true) {
			int tmp = RandomHelper.randomInt(1, numWare);
			if (tmp != homeWare)
				return tmp;
		}
	}
}
