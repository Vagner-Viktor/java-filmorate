MERGE INTO "genres" VALUES (1, 'Комедия');
MERGE INTO "genres" VALUES (2, 'Драма');
MERGE INTO "genres" VALUES (3, 'Мультфильм');
MERGE INTO "genres" VALUES (4, 'Триллер');
MERGE INTO "genres" VALUES (5, 'Документальный');
MERGE INTO "genres" VALUES (6, 'Боевик');

MERGE INTO "mpas" VALUES (1, 'G');
MERGE INTO "mpas" VALUES (2, 'PG');
MERGE INTO "mpas" VALUES (3, 'PG-13');
MERGE INTO "mpas" VALUES (4, 'R');
MERGE INTO "mpas" VALUES (5, 'NC-17');

MERGE INTO "friendship_status" VALUES (1, 'CONFIRMED');
MERGE INTO "friendship_status" VALUES (2, 'UNCONFIRMED');