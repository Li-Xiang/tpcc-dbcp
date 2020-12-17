use tpcc;
go
create table warehouse (
  w_id smallint not null,
  w_name nvarchar(10), 
  w_street_1 nvarchar(20), 
  w_street_2 nvarchar(20), 
  w_city nvarchar(20), 
  w_state nchar(2), 
  w_zip nchar(9), 
  w_tax decimal(4,2), 
  w_ytd decimal(12,2),
  constraint warehouse_pk primary key (w_id)
);
go

create table district (
  d_id tinyint not null, 
  d_w_id smallint not null, 
  d_name nvarchar(10), 
  d_street_1 nvarchar(20), 
  d_street_2 nvarchar(20), 
  d_city nvarchar(20), 
  d_state nchar(2), 
  d_zip nchar(9), 
  d_tax decimal(4,2), 
  d_ytd decimal(12,2), 
  d_next_o_id int,
  constraint district_pk primary key (d_w_id, d_id)
);
go


create table customer (
  c_id int not null, 
  c_d_id tinyint not null,
  c_w_id smallint not null, 
  c_first nvarchar(16), 
  c_middle nchar(2), 
  c_last nvarchar(16), 
  c_street_1 nvarchar(20), 
  c_street_2 nvarchar(20), 
  c_city nvarchar(20), 
  c_state nchar(2), 
  c_zip nchar(9), 
  c_phone nchar(16), 
  c_since datetime2, 
  c_credit nchar(2), 
  c_credit_lim bigint, 
  c_discount decimal(4,2), 
  c_balance decimal(12,2), 
  c_ytd_payment decimal(12,2), 
  c_payment_cnt smallint, 
  c_delivery_cnt smallint, 
  c_data ntext,
  constraint customer_pk primary key (c_w_id, c_d_id, c_id)
);
go

create table history (
  h_c_id int, 
  h_c_d_id tinyint, 
  h_c_w_id smallint,
  h_d_id tinyint,
  h_w_id smallint,
  h_date datetime2,
  h_amount decimal(6,2), 
  h_data nvarchar(24) 
);
go

create table new_orders (
  no_o_id int not null,
  no_d_id tinyint not null,
  no_w_id smallint not null,
  constraint new_orders_pk primary key (no_w_id, no_d_id, no_o_id)
);
go

create table orders (
  o_id int not null, 
  o_d_id tinyint not null, 
  o_w_id smallint not null,
  o_c_id int,
  o_entry_d datetime2,
  o_carrier_id tinyint,
  o_ol_cnt tinyint, 
  o_all_local tinyint,
  constraint orders_pk primary key (o_w_id, o_d_id, o_id) 
);
go

create table order_line ( 
  ol_o_id int not null, 
  ol_d_id tinyint not null,
  ol_w_id smallint not null,
  ol_number tinyint not null,
  ol_i_id int, 
  ol_supply_w_id smallint,
  ol_delivery_d datetime2, 
  ol_quantity tinyint, 
  ol_amount decimal(6,2), 
  ol_dist_info nchar(24),
  constraint order_line_pk primary key(ol_w_id, ol_d_id, ol_o_id, ol_number)
);
go


create table item (
  i_id int not null, 
  i_im_id int, 
  i_name nvarchar(24), 
  i_price decimal(5,2), 
  i_data nvarchar(50),
  constraint item_pk primary key (i_id)
);
go

create table stock (
  s_i_id int not null, 
  s_w_id smallint not null, 
  s_quantity smallint, 
  s_dist_01 nchar(24), 
  s_dist_02 nchar(24),
  s_dist_03 nchar(24),
  s_dist_04 nchar(24), 
  s_dist_05 nchar(24), 
  s_dist_06 nchar(24), 
  s_dist_07 nchar(24), 
  s_dist_08 nchar(24), 
  s_dist_09 nchar(24), 
  s_dist_10 nchar(24), 
  s_ytd decimal(8,0), 
  s_order_cnt smallint, 
  s_remote_cnt smallint,
  s_data nvarchar(50),
  constraint stock_pk primary key (s_w_id, s_i_id)
);
go
