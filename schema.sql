-- USER table
CREATE TABLE USER (
    username VARCHAR(250) PRIMARY KEY, -- Unique user ID
    bio TEXT NOT NULL,
    password VARCHAR(250) NOT NULL,
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP -- Account creation time
);

-- ADMIN_USER (inherits from USER)
CREATE TABLE ADMIN_USER (
    username VARCHAR(250) PRIMARY KEY, -- Must exist in USER
    FOREIGN KEY (username) REFERENCES USER(username)
);

-- REGULAR_USER (inherits from USER)
CREATE TABLE REGULAR_USER (
    username VARCHAR(250) PRIMARY KEY, -- Must exist in USER
    FOREIGN KEY (username) REFERENCES USER(username)
);

-- PICTURE table
CREATE TABLE PICTURE (
    imagePath VARCHAR(250) PRIMARY KEY, -- Unique path to image
    caption TEXT NOT NULL,
    username VARCHAR(250) NOT NULL, -- Owner
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Upload time
    FOREIGN KEY (username) REFERENCES USER(username)
);

-- COMMENT table
CREATE TABLE COMMENT (
    commentId INT AUTO_INCREMENT PRIMARY KEY, -- Auto-generated comment ID
    text TEXT NOT NULL, -- No default, comment required
    username VARCHAR(250) NOT NULL, -- Commenter
    imagePath VARCHAR(250) NOT NULL, -- Associated picture
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Comment time
    FOREIGN KEY (username) REFERENCES USER(username),
    FOREIGN KEY (imagePath) REFERENCES PICTURE(imagePath)
);

-- LIKE table
CREATE TABLE `LIKE` (
    username VARCHAR(250), -- Liker
    imagePath VARCHAR(250), -- Liked picture
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Like time
    PRIMARY KEY (username, imagePath), -- One like per user per image
    FOREIGN KEY (username) REFERENCES USER(username),
    FOREIGN KEY (imagePath) REFERENCES PICTURE(imagePath)
);

-- FOLLOW table
CREATE TABLE FOLLOW (
    follower VARCHAR(250), -- User who follows
    followed VARCHAR(250), -- User being followed
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Follow time
    PRIMARY KEY (follower, followed), -- One follow per pair
    FOREIGN KEY (follower) REFERENCES USER(username),
    FOREIGN KEY (followed) REFERENCES USER(username),
    CHECK (follower <> followed) -- Cannot follow self
);

-- NOTIFICATION table
CREATE TABLE NOTIFICATION (
    notificationId INT AUTO_INCREMENT PRIMARY KEY, -- Auto-generated notification ID
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- When notified
    text TEXT NOT NULL,
    username VARCHAR(250) NOT NULL, -- Receiver
    sourceType VARCHAR(250) NOT NULL, -- Event type
    sourceId VARCHAR(250) NOT NULL, -- ID of related event
    FOREIGN KEY (username) REFERENCES USER(username),
    CHECK (sourceType IN ('like', 'comment', 'follow')) -- Event must be one of allowed
);




-- USERS
INSERT INTO USER (username, bio, password)
VALUES
('admin_anna', 'I manage ducks.', 'pass123'),
('admin_bob', 'Keeping it clean.', 'pass456'),
('john_doe', 'Just another duck enthusiast.', 'secret1'),
('jane_doe', 'Quacking through life.', 'secret2'),
('quackfan', 'Love everything duck-related!', 'secret3');

-- ADMIN_USER
INSERT INTO ADMIN_USER (username)
VALUES ('admin_anna'), ('admin_bob');

-- REGULAR_USER
INSERT INTO REGULAR_USER (username)
VALUES ('john_doe'), ('jane_doe'), ('quackfan');

-- PICTURES
INSERT INTO PICTURE (imagePath, caption, username)
VALUES
('img001.jpg', 'Sunset in Bali', 'jane_doe'),
('img002.jpg', 'Duck in the pond', 'quackfan'),
('img003.jpg', 'Snowy mountain', 'john_doe'),
('img004.jpg', 'Night sky', 'john_doe');

-- COMMENTS
INSERT INTO COMMENT (text, username, imagePath)
VALUES
('Beautiful!', 'john_doe', 'img001.jpg'),
('Love the colors!', 'quackfan', 'img001.jpg'),
('Nice shot!', 'jane_doe', 'img002.jpg'),
('Chilling view.', 'jane_doe', 'img003.jpg'),
('Awesome stars!', 'quackfan', 'img004.jpg');

-- LIKES
INSERT INTO `LIKE` (username, imagePath)
VALUES
('john_doe', 'img001.jpg'),
('jane_doe', 'img002.jpg'),
('quackfan', 'img003.jpg'),
('jane_doe', 'img003.jpg'),
('john_doe', 'img004.jpg'),
('quackfan', 'img004.jpg');

-- FOLLOWS
INSERT INTO FOLLOW (follower, followed)
VALUES
('john_doe', 'jane_doe'),
('jane_doe', 'quackfan'),
('quackfan', 'john_doe'),
('john_doe', 'quackfan');

-- NOTIFICATIONS
INSERT INTO NOTIFICATION (text, username, sourceType, sourceId)
VALUES
('john_doe liked your post.', 'jane_doe', 'like', 'img001.jpg'),
('jane_doe commented on your post.', 'quackfan', 'comment', '3'),
('quackfan followed you.', 'john_doe', 'follow', 'quackfan'),
('quackfan liked your photo.', 'john_doe', 'like', 'img004.jpg');
