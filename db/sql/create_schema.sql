create table users(
	user_id serial primary key,
	username varchar(50) not null unique,
	password varchar(500) not null
);

create table roles(
	role_id serial primary key,
	role_name varchar(50),
	description varchar(100),
	user_id bigint not null,
	constraint fk_user foreign key(user_id) references users(user_id),
	CONSTRAINT uk_user_role UNIQUE (user_id, role_name)
);