package org.littlestar.tpcc.dbo;

public class OrdersDbo {
	public static final String SCHEMA_NAME   = "tpcc";
	public static final String TABLE_NAME    = "orders";
	//CN_xxx: Column Name.
	public static final String CN_O_ID          = "o_id";
	public static final String CN_O_D_ID        = "o_d_id";
	public static final String CN_O_W_ID        = "o_w_id";
	public static final String CN_O_C_ID        = "o_c_id";
	public static final String CN_O_ENTRY_D     = "o_entry_d";
	public static final String CN_O_CARRIER_ID  = "o_carrier_id";
	public static final String CN_O_OL_CNT      = "o_ol_cnt";
	public static final String CN_O_ALL_LOCAL   = "o_all_local";
	
	//CI_xxx: Column Index/Position, Start with 1.
	public static final int CI_O_ID          = 1;
	public static final int CI_O_D_ID        = 2;
	public static final int CI_O_W_ID        = 3;
	public static final int CI_O_C_ID        = 4;
	public static final int CI_O_ENTRY_D     = 5;
	public static final int CI_O_CARRIER_ID  = 6;
	public static final int CI_O_OL_CNT      = 7;
	public static final int CI_O_ALL_LOCAL   = 8;
	
	public static String tableName(boolean withSchema) {
		String name = TABLE_NAME;
		if (withSchema) {
			name = SCHEMA_NAME + "." + TABLE_NAME;
		}
		return name;
	}
}
