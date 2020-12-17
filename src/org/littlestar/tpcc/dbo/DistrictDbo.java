package org.littlestar.tpcc.dbo;

public class DistrictDbo {
	public static final String SCHEMA_NAME   = "tpcc";
	public static final String TABLE_NAME    = "district";
	
	//CN_xxx: Column Name.
	public static final String CN_D_ID          = "d_id";
	public static final String CN_D_W_ID        = "d_w_id";
	public static final String CN_D_NAME        = "d_name";
	public static final String CN_D_STREET_1    = "d_street_1";
	public static final String CN_D_STREET_2    = "d_street_2";
	public static final String CN_D_CITY        = "d_city";
	public static final String CN_D_STATE       = "d_state";
	public static final String CN_D_ZIP         = "d_zip";
	public static final String CN_D_TAX         = "d_tax";
	public static final String CN_D_YTD         = "d_ytd";
	public static final String CN_D_NEXT_O_ID   = "d_next_o_id";
	
	//CI_xxx: Column Index/Position, Start with 1.
	public static final int CI_D_ID          = 1;
	public static final int CI_D_W_ID        = 2;
	public static final int CI_D_NAME        = 3;
	public static final int CI_D_STREET_1    = 4;
	public static final int CI_D_STREET_2    = 5;
	public static final int CI_D_CITY        = 6;
	public static final int CI_D_STATE       = 7;
	public static final int CI_D_ZIP         = 8;
	public static final int CI_D_TAX         = 9;
	public static final int CI_D_YTD         = 10;
	public static final int CI_D_NEXT_O_ID   = 11;
	
	public static String tableName(boolean withSchema) {
		String name = TABLE_NAME;
		if (withSchema) {
			name = SCHEMA_NAME + "." + TABLE_NAME;
		}
		return name;
	}

}
