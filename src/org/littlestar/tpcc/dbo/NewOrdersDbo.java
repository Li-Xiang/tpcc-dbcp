package org.littlestar.tpcc.dbo;

public class NewOrdersDbo {
	public static final String SCHEMA_NAME   = "tpcc";
	public static final String TABLE_NAME    = "new_orders";
	//CN_xxx: Column Name.
	public static final String CN_NO_O_ID = "no_o_id";
	public static final String CN_NO_D_ID = "no_d_id";
	public static final String CN_NO_W_ID = "no_w_id";
	
	//CI_xxx: Column Index/Position, Start with 1.
	public static final int CI_NO_O_ID = 1;
	public static final int CI_NO_D_ID = 2;
	public static final int CI_NO_W_ID = 3;
	
	public static String tableName(boolean withSchema) {
		String name = TABLE_NAME;
		if (withSchema) {
			name = SCHEMA_NAME + "." + TABLE_NAME;
		}
		return name;
	}
}
