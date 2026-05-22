package util;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class FiltroNota extends DocumentFilter {

    private final Component tela;
    private final String nomeCampo;
    private final int limite;

    public FiltroNota(Component tela, String nomeCampo, int limite) {
        this.tela = tela;
        this.nomeCampo = nomeCampo;
        this.limite = limite;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attr)
            throws BadLocationException {
        substituir(fb, offset, 0, texto, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String texto, AttributeSet attrs)
            throws BadLocationException {
        substituir(fb, offset, length, texto, attrs);
    }

    private void substituir(FilterBypass fb, int offset, int length, String texto, AttributeSet attrs)
            throws BadLocationException {
        if (texto == null) return;

        String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String novo = atual.substring(0, offset) + texto + atual.substring(offset + length);

        if (!texto.matches("[0-9,.]*")) {
            mostrarAviso("O campo " + nomeCampo + " aceita somente números, vírgula ou ponto. Letras não são permitidas.");
            return;
        }

        if (novo.length() > limite) {
            mostrarAviso("O campo " + nomeCampo + " aceita no máximo " + limite + " caracteres. Exemplo: 0,25 ou 10.");
            return;
        }

        int virgulas = contar(novo, ',');
        int pontos = contar(novo, '.');
        if (virgulas + pontos > 1) {
            mostrarAviso("Use apenas uma vírgula ou um ponto no campo " + nomeCampo + ".");
            return;
        }

        if (!novo.isEmpty() && !novo.equals(",") && !novo.equals(".")) {
            try {
                double valor = Double.parseDouble(novo.replace(',', '.'));
                if (valor > 10) {
                    mostrarAviso("A nota máxima permitida é 10.");
                    return;
                }
            } catch (NumberFormatException ex) {
                mostrarAviso("Digite uma nota válida.");
                return;
            }
        }

        super.replace(fb, offset, length, texto, attrs);
    }

    private int contar(String texto, char caractere) {
        int total = 0;
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) == caractere) total++;
        }
        return total;
    }

    private void mostrarAviso(String mensagem) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(tela, mensagem, "Aviso", JOptionPane.WARNING_MESSAGE)
        );
    }
}
