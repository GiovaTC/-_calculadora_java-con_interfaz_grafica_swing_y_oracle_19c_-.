package com.miempresa.calculadora;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResultDAO {

    private static final String INSERT_SQL = "INSERT INTO calculos (expresion, resultado) VALUES (?, ?)";

    public void save(String expresion, String resultado) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, expresion);
            ps.setString(2, resultado);
            ps.executeUpdate();
        }
    }
}