package org.littlestar.tpcc.dbo;

public class OrderLineDbo {
	public static final String SCHEMA_NAME   = "tpcc";
	public static final String TABLE_NAME    = "order_line";
	//CN_xxx: Column Name.
	public static final String CN_OL_O_ID        = "ol_o_id";
	public static final String CN_OL_D_ID        = "ol_d_id";
	public static final String CN_OL_W_ID        = "ol_w_id";
	public static final String CN_OL_NUMBER      = "ol_number";
	public static final String CN_OL_I_ID        = "ol_i_id";
	public static final String CN_OL_SUPPLY_W_ID = "ol_supply_w_id";
	public static final String CN_OL_DELIVERY_D  = "ol_delivery_d";
	public static final String CN_OL_QUANTITY    = "ol_quantity";
	public static final String CN_OL_AMOUNT      = "ol_amount";
	public static final String CN_OL_DIST_INFO   = "ol_dist_info";
	
	//CI_xxx: Column Index/Position, Start with 1.
	public static final int CI_OL_O_ID        = 1;
	public static final int CI_OL_D_ID        = 2;
	public static final int CI_OL_W_ID        = 3;
	public static final int CI_OL_NUMBER      = 4;
	public static final int CI_OL_I_ID        = 5;
	public static final int CI_OL_SUPPLY_W_ID = 6;
	public static final int CI_OL_DELIVERY_D  = 7;
	public static final int CI_OL_QUANTITY    = 8;
	public static final int CI_OL_AMOUNT      = 9;
	public static final int CI_OL_DIST_INFO   = 10;
	
	public static String tableName(boolean withSchema) {
		String name = TABLE_NAME;
		if (withSchema) {
			name = SCHEMA_NAME + "." + TABLE_NAME;
		}
		return name;
	}
}
