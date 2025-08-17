
DELIMITER //

-- Log user activity
CREATE PROCEDURE LogUserActivity(IN user_name VARCHAR(250), IN action_type VARCHAR(50))
BEGIN
    INSERT INTO NOTIFICATION(username, text, sourceType, sourceId)
    VALUES (user_name, CONCAT('User performed: ', action_type), 'follow', '0');
END//

--  Count user pictures
CREATE FUNCTION CountUserPictures(user_name VARCHAR(250)) 
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE pic_count INT;
    SELECT COUNT(*) INTO pic_count FROM PICTURE WHERE username = user_name;
    RETURN pic_count;
END//

-- Prevent empty comments
CREATE TRIGGER before_comment_insert
BEFORE INSERT ON COMMENT
FOR EACH ROW
BEGIN
    IF NEW.text = '' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Comment cannot be empty';
    END IF;
END//

-- Welcome new users
CREATE TRIGGER after_user_insert
AFTER INSERT ON USER
FOR EACH ROW
BEGIN
    CALL LogUserActivity(NEW.username, 'account creation');
    INSERT INTO NOTIFICATION(username, text, sourceType, sourceId)
    VALUES (NEW.username, 'Welcome to our platform!', 'follow', '0');
END//

DELIMITER ;