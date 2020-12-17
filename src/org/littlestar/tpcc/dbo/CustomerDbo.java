package org.littlestar.tpcc.dbo;

public class CustomerDbo {
	public static final String SCHEMA_NAME   = "tpcc";
	public static final String TABLE_NAME    = "customer";
	//CN_xxx: Column Name.
	public static final String CN_C_ID           = "c_id";
	public static final String CN_C_D_ID         = "c_d_id";
	public static final String CN_C_W_ID         = "c_w_id";
	public static final String CN_C_FIRST        = "c_first";
	public static final String CN_C_MIDDLE       = "c_middle";
	public static final String CN_C_LAST         = "c_last";
	public static final String CN_C_STREET_1     = "c_street_1";
	public static final String CN_C_STREET_2     = "c_street_2";
	public static final String CN_C_CITY         = "c_city";
	public static final String CN_C_STATE        = "c_state";
	public static final String CN_C_ZIP          = "c_zip";
	public static final String CN_C_PHONE        = "c_phone";
	public static final String CN_C_SINCE        = "c_since";
	public static final String CN_C_CREDIT       = "c_credit";
	public static final String CN_C_CREDIT_LIM   = "c_credit_lim";
	public static final String CN_C_DISCOUNT     = "c_discount";
	public static final String CN_C_BALANCE      = "c_balance";
	public static final String CN_C_YTD_PAYMENT  = "c_ytd_payment";
	public static final String CN_C_PAYMENT_CNT  = "c_payment_cnt";
	public static final String CN_C_DELIVERY_CNT = "c_delivery_cnt";
	public static final String CN_C_DATA         = "c_data";
	
	//CI_xxx: Column Index/Position, Start with 1.
	public static final int CI_C_ID           = 1;
	public static final int CI_C_D_ID         = 2;
	public static final int CI_C_W_ID         = 3;
	public static final int CI_C_FIRST        = 4;
	public static final int CI_C_MIDDLE       = 5;
	public static final int CI_C_LAST         = 6;
	public static final int CI_C_STREET_1     = 7;
	public static final int CI_C_STREET_2     = 8;
	public static final int CI_C_CITY         = 9;
	public static final int CI_C_STATE        = 10;
	public static final int CI_C_ZIP          = 11;
	public static final int CI_C_PHONE        = 12;
	public static final int CI_C_SINCE        = 13;
	public static final int CI_C_CREDIT       = 14;
	public static final int CI_C_CREDIT_LIM   = 15;
	public static final int CI_C_DISCOUNT     = 16;
	public static final int CI_C_BALANCE      = 17;
	public static final int CI_C_YTD_PAYMENT  = 18;
	public static final int CI_C_PAYMENT_CNT  = 19;
	public static final int CI_C_DELIVERY_CNT = 20;
	public static final int CI_C_DATA         = 21;
	
	public static String tableName(boolean withSchema) {
		String name = TABLE_NAME;
		if (withSchema) {
			name = SCHEMA_NAME + "." + TABLE_NAME;
		}
		return name;
	}
}
