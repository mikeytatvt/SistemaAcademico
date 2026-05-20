package dao;

import connection.Conexao;
import model.NotaFalta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotaFaltaDAO {

    public void salvar(NotaFalta notaFalta) throws SQLException {
        String sql = "INSERT INTO notas_faltas (rgm_aluno, disciplina, semestre, nota, faltas) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE nota = VALUES(nota), faltas = VALUES(faltas)";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notaFalta.getRgmAluno());
            stmt.setString(2, notaFalta.getDisciplina());
            stmt.setString(3, notaFalta.getSemestre());
            stmt.setDouble(4, notaFalta.getNota());
            stmt.setInt(5, notaFalta.getFaltas());
            stmt.executeUpdate();
        }
    }

    public int excluirPorRgm(String rgm) throws SQLException {
        String sql = "DELETE FROM notas_faltas WHERE rgm_aluno = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rgm);
            return stmt.executeUpdate();
        }
    }

    public List<NotaFalta> listarPorRgm(String rgm) throws SQLException {
        List<NotaFalta> lista = new ArrayList<>();
        String sql = "SELECT id, rgm_aluno, disciplina, semestre, nota, faltas " +
                "FROM notas_faltas WHERE rgm_aluno = ? ORDER BY semestre, disciplina";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rgm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new NotaFalta(
                            rs.getInt("id"),
                            rs.getString("rgm_aluno"),
                            rs.getString("disciplina"),
                            rs.getString("semestre"),
                            rs.getDouble("nota"),
                            rs.getInt("faltas")
                    ));
                }
            }
        }

        return lista;
    }
}

