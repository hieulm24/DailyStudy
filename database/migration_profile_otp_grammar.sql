-- ============================================================================
-- SQL MIGRATION: BỔ SUNG CÁC TRƯỜNG & BẢNG MỚI (PROFILE, OTP, GRAMMAR AI)
-- ============================================================================

-- 1. Bổ sung các trường quản lý thông tin cá nhân vào bảng users
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'phone_number')
BEGIN
    ALTER TABLE users ADD phone_number NVARCHAR(20) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'bio')
BEGIN
    ALTER TABLE users ADD bio NVARCHAR(500) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'target_score')
BEGIN
    ALTER TABLE users ADD target_score INT DEFAULT 650 NULL;
END
GO

-- 2. Tạo bảng lưu trữ mã xác thực OTP gửi qua email (password_reset_otps)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'password_reset_otps')
BEGIN
    CREATE TABLE password_reset_otps (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        email NVARCHAR(255) NOT NULL,
        otp_code NVARCHAR(10) NOT NULL,
        expiry_time DATETIME2 NOT NULL,
        is_used BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );

    CREATE INDEX idx_otps_email_expiry ON password_reset_otps(email, expiry_time, is_used);
END
GO

-- 3. Tạo bảng lưu lịch sử làm bài tập ngữ pháp AI (grammar_exercise_histories)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grammar_exercise_histories')
BEGIN
    CREATE TABLE grammar_exercise_histories (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        grammar_topic_id BIGINT NOT NULL,
        topic_name NVARCHAR(255) NOT NULL,
        title NVARCHAR(255) NOT NULL,
        exercise_type NVARCHAR(50) NOT NULL,
        level NVARCHAR(20) NULL,
        total_questions INT NOT NULL,
        correct_count INT NOT NULL,
        score INT NOT NULL,
        time_spent_seconds INT NOT NULL DEFAULT 0,
        questions_json NVARCHAR(MAX) NOT NULL,
        user_answers_json NVARCHAR(MAX) NOT NULL,
        completed_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_exercise_grammar_topic FOREIGN KEY (grammar_topic_id) REFERENCES grammar_topics(id) ON DELETE CASCADE
    );

    CREATE INDEX idx_grammar_histories_topic ON grammar_exercise_histories(grammar_topic_id, completed_at);
END
GO
