package com.MyProject.User;

import java.sql.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseUniquenessRaceTest {
    @Autowired DataSource source;

    @Test void emailConcurrentWritesAreProtectedByUniqueIndex() throws Exception {
        race("UserDB", "users", "email", "UQ_users_email", "Race@Example.invalid");
    }
    @Test void tuitionStudentConcurrentWritesAreProtectedByUniqueIndex() throws Exception {
        race("TuitionDB", "tuition", "student_id", "UQ_tuition_student_id", "00001");
    }
    private void race(String database, String table, String column, String index, String value) throws Exception {
        // Verify the deployed index first. Use a global temporary table with the same column
        // type/collation for concurrent insert probes, never write to existing account/tuition rows.
        String fixture = "##unique_probe_" + Long.toUnsignedString(System.nanoTime());
        try (Connection owner = source.getConnection(); Statement statement = owner.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + database + ".sys.indexes i JOIN "
                    + database + ".sys.tables t ON t.object_id=i.object_id WHERE t.name='" + table
                    + "' AND i.name='" + index + "' AND i.is_unique=1")) {
                rs.next(); assertEquals(1, rs.getInt(1));
            }
            statement.execute("SELECT TOP(0) " + column + " INTO " + fixture + " FROM " + database + ".dbo." + table);
            statement.execute("CREATE UNIQUE INDEX unique_probe ON " + fixture + "(" + column + ")");
            var ready = new CountDownLatch(2);
            var start = new CountDownLatch(1);
            try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
                Callable<Boolean> insert = () -> {
                    try (Connection connection = source.getConnection();
                            PreparedStatement query = connection.prepareStatement("INSERT " + fixture + " VALUES (?)")) {
                        connection.setAutoCommit(true);
                        query.setString(1, value);
                        ready.countDown();
                        if (!start.await(10, TimeUnit.SECONDS)) throw new AssertionError("Race did not start");
                        try { query.executeUpdate(); return true; }
                        catch (SQLException ex) {
                            if (ex.getErrorCode() == 2601 || ex.getErrorCode() == 2627) return false;
                            throw ex;
                        }
                    }
                };
                Future<Boolean> first=executor.submit(insert), second=executor.submit(insert);
                boolean bothReady=ready.await(10, TimeUnit.SECONDS);
                start.countDown();
                assertTrue(bothReady);
                assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS),
                    "Exactly one concurrent insert must succeed");
            }
        }
        // SQL Server removes the temporary fixture when the owning physical connection closes.
        // A pooled owner may remain open until context shutdown; names are unique per test run.
    }
}
