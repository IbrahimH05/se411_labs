INSERT INTO course(id, code, name, description, credits) VALUES (1, 'SE411', 'Software Construction', 'Building reliable software: generics, patterns, concurrency and web APIs', 3);
INSERT INTO course(id, code, name, description, credits) VALUES (2, 'SE311', 'Software Design', 'Design principles, UML modelling and architectural patterns', 3);
INSERT INTO course(id, code, name, description, credits) VALUES (3, 'CS201', 'Data Structures', 'Lists, trees, graphs, hashing and their complexity', 4);
INSERT INTO course(id, code, name, description, credits) VALUES (4, 'CS340', 'Operating Systems', 'Processes, scheduling, memory management and concurrency', 3);
INSERT INTO course(id, code, name, description, credits) VALUES (5, 'CS370', 'Databases', 'Relational modelling, SQL, transactions and indexing', 3);
INSERT INTO course(id, code, name, description, credits) VALUES (6, 'MATH151', 'Discrete Mathematics', 'Logic, proof, combinatorics and graph theory', 4);

-- The rows above set their ids explicitly, which does NOT advance H2's IDENTITY
-- sequence. Without this the next auto-assigned id would be 1 and collide with
-- SE411 on the primary key.
ALTER TABLE course ALTER COLUMN id RESTART WITH 7;
