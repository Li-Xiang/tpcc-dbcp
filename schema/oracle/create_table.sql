
create table warehouse (
  w_id smallint not null,
  w_name varchar2(10), 
  w_street_1 varchar2(20), 
  w_street_2 varchar2(20), 
  w_city varchar2(20), 
  w_state char(2), 
  w_zip char(9), 
  w_tax decimal(4,2), 
  w_ytd decimal(12,2),
  constraint warehouse_pk primary key (w_id)
);

create table district (
  d_id number(3,0) not null, 
  d_w_id smallint not null, 
  d_name varchar2(10), 
  d_street_1 varchar2(20), 
  d_street_2 varchar2(20), 
  d_city varchar2(20), 
  d_state char(2), 
  d_zip char(9), 
  d_tax decimal(4,2), 
  d_ytd decimal(12,2), 
  d_next_o_id int,
  constraint district_pk primary key (d_w_id, d_id)
);

create table customer (
  c_id number(10,0) not null, 
  c_d_id number(3,0) not null, 
  c_w_id smallint not null, 
  c_first varchar2(16), 
  c_middle char(2), 
  c_last varchar2(16), 
  c_street_1 varchar2(20), 
  c_street_2 varchar2(20), 
  c_city varchar2(20), 
  c_state char(2), 
  c_zip char(9), 
  c_phone char(16), 
  c_since date, 
  c_credit char(2), 
  c_credit_lim number(24,0), 
  c_discount decimal(4,2), 
  c_balance decimal(12,2), 
  c_ytd_payment decimal(12,2), 
  c_payment_cnt smallint, 
  c_delivery_cnt smallint, 
  c_data clob, 
  constraint customer_pk primary key (c_w_id, c_d_id, c_id)
);
  

create table history (
  h_c_id int, 
  h_c_d_id number(3,0), 
  h_c_w_id smallint,
  h_d_id number(3,0),
  h_w_id smallint,
  h_date date,
  h_amount decimal(6,2), 
  h_data varchar2(24)
);

create table new_orders (
  no_o_id int not null,
  no_d_id number(3,0) not null,
  no_w_id smallint not null,
  constraint new_orders_pk primary key (no_w_id, no_d_id, no_o_id)
);

create table orders (
  o_id int not null, 
  o_d_id number(3,0) not null, 
  o_w_id smallint not null,
  o_c_id int,
  o_entry_d date,
  o_carrier_id number(3,0),
  o_ol_cnt number(3,0), 
  o_all_local number(3,0),
  constraint orders_pk primary key (o_w_id, o_d_id, o_id)
);

create table order_line ( 
  ol_o_id int not null, 
  ol_d_id number(3,0) not null,
  ol_w_id smallint not null,
  ol_number  number(3,0) not null,
  ol_i_id int, 
  ol_supply_w_id smallint,
  ol_delivery_d date, 
  ol_quantity number(3,0), 
  ol_amount decimal(6,2), 
  ol_dist_info char(24),
  constraint order_line_pk primary key (ol_w_id, ol_d_id, ol_o_id, ol_number)
);

create table item (
  i_id int not null, 
  i_im_id int, 
  i_name varchar2(24), 
  i_price decimal(5,2), 
  i_data varchar2(50),
  constraint item_pk primary key (i_id)
);

create table stock(
  s_i_id int not null, 
  s_w_id smallint not null, 
  s_quantity smallint, 
  s_dist_01 char(24), 
  s_dist_02 char(24),
  s_dist_03 char(24),
  s_dist_04 char(24), 
  s_dist_05 char(24), 
  s_dist_06 char(24), 
  s_dist_07 char(24), 
  s_dist_08 char(24), 
  s_dist_09 char(24), 
  s_dist_10 char(24), 
  s_ytd decimal(8,0), 
  s_order_cnt smallint, 
  s_remote_cnt smallint,
  s_data varchar2(50),
  constraint stock_pk primary key (s_w_id, s_i_id)
);


