insert into customers(username, password) values
	('admin123', 'to_be_encoded'),
	('user123', 'to_be_encoded');

insert into roles(role_name, description, user_id) values
	('ROLE_ADMIN', 'Cant view account endpoint', 1),
	('ROLE_USER', 'Cant view cards endpoint', 2);