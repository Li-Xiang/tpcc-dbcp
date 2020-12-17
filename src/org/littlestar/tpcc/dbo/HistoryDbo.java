package org.littlestar.tpcc.dbo;

public class HistoryDbo {
	public static final String SCHEMA_NAME   = "tpcc";
	public static final String TABLE_NAME    = "history";
	//CN_xxx: Column Name.
	public static final String CN_H_C_ID    = "h_c_id";
	public static final String CN_H_C_D_ID  = "h_c_d_id";
	public static final String CN_H_C_W_ID  = "h_c_w_id";
	public static final String CN_H_D_ID    = "h_d_id";
	public static final String CN_H_W_ID    = "h_w_id";
	public static final String CN_H_DATE    = "h_date";
	public static final String CN_H_AMOUNT  = "h_amount";
	public static final String CN_H_DATA    = "h_data";
	
	//CI_xxx: Column Index/Position, Start with 1.
	public static final int CI_H_C_ID    = 1;
	public static final int CI_H_C_D_ID  = 2;
	public static final int CI_H_C_W_ID  = 3;
	public static final int CI_H_D_ID    = 4;
	public static final int CI_H_W_ID    = 5;
	public static final int CI_H_DATE    = 6;
	public static final int CI_H_AMOUNT  = 7;
	public static final int CI_H_DATA    = 8;
	
	public static String tableName(boolean withSchema) {
		String name = TABLE_NAME;
		if (withSchema) {
			name = SCHEMA_NAME + "." + TABLE_NAME;
		}
		return name;
	}
}
