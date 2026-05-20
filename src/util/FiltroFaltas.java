package util;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class FiltroFaltas extends DocumentFilter {

    private final Component tela;
    private final String nomeCampo;
    private final int maximo;

    public FiltroFaltas(Component tela, String nomeCampo, int maximo) {
        this.tela = tela;
        this.nomeCampo = nomeCampo;
        this.maximo = maximo;
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

        if (!texto.matches("\\d*")) {
            mostrarAviso("O campo " + nomeCampo + " aceita somente números. Letras não são permitidas.");
            return;
        }

        if (!novo.isEmpty()) {
            int valor = Integer.parseInt(novo);
            if (valor > maximo) {
                mostrarAviso("O máximo permitido no campo " + nomeCampo + " é " + maximo + ".");
                return;
            }
        }

        super.replace(fb, offset, length, texto, attrs);
    }

    private void mostrarAviso(String mensagem) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(tela, mensagem, "Aviso", JOptionPane.WARNING_MESSAGE)
        );
    }
}

