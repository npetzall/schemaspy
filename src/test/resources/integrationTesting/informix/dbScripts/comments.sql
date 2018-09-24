create database test in rootdbs;
database test;

create table test(
  id int,
  firstname char(32),
  lastname char(32),
  age smallint,
  weight smallint,
  height SMALLINT
);

create index test_index on test
    (firstname,lastname,age) using btree ;

alter table test add constraint primary key (id)
    constraint pk_id ;

--drop table if exists qx_table_comments ;
create table qx_table_comments (
tabid INTEGER,
comments lvarchar(4096)
) ;
create index fk_systables_qx_table_comm on qx_table_comments(tabid) ;

--drop table if exists qx_column_comments ;
create table qx_column_comments (
tabid INTEGER,
colno INTEGER,
comments lvarchar(4096)
) ;
create index fk_syscolumns_qx_column_comm on qx_column_comments(tabid,colno) ;

insert into qx_table_comments (tabid, comments) values ((select tabid from systables where tabname == 'test'), 'table used for test') ;
insert into qx_column_comments (tabid, colno, comments) VALUES ((select tabid from systables where tabname == 'test'),(select colno from syscolumns where colname = 'firstname' and tabid = (select tabid from systables where tabname == 'test')), 'Firstname column') ;
