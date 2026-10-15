-- Module 4: In-App Notifications
-- Author: Anusuya

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_id BIGINT NULL,
    type VARCHAR(25) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    read_at DATETIME NULL,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_notif_request FOREIGN KEY (request_id) REFERENCES emergency_requests (id)
);

CREATE INDEX idx_notif_user_read_created ON notifications (user_id, is_read, created_at);
