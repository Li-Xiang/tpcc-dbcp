create table warehouse (
  w_id smallint not null,
  w_name varchar(10),
  w_street_1 varchar(20),
  w_street_2 varchar(20),
  w_city varchar(20),
  w_state char(2),
  w_zip char(9),
  w_tax decimal(4,2),
  w_ytd decimal(12,2),
  constraint warehouse_pk primary key (w_id)
);

create table customer (
  c_id int not null,
  c_d_id smallint not null,
  c_w_id smallint not null,
  c_first varchar(16),
  c_middle char(2),
  c_last varchar(16),
  c_street_1 varchar(20),
  c_street_2 varchar(20),
  c_city varchar(20),
  c_state char(2),
  c_zip char(9),
  c_phone char(16),
  c_since timestamp,
  c_credit char(2),
  c_credit_lim bigint,
  c_discount decimal(4,2),
  c_balance decimal(12,2),
  c_ytd_payment decimal(12,2),
  c_payment_cnt smallint,
  c_delivery_cnt smallint,
  c_data text,
  constraint customer_pk primary key (c_w_id, c_d_id, c_id),
  constraint fkey_customer_1 foreign key(c_w_id,c_d_id) references district(d_w_id,d_id)
);

create table district (
  d_id smallint not null,
  d_w_id smallint not null,
  d_name varchar(10),
  d_street_1 varchar(20),
  d_street_2 varchar(20),
  d_city varchar(20),
  d_state char(2),
  d_zip char(9),
  d_tax decimal(4,2),
  d_ytd decimal(12,2),
  d_next_o_id int,
  constraint district_pk primary key (d_w_id, d_id),
  constraint fkey_district_1 foreign key(d_w_id) references warehouse(w_id)
);

create table history (
  h_c_id int,
  h_c_d_id smallint,
  h_c_w_id smallint,
  h_d_id smallint,
  h_w_id smallint,
  h_date timestamp,
  h_amount decimal(6,2),
  h_data varchar(24),
  constraint fkey_history_1 foreign key(h_c_w_id,h_c_d_id,h_c_id) references customer(c_w_id,c_d_id,c_id)
);


create table item (
  i_id int not null,
  i_im_id int,
  i_name varchar(24),
  i_price decimal(5,2),
  i_data varchar(50),
  constraint item_pk primary key (i_id)
);

create table new_orders (
  no_o_id int not null,
  no_d_id smallint not null,
  no_w_id smallint not null,
  constraint new_orders_pk primary key (no_w_id, no_d_id, no_o_id),
  constraint fkey_new_orders_1 foreign key(no_w_id,no_d_id,no_o_id) references orders(o_w_id,o_d_id,o_id)
);

create table order_line (
  ol_o_id int not null,
  ol_d_id smallint not null,
  ol_w_id smallint not null,
  ol_number smallint not null,
  ol_i_id int,
  ol_supply_w_id smallint,
  ol_delivery_d timestamp,
  ol_quantity smallint,
  ol_amount decimal(6,2),
  ol_dist_info char(24),
  constraint order_line_pk primary key(ol_w_id, ol_d_id, ol_o_id, ol_number),
  constraint fkey_order_line_1 foreign key(ol_w_id,ol_d_id,ol_o_id) references orders(o_w_id,o_d_id,o_id),
  constraint fkey_order_line_2 foreign key(ol_supply_w_id,ol_i_id) references stock(s_w_id,s_i_id)
);

create table orders (
  o_id int not null,
  o_d_id smallint not null,
  o_w_id smallint not null,
  o_c_id int,
  o_entry_d timestamp,
  o_carrier_id smallint,
  o_ol_cnt smallint,
  o_all_local smallint,
  constraint orders_pk primary key (o_w_id, o_d_id, o_id),
  constraint fkey_orders_1 foreign key(o_w_id,o_d_id,o_c_id) references customer(c_w_id,c_d_id,c_id)
);

create table stock (
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
  s_data varchar(50),
  constraint stock_pk primary key (s_w_id, s_i_id),
  constraint fkey_stock_1 foreign key(s_w_id) references warehouse(w_id),
  constraint fkey_stock_2 foreign key(s_i_id) references item(i_id)
);