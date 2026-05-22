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
        String sqlBuscar = "SELECT id FROM notas_faltas " +
                "WHERE rgm_aluno = ? AND disciplina = ? AND semestre = ? " +
                "ORDER BY id DESC LIMIT 1";

        String sqlAtualizar = "UPDATE notas_faltas SET nota = ?, faltas = ? WHERE id = ?";

        String sqlInserir = "INSERT INTO notas_faltas (rgm_aluno, disciplina, semestre, nota, faltas) " +
                "VALUES (?, ?, ?, ?, ?)";

        String sqlLimparDuplicadas = "DELETE FROM notas_faltas " +
                "WHERE rgm_aluno = ? AND disciplina = ? AND semestre = ? AND id <> ?";

        try (Connection conn = Conexao.obterConexao()) {
            conn.setAutoCommit(false);

            try {
                Integer idExistente = null;

                try (PreparedStatement stmtBuscar = conn.prepareStatement(sqlBuscar)) {
                    stmtBuscar.setString(1, notaFalta.getRgmAluno());
                    stmtBuscar.setString(2, notaFalta.getDisciplina());
                    stmtBuscar.setString(3, notaFalta.getSemestre());

                    try (ResultSet rs = stmtBuscar.executeQuery()) {
                        if (rs.next()) {
                            idExistente = rs.getInt("id");
                        }
                    }
                }

                if (idExistente != null) {
                    try (PreparedStatement stmtAtualizar = conn.prepareStatement(sqlAtualizar)) {
                        stmtAtualizar.setDouble(1, notaFalta.getNota());
                        stmtAtualizar.setInt(2, notaFalta.getFaltas());
                        stmtAtualizar.setInt(3, idExistente);
                        stmtAtualizar.executeUpdate();
                    }

                    try (PreparedStatement stmtLimpar = conn.prepareStatement(sqlLimparDuplicadas)) {
                        stmtLimpar.setString(1, notaFalta.getRgmAluno());
                        stmtLimpar.setString(2, notaFalta.getDisciplina());
                        stmtLimpar.setString(3, notaFalta.getSemestre());
                        stmtLimpar.setInt(4, idExistente);
                        stmtLimpar.executeUpdate();
                    }
                } else {
                    try (PreparedStatement stmtInserir = conn.prepareStatement(sqlInserir)) {
                        stmtInserir.setString(1, notaFalta.getRgmAluno());
                        stmtInserir.setString(2, notaFalta.getDisciplina());
                        stmtInserir.setString(3, notaFalta.getSemestre());
                        stmtInserir.setDouble(4, notaFalta.getNota());
                        stmtInserir.setInt(5, notaFalta.getFaltas());
                        stmtInserir.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
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
        String sql = "SELECT nf.id, nf.rgm_aluno, nf.disciplina, nf.semestre, nf.nota, nf.faltas " +
                "FROM notas_faltas nf " +
                "INNER JOIN ( " +
                "    SELECT rgm_aluno, disciplina, semestre, MAX(id) AS ultimo_id " +
                "    FROM notas_faltas " +
                "    WHERE rgm_aluno = ? " +
                "    GROUP BY rgm_aluno, disciplina, semestre " +
                ") ult ON nf.id = ult.ultimo_id " +
                "ORDER BY nf.semestre, nf.disciplina";

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
