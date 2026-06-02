create database shop_db;

create table products(
    id serial primary key,
    name varchar(255) not null ,
    price decimal(10,2) not null
);

create table customer(
    id serial primary key,
    name varchar(255) not null,
    email varchar(255) unique
);

create table "order"(
    id serial primary key,
    customer_id integer references Customer (id),
    product_id integer references Products (id),
    order_date date not null,
    total_amount decimal(10, 2) not null
)
