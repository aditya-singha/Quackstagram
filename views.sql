CREATE VIEW MostActiveCommenters AS
SELECT username, COUNT(*) AS commentCount
FROM COMMENT
GROUP BY username
HAVING COUNT(*) > 2
ORDER BY commentCount DESC;

CREATE VIEW UserEngagementScores AS
SELECT u.username, 
       COUNT(DISTINCT l.imagePath) AS likeCount,
       COUNT(DISTINCT c.commentId) AS commentCount,
       (COUNT(DISTINCT l.imagePath) + (COUNT(DISTINCT c.commentId) * 2)) AS engagementScore
FROM USER u
LEFT JOIN `LIKE` l ON u.username = l.username
LEFT JOIN COMMENT c ON u.username = c.username
GROUP BY u.username
HAVING engagementScore > 0
ORDER BY engagementScore DESC;

CREATE VIEW TopLikedPictures AS
SELECT p.imagePath, p.caption, p.username, l.likeCount
FROM PICTURE p
JOIN (
    SELECT imagePath, COUNT(*) AS likeCount
    FROM `LIKE`
    GROUP BY imagePath
    HAVING COUNT(*) > 2
) AS l ON p.imagePath = l.imagePath
ORDER BY likeCount DESC;

CREATE VIEW RecentPopularContent AS
SELECT p.imagePath, p.caption, p.username, p.createdAt,
       COUNT(DISTINCT l.username) AS likeCount,
       COUNT(DISTINCT c.commentId) AS commentCount
FROM PICTURE p
LEFT JOIN `LIKE` l ON p.imagePath = l.imagePath
LEFT JOIN COMMENT c ON p.imagePath = c.imagePath
WHERE p.createdAt > DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY p.imagePath, p.caption, p.username, p.createdAt
HAVING likeCount > 0 OR commentCount > 0
ORDER BY p.createdAt DESC;

CREATE VIEW DailyRegistrations AS
SELECT DATE(createdAt) AS registerDate, COUNT(*) AS userCount
FROM USER
GROUP BY DATE(createdAt)
HAVING COUNT(*) > 1
ORDER BY registerDate DESC;

CREATE VIEW NotificationTrends AS
SELECT DATE(timestamp) AS notificationDate,
       sourceType,
       COUNT(*) AS notificationCount,
       COUNT(DISTINCT username) AS affectedUsers
FROM NOTIFICATION
GROUP BY DATE(timestamp), sourceType
HAVING COUNT(*) > 0
ORDER BY notificationDate DESC, notificationCount DESC;

CREATE INDEX idx_like_imagePath ON `LIKE`(imagePath);

CREATE INDEX idx_comment_username ON COMMENT(username);

CREATE INDEX idx_picture_createdAt ON PICTURE(createdAt);

CREATE INDEX idx_notification_username_type ON NOTIFICATION(username, sourceType);

CREATE INDEX idx_follow_follower ON FOLLOW(follower);