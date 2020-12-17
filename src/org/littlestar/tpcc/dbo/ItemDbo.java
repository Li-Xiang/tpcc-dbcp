package org.littlestar.tpcc.dbo;

/**
商品信息表(item)
create table item (
  i_id int not null,     --> Item ID (Primary key)
  i_im_id int,           --> Image ID associated to Item
  i_name varchar(24),    --> Name
  i_price decimal(5,2),  --> Item price
  i_data varchar(50),    --> Brand information
  PRIMARY KEY(i_id) 
);
  
 * @author LiXiang
 *
 */
public class ItemDbo {
	public static final String SCHEMA_NAME   = "tpcc";
	public static final String TABLE_NAME    = "item";
	//CN_xxx: Column Name.
	public static final String CN_I_ID       = "i_id";
	public static final String CN_I_IM_ID    = "i_im_id";
	public static final String CN_I_NAME     = "i_name";
	public static final String CN_I_PRICE    = "i_price";
	public static final String CN_I_DATA     = "i_data";
	
	//CI_xxx: Column Index/Position, Start with 1.
	public static final int CI_I_ID       = 1;
	public static final int CI_I_IM_ID    = 2;
	public static final int CI_I_NAME     = 3;
	public static final int CI_I_PRICE    = 4;
	public static final int CI_I_DATA     = 5;
	
	public static String tableName(boolean withSchema) {
		String name = TABLE_NAME;
		if (withSchema) {
			name = SCHEMA_NAME + "." + TABLE_NAME;
		}
		return name;
	}
}
