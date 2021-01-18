package org.littlestar.tpcc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.littlestar.helper.DataSourceHelper;

public interface TpccHelper {
	static final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";//"yyyy-MM-dd HH:mm:ss.SSSS";

	public static Timestamp getTimestamp() {
		return Timestamp.valueOf(LocalDateTime.now());
	}

	public static String getTimestampString(LocalDateTime dt) {
		return dt.format(DateTimeFormatter.ofPattern(dateTimePattern));
	}

	public static String getTimestampString() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateTimePattern));
	}

	public static void output(StringBuilder msg, boolean withTimestamp) {
		if (withTimestamp) {
			String timestamp = getTimestampString() + " ";
			msg.insert(0, timestamp);
		}
		System.out.println(msg.toString());
	}

	public static void output(String msg, boolean withTimestamp) {
		if (withTimestamp) {
			String timestamp = getTimestampString() + " ";
			msg = timestamp + msg;
		}
		System.out.println(msg);
	}
	
	public static void output(StringBuilder msg) {
		output(msg, true);
	}
	
	public static void output(String msg) {
		output(msg, true);
	}
	
	public static void reportHeader() {
		output("                    |    Total     |        New-Order         |        Payment           |       Order-Status       |         Delivery         |       Stock-Level        |", false);
		output("                    |    TXs/  Tps/|   TXs/  Tps/AvgRt/ MaxRt/|   TXs/  Tps/ AvgRt/MaxRt/|   TXs/  Tps/ AvgRt/MaxRt/|   TXs/  Tps/ AvgRt/MaxRt/|   TXs/  Tps/ AvgRt/MaxRt/|", false);
		output("--------------------+--------------+--------------------------+--------------------------+--------------------------+--------------------------+--------------------------+", false);
	}
	
	public static void reportRow(long totalTxs, long totalTps
			,long noTxs, long noTps, long noAvgRt, long noMaxRt
			,long pyTxs, long pyTps, long pyAvgRt, long pyMaxRt
			,long osTxs, long osTps, long osAvgRt, long osMaxRt
			,long dlTxs, long dlTps, long dlAvgRt, long dlMaxRt
			,long slTxs, long slTps, long slAvgRt, long slMaxRt
			) {
				String totalColumn = String.format("| %6s %5s ", totalTxs, totalTps);
				String newOrderColumn = String.format("| %6s %5s %5s %5s ", noTxs, noTps, noAvgRt, noMaxRt);
				String playmentColumn = String.format("| %6s %5s %5s %5s ", pyTxs, pyTps, pyAvgRt, pyMaxRt);
				String orderStatusColumn = String.format("| %6s %5s %5s %5s ", osTxs, osTps, osAvgRt, osMaxRt);
				String deliveryColumn = String.format("| %6s %5s %5s %5s ", dlTxs, dlTps, dlAvgRt, dlMaxRt);
				String stockLevelColumn = String.format("| %6s %5s %5s %5s |", slTxs, slTps, slAvgRt, slMaxRt);
				output(totalColumn + newOrderColumn + playmentColumn + orderStatusColumn + deliveryColumn +stockLevelColumn);
	}
	
	public static void reportFooter(List<TpccThread> tpccThreads, long measureTime) {
		
		long noTxs  = 0l, pyTxs  = 0l, osTxs  = 0l, dlTxs  = 0l, slTxs  = 0l;
		long noRt   = 0l, pyRt   = 0l, osRt   = 0l, dlRt   = 0l, slRt   = 0l;
		long noMxRt = 0l, pyMxRt = 0l, osMxRt = 0l, dlMxRt = 0l, slMxRt = 0l;
		long noFl   = 0l, pyFl   = 0l, osFl   = 0l, dlFl   = 0l, slFl   = 0l;
		long noRy   = 0l, pyRy   = 0l, osRy   = 0l, dlRy   = 0l, slRy   = 0l;
		
		for (TpccThread tpccThread : tpccThreads) {
			long[] noStatistics = tpccThread.getNewOrderTransactionStatistics();
			long[] pyStatistics = tpccThread.getPlaymentTransactionStatistics();
			long[] osStatistics = tpccThread.getOrderStatusTransactionStatistics();
			long[] dlStatistics = tpccThread.getDeliveryTransactionStatistics();
			long[] slStatistics = tpccThread.getStockLevelTranscationStatistics();
			/*
			 * statistics[0] = xxTotalRuntime;
			 * statistics[1] = xxMaxRuntime;
			 * statistics[2] = xxSucceedCount;
			 * statistics[3] = xxFailureCount;
			 * statistics[3] = xxRetryCount;
			 */
			noRt += noStatistics[0];
			pyRt += pyStatistics[0];
			osRt += osStatistics[0];
			dlRt += dlStatistics[0];
			slRt += slStatistics[0];
			
			noMxRt = Math.max(noMxRt, noStatistics[1]);
			pyMxRt = Math.max(pyMxRt, pyStatistics[1]);
			osMxRt = Math.max(osMxRt, osStatistics[1]);
			dlMxRt = Math.max(dlMxRt, dlStatistics[1]);
			slMxRt = Math.max(slMxRt, slStatistics[1]);
			
			noTxs += noStatistics[2];
			pyTxs += pyStatistics[2];
			osTxs += osStatistics[2];
			dlTxs += dlStatistics[2];
			slTxs += slStatistics[2];
			
			noFl += noStatistics[3];
			pyFl += pyStatistics[3];
			osFl += osStatistics[3];
			dlFl += dlStatistics[3];
			slFl += slStatistics[3];
			
			noRy += noStatistics[4];
			pyRy += pyStatistics[4];
			osRy += osStatistics[4];
			dlRy += dlStatistics[4];
			slRy += slStatistics[4];
		}
		
		double totalTxs = noTxs + pyTxs + osTxs + dlTxs + slTxs;
		//double totalRt  = noRt  + pyRt  + osRt  + dlRt  + slRt;
		double totalTps = (measureTime > 0) ? ((double) totalTxs / measureTime) : 0.0D;
		double totalTpmc = totalTps * 60.0D;
		//// New-Order
		double noTps = (measureTime > 0) ? ((double) noTxs / measureTime) : 0.0D;
		double noTpmc = noTps * 60.0D;
		double noAvgRt = (noTxs > 0) ? ((double) noRt / noTxs) : 0.0D;
		double noTxPct = (totalTxs > 0) ? ((double) noTxs / totalTxs) * 100.0D : 0.0D;
		
		//// Payment
		double pyTps = (measureTime > 0) ? ((double) pyTxs / measureTime) : 0.0D;
		double pyTpmc = pyTps * 60.0D;
		double pyAvgRt = (pyTxs > 0) ? ((double) pyRt / pyTxs) : 0.0D;
		double pyTxPct = (totalTxs > 0) ? ((double) pyTxs / totalTxs) * 100.0D : 0.0D;
		
		//// Order-Status
		double osTps = (measureTime > 0) ? ((double) osTxs / measureTime) : 0.0D;
		double osTpmc = osTps * 60.0D;
		double osAvgRt = (osTxs > 0) ? ((double) osRt / osTxs) : 0.0D;
		double osTxPct = (totalTxs > 0) ? ((double) osTxs / totalTxs) * 100.0D : 0.0D;
		
		//// Delivery
		double dlTps = (measureTime > 0) ? ((double) dlTxs / measureTime) : 0.0D;
		double dlTpmc = dlTps * 60.0D;
		double dlAvgRt = (dlTxs > 0) ? ((double) dlRt / dlTxs) : 0.0D;
		double dlTxPct = (totalTxs > 0) ? ((double) dlTxs / totalTxs) * 100.0D : 0.0D;
		
		//// Stock-Level
		double slTps = (measureTime > 0) ? ((double) slTxs / measureTime) : 0.0D;
		double slTpmc = slTps * 60.0D;
		double slAvgRt = (slTxs > 0) ? ((double) slRt / slTxs) : 0.0D;
		double slTxPct = (totalTxs > 0) ? ((double) slTxs / totalTxs) * 100.0D : 0.0D;
		
		output("..... TPC-C benchmark completed ......"); 
		output(String.format("[Transaction Summary]: %.2f TpmC, %.2f Tps ", totalTpmc, totalTps));
		output("[Transaction Details]: ");
		output(String.format("     New-Order -> TX: %s (Failed: %s, Retries: %s), Tpmc: %.2f, Tps: %.2f, Avg-Rt: %.2f ms, Max-Rt: %s ms, Pct: %.2f %%"                , noTxs, noFl, noRy, noTpmc, noTps, noAvgRt, noMxRt, noTxPct));
		output(String.format("       Payment -> TX: %s (Failed: %s, Retries: %s), Tpmc: %.2f, Tps: %.2f, Avg-Rt: %.2f ms, Max-Rt: %s ms, Pct: %.2f %% (>43.0%% is OK)", pyTxs, pyFl, pyRy, pyTpmc, pyTps, pyAvgRt, pyMxRt, pyTxPct));
		output(String.format("  Order-Status -> TX: %s (Failed: %s, Retries: %s), Tpmc: %.2f, Tps: %.2f, Avg-Rt: %.2f ms, Max-Rt: %s ms, Pct: %.2f %% (> 4.0%% is OK)", osTxs, osFl, osRy, osTpmc, osTps, osAvgRt, osMxRt, osTxPct));
		output(String.format("      Delivery -> TX: %s (Failed: %s, Retries: %s), Tpmc: %.2f, Tps: %.2f, Avg-Rt: %.2f ms, Max-Rt: %s ms, Pct: %.2f %% (> 4.0%% is OK)", dlTxs, dlFl, dlRy, dlTpmc, dlTps, dlAvgRt, dlMxRt, dlTxPct));
		output(String.format("   Stock-Level -> TX: %s (Failed: %s, Retries: %s), Tpmc: %.2f, Tps: %.2f, Avg-Rt: %.2f ms, Max-Rt: %s ms, Pct: %.2f %% (> 4.0%% is OK)", slTxs, slFl, slRy, slTpmc, slTps, slAvgRt, slMxRt, slTxPct));
	}
	
	public static void check() throws Throwable {
		output("Checking Tables ...");
		output("  Warehouse (w)                                  : " + checkWarehouse() + " rows.");
		output("  Item (100000)                                  : " + checkItem() + " rows.");
		output("  Stock (w*100000)                               : " + checkStock() + " rows.");
		output("  District (w*10)                                : " + checkDistrict() + " rows.");
		output("  Customer (w*10*3000)                           : " + checkCustomer() + " rows.");
		output("  Order (number of customers (initial value))    : " + checkOrder() + " rows.");
		output("  New-Order (30% of the orders (initial value))  : " + checkNewOrder() + " rows.");
		output("  Order-Line (approx. 10 per order)              : " + checkOrderLine() + " rows.");
		output("  History (number of customers (initial value))  : " + checkHistory()+ " rows.");
		output("...");
	}
	
	public static int checkWarehouse() throws Throwable {
		int rows = -1;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.checkWarehouseStmt();
		try {
			final Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			if(rs.next()) {
				rows = rs.getInt(1);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	public static long checkItem() throws Throwable {
		long rows = -1;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.checkItemsStmt();
		try {
			final Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			if(rs.next()) {
				rows = rs.getLong(1);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	public static long checkStock() throws Throwable {
		long rows = -1;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.checkStockStmt();
		try {
			final Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			if(rs.next()) {
				rows = rs.getLong(1);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	public static long checkCustomer() throws Throwable {
		long rows = -1;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.checkCustomerStmt();
		try {
			final Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			if(rs.next()) {
				rows = rs.getLong(1);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	public static long checkDistrict() throws Throwable {
		long rows = -1;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.checkDistrictStmt();
		try {
			final Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			if(rs.next()) {
				rows = rs.getLong(1);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	public static long checkNewOrder() throws Throwable {
		long rows = -1;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.checkNewOrdersStmt();
		try {
			final Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			if(rs.next()) {
				rows = rs.getLong(1);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	public static long checkOrderLine() throws Throwable {
		long rows = -1;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.checkOrderLineStmt();
		try {
			final Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			if(rs.next()) {
				rows = rs.getLong(1);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	public static long checkOrder() throws Throwable {
		long rows = -1;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.checkOrdersStmt();
		try {
			final Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			if(rs.next()) {
				rows = rs.getLong(1);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
	
	public static long checkHistory() throws Throwable {
		long rows = -1;
		final Connection connection = TpccContext.getContext().getConnection();
		final String sqlText = TpccStatements.checkHistoryStmt();
		try {
			final Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sqlText);
			if(rs.next()) {
				rows = rs.getLong(1);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			DataSourceHelper.closeConnection(connection);
		}
		return rows;
	}
}
