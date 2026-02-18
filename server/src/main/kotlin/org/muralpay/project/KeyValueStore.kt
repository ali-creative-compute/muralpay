package org.muralpay.project

import java.sql.Connection
import java.sql.DriverManager

object KeyValueStore {
    private const val DB_PATH = "data.db"
    private val connection: Connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:$DB_PATH").also { conn ->
            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS kv_store (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
                """
            )
        }
    }

    fun get(key: String): String? {
        val stmt = connection.prepareStatement("SELECT value FROM kv_store WHERE key = ?")
        stmt.setString(1, key)
        val rs = stmt.executeQuery()
        return if (rs.next()) rs.getString("value") else null
    }

    fun put(key: String, value: String): String? {
        val previous = get(key)
        val stmt = connection.prepareStatement(
            "INSERT INTO kv_store (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = ?"
        )
        stmt.setString(1, key)
        stmt.setString(2, value)
        stmt.setString(3, value)
        stmt.executeUpdate()
        return previous
    }

    fun delete(key: String): String? {
        val previous = get(key)
        val stmt = connection.prepareStatement("DELETE FROM kv_store WHERE key = ?")
        stmt.setString(1, key)
        stmt.executeUpdate()
        return previous
    }

    fun exists(key: String): Boolean {
        val stmt = connection.prepareStatement("SELECT 1 FROM kv_store WHERE key = ?")
        stmt.setString(1, key)
        return stmt.executeQuery().next()
    }

    fun keys(): Set<String> {
        val stmt = connection.createStatement()
        val rs = stmt.executeQuery("SELECT key FROM kv_store")
        val keys = mutableSetOf<String>()
        while (rs.next()) {
            keys.add(rs.getString("key"))
        }
        return keys
    }

    fun clear() {
        connection.createStatement().execute("DELETE FROM kv_store")
    }
}
