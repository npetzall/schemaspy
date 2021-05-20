create database compfk in rootdbs;
database compfk;

create table parent(
  id int,
  firstname char(32),
  lastname char(32),
  age smallint,
  weight smallint,
  height SMALLINT
);

create table child(
   id int,
   firstname char(32),
   lastname char(32),
   p1_firstname char(32) NOT NULL,
   p1_lastname char(32) NOT NULL,
   p2_firstname char(32) NULL,
   p2_lastname char(32) NULL,
   age smallint,
   weight smallint,
   height SMALLINT
);

create index parent_age on parent
    (age) using btree ;

create index child_age on child
    (age) using btree ;

alter table parent add constraint primary key (id)
    constraint pk_parent ;

alter table parent add constraint unique (firstname, lastname)
    constraint uq_firstlastname ;

alter table child add constraint primary key (id)
    constraint pk_child ;

alter table child add constraint foreign key (p1_firstname, p1_lastname)
    references parent(firstname, lastname)
    constraint fk_primary_parent ;

alter table child add constraint foreign key (p2_firstname, p2_lastname)
    references parent(firstname, lastname)
    constraint fk_secondary_parent ;