package dev.tonysp.dodgeball.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.tonysp.dodgeball.Dodgeball;
import dev.tonysp.dodgeball.Manager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager extends Manager {
    private String url, username, password;
    private HikariConfig hikariConfig;
    private HikariDataSource hikariDataSource;

    public DatabaseManager (Dodgeball plugin) {
        super(plugin);
    }

    @Override
    public boolean load () {
        FileConfiguration config = Dodgeball.getInstance().getConfig();
        url = config.getString("mysql.url", "");
        username = config.getString("mysql.username", "");
        password = config.getString("mysql.password", "");
        hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        hikariDataSource = new HikariDataSource(this.hikariConfig);

        try {
            hikariDataSource.getConnection();
            initializeTables();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean unload () {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
        return true;
    }

    private Connection getConnection () throws SQLException {
        return hikariDataSource.getConnection();
    }

    public void initializeTables(){
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("CREATE TABLE IF NOT EXISTS dodgeball_score (uuid varchar(36) NOT NULL, score int NOT NULL, PRIMARY KEY (uuid));");
            sql.executeUpdate();
            sql.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateScore (UUID uuid, int addScore){
        Bukkit.getScheduler().runTask(Dodgeball.getInstance(), () -> {
            try (Connection connection = getConnection();
                 PreparedStatement sql = connection.prepareStatement("INSERT INTO dodgeball_score (uuid, score) VALUES (?,?) ON CONFLICT (uuid) DO UPDATE SET score = dodgeball_score.score + ?;");
            ) {
                sql.setString(1, uuid.toString());
                sql.setInt(2, addScore);
                sql.setInt(3, addScore);
                sql.executeUpdate();
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}

