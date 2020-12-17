package org.littlestar.tpcc.dbo;

/**
 * 商品仓库表(warehouse):
 * 
 * 
 * @author LiXiang
 *
 */
public class WarehouseDbo {
	public static final String SCHEMA_NAME   = "tpcc";
	public static final String TABLE_NAME    = "warehouse";
	//CN_xxx: Column Name.
	public static final String CN_W_ID        = "w_id";
	public static final String CN_W_NAME      = "w_name";
	public static final String CN_W_STREET_1  = "w_street_1";
	public static final String CN_W_STREET_2  = "w_street_2";
	public static final String CN_W_CITY      = "w_city";
	public static final String CN_W_STATE     = "w_state";
	public static final String CN_W_ZIP       = "w_zip";
	public static final String CN_W_TAX       = "w_tax";
	public static final String CN_W_YTD       = "w_ytd";
	
	//CI_xxx: Column Index/Position, Start with 1.
	public static final int CI_W_ID        = 1;
	public static final int CI_W_NAME      = 2;
	public static final int CI_W_STREET_1  = 3;
	public static final int CI_W_STREET_2  = 4;
	public static final int CI_W_CITY      = 5;
	public static final int CI_W_STATE     = 6;
	public static final int CI_W_ZIP       = 7;
	public static final int CI_W_TAX       = 8;
	public static final int CI_W_YTD       = 9;
	
	public static String tableName(boolean withSchema) {
		String name = TABLE_NAME;
		if (withSchema) {
			name = SCHEMA_NAME + "." + TABLE_NAME;
		}
		return name;
	}
}
