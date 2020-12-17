package org.littlestar.tpcc;

import org.littlestar.helper.DataSourceHelper.DBMS;
import org.littlestar.tpcc.dbo.CustomerDbo;
import org.littlestar.tpcc.dbo.DistrictDbo;
import org.littlestar.tpcc.dbo.HistoryDbo;
import org.littlestar.tpcc.dbo.ItemDbo;
import org.littlestar.tpcc.dbo.NewOrdersDbo;
import org.littlestar.tpcc.dbo.OrderLineDbo;
import org.littlestar.tpcc.dbo.OrdersDbo;
import org.littlestar.tpcc.dbo.StockDbo;
import org.littlestar.tpcc.dbo.WarehouseDbo;

public class TpccStatements {
	public static String loadItemsStmt() throws Throwable {
		final String sqlText  = "insert into " + ItemDbo.TABLE_NAME  + " values (?,?,?,?,?)";
		return sqlText;
	}
	
	public static String loadWarehouseStmt() throws Throwable {
		final String sqlText = "insert into " + WarehouseDbo.TABLE_NAME + " values (?,?,?,?,?,?,?,?,?)";
		return sqlText;
	}
	
	public static String loadStockStmt() throws Throwable {
		final String sqlText  = "insert into " + StockDbo.TABLE_NAME  + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return sqlText;
	}
	
	public static String loadDistrictStmt() throws Throwable {
		final String sqlText  = "insert into " + DistrictDbo.TABLE_NAME  + " values (?,?,?,?,?,?,?,?,?,?,?)";
		return sqlText;
	}
	
	public static String loadCustomerStmt() throws Throwable {
		final String sqlText = "insert into " + CustomerDbo.TABLE_NAME + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return sqlText;

	}
	
	public static String loadHistoryStmt() throws Throwable {
		final String sqlText  = "insert into " + HistoryDbo.TABLE_NAME  + " values (?,?,?,?,?,?,?,?)";
		return sqlText;
	}
	
	public static String loadOrdersStmt() throws Throwable {
		final String sqlText  = "insert into " + OrdersDbo.TABLE_NAME  + " values (?,?,?,?,?,?,?,?)";
		return sqlText;
	}
	
	public static String loadNewOrdersStmt() throws Throwable {
		final String sqlText  = "insert into " + NewOrdersDbo.TABLE_NAME  + " values (?,?,?)";
		return sqlText;
	}
	
	public static String loadOrderLineStmt() throws Throwable {
		final String sqlText  = "insert into " + OrderLineDbo.TABLE_NAME  + " values (?,?,?,?,?,?,?,?,?,?)";
		return sqlText;
	}
	
	public static String checkItemsStmt() {
		final String sqlText = "select count(*) as cnt from " + ItemDbo.TABLE_NAME;
		return sqlText;
	}
	
	public static String checkWarehouseStmt() {
		final String sqlText = "select count(*) as cnt from " + WarehouseDbo.TABLE_NAME;
		return sqlText;
	}
	
	public static String checkStockStmt() {
		final String sqlText = "select count(*) as cnt from " + StockDbo.TABLE_NAME;
		return sqlText;
	}
	
	public static String checkDistrictStmt() {
		final String sqlText = "select count(*) as cnt from " + DistrictDbo.TABLE_NAME;
		return sqlText;
	}
	
	public static String checkCustomerStmt() {
		final String sqlText = "select count(*) as cnt from " + CustomerDbo.TABLE_NAME;
		return sqlText;
	}
	
	public static String checkHistoryStmt() {
		final String sqlText = "select count(*) as cnt from " + HistoryDbo.TABLE_NAME;
		return sqlText;
	}
	
	public static String checkOrdersStmt() {
		final String sqlText = "select count(*) as cnt from " + OrdersDbo.TABLE_NAME;
		return sqlText;
	}
	
	public static String checkNewOrdersStmt() {
		final String sqlText = "select count(*) as cnt from " + NewOrdersDbo.TABLE_NAME;
		return sqlText;
	}
	
	public static String checkOrderLineStmt() {
		final String sqlText = "select count(*) as cnt from " + OrderLineDbo.TABLE_NAME;
		return sqlText;
	}
	
	
	// A.1	The New-Order Transaction
	/**
	 * SELECT c_discount, c_last, c_credit, w_tax 
     *     INTO :c_discount, :c_last, :c_credit, :w_tax
     * FROM customer, warehouse
     * WHERE w_id = :w_id AND c_w_id = w_id AND
     *       c_d_id = :d_id AND c_id = :c_id;
     *               
	 * @return
	 */
	public static String newOrderStmt1() {
		final String sqlText = "select " + CustomerDbo.CN_C_DISCOUNT + ", " + CustomerDbo.CN_C_LAST + ","
				+ CustomerDbo.CN_C_CREDIT + ", " + WarehouseDbo.CN_W_TAX + " from " + CustomerDbo.TABLE_NAME + ", "
				+ WarehouseDbo.TABLE_NAME + " where " + WarehouseDbo.CN_W_ID + " = ? and " + CustomerDbo.CN_C_W_ID
				+ " = " + WarehouseDbo.CN_W_ID + " and " + CustomerDbo.CN_C_D_ID + " = ? and " + CustomerDbo.CN_C_ID + " = ?";

		return sqlText;
	}
	
	/**
	 * SELECT d_next_o_id, d_tax INTO :d_next_o_id, :d_tax
	 * FROM district
	 * WHERE d_id = :d_id AND d_w_id = :w_id
	 * FOR UPDATE;
	 * @return
	 */
	public static String newOrderStmt2() {
		final String sqlText = "select " + DistrictDbo.CN_D_NEXT_O_ID + ", " + DistrictDbo.CN_D_TAX + " from "
				+ DistrictDbo.TABLE_NAME + " where " + DistrictDbo.CN_D_ID + " = ? and " + DistrictDbo.CN_D_W_ID
				+ " = ? for update"; 
		DBMS dbms = TpccContext.getContext().getDBMS();
		if (dbms.equals(DBMS.MSSQL)) { //mssql not support select ... for update.
			final String sqlTextMssql = "select " + DistrictDbo.CN_D_NEXT_O_ID + ", " + DistrictDbo.CN_D_TAX + " from "
					+ DistrictDbo.TABLE_NAME + " with (UPDLOCK, INDEX(idx_district_1)) " 
					+ " where " + DistrictDbo.CN_D_ID + " = ? and " + DistrictDbo.CN_D_W_ID
					+ " = ? ";
			return sqlTextMssql;
		} else if(dbms.equals(DBMS.SQLite)) { //sqlite也不支持select ... for update语法, 也不支持加行锁定, 所以不支持多线程.
			final String sqlTextSqlite = "select " + DistrictDbo.CN_D_NEXT_O_ID + ", " + DistrictDbo.CN_D_TAX + " from "
					+ DistrictDbo.TABLE_NAME + " where " + DistrictDbo.CN_D_ID + " = ? and " + DistrictDbo.CN_D_W_ID
					+ " = ?";
			return sqlTextSqlite;
		} else {
			return sqlText;
		}
	}
	
	/**
	 * UPDATE district SET d_next_o_id = :d_next_o_id + 1
	 * WHERE d_id = :d_id AND d_w_id = :w_id;
	 * @return
	 */
	public static String newOrderStmt3() {
		final String sqlText = "update " + DistrictDbo.TABLE_NAME + " set " + DistrictDbo.CN_D_NEXT_O_ID
				+ " = ? + 1 " + " where " + DistrictDbo.CN_D_ID + " = ? and " + DistrictDbo.CN_D_W_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * INSERT INTO ORDERS (o_id, o_d_id, o_w_id, o_c_id, o_entry_d, o_ol_cnt, o_all_local)
	 * VALUES (:o_id, :d_id, :w_id, :c_id, :datetime, :o_ol_cnt, :o_all_local);
	 * @return
	 */
	public static String newOrderStmt4() {
		final String sqlText = "insert into " + OrdersDbo.TABLE_NAME + "(" + OrdersDbo.CN_O_ID + ","
				+ OrdersDbo.CN_O_D_ID + ", " + OrdersDbo.CN_O_W_ID + "," + OrdersDbo.CN_O_C_ID + ","
				+ OrdersDbo.CN_O_ENTRY_D + "," + OrdersDbo.CN_O_OL_CNT + "," + OrdersDbo.CN_O_ALL_LOCAL
				+ ") values (?,?,?,?,?,?,?)";
		return sqlText;
	}
	
	/**
	 * INSERT INTO NEW_ORDER (no_o_id, no_d_id, no_w_id)
	 * VALUES (:o_id, :d_id, :w_id);
	 * @return
	 */
	public static String newOrderStmt5() {
		final String sqlText = "insert into " + NewOrdersDbo.TABLE_NAME + " (" + NewOrdersDbo.CN_NO_O_ID
				+ ", " + NewOrdersDbo.CN_NO_D_ID + ", " + NewOrdersDbo.CN_NO_W_ID + ") values (?,?,?)";
		return sqlText;
	}
	
	/**
	 * SELECT i_price, i_name , i_data 
	 * INTO :i_price, :i_name, :i_data
	 * FROM item
	 * WHERE i_id = :ol_i_id;
	 * 
	 * @return
	 */
	public static String newOrderStmt6() {
		final String sqlText = "select " + ItemDbo.CN_I_PRICE + ", " + ItemDbo.CN_I_NAME + "," + ItemDbo.CN_I_DATA
				+ " from " + ItemDbo.TABLE_NAME + " where " + ItemDbo.CN_I_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * SELECT s_quantity, s_data, s_dist_01, s_dist_02,
	 *   s_dist_03, s_dist_04, s_dist_05, s_dist_06,
	 *   s_dist_07, s_dist_08, s_dist_09, s_dist_10
	 * INTO :s_quantity, :s_data, :s_dist_01, :s_dist_02,
	 *   :s_dist_03, :s_dist_04, :s_dist_05, :s_dist_06,
	 *   :s_dist_07, :s_dist_08, :s_dist_09, :s_dist_10
	 * FROM stock
	 * WHERE s_i_id = :ol_i_id 
	 * AND s_w_id = :ol_supply_w_id
	 * FOR UPDATE
	 * @return
	 */
	public static String newOrderStmt7() {
		final String sqlText = "select " + StockDbo.CN_S_QUANTITY + ", " + StockDbo.CN_S_DATA + ", "
				+ StockDbo.CN_S_DIST_01 + "," + StockDbo.CN_S_DIST_02 + "," + StockDbo.CN_S_DIST_03 + ","
				+ StockDbo.CN_S_DIST_04 + "," + StockDbo.CN_S_DIST_05 + "," + StockDbo.CN_S_DIST_06 + ","
				+ StockDbo.CN_S_DIST_07 + "," + StockDbo.CN_S_DIST_08 + "," + StockDbo.CN_S_DIST_09 + ","
				+ StockDbo.CN_S_DIST_10 
				+ " from " + StockDbo.TABLE_NAME 
				+ " where " + StockDbo.CN_S_I_ID + " = ? and " 
				+ StockDbo.CN_S_W_ID + " = ? for update"; //
		DBMS dbms = TpccContext.getContext().getDBMS();
		if (dbms.equals(DBMS.MSSQL)) { //mssql not support select ... for update.
			final String sqlTextMssql = "select " + StockDbo.CN_S_QUANTITY + ", " + StockDbo.CN_S_DATA + ", "
					+ StockDbo.CN_S_DIST_01 + "," + StockDbo.CN_S_DIST_02 + "," + StockDbo.CN_S_DIST_03 + ","
					+ StockDbo.CN_S_DIST_04 + "," + StockDbo.CN_S_DIST_05 + "," + StockDbo.CN_S_DIST_06 + ","
					+ StockDbo.CN_S_DIST_07 + "," + StockDbo.CN_S_DIST_08 + "," + StockDbo.CN_S_DIST_09 + ","
					+ StockDbo.CN_S_DIST_10 
					+ " from " + StockDbo.TABLE_NAME +" with (UPDLOCK, INDEX(idx_stock_1)) " 
					+ " where " + StockDbo.CN_S_I_ID + " = ? and " 
					+ StockDbo.CN_S_W_ID + " = ?"; //
			return sqlTextMssql;
		} else if(dbms.equals(DBMS.SQLite)) {
			final String sqlTextSqlite = "select " + StockDbo.CN_S_QUANTITY + ", " + StockDbo.CN_S_DATA + ", "
					+ StockDbo.CN_S_DIST_01 + "," + StockDbo.CN_S_DIST_02 + "," + StockDbo.CN_S_DIST_03 + ","
					+ StockDbo.CN_S_DIST_04 + "," + StockDbo.CN_S_DIST_05 + "," + StockDbo.CN_S_DIST_06 + ","
					+ StockDbo.CN_S_DIST_07 + "," + StockDbo.CN_S_DIST_08 + "," + StockDbo.CN_S_DIST_09 + ","
					+ StockDbo.CN_S_DIST_10 
					+ " from " + StockDbo.TABLE_NAME 
					+ " where " + StockDbo.CN_S_I_ID + " = ? and " 
					+ StockDbo.CN_S_W_ID + " = ?";
			return sqlTextSqlite;
		} else {
			return sqlText;
		}
		
	}

	/**
	 * 
	 * UPDATE stock SET s_quantity = :s_quantity
	 * WHERE s_i_id = :ol_i_id
	 * AND s_w_id = :ol_supply_w_id;
	 * 
	 * @return
	 */
	public static String newOrderStmt8() {
		final String sqlText = "update " + StockDbo.TABLE_NAME + " set " + StockDbo.CN_S_QUANTITY
				+ " = ? where " + StockDbo.CN_S_I_ID + " = ? and " + StockDbo.CN_S_W_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * 
	 * INSERT  INTO order_line (ol_o_id, ol_d_id, ol_w_id, ol_number,ol_i_id, 
	 *   ol_supply_w_id, ol_quantity, ol_amount, ol_dist_info) 
	 * VALUES (:o_id, :d_id, :w_id, :ol_number,:ol_i_id, :ol_supply_w_id, 
	 *   :ol_quantity, :ol_amount, :ol_dist_info);
	 *   
	 * @return
	 */
	public static String newOrderStmt9() {
		final String sqlText = "insert into " + OrderLineDbo.TABLE_NAME + " ( " 
				+ OrderLineDbo.CN_OL_O_ID + "," 
				+ OrderLineDbo.CN_OL_D_ID + "," 
				+ OrderLineDbo.CN_OL_W_ID + "," 
				+ OrderLineDbo.CN_OL_NUMBER + ","
				+ OrderLineDbo.CN_OL_I_ID + "," 
				+ OrderLineDbo.CN_OL_SUPPLY_W_ID + "," 
				+ OrderLineDbo.CN_OL_QUANTITY + "," 
				+ OrderLineDbo.CN_OL_AMOUNT + "," 
				+ OrderLineDbo.CN_OL_DIST_INFO + ") values (?,?,?,?,?,?,?,?,?)";
		return sqlText;
	}
	
	/**
	 * 
	 * UPDATE warehouse SET w_ytd = w_ytd + :h_amount WHERE w_id=:w_id;
	 * @return
	 */
	public static String paymentStmt1() {
		final String sqlText = "update " + WarehouseDbo.TABLE_NAME + " set " + WarehouseDbo.CN_W_YTD + " = "
				+ WarehouseDbo.CN_W_YTD + " + ?  where " + WarehouseDbo.CN_W_ID + " = ? ";
		return sqlText;
	}
	
	/**
	 * 
	 * SELECT w_street_1, w_street_2, w_city, w_state, w_zip, w_name
	 * INTO :w_street_1, :w_street_2, :w_city, :w_state, :w_zip, :w_name
	 * FROM warehouse
	 * WHERE w_id=:w_id;
	 * @return
	 */
	
	public static String paymentStmt2() {
		final String sqlText = "select "+WarehouseDbo.CN_W_STREET_1 +", "
				+WarehouseDbo.CN_W_STREET_2 +", "
				+WarehouseDbo.CN_W_CITY +", "
				+WarehouseDbo.CN_W_STATE +", "
				+WarehouseDbo.CN_W_ZIP +", "
				+WarehouseDbo.CN_W_NAME 
				+"  from "+WarehouseDbo.TABLE_NAME 
				+"  where "+WarehouseDbo.CN_W_ID + "=?";
		return sqlText;
	}
	
	/**
	 * 
	 * UPDATE district SET d_ytd = d_ytd + :h_amount 
	 * WHERE d_w_id=:w_id AND d_id=:d_id;
	 * 
	 * @return
	 */
	public static String paymentStmt3() {
		final String sqlText = "update " + DistrictDbo.TABLE_NAME + " set " + DistrictDbo.CN_D_YTD + " = "
				+ DistrictDbo.CN_D_YTD + " + ? where " + DistrictDbo.CN_D_W_ID + " = ? and " + DistrictDbo.CN_D_ID
				+ " = ? ";
		return sqlText;
	}
	
	/**
	 * SELECT d_street_1, d_street_2, d_city, d_state, d_zip, d_name
	 * INTO :d_street_1, :d_street_2, :d_city, :d_state, :d_zip, :d_name
	 * FROM district
	 * WHERE d_w_id=:w_id AND d_id=:d_id;
	 * 
	 * @return
	 */
	public static String paymentStmt4() {
		final String sqlText = "select "+DistrictDbo.CN_D_STREET_1+","
				+DistrictDbo.CN_D_STREET_2+","
				+DistrictDbo.CN_D_CITY+","
				+DistrictDbo.CN_D_STATE+","
				+DistrictDbo.CN_D_ZIP+","
				+DistrictDbo.CN_D_NAME
				+" from " + DistrictDbo.TABLE_NAME
				+" where " + DistrictDbo.CN_D_W_ID + " = ? and "+DistrictDbo.CN_D_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * 
	 * SELECT count(c_id) INTO :namecnt  FROM customer
	 * WHERE c_last=:c_last AND c_d_id=:c_d_id AND c_w_id=:c_w_id;
	 * 
	 * @return
	 */
	public static String paymentStmt5() {
		final String sqlText = "select count(" + CustomerDbo.CN_C_ID + ") as namecnt from " + CustomerDbo.TABLE_NAME
				+ " where " + CustomerDbo.CN_C_LAST + " = ? and " + CustomerDbo.CN_C_D_ID + " = ? and " +CustomerDbo.CN_C_W_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * SELECT c_first, c_middle, c_id,
	 * c_street_1, c_street_2, c_city, c_state, c_zip, 
	 * c_phone, c_credit, c_credit_lim,
	 * c_discount, c_balance, c_since
	 * FROM customer
	 * WHERE c_w_id=:c_w_id AND c_d_id=:c_d_id AND c_last=:c_last
	 * ORDER BY c_first;
	 * 
	 * @return
	 */
	public static String paymentStmt6() {
		final String sqlText = "select "+CustomerDbo.CN_C_FIRST+","
				+CustomerDbo.CN_C_MIDDLE+","
				+CustomerDbo.CN_C_ID+","
				+CustomerDbo.CN_C_STREET_1+","
				+CustomerDbo.CN_C_STREET_2+","
				+CustomerDbo.CN_C_CITY+","
				+CustomerDbo.CN_C_STATE+","
				+CustomerDbo.CN_C_ZIP+","
				+CustomerDbo.CN_C_PHONE+","
				+CustomerDbo.CN_C_CREDIT+","
				+CustomerDbo.CN_C_CREDIT_LIM+","
				+CustomerDbo.CN_C_DISCOUNT+","
				+CustomerDbo.CN_C_BALANCE+","
				+CustomerDbo.CN_C_SINCE
				+" from "+CustomerDbo.TABLE_NAME
				+" where "+ CustomerDbo.CN_C_W_ID +" = ? and "
				+ CustomerDbo.CN_C_D_ID+ " = ? and " 
				+ CustomerDbo.CN_C_LAST + " = ?  order by "+CustomerDbo.CN_C_FIRST;
		return sqlText;
	}
	
	/**
	 * 
	 * SELECT c_first, c_middle, c_last,
	 *   c_street_1, c_street_2, c_city, c_state, c_zip, 
	 *   c_phone, c_credit, c_credit_lim,
	 *   c_discount, c_balance, c_since
	 *   INTO :c_first, :c_middle, :c_last,
	 *   :c_street_1, :c_street_2, :c_city, :c_state, :c_zip,
	 *   :c_phone, :c_credit, :c_credit_lim,
	 *   :c_discount, :c_balance, :c_since
	 * FROM customer
	 * WHERE c_w_id=:c_w_id AND c_d_id=:c_d_id AND c_id=:c_id;
	 * 
	 * @return
	 */
	public static String paymentStmt7() {
		final String sqlText = "select " +CustomerDbo.CN_C_FIRST+", "
				 +CustomerDbo.CN_C_MIDDLE+", "
				 +CustomerDbo.CN_C_LAST+", "
				 +CustomerDbo.CN_C_STREET_1+", "
				 +CustomerDbo.CN_C_STREET_2+", "
				 +CustomerDbo.CN_C_CITY+", "
				 +CustomerDbo.CN_C_STATE+", "
				 +CustomerDbo.CN_C_ZIP+", "
				 +CustomerDbo.CN_C_PHONE+", "
				 +CustomerDbo.CN_C_CREDIT+", "
				+CustomerDbo.CN_C_CREDIT_LIM+","
				+CustomerDbo.CN_C_DISCOUNT+","
				+CustomerDbo.CN_C_BALANCE+","
				+CustomerDbo.CN_C_SINCE
				+" from "+CustomerDbo.TABLE_NAME
				+" where "+ CustomerDbo.CN_C_W_ID +" = ? and "
				+ CustomerDbo.CN_C_D_ID+ " = ? and " 
				+ CustomerDbo.CN_C_ID + " = ? ";
		return sqlText;
	}
	
	/**
	 * SELECT c_data INTO :c_data 
	 * FROM customer
	 * WHERE c_w_id=:c_w_id AND c_d_id=:c_d_id AND c_id=:c_id;
	 * @return
	 */
	public static String paymentStmt8() {
		final String sqlText = "select " + CustomerDbo.CN_C_DATA + " from " + CustomerDbo.TABLE_NAME
				+ " where " + CustomerDbo.CN_C_W_ID + " = ? and " + CustomerDbo.CN_C_D_ID + " = ?  and "
				+ CustomerDbo.CN_C_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * 
	 * UPDATE customer
	 * SET c_balance = :c_balance,  c_data = :c_new_data
	 * WHERE c_w_id = :c_w_id AND c_d_id = :c_d_id AND
	 * c_id = :c_id;
	 * 
	 * @return
	 */
	public static String paymentStmt9() {
		final String sqlText = "update " + CustomerDbo.TABLE_NAME + " set " + CustomerDbo.CN_C_BALANCE
				+ "=?, " + CustomerDbo.CN_C_DATA + " = ? where " + CustomerDbo.CN_C_W_ID + "=? and "
				+ CustomerDbo.CN_C_D_ID + "=? and " + CustomerDbo.CN_C_ID + "=?";
		return sqlText;
	}
	
	/**
	 * UPDATE customer SET c_balance = :c_balance
	 * WHERE c_w_id = :c_w_id AND c_d_id = :c_d_id AND
	 * c_id = :c_id;
	 * 
	 * @return
	 */
	public static String paymentStmt10() {
		final String sqlText = "update " + CustomerDbo.TABLE_NAME + " set " + CustomerDbo.CN_C_BALANCE
				+ " = ? where " + CustomerDbo.CN_C_W_ID + "=? and " + CustomerDbo.CN_C_D_ID + "=? and "
				+ CustomerDbo.CN_C_ID + "=?";
		return sqlText;
	}
	
	/**
	 * INSERT INTO history (h_c_d_id, h_c_w_id, h_c_id, h_d_id,
	 *  h_w_id, h_date, h_amount, h_data) 
	 * VALUES (:c_d_id, :c_w_id, :c_id, :d_id,
	 *  :w_id, :datetime, :h_amount, :h_data); 
	 * @return
	 */
	
	public static String paymentStmt11() {
		final String sqlText = "insert into " + HistoryDbo.TABLE_NAME + "(" + HistoryDbo.CN_H_C_D_ID + ", "
				+ HistoryDbo.CN_H_C_W_ID + ", " + HistoryDbo.CN_H_C_ID + "," + HistoryDbo.CN_H_D_ID + ","
				+ HistoryDbo.CN_H_W_ID + "," + HistoryDbo.CN_H_DATE + "," + HistoryDbo.CN_H_AMOUNT + ","
				+ HistoryDbo.CN_H_DATA + ") values (?,?,?,?,?,?,?,?)";
		return sqlText;
	}
	
	///Order-Status
	
	/**
	 * SELECT count(c_id) INTO :namecnt
	 * FROM customer
	 * WHERE c_last=:c_last AND c_d_id=:d_id AND c_w_id=:w_id;
	 * 
	 * @return
	 */
	public static String orderStatusStmt1() {
		final String sqlText = "select count(" + CustomerDbo.CN_C_ID + ") as namecnt from " + CustomerDbo.TABLE_NAME
				+ " where " + CustomerDbo.CN_C_LAST + " = ? and " + CustomerDbo.CN_C_D_ID + " = ? and "
				+ CustomerDbo.CN_C_W_ID + " = ? ";
		return sqlText;
	}
	
	/**
	 * SELECT c_balance, c_first, c_middle, c_id
	 * FROM customer
	 * WHERE c_last=:c_last AND c_d_id=:d_id AND c_w_id=:w_id
	 * ORDER BY c_first;
	 * @return
	 */
	public static String orderStatusStmt2() {
		final String sqlText = "select " + CustomerDbo.CN_C_BALANCE + ", " + CustomerDbo.CN_C_FIRST + ", "
				+ CustomerDbo.CN_C_MIDDLE + ", " + CustomerDbo.CN_C_ID + " from " + CustomerDbo.TABLE_NAME
				+ " where " + CustomerDbo.CN_C_LAST + " = ? and " + CustomerDbo.CN_C_D_ID + " = ? and "
				+ CustomerDbo.CN_C_W_ID + " = ? order by " + CustomerDbo.CN_C_FIRST;

		return sqlText;
	}
	
	/**
	 * 
	 * SELECT c_balance, c_first, c_middle, c_last
	 *   INTO :c_balance, :c_first, :c_middle, :c_last 
	 * FROM customer
	 * WHERE c_id=:c_id AND c_d_id=:d_id AND c_w_id=:w_id;
	 * 
	 * @return
	 */
	public static String orderStatusStmt3() {
		final String sqlText = "select " + CustomerDbo.CN_C_BALANCE + ", " + CustomerDbo.CN_C_FIRST + ", "
				+ CustomerDbo.CN_C_MIDDLE + ", " + CustomerDbo.CN_C_LAST + " from " + CustomerDbo.TABLE_NAME
				+ " where " + CustomerDbo.CN_C_ID + " = ? and " + CustomerDbo.CN_C_D_ID + " = ? and "
				+ CustomerDbo.CN_C_W_ID + " = ?";

		return sqlText;
	}
	
	/**
	 * SELECT o_id, o_entry_d, COALESCE(o_carrier_id,0)
	 *   INTO :o_id, :o_entry_d, :o_carrier_id
	 * FROM orders
	 * WHERE o_w_id = :c_w_id
	 *   AND o_d_id = :c_d_id
	 *   AND o_c_id = :c_id
	 *   AND o_id = (SELECT MAX(o_id) FROM orders
	 *     WHERE o_w_id = :c_w_id
	 *           AND o_d_id = :c_d_id AND o_c_id = :c_id);
	 * @return
	 */
	public static String orderStatusStmt4() {
		final String sqlText = "select "
				+ OrdersDbo.CN_O_ID + ","
				+ OrdersDbo.CN_O_ENTRY_D + ", coalesce("
				+ OrdersDbo.CN_O_CARRIER_ID +", 0)  as " + OrdersDbo.CN_O_CARRIER_ID
				+ " from "+OrdersDbo.TABLE_NAME
				+ " where "+OrdersDbo.CN_O_W_ID +" = ? "
				+ "  and "+OrdersDbo.CN_O_D_ID + " = ? "
				+ "  and "+OrdersDbo.CN_O_C_ID + " = ? "
				+ "  and "+OrdersDbo.CN_O_ID 
				+ " = (select max("+ OrdersDbo.CN_O_ID+") from "+OrdersDbo.TABLE_NAME
				+ " where "+OrdersDbo.CN_O_W_ID + " = ? and "+OrdersDbo.CN_O_D_ID + " = ? and "+OrdersDbo.CN_O_C_ID +" =?)";
		return sqlText;
	}
	
    /**
     * 
     * SELECT ol_i_id, ol_supply_w_id, ol_quantity, ol_amount, ol_delivery_d
     * FROM order_line
     * WHERE ol_o_id=:o_id AND ol_d_id=:d_id AND ol_w_id=:w_id;
     * @return
     */
	public static String orderStatusStmt5() {
		final String sqlText = "select "+OrderLineDbo.CN_OL_I_ID +", "
				+OrderLineDbo.CN_OL_SUPPLY_W_ID +", "
				+OrderLineDbo.CN_OL_QUANTITY +", "
				+OrderLineDbo.CN_OL_AMOUNT +", "
				+OrderLineDbo.CN_OL_DELIVERY_D 
				+" from " +OrderLineDbo.TABLE_NAME
				+" where "+OrderLineDbo.CN_OL_O_ID +" = ? and "
				+  OrderLineDbo.CN_OL_D_ID + " = ? and "
				+  OrderLineDbo.CN_OL_W_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * 
	 * SELECT coalesce(min(no_o_id),0) as no_o_id
	 * FROM new_order
	 * WHERE no_d_id = :d_id AND no_w_id = :w_id ;
	 * 
	 * @return
	 */
	public static String deliveryStmt1() {
		final String sqlText = "select coalesce(min(" + NewOrdersDbo.CN_NO_O_ID +"),0) as no_o_id from "
				+ NewOrdersDbo.TABLE_NAME
				+" where "+NewOrdersDbo.CN_NO_D_ID +" = ? and "+NewOrdersDbo.CN_NO_W_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * DELETE FROM new_orders WHERE no_d_id = ? AND no_w_id = ? AND no_o_id = ?"
	 * 
	 * @return
	 */
	public static String deliveryStmt2() {
		final String sqlText = "delete from " +NewOrdersDbo.TABLE_NAME
			+" where "+ NewOrdersDbo.CN_NO_D_ID + " = ? and " +NewOrdersDbo.CN_NO_W_ID + " = ? and " + NewOrdersDbo.CN_NO_O_ID +" = ? ";
		return sqlText;
	}
	
	/**
	 * SELECT o_c_id INTO :c_id FROM orders
	 * WHERE o_id = :no_o_id AND o_d_id = :d_id AND o_w_id = :w_id;
	 * 
	 * @return
	 */
	public static String deliveryStmt3() {
		final String sqlText = "select " + OrdersDbo.CN_O_C_ID 
				+ " from " + OrdersDbo.TABLE_NAME 
				+ " where " + OrdersDbo.CN_O_ID + " = ? and " 
				+ OrdersDbo.CN_O_D_ID + " = ? and " + OrdersDbo.CN_O_W_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * 
	 * UPDATE orders SET o_carrier_id = :o_carrier_id
	 * WHERE o_id = :no_o_id AND o_d_id = :d_id AND o_w_id = :w_id;  
	 * @return
	 */
	public static String deliveryStmt4() {
		final String sqlText = "update "+OrdersDbo.TABLE_NAME + " set "+OrdersDbo.CN_O_CARRIER_ID 
				+ " = ? where " +OrdersDbo.CN_O_ID +" = ? and " + OrdersDbo.CN_O_D_ID +" = ? and "+OrdersDbo.CN_O_W_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * UPDATE order_line SET ol_delivery_d = :datetime
	 * WHERE ol_o_id = :no_o_id AND ol_d_id = :d_id AND
	 * ol_w_id = :w_id;
	 * @return
	 */
	public static String deliveryStmt5() {
		final String sqlText = "update " + OrderLineDbo.TABLE_NAME+ " set " +OrderLineDbo.CN_OL_DELIVERY_D 
				+ " = ? where " + OrderLineDbo.CN_OL_O_ID+ " = ?  and " 
				+ OrderLineDbo.CN_OL_D_ID+ " = ? and " + OrderLineDbo.CN_OL_W_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * 
	 * SELECT SUM(ol_amount) INTO :ol_total
	 * FROM order_line
	 * WHERE ol_o_id = :no_o_id AND ol_d_id = :d_id
	 * AND ol_w_id = :w_id;
	 * 
	 * @return
	 */
	
	public static String deliveryStmt6() {
		final String sqlText = "select sum(" + OrderLineDbo.CN_OL_AMOUNT + ") as ol_total from " + OrderLineDbo.TABLE_NAME 
				+ " where " + OrderLineDbo.CN_OL_O_ID + " = ? and " + OrderLineDbo.CN_OL_D_ID + " = ? and " 
				+ OrderLineDbo.CN_OL_W_ID + " = ?";
		return sqlText;
	}
	
	/**
	 * 
	 * UPDATE customer SET c_balance = c_balance + :ol_total
	 * WHERE c_id = :c_id AND c_d_id = :d_id AND c_w_id = :w_id;
	 * @return
	 */
	
	public static String deliveryStmt7() {
		final String sqlText = "update " + CustomerDbo.TABLE_NAME + " set " + CustomerDbo.CN_C_BALANCE + " ="
				+ CustomerDbo.CN_C_BALANCE + " + ? where " + CustomerDbo.CN_C_ID + " = ? and " + CustomerDbo.CN_C_D_ID
				+ " = ? and " + CustomerDbo.CN_C_W_ID + " = ? ";
		return sqlText;
	}
	
	/**
	 * SELECT d_next_o_id INTO :o_id
	 * FROM district
	 * WHERE d_w_id=:w_id AND d_id=:d_id; 
	 * 
	 * @return
	 */
	public static String stockLevelStmt1() {
		final String sqlText = "select " + DistrictDbo.CN_D_NEXT_O_ID + " from " + DistrictDbo.TABLE_NAME
				+ " where " + DistrictDbo.CN_D_W_ID + " = ? and " + DistrictDbo.CN_D_ID + " = ? ";
		return sqlText;
	}
	
	/**
	 * SELECT COUNT(DISTINCT (s_i_id)) INTO :stock_count
               FROM order_line, stock
               WHERE ol_w_id=:w_id AND
                     ol_d_id=:d_id AND ol_o_id<:o_id AND
                     ol_o_id>=:o_id-20 AND s_w_id=:w_id AND
                     s_i_id=ol_i_id AND s_quantity < :threshold;
	 * 
	 * @return
	 */
	public static String stockLevelStmt2() {
		final String sqlText = "select count( distinct("+StockDbo.CN_S_I_ID+")) as stock_count from "
				+ OrderLineDbo.TABLE_NAME +", "+StockDbo.TABLE_NAME
				+ " where " +OrderLineDbo.CN_OL_W_ID +" = ? and " 
				+ OrderLineDbo.CN_OL_D_ID + " = ? and " 
				+ OrderLineDbo.CN_OL_O_ID +" < ? and " 
				+ OrderLineDbo.CN_OL_O_ID +" >= (? - 20) and " 
				+ StockDbo.CN_S_W_ID + " = ? and " 
				+ StockDbo.CN_S_I_ID + " = " + OrderLineDbo.CN_OL_I_ID +" and "
				+ StockDbo.CN_S_QUANTITY+" < ?";
		return sqlText;
	}
	
}
