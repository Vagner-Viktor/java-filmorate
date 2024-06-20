# java-filmorate
**Filmorate project** - Educational project to create a service for rating films.

## Database structure
![Database structure](dbdiagram.svg)

The database contains information about films (**table films**) and users (**table users**).
Each film can have several genres (**table genre**), rating of the Motion Picture Association (abbreviated as MPA). This rating determines the age limit for the film. The user can like each movie (**likes table**).

A user can send a friend request to another user. Status for a “friendship” (**table friendship_status**) connection between two users:
* unconfirmed - when one user sent a request to add another user as a friend,
* confirmed - when the second user agreed to be added.

### add-marks refactoring
[Postman tests](postman/add-marks/add-marks.json) 
<br/>P.S. Not all tests pass all the checks. Watch the Response Body and check it yourself!

API changes:

| № | API before                      | API after                                   | Params                                                                                             |
|---|---------------------------------|---------------------------------------------|----------------------------------------------------------------------------------------------------|
| 1 | ```/films/{id}/like/{userId}``` | ```/films/{id}/like/{userId}?mark={mark}``` | ```id, userId``` — positive integer (User ID), <br/>```mark``` — positive fractional from 0 to 10. |

The changes affected the logic of the following APIs:
1. ```/films/popular```
2. ```/films/search```
3. ```/films/director/{id}```
4. ```/films/common```
5. ```/users/{id}/recommendations```

After replacing likes with ratings, the rating system has changed. Now the rating is calculated as an arithmetic mean. A score from 1 to 5 is considered negative, a score from 6 to 10 is considered positive.

Now the recommendation algorithm searches for similar ratings among other users, and recommends movies only with a positive average rating.

In the answers, where sorting by the number of likes used to take place, sorting by average rating is now taking place

### Examples of SQL queries

Films genre:
```SQL
SELECT 
	f."name",
	g."genre"
FROM "films" AS f
LEFT JOIN "films_genre" AS fg ON fg."film_id" = f."film_id"
LEFT JOIN "genres" AS g ON fg."genre_id" = g."genre_id";
```

Top list of best films:
```SQL
SELECT 
	f."name" AS name,
	COUNT(l."film_id") AS count
FROM "films" AS f
LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
GROUP BY name
ORDER BY count DESC;
```

Number of user friends:
```SQL
SELECT 
	u."username" AS name,
	COUNT(f."friend_id") AS count
FROM "users" AS u
LEFT JOIN "friends" AS f ON f."user_id" = u."user_id"
GROUP BY name
ORDER BY count DESC;
```

Common friends of users:
```SQL
SELECT *
FROM "users" AS u
WHERE u."user_id" IN (
    SELECT friends_of_first.friend
    FROM (
        SELECT "friend_id" AS friend FROM "friends" WHERE "user_id" = 1
        UNION
        SELECT "user_id" AS friend FROM "friends" WHERE "friend_id" = 1
        ) AS friends_of_first
    JOIN (
        SELECT "friend_id" AS friend FROM "friends" WHERE "user_id" = 2
        UNION
        SELECT "user_id" AS friend FROM "friends" WHERE "friend_id" = 2
        ) AS friends_of_second
    ON friends_of_first.friend = friends_of_second.friend
);
```