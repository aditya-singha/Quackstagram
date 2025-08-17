use quack;

-- 1. List all users who have more than X followers where X can be any integer value.
SELECT followed AS username, COUNT(*) AS follower_count
FROM FOLLOW
GROUP BY followed
HAVING COUNT(*) > ?  
ORDER BY follower_count DESC;

-- 2. Show the total number of posts made by each user.
SELECT username, COUNT(*) AS post_count
FROM PICTURE
GROUP BY username
ORDER BY post_count DESC;

-- 3. Find all comments made on a particular user's post
SELECT c.*
FROM COMMENT c
JOIN PICTURE p ON c.imagePath = p.imagePath
WHERE p.username = ?
ORDER BY c.createdAt DESC;

-- 4. Display the top X most liked posts
SELECT p.*, COUNT(l.username) AS like_count
FROM PICTURE p
LEFT JOIN `LIKE` l ON p.imagePath = l.imagePath
GROUP BY p.imagePath
ORDER BY like_count DESC
LIMIT ?; 

-- 5. Count the number of posts each user has liked
SELECT username, COUNT(DISTINCT imagePath) AS liked_posts_count
FROM `LIKE`
GROUP BY username
ORDER BY liked_posts_count DESC;

-- 6. List all users who haven't made a post yet
SELECT u.username
FROM USER u
LEFT JOIN PICTURE p ON u.username = p.username
WHERE p.imagePath IS NULL;

-- 7. List users who follow each other
SELECT f1.follower AS user1, f1.followed AS user2
FROM FOLLOW f1
JOIN FOLLOW f2 ON f1.follower = f2.followed AND f1.followed = f2.follower
WHERE f1.follower < f1.followed; 

-- 8. Show the user with the highest number of posts
SELECT username, COUNT(*) AS post_count
FROM PICTURE
GROUP BY username
ORDER BY post_count DESC
LIMIT 1;

-- 9. List the top X users with the most followers
SELECT followed AS username, COUNT(*) AS follower_count
FROM FOLLOW
GROUP BY followed
ORDER BY follower_count DESC
LIMIT ?;  

-- 10. Find posts that have been liked by all users
SELECT l.imagePath
FROM `LIKE` l
GROUP BY l.imagePath
HAVING COUNT(DISTINCT l.username) = (SELECT COUNT(*) FROM USER);

-- 11. Display the most active user
SELECT u.username,
       (COUNT(DISTINCT p.imagePath) + 
       (COUNT(DISTINCT c.commentId) * 2) + 
       (COUNT(DISTINCT l.imagePath) * 0.5) AS activity_score
FROM USER u
LEFT JOIN PICTURE p ON u.username = p.username
LEFT JOIN COMMENT c ON u.username = c.username
LEFT JOIN `LIKE` l ON u.username = l.username
GROUP BY u.username
ORDER BY activity_score DESC
LIMIT 1;

-- 12. Find the average number of likes per post for each user.
SELECT p.username, 
       COUNT(DISTINCT p.imagePath) AS post_count,
       COUNT(l.username) AS total_likes,
       COUNT(l.username) / COUNT(DISTINCT p.imagePath) AS avg_likes_per_post
FROM PICTURE p
LEFT JOIN `LIKE` l ON p.imagePath = l.imagePath
GROUP BY p.username
ORDER BY avg_likes_per_post DESC;

-- 13. Show posts that have more comments than likes
SELECT p.*, 
       COUNT(DISTINCT c.commentId) AS comment_count,
       COUNT(DISTINCT l.username) AS like_count
FROM PICTURE p
LEFT JOIN COMMENT c ON p.imagePath = c.imagePath
LEFT JOIN `LIKE` l ON p.imagePath = l.imagePath
GROUP BY p.imagePath
HAVING comment_count > like_count;

-- 14. List the users who have liked every post of a specific user.
SELECT l.username
FROM `LIKE` l
JOIN PICTURE p ON l.imagePath = p.imagePath
WHERE p.username = ? 
GROUP BY l.username
HAVING COUNT(DISTINCT l.imagePath) = (
    SELECT COUNT(*) 
    FROM PICTURE 
    WHERE username = ?  
);

-- 15. Display the most popular post of each user
WITH PostPopularity AS (
    SELECT p.username, p.imagePath, COUNT(l.username) AS like_count,
           RANK() OVER (PARTITION BY p.username ORDER BY COUNT(l.username) DESC) AS rank
    FROM PICTURE p
    LEFT JOIN `LIKE` l ON p.imagePath = l.imagePath
    GROUP BY p.username, p.imagePath
)
SELECT username, imagePath, like_count
FROM PostPopularity
WHERE rank = 1;

-- 16. Find the user(s) with the highest ratio of followers to following.
WITH UserStats AS (
    SELECT u.username,
           COUNT(DISTINCT f1.followed) AS following_count,
           COUNT(DISTINCT f2.follower) AS follower_count,
           CASE WHEN COUNT(DISTINCT f1.followed) = 0 THEN NULL
                ELSE COUNT(DISTINCT f2.follower) / COUNT(DISTINCT f1.followed) 
           END AS ratio
    FROM USER u
    LEFT JOIN FOLLOW f1 ON u.username = f1.follower
    LEFT JOIN FOLLOW f2 ON u.username = f2.followed
    GROUP BY u.username
)
SELECT username, follower_count, following_count, ratio
FROM UserStats
WHERE ratio IS NOT NULL
ORDER BY ratio DESC
LIMIT 1;

-- 17. Show the month with the highest number of posts made
SELECT MONTH(createdAt) AS month, 
       YEAR(createdAt) AS year,
       COUNT(*) AS post_count
FROM PICTURE
GROUP BY YEAR(createdAt), MONTH(createdAt)
ORDER BY post_count DESC
LIMIT 1;

-- 18. Identify users who have not interacted with a specific user's posts
SELECT u.username
FROM USER u
WHERE u.username NOT IN (
    SELECT DISTINCT l.username
    FROM `LIKE` l
    JOIN PICTURE p ON l.imagePath = p.imagePath
    WHERE p.username = ?  
)
AND u.username NOT IN (
    SELECT DISTINCT c.username
    FROM COMMENT c
    JOIN PICTURE p ON c.imagePath = p.imagePath
    WHERE p.username = ? 
)
AND u.username != ?; 

-- 19. Display the user with the greatest increase in followers in the last X days
SELECT followed AS username, COUNT(*) AS new_followers
FROM FOLLOW
WHERE createdAt >= DATE_SUB(CURRENT_DATE(), INTERVAL ? DAY)  -- X days provided
GROUP BY followed
ORDER BY new_followers DESC
LIMIT 1;

-- 20. Find users who are followed by more than X% of the platform users
WITH PlatformStats AS (
    SELECT COUNT(*) AS total_users FROM USER
)
SELECT f.followed AS username, 
       COUNT(*) AS follower_count,
       (COUNT(*) / (SELECT total_users FROM PlatformStats)) * 100 AS percentage
FROM FOLLOW f
GROUP BY f.followed
HAVING percentage > ? 
ORDER BY percentage DESC;