create database informix_test;

{ TABLE "informix".company row size = 624 number of columns = 22 index size = 15 }

create table "informix".company
(
    cmpy_code nchar(2),
    name_text nvarchar(40),
    addr1_text nvarchar(60),
    addr2_text nvarchar(60),
    city_text nvarchar(30),
    state_code nchar(6),
    post_code nchar(10),
    country_code nchar(3),
    language_code nchar(3),
    fax_text nvarchar(20),
    tax_text nvarchar(30),
    telex_text nvarchar(30),
    com1_text nvarchar(50),
    com2_text nvarchar(50),
    tele_text nvarchar(20),
    mobile_phone char(20),
    email varchar(128),
    curr_code nchar(3),
    module_text nvarchar(26),
    vat_code nchar(14),
    vat_div_code nchar(3),
    legal_creation_date date
);

revoke all on "informix".company from "public" as "informix";


create index "informix".d01_company on "informix".company (country_code) using btree ;
create unique index "informix".u_company on "informix".company (cmpy_code) using btree ;
alter table "informix".company add constraint primary key (cmpy_code) constraint "informix".pk_company  ;


{ TABLE "informix".coa row size = 4276 number of columns = 17 index size = 46 }

create table "informix".coa
(
    cmpy_code nchar(2),
    acct_code nchar(18),
    desc_text nvarchar(90,50),
    start_year_num smallint,
    start_period_num smallint,
    end_year_num smallint,
    end_period_num smallint,
    group_code nchar(7),
    analy_req_flag nchar(1),
    analy_prompt_text nvarchar(20),
    qty_flag nchar(1),
    uom_code nchar(4),
    type_ind nchar(1),
    tax_code nchar(3),
    is_nominalcode "informix".boolean,
    parentid nchar(18),
    analy_class "informix".bson
);

revoke all on "informix".coa from "public" as "informix";


create index "informix".d01_coa on "informix".coa (group_code, cmpy_code) using btree ;
create index "informix".d_coa_01 on "informix".coa (cmpy_code) using btree ;
create unique index "informix".u_coa on "informix".coa (acct_code, cmpy_code) using btree ;
alter table "informix".coa add constraint primary key (acct_code, cmpy_code) constraint "informix".pk_coa  ;
alter table "informix".coa add constraint (foreign key (cmpy_code) references "informix".company  constraint "informix".fk_coa_company);



{ TABLE "informix".journal row size = 47 number of columns = 4 index size = 17 }

create table "informix".journal
(
    cmpy_code nchar(2),
    jour_code nchar(3),
    desc_text nvarchar(40),
    gl_flag nchar(1)
);

revoke all on "informix".journal from "public" as "informix";


create index "informix".fk2_journal on "informix".journal (cmpy_code) using btree ;
create unique index "informix".u_journal on "informix".journal (jour_code,cmpy_code) using btree ;
alter table "informix".journal add constraint primary key (jour_code, cmpy_code) constraint "informix".pk_journal  ;
alter table "informix".journal add constraint (foreign key (cmpy_code) references "informix".company  constraint "informix".fk_journal_company);


{ TABLE "informix".used_currency row size = 13 number of columns = 4 index size = 25 }

create table "informix".used_currency
(
    cmpy_code nchar(2),
    currency_code nchar(3),
    start_date date,
    end_date date
);

revoke all on "informix".used_currency from "public" as "informix";
create index "informix".d_currency on "informix".used_currency (currency_code) using btree ;
create index "informix".fk_used_currency_cmpy on "informix".used_currency (cmpy_code) using btree ;
create unique index "informix".pk_used_currency on "informix".used_currency (currency_code,cmpy_code) using btree ;
alter table "informix".used_currency add constraint primary key (currency_code,cmpy_code) constraint "informix".pk_used_currency ;
alter table "informix".used_currency add constraint (foreign key (cmpy_code) references "informix".company  constraint "informix".fk_used_currency_company);

{ TABLE "informix".glparms row size = 349 number of columns = 44 index size = 168 }

create table "informix".glparms
(
    cmpy_code nchar(2),
    key_code nchar(1),
    next_jour_num integer,
    next_seq_num integer,
    next_post_num integer,
    next_load_num integer,
    next_consol_num integer,
    gj_code nchar(3),
    last_depr_date date,
    rj_code nchar(3),
    cb_code nchar(3),
    last_post_date date,
    last_update_date date,
    last_close_date date,
    last_del_date date,
    cash_book_flag nchar(1),
    post_susp_flag nchar(1),
    susp_acct_code nchar(18),
    exch_acct_code nchar(18),
    unexch_acct_code nchar(18),
    clear_acct_code nchar(18),
    post_total_amt money(17,2),
    control_tot_flag nchar(1),
    use_clear_flag nchar(1),
    use_currency_flag nchar(1),
    base_currency_code nchar(3),
    budg1_text nvarchar(30),
    budg1_close_flag nchar(1),
    budg2_text nvarchar(30),
    budg2_close_flag nchar(1),
    budg3_text nvarchar(30),
    budg3_close_flag nchar(1),
    budg4_text nvarchar(30),
    budg4_close_flag nchar(1),
    budg5_text nvarchar(30),
    budg5_close_flag nchar(1),
    budg6_text nvarchar(30),
    budg6_close_flag nchar(1),
    style_ind smallint,
    site_code nchar(3),
    acrl_code nchar(3),
    rev_acrl_code nchar(3),
    last_acrl_yr_num smallint,
    last_acrl_per_num smallint
);

revoke all on "informix".glparms from "public" as "informix";


create unique index "informix".u_glparms on "informix".glparms (key_code,cmpy_code) using btree ;
alter table "informix".glparms add constraint primary key (key_code, cmpy_code) constraint "informix".pk_glparms  ;
alter table "informix".glparms add constraint (foreign key (exch_acct_code, cmpy_code) references "informix".coa  constraint "informix".fk_glparms_coa_exch);
alter table "informix".glparms add constraint (foreign key (unexch_acct_code, cmpy_code) references "informix".coa  constraint "informix".fk_glparms_coa_unexch);
alter table "informix".glparms add constraint (foreign key (susp_acct_code, cmpy_code) references "informix".coa  constraint "informix".fk_glparms_coa_susp);
alter table "informix".glparms add constraint (foreign key (clear_acct_code, cmpy_code) references "informix".coa  constraint "informix".fk_glparms_coa_clear);
alter table "informix".glparms add constraint (foreign key (gj_code, cmpy_code) references "informix".journal  constraint "informix".fk_glparms_journal_gj);
alter table "informix".glparms add constraint (foreign key (rj_code, cmpy_code) references "informix".journal  constraint "informix".fk_glparms_journal_rj);
alter table "informix".glparms add constraint (foreign key (cb_code, cmpy_code) references "informix".journal  constraint "informix".fk_glparms_journal_cb);
alter table "informix".glparms add constraint (foreign key (acrl_code, cmpy_code) references "informix".journal  constraint "informix".fk_glparms_journal_acrl);
alter table "informix".glparms add constraint (foreign key (rev_acrl_code, cmpy_code) references "informix".journal  constraint "informix".fk_glparms_journal_rev_acrl);
alter table "informix".glparms add constraint (foreign key (base_currency_code, cmpy_code) references "informix".used_currency  constraint "informix".fk_glparms_used_currency);