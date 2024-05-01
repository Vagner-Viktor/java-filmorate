# java-filmorate
**Filmorate project** - Educational project to create a service for rating films.

## Database structure
![Database structure](dbdiagram.svg)

### Examples of SQL queries

Films genre:
```SQL
SELECT 
	f.name,
	g.gerne
FROM films AS f
LEFT JOIN films_gerne AS fg ON fg.film_id = f.id
LEFT JOIN gerne AS g ON fg.gerne_id = g.id;
```

Top list of best films:
```SQL
SELECT 
	f.name AS name,
	COUNT(l.film_id) AS count
FROM films AS f
LEFT JOIN likes AS l ON l.film_id = f.id
GROUP BY name
ORDER BY count DESC;
```

Number of user friends:
```SQL
SELECT 
	u.username AS name,
	COUNT(f.friend_id) AS count
FROM users AS u
LEFT JOIN friends AS f ON f.user_id = u.id
GROUP BY name
ORDER BY count DESC;
```

Common friends of users:
```SQL
SELECT u.username
FROM users AS u
WHERE u.id IN (
	SELECT friends_of_first.friend
	FROM (
		SELECT friend_id AS friend FROM friends WHERE user_id = 1
		UNION 
		SELECT user_id AS friend FROM friends WHERE friend_id = 1
		) AS friends_of_first
	JOIN (
		SELECT friend_id AS friend FROM friends WHERE user_id = 2
		union 
		SELECT user_id AS friend FROM friends WHERE friend_id = 2
		) AS friends_of_second
	ON friends_of_first.friend = friends_of_second.friend
);
```