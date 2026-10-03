$connString = "Server=localhost,1433;Database=EnglishLearning;User Id=sa;Password=fsh@12345;TrustServerCertificate=True;"
$conn = New-Object System.Data.SqlClient.SqlConnection($connString)

try {
    $conn.Open()
    Write-Host "Connected to SQL Server EnglishLearning database successfully." -ForegroundColor Green

    # List all tables in DB
    $cmd = $conn.CreateCommand()
    $cmd.CommandText = "SELECT name FROM sys.tables ORDER BY name"
    $reader = $cmd.ExecuteReader()
    $allTables = @()
    while ($reader.Read()) {
        $allTables += $reader["name"]
    }
    $reader.Close()
    Write-Host "Found tables in database: $($allTables -join ', ')" -ForegroundColor Gray

    # Tables to completely clear (sample data)
    $tablesToClear = @(
        'game_answers',
        'game_question_options',
        'game_questions',
        'game_sessions',
        'review_histories',
        'review_items',
        'learning_activities',
        'daily_learning_statistics',
        'learning_goal_progress',
        'learning_goals',
        'entity_tags',
        'tags',
        'vocabulary_examples',
        'vocabularies',
        'grammar_examples',
        'grammar_rules',
        'grammar_topics',
        'listening_notes',
        'listening_lessons',
        'speaking_notes',
        'speaking_recordings',
        'speaking_lessons'
    )

    $sql = "BEGIN TRANSACTION;`n"
    foreach ($tbl in $tablesToClear) {
        if ($allTables -contains $tbl) {
            $sql += "DELETE FROM [$tbl];`n"
        }
    }
    
    if ($allTables -contains 'study_streaks') {
        $sql += "UPDATE [study_streaks] SET current_streak = 0, longest_streak = 0, last_study_date = NULL;`n"
    }
    $sql += "COMMIT TRANSACTION;`n"

    $cmd.CommandText = $sql
    $cmd.ExecuteNonQuery() | Out-Null
    Write-Host "`nAll sample/dummy learning data deleted successfully!" -ForegroundColor Green

    # Verify counts for each table
    Write-Host "`nCurrent Table Record Counts:" -ForegroundColor Cyan
    foreach ($tbl in $allTables) {
        $cmd.CommandText = "SELECT COUNT(*) FROM [$tbl]"
        $count = $cmd.ExecuteScalar()
        Write-Host ("  {0,-30} : {1}" -f $tbl, $count)
    }

} catch {
    Write-Host "Error occurred: $_" -ForegroundColor Red
    if ($conn.State -eq 'Open') {
        $rollbackCmd = $conn.CreateCommand()
        $rollbackCmd.CommandText = "IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;"
        $rollbackCmd.ExecuteNonQuery() | Out-Null
    }
} finally {
    $conn.Close()
}
