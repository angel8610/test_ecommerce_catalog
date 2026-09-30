insert into users(username, password) values
	('admin123', '$2a$12$qpwk9Q/5a6feJGFlo.Vwje6Dx5CU1Z3FPYBnKXfUgEfVaF90cAB7.'),
	('user123', '$2a$12$qZI7g0QuT5O5kEpkpulD6uEkplRtXs/j3r78FuRwjR0yJGSf/gKES');

insert into roles(role_name, description, user_id) values
	('ROLE_ADMIN', 'Cant view menu', 1),
	('ROLE_USER', 'Cant view menu', 2);