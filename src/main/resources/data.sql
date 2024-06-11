merge into "genres" VALUES (1, 'Комедия');
merge into "genres" VALUES (2, 'Драма');
merge into "genres" VALUES (3, 'Мультфильм');
merge into "genres" VALUES (4, 'Триллер');
merge into "genres" VALUES (5, 'Документальный');
merge into "genres" VALUES (6, 'Боевик');

merge into "mpas" VALUES (1, 'G');
merge into "mpas" VALUES (2, 'PG');
merge into "mpas" VALUES (3, 'PG-13');
merge into "mpas" VALUES (4, 'R');
merge into "mpas" VALUES (5, 'NC-17');

merge into "friendship_status" VALUES (1, 'CONFIRMED');
merge into "friendship_status" VALUES (2, 'UNCONFIRMED');

merge into "usabilitys" VALUES (1, 'USEFUL', 1);
merge into "usabilitys" VALUES (2, 'USELESS', -1);

merge into "event_types" VALUES (1, 'LIKE');
merge into "event_types" VALUES (2, 'REVIEW');
merge into "event_types" VALUES (3, 'FRIEND');

merge into "operation_types" VALUES (1, 'REMOVE');
merge into "operation_types" VALUES (2, 'ADD');
merge into "operation_types" VALUES (3, 'UPDATE');