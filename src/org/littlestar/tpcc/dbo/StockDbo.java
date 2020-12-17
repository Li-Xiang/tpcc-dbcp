package org.littlestar.tpcc.dbo;

public class StockDbo {
	public static final String SCHEMA_NAME   = "tpcc";
	public static final String TABLE_NAME    = "stock";
	//CN_xxx: Column Name.
	public static final String CN_S_I_ID       = "s_i_id";
	public static final String CN_S_W_ID       = "s_w_id";
	public static final String CN_S_QUANTITY   = "s_quantity";
	public static final String CN_S_DIST_01    = "s_dist_01";
	public static final String CN_S_DIST_02    = "s_dist_02";
	public static final String CN_S_DIST_03    = "s_dist_03";
	public static final String CN_S_DIST_04    = "s_dist_04";
	public static final String CN_S_DIST_05    = "s_dist_05";
	public static final String CN_S_DIST_06    = "s_dist_06";
	public static final String CN_S_DIST_07    = "s_dist_07";
	public static final String CN_S_DIST_08    = "s_dist_08";
	public static final String CN_S_DIST_09    = "s_dist_09";
	public static final String CN_S_DIST_10    = "s_dist_10";
	public static final String CN_S_YTD        = "s_ytd";
	public static final String CN_S_ORDER_CNT  = "s_order_cnt";
	public static final String CN_S_REMOTE_CNT = "s_remote_cnt";
	public static final String CN_S_DATA       = "s_data";
	
	//CI_xxx: Column Index/Position, Start with 1.
	public static final int CI_S_I_ID       = 1;
	public static final int CI_S_W_ID       = 2;
	public static final int CI_S_QUANTITY   = 3;
	public static final int CI_S_DIST_01    = 4;
	public static final int CI_S_DIST_02    = 5;
	public static final int CI_S_DIST_03    = 6;
	public static final int CI_S_DIST_04    = 7;
	public static final int CI_S_DIST_05    = 8;
	public static final int CI_S_DIST_06    = 9;
	public static final int CI_S_DIST_07    = 10;
	public static final int CI_S_DIST_08    = 11;
	public static final int CI_S_DIST_09    = 12;
	public static final int CI_S_DIST_10    = 13;
	public static final int CI_S_YTD        = 14;
	public static final int CI_S_ORDER_CNT  = 15;
	public static final int CI_S_REMOTE_CNT = 16;
	public static final int CI_S_DATA       = 17;
	
	public static String tableName(boolean withSchema) {
		String name = TABLE_NAME;
		if (withSchema) {
			name = SCHEMA_NAME + "." + TABLE_NAME;
		}
		return name;
	}
}
