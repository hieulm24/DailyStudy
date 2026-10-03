# Database Schema Summary: EnglishLearning (22 Tables)

## Table: `daily_learning_statistics`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `statistic_date` | date(3) | NO |  |  |
| `vocabulary_count` | int(4) | NO |  | ((0)) |
| `grammar_count` | int(4) | NO |  | ((0)) |
| `listening_count` | int(4) | NO |  | ((0)) |
| `speaking_count` | int(4) | NO |  | ((0)) |
| `review_count` | int(4) | NO |  | ((0)) |
| `game_count` | int(4) | NO |  | ((0)) |
| `total_learning_count` | int(4) | NO |  | ((0)) |
| `total_learning_seconds` | int(4) | NO |  | ((0)) |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `entity_tags`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `tag_id` | bigint(8) | NO | FK -> tags(id) |  |
| `entity_type` | nvarchar(100) | NO |  |  |
| `entity_id` | bigint(8) | NO |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `game_answers`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `game_session_id` | bigint(8) | NO | FK -> game_sessions(id) |  |
| `question_id` | bigint(8) | NO | FK -> game_questions(id) |  |
| `selected_option_id` | bigint(8) | YES |  |  |
| `answer_text` | nvarchar(4000) | YES |  |  |
| `is_correct` | bit(1) | NO |  |  |
| `answered_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `game_question_options`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `question_id` | bigint(8) | NO | FK -> game_questions(id) |  |
| `option_text` | nvarchar(4000) | NO |  |  |
| `is_correct` | bit(1) | NO |  | ((0)) |
| `display_order` | int(4) | NO |  | ((0)) |

## Table: `game_questions`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `game_id` | bigint(8) | NO | FK -> games(id) |  |
| `user_id` | bigint(8) | YES |  |  |
| `content_type` | nvarchar(60) | YES |  |  |
| `content_id` | bigint(8) | YES |  |  |
| `question_text` | nvarchar(4000) | NO |  |  |
| `explanation` | nvarchar(-1) | YES |  |  |
| `difficulty` | nvarchar(60) | YES |  |  |
| `is_active` | bit(1) | NO |  | ((1)) |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `game_sessions`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `game_id` | bigint(8) | NO | FK -> games(id) |  |
| `total_questions` | int(4) | NO |  | ((0)) |
| `correct_answers` | int(4) | NO |  | ((0)) |
| `wrong_answers` | int(4) | NO |  | ((0)) |
| `score` | decimal(5) | NO |  | ((0)) |
| `started_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `completed_at` | datetime2(8) | YES |  |  |

## Table: `games`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `code` | nvarchar(100) | NO |  |  |
| `name` | nvarchar(510) | NO |  |  |
| `description` | nvarchar(2000) | YES |  |  |
| `game_type` | nvarchar(100) | NO |  |  |
| `is_active` | bit(1) | NO |  | ((1)) |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `grammar_examples`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `grammar_topic_id` | bigint(8) | NO | FK -> grammar_topics(id) |  |
| `example_sentence` | nvarchar(4000) | NO |  |  |
| `meaning` | nvarchar(4000) | YES |  |  |
| `is_primary` | bit(1) | NO |  | ((0)) |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `grammar_topics`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `topic` | nvarchar(510) | NO |  |  |
| `level` | nvarchar(40) | YES |  |  |
| `structure` | nvarchar(4000) | YES |  |  |
| `positive_structure` | nvarchar(4000) | YES |  |  |
| `negative_structure` | nvarchar(4000) | YES |  |  |
| `question_structure` | nvarchar(4000) | YES |  |  |
| `usage` | nvarchar(-1) | YES |  |  |
| `signal_words` | nvarchar(-1) | YES |  |  |
| `common_mistakes` | nvarchar(-1) | YES |  |  |
| `note` | nvarchar(-1) | YES |  |  |
| `status` | nvarchar(60) | NO |  | ('NEW') |
| `mastery_level` | int(4) | NO |  | ((0)) |
| `review_count` | int(4) | NO |  | ((0)) |
| `last_reviewed_at` | datetime2(8) | YES |  |  |
| `next_review_at` | datetime2(8) | YES |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `learning_activities`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `activity_type` | nvarchar(100) | NO |  |  |
| `content_type` | nvarchar(100) | YES |  |  |
| `content_id` | bigint(8) | YES |  |  |
| `title` | nvarchar(1000) | NO |  |  |
| `description` | nvarchar(4000) | YES |  |  |
| `activity_date` | datetime2(8) | NO |  | (sysdatetime()) |
| `duration_seconds` | int(4) | YES |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `learning_goal_progress`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `goal_id` | bigint(8) | NO | FK -> learning_goals(id) |  |
| `progress_date` | date(3) | NO |  |  |
| `value` | decimal(9) | NO |  | ((0)) |
| `note` | nvarchar(2000) | YES |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `learning_goals`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `goal_type` | nvarchar(100) | NO |  |  |
| `title` | nvarchar(1000) | NO |  |  |
| `description` | nvarchar(-1) | YES |  |  |
| `target_value` | decimal(9) | NO |  |  |
| `current_value` | decimal(9) | NO |  | ((0)) |
| `unit` | nvarchar(100) | YES |  |  |
| `start_date` | date(3) | NO |  |  |
| `target_date` | date(3) | YES |  |  |
| `status` | nvarchar(60) | NO |  | ('ACTIVE') |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `listening_lessons`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `title` | nvarchar(1000) | NO |  |  |
| `description` | nvarchar(-1) | YES |  |  |
| `url` | nvarchar(4000) | YES |  |  |
| `duration_seconds` | int(4) | YES |  |  |
| `level` | nvarchar(40) | YES |  |  |
| `note` | nvarchar(-1) | YES |  |  |
| `status` | nvarchar(60) | NO |  | ('NOT_STARTED') |
| `listened_count` | int(4) | NO |  | ((0)) |
| `learned_at` | datetime2(8) | YES |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `review_histories`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `review_item_id` | bigint(8) | NO | FK -> review_items(id) |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `result` | nvarchar(60) | NO |  |  |
| `difficulty` | nvarchar(60) | YES |  |  |
| `previous_mastery_level` | int(4) | YES |  |  |
| `new_mastery_level` | int(4) | YES |  |  |
| `previous_interval_days` | int(4) | YES |  |  |
| `new_interval_days` | int(4) | YES |  |  |
| `reviewed_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `review_items`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `content_type` | nvarchar(60) | NO |  |  |
| `content_id` | bigint(8) | NO |  |  |
| `review_status` | nvarchar(60) | NO |  | ('ACTIVE') |
| `mastery_level` | int(4) | NO |  | ((0)) |
| `review_count` | int(4) | NO |  | ((0)) |
| `current_interval_days` | int(4) | NO |  | ((1)) |
| `last_reviewed_at` | datetime2(8) | YES |  |  |
| `next_review_at` | datetime2(8) | YES |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `speaking_lessons`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `title` | nvarchar(1000) | NO |  |  |
| `topic` | nvarchar(1000) | YES |  |  |
| `description` | nvarchar(-1) | YES |  |  |
| `url` | nvarchar(4000) | YES |  |  |
| `duration_seconds` | int(4) | YES |  |  |
| `level` | nvarchar(40) | YES |  |  |
| `note` | nvarchar(-1) | YES |  |  |
| `status` | nvarchar(60) | NO |  | ('NOT_STARTED') |
| `practice_count` | int(4) | NO |  | ((0)) |
| `practiced_at` | datetime2(8) | YES |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `study_streaks`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `current_streak` | int(4) | NO |  | ((0)) |
| `longest_streak` | int(4) | NO |  | ((0)) |
| `last_study_date` | date(3) | YES |  |  |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `tags`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `name` | nvarchar(200) | NO |  |  |
| `color` | nvarchar(40) | YES |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `user_settings`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `theme` | nvarchar(40) | NO |  | ('LIGHT') |
| `language` | nvarchar(20) | NO |  | ('vi') |
| `timezone` | nvarchar(200) | NO |  | ('Asia/Ho_Chi_Minh') |
| `daily_learning_target` | int(4) | NO |  | ((30)) |
| `review_enabled` | bit(1) | NO |  | ((1)) |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `users`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `email` | nvarchar(510) | NO |  |  |
| `password_hash` | nvarchar(510) | NO |  |  |
| `display_name` | nvarchar(200) | YES |  |  |
| `avatar_url` | nvarchar(1000) | YES |  |  |
| `is_active` | bit(1) | NO |  | ((1)) |
| `last_login_at` | datetime2(8) | YES |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `vocabularies`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `user_id` | bigint(8) | NO | FK -> users(id) |  |
| `word` | nvarchar(510) | NO |  |  |
| `meaning` | nvarchar(2000) | NO |  |  |
| `pronunciation` | nvarchar(510) | YES |  |  |
| `part_of_speech` | nvarchar(200) | YES |  |  |
| `level` | nvarchar(40) | YES |  |  |
| `note` | nvarchar(-1) | YES |  |  |
| `status` | nvarchar(60) | NO |  | ('NEW') |
| `mastery_level` | int(4) | NO |  | ((0)) |
| `review_count` | int(4) | NO |  | ((0)) |
| `last_reviewed_at` | datetime2(8) | YES |  |  |
| `next_review_at` | datetime2(8) | YES |  |  |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |
| `updated_at` | datetime2(8) | NO |  | (sysdatetime()) |

## Table: `vocabulary_examples`

| Column | Type | Nullable | Key / Constraint | Default |
|---|---|---|---|---|
| `id` | bigint(8) | NO | PK, IDENTITY |  |
| `vocabulary_id` | bigint(8) | NO | FK -> vocabularies(id) |  |
| `example_sentence` | nvarchar(4000) | NO |  |  |
| `meaning` | nvarchar(4000) | YES |  |  |
| `is_primary` | bit(1) | NO |  | ((0)) |
| `created_at` | datetime2(8) | NO |  | (sysdatetime()) |

