package view;

import dao.AlunoDAO;
import dao.NotaFaltaDAO;
import model.Aluno;
import model.NotaFalta;
import util.FiltroFaltas;
import util.FiltroLetras;
import util.FiltroLimite;
import util.FiltroNumeros;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class SistemaAcademico extends JFrame {

    private static final Font FONTE_MENU = new Font("Arial", Font.PLAIN, 13);
    private static final Font FONTE_ABAS = new Font("Arial", Font.BOLD, 13);
    private static final Font FONTE_LABEL = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONTE_CAMPO = new Font("Arial", Font.PLAIN, 14);

    private final AlunoDAO alunoDAO = new AlunoDAO();
    private final NotaFaltaDAO notaFaltaDAO = new NotaFaltaDAO();

    private JTabbedPane abas;

    // Aba Dados Pessoais
    private JTextField txtRgm, txtNome, txtEmail, txtEndereco, txtMunicipio;
    private JFormattedTextField txtDataNasc, txtCpf, txtCelular;
    private JComboBox<String> cbUf;

    // Aba Curso
    private JComboBox<String> cbCurso, cbCampus;
    private JRadioButton rbMatutino, rbVespertino, rbNoturno;
    private ButtonGroup grupoPeriodo;

    // Aba Notas e Faltas
    private JTextField txtNotasRgm, txtNotasNome, txtNotasCurso, txtFaltas;
    private JComboBox<String> cbDisciplina, cbSemestre, cbNota;

    // Aba Boletim
    private JTextField txtBoletimRgm;
    private JLabel lblBoletimRgm, lblBoletimNome, lblBoletimCurso, lblBoletimStatus;
    private JTable tabelaBoletim;
    private DefaultTableModel modeloBoletim;

    public SistemaAcademico() {
        setTitle("Sistema Acadêmico");
        configurarFontesPadrao();

        ImageIcon iconeJanela = carregarIcone("java.png");
        if (iconeJanela != null) {
            setIconImage(iconeJanela.getImage());
        }

        setSize(600, 490);
        setMinimumSize(new Dimension(600, 490));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        criarMenuBar();

        abas = new JTabbedPane();
        abas.setFont(FONTE_ABAS);
        abas.addTab("Dados Pessoais", criarAbaDadosPessoais());
        abas.addTab("Curso", criarAbaCurso());
        abas.addTab("Notas e Faltas", criarAbaNotasFaltas());
        abas.addTab("Boletim", criarAbaBoletim());

        configurarCorDasAbas();

        JPanel painelCentral = new JPanel(new BorderLayout());
        painelCentral.setBackground(new Color(238, 238, 238));
        painelCentral.setBorder(BorderFactory.createEmptyBorder(38, 12, 10, 12));
        painelCentral.add(abas, BorderLayout.CENTER);

        add(painelCentral, BorderLayout.CENTER);

        aplicarValidacoesCampos();
        atualizarMunicipioPorUf();
    }

    // =========================================================
    // MENU SUPERIOR
    // =========================================================
    private void criarMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuAluno = new JMenu("Aluno");
        JMenuItem miSalvar = criarItemMenu("Salvar", "control S", e -> salvarAluno());
        JMenuItem miAlterar = criarItemMenu("Alterar", "control A", e -> alterarAluno());
        JMenuItem miExcluir = criarItemMenu("Excluir", null, e -> excluirAluno());
        JMenuItem miConsultar = criarItemMenu("Consultar", "control C", e -> consultarAluno(true));
        JMenuItem miSair = criarItemMenu("Sair", null, e -> sair());

        menuAluno.add(miSalvar);
        menuAluno.add(miAlterar);
        menuAluno.add(miExcluir);
        menuAluno.add(miConsultar);
        menuAluno.addSeparator();
        menuAluno.add(miSair);

        JMenu menuNotas = new JMenu("Notas e Faltas");
        JMenuItem miSalvarNota = criarItemMenu("Salvar", null, e -> salvarNotaFalta());
        JMenuItem miAlterarNota = criarItemMenu("Alterar", "control A", e -> salvarNotaFalta());
        JMenuItem miExcluirNota = criarItemMenu("Excluir", null, e -> excluirNotasFaltas());
        JMenuItem miConsultarNota = criarItemMenu("Consultar", null, e -> carregarAlunoNaAbaNotas(true));

        menuNotas.add(miSalvarNota);
        menuNotas.add(miAlterarNota);
        menuNotas.add(miExcluirNota);
        menuNotas.add(miConsultarNota);


        JMenu menuAjuda = new JMenu("Ajuda");
        JMenuItem miSobre = new JMenuItem("Sobre");
        miSobre.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Sistema Acadêmico\nJava Swing + MySQL\nCadastro de alunos, notas, faltas e boletim.",
                "Sobre",
                JOptionPane.INFORMATION_MESSAGE
        ));
        menuAjuda.add(miSobre);

        aplicarFonteMenu(menuAluno);
        aplicarFonteMenu(menuNotas);
        aplicarFonteMenu(menuAjuda);
        aplicarFonteMenu(miSalvar);
        aplicarFonteMenu(miAlterar);
        aplicarFonteMenu(miExcluir);
        aplicarFonteMenu(miConsultar);
        aplicarFonteMenu(miSair);
        aplicarFonteMenu(miSalvarNota);
        aplicarFonteMenu(miAlterarNota);
        aplicarFonteMenu(miExcluirNota);
        aplicarFonteMenu(miConsultarNota);
        aplicarFonteMenu(miSobre);

        menuBar.add(menuAluno);
        menuBar.add(menuNotas);
        menuBar.add(menuAjuda);
        setJMenuBar(menuBar);
    }

    private JMenuItem criarItemMenu(String texto, String atalho, ActionListener acao) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(FONTE_MENU);
        if (atalho != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(atalho));
        }
        item.addActionListener(acao);
        return item;
    }

    private void configurarFontesPadrao() {
        UIManager.put("Menu.font", FONTE_MENU);
        UIManager.put("MenuItem.font", FONTE_MENU);
        UIManager.put("Label.font", FONTE_LABEL);
        UIManager.put("TextField.font", FONTE_CAMPO);
        UIManager.put("FormattedTextField.font", FONTE_CAMPO);
        UIManager.put("ComboBox.font", FONTE_CAMPO);
        UIManager.put("RadioButton.font", FONTE_LABEL);
        UIManager.put("Button.font", FONTE_MENU);
        UIManager.put("Table.font", FONTE_MENU);
        UIManager.put("TableHeader.font", FONTE_MENU);
    }

    private void aplicarFonteMenu(JComponent componente) {
        componente.setFont(FONTE_MENU);
    }

    // =========================================================
    // ABA 1 - DADOS PESSOAIS
    // =========================================================
    private JPanel criarAbaDadosPessoais() {
        JPanel painel = criarPainelPadrao();

        adicionarLabel(painel, "RGM", 15, 25, 45, 25);
        txtRgm = new JTextField();
        adicionarCampo(painel, txtRgm, 70, 25, 105, 25);

        adicionarLabel(painel, "Nome", 190, 25, 50, 25);
        txtNome = new JTextField();
        adicionarCampo(painel, txtNome, 240, 25, 250, 25);

        adicionarLabel(painel, "Data de Nascimento", 15, 65, 135, 25);
        txtDataNasc = criarCampoFormatado("##/##/####");
        adicionarCampo(painel, txtDataNasc, 150, 65, 105, 25);

        adicionarLabel(painel, "CPF", 285, 65, 40, 25);
        txtCpf = criarCampoFormatado("###.###.###-##");
        adicionarCampo(painel, txtCpf, 325, 65, 165, 25);

        adicionarLabel(painel, "Email", 15, 105, 45, 25);
        txtEmail = new JTextField();
        adicionarCampo(painel, txtEmail, 70, 105, 420, 25);

        adicionarLabel(painel, "End.", 15, 145, 45, 25);
        txtEndereco = new JTextField();
        adicionarCampo(painel, txtEndereco, 70, 145, 420, 25);

        adicionarLabel(painel, "Município", 15, 185, 70, 25);
        txtMunicipio = new JTextField();
        txtMunicipio.setEditable(false);
        txtMunicipio.setBackground(Color.WHITE);
        adicionarCampo(painel, txtMunicipio, 90, 185, 110, 25);

        adicionarLabel(painel, "UF", 215, 185, 25, 25);
        cbUf = new JComboBox<>(new String[]{"SP", "RJ", "MG", "PR", "SC", "RS", "BA", "GO", "DF", "PE", "CE"});
        cbUf.setBounds(245, 185, 60, 25);
        cbUf.setFont(FONTE_CAMPO);
        cbUf.addActionListener(e -> atualizarMunicipioPorUf());
        painel.add(cbUf);

        adicionarLabel(painel, "Celular", 320, 185, 60, 25);
        txtCelular = criarCampoFormatado("(##) #####-####");
        adicionarCampo(painel, txtCelular, 380, 185, 110, 25);

        JLabel lblLogoDados = criarLabelImagem("universidade_banner.png", 95, 225, 360, 95);
        painel.add(lblLogoDados);

        return painel;
    }

    // =========================================================
    // ABA 2 - CURSO
    // =========================================================
    private JPanel criarAbaCurso() {
        JPanel painel = criarPainelPadrao();

        adicionarLabel(painel, "Curso", 15, 30, 70, 25);
        cbCurso = new JComboBox<>(new String[]{
                "Análise e Desenvolvimento de Sistemas",
                "Ciência da Computação",
                "Engenharia de Software",
                "Sistemas de Informação",
                "Banco de Dados"
        });
        cbCurso.setBounds(95, 30, 395, 25);
        cbCurso.setFont(FONTE_CAMPO);
        painel.add(cbCurso);

        adicionarLabel(painel, "Campus", 15, 70, 70, 25);
        cbCampus = new JComboBox<>(new String[]{"Tatuapé", "Anália Franco", "Liberdade", "Vila Formosa", "Virtual"});
        cbCampus.setBounds(95, 70, 395, 25);
        cbCampus.setFont(FONTE_CAMPO);
        painel.add(cbCampus);

        adicionarLabel(painel, "Período", 15, 115, 70, 25);
        rbMatutino = new JRadioButton("Matutino");
        rbVespertino = new JRadioButton("Vespertino");
        rbNoturno = new JRadioButton("Noturno", true);
        rbMatutino.setOpaque(false);
        rbVespertino.setOpaque(false);
        rbNoturno.setOpaque(false);
        rbMatutino.setFont(FONTE_LABEL);
        rbVespertino.setFont(FONTE_LABEL);
        rbNoturno.setFont(FONTE_LABEL);

        grupoPeriodo = new ButtonGroup();
        grupoPeriodo.add(rbMatutino);
        grupoPeriodo.add(rbVespertino);
        grupoPeriodo.add(rbNoturno);

        rbMatutino.setBounds(95, 115, 100, 25);
        rbVespertino.setBounds(205, 115, 110, 25);
        rbNoturno.setBounds(325, 115, 100, 25);
        painel.add(rbMatutino);
        painel.add(rbVespertino);
        painel.add(rbNoturno);

        adicionarBotoesImagemCurso(painel);

        return painel;
    }

    // =========================================================
    // ABA 3 - NOTAS E FALTAS
    // =========================================================
    private JPanel criarAbaNotasFaltas() {
        JPanel painel = criarPainelPadrao();

        adicionarLabel(painel, "RGM", 15, 25, 45, 25);
        txtNotasRgm = new JTextField();
        adicionarCampo(painel, txtNotasRgm, 70, 25, 110, 25);
        txtNotasRgm.addActionListener(e -> carregarAlunoNaAbaNotas(true));

        txtNotasNome = new JTextField();
        txtNotasNome.setEditable(false);
        adicionarCampo(painel, txtNotasNome, 195, 25, 295, 25);

        txtNotasCurso = new JTextField();
        txtNotasCurso.setEditable(false);
        adicionarCampo(painel, txtNotasCurso, 15, 65, 475, 25);

        adicionarLabel(painel, "Disciplina", 15, 110, 90, 25);
        cbDisciplina = new JComboBox<>(new String[]{
                "Programação Orientada a Objetos",
                "Banco de Dados I",
                "Estrutura de Dados",
                "Engenharia de Requisitos",
                "Sistemas Operacionais"
        });
        cbDisciplina.setBounds(105, 110, 385, 25);
        cbDisciplina.setFont(FONTE_CAMPO);
        painel.add(cbDisciplina);

        adicionarLabel(painel, "Semestre", 15, 155, 75, 25);
        cbSemestre = new JComboBox<>(new String[]{"2026-1", "2026-2", "2027-1", "2027-2"});
        cbSemestre.setBounds(90, 155, 95, 25);
        cbSemestre.setFont(FONTE_CAMPO);
        painel.add(cbSemestre);

        adicionarLabel(painel, "Nota", 210, 155, 45, 25);
        cbNota = new JComboBox<>(gerarNotas());
        cbNota.setEditable(true);
        cbNota.setBounds(255, 155, 75, 25);
        cbNota.setFont(FONTE_CAMPO);
        painel.add(cbNota);

        adicionarLabel(painel, "Faltas", 355, 155, 55, 25);
        txtFaltas = new JTextField();
        adicionarCampo(painel, txtFaltas, 410, 155, 80, 25);

        adicionarBotoesImagemNotas(painel);

        return painel;
    }

    // =========================================================
    // ABA 4 - BOLETIM
    // =========================================================
    private JPanel criarAbaBoletim() {
        JPanel painel = criarPainelPadrao();

        adicionarLabel(painel, "RGM", 15, 20, 45, 25);
        txtBoletimRgm = new JTextField();
        adicionarCampo(painel, txtBoletimRgm, 70, 20, 110, 25);
        txtBoletimRgm.addActionListener(e -> gerarBoletim());

        JButton btnGerar = new JButton("Gerar Boletim");
        btnGerar.setBounds(195, 20, 135, 25);
        btnGerar.setFocusPainted(false);
        btnGerar.setFont(FONTE_MENU);
        btnGerar.addActionListener(e -> gerarBoletim());
        painel.add(btnGerar);

        lblBoletimRgm = new JLabel("RGM: -");
        lblBoletimNome = new JLabel("Nome: -");
        lblBoletimCurso = new JLabel("Curso: -");
        lblBoletimStatus = new JLabel("Status: -");
        lblBoletimRgm.setFont(FONTE_LABEL);
        lblBoletimNome.setFont(FONTE_LABEL);
        lblBoletimCurso.setFont(FONTE_LABEL);
        lblBoletimStatus.setFont(new Font("Arial", Font.BOLD, 14));

        lblBoletimRgm.setBounds(15, 60, 150, 25);
        lblBoletimNome.setBounds(180, 60, 190, 25);
        lblBoletimCurso.setBounds(15, 88, 350, 25);
        lblBoletimStatus.setBounds(15, 116, 350, 25);

        painel.add(lblBoletimRgm);
        painel.add(lblBoletimNome);
        painel.add(lblBoletimCurso);
        painel.add(lblBoletimStatus);

        JLabel lblLogoBoletim = criarLabelImagem("universidade_logo.png", 398, 34, 92, 92);
        painel.add(lblLogoBoletim);

        modeloBoletim = new DefaultTableModel(new String[]{"Disciplina", "Semestre", "Nota", "Faltas", "Situação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaBoletim = new JTable(modeloBoletim);
        tabelaBoletim.setFont(FONTE_MENU);
        tabelaBoletim.getTableHeader().setFont(FONTE_MENU);
        tabelaBoletim.setRowHeight(25);

        JScrollPane scroll = new JScrollPane(tabelaBoletim);
        scroll.setBounds(15, 150, 475, 150);
        painel.add(scroll);

        return painel;
    }

    // =========================================================
    // PAINÉIS E COMPONENTES AUXILIARES
    // =========================================================
    private JPanel criarPainelPadrao() {
        JPanel painel = new JPanel(null);
        painel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(120, 150, 210)),
                "",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));
        painel.setBackground(new Color(238, 238, 238));
        return painel;
    }

    private void adicionarLabel(JPanel painel, String texto, int x, int y, int largura, int altura) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_LABEL);
        label.setBounds(x, y, largura, altura);
        painel.add(label);
    }

    private void adicionarCampo(JPanel painel, JComponent campo, int x, int y, int largura, int altura) {
        campo.setFont(FONTE_CAMPO);
        campo.setBounds(x, y, largura, altura);
        painel.add(campo);
    }

    private JFormattedTextField criarCampoFormatado(String mascara) {
        try {
            MaskFormatter formatter = new MaskFormatter(mascara);
            formatter.setPlaceholderCharacter(' ');
            return new JFormattedTextField(formatter);
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }


    private void configurarCorDasAbas() {
        abas.setOpaque(false);
        abas.setBackground(new Color(238, 238, 238));
        abas.setForeground(Color.BLACK);
        abas.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        abas.setUI(new BasicTabbedPaneUI() {
            @Override
            protected Insets getTabInsets(int tabPlacement, int tabIndex) {
                return new Insets(4, 12, 4, 12);
            }

            @Override
            protected Insets getContentBorderInsets(int tabPlacement) {
                return new Insets(4, 1, 1, 1);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? new Color(80, 130, 200) : new Color(238, 238, 238));
                g2.fillRoundRect(x + 1, y + 2, w - 2, h - 3, 8, 8);
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                g.setColor(new Color(150, 150, 150));
                g.drawRoundRect(x + 1, y + 2, w - 3, h - 4, 8, 8);
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font,
                                     FontMetrics metrics, int tabIndex, String title,
                                     Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(font);
                g2.setColor(isSelected ? Color.WHITE : Color.BLACK);
                int textY = textRect.y + metrics.getAscent();
                g2.drawString(title, textRect.x, textY);
                g2.dispose();
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                               Rectangle[] rects, int tabIndex,
                                               Rectangle iconRect, Rectangle textRect,
                                               boolean isSelected) {
                // sem borda pontilhada
            }
        });
    }

    private ImageIcon carregarIcone(String nomeImagem) {
        java.net.URL url = getClass().getResource("/imagens/" + nomeImagem);

        if (url == null) {
            JOptionPane.showMessageDialog(this, "Imagem não encontrada: " + nomeImagem);
            return null;
        }

        ImageIcon iconeOriginal = new ImageIcon(url);
        Image imagem = iconeOriginal.getImage().getScaledInstance(42, 42, Image.SCALE_SMOOTH);
        return new ImageIcon(imagem);
    }

    private JButton criarBotaoImagem(String nomeImagem, String dica, ActionListener acao) {
        JButton botao = new JButton(carregarIcone(nomeImagem));
        botao.setToolTipText(dica);
        botao.setFocusPainted(false);
        botao.addActionListener(acao);
        return botao;
    }

    private ImageIcon carregarImagem(String nomeImagem, int largura, int altura) {
        java.net.URL url = getClass().getResource("/imagens/" + nomeImagem);

        if (url == null) {
            JOptionPane.showMessageDialog(this, "Imagem não encontrada: " + nomeImagem);
            return null;
        }

        ImageIcon iconeOriginal = new ImageIcon(url);
        Image imagem = iconeOriginal.getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(imagem);
    }

    private JLabel criarLabelImagem(String nomeImagem, int x, int y, int largura, int altura) {
        JLabel label = new JLabel();
        ImageIcon icone = carregarImagem(nomeImagem, largura, altura);

        if (icone != null) {
            label.setIcon(icone);
        }

        label.setBounds(x, y, largura, altura);
        return label;
    }

    private void adicionarBotoesImagemCurso(JPanel painel) {
        JButton btnSair = criarBotaoImagem("sair.png", "Sair", e -> sair());
        btnSair.setBounds(45, 190, 68, 64);
        painel.add(btnSair);

        JButton btnSalvar = criarBotaoImagem("salvar.png", "Salvar aluno", e -> salvarAluno());
        btnSalvar.setBounds(130, 190, 68, 64);
        painel.add(btnSalvar);

        JButton btnConsultar = criarBotaoImagem("consultar.png", "Consultar aluno", e -> consultarAluno(true));
        btnConsultar.setBounds(215, 190, 68, 64);
        painel.add(btnConsultar);

        JButton btnExcluir = criarBotaoImagem("excluir.png", "Excluir aluno", e -> excluirAluno());
        btnExcluir.setBounds(300, 190, 68, 64);
        painel.add(btnExcluir);

        JButton btnJava = criarBotaoImagem("java.png", "Sobre", e -> JOptionPane.showMessageDialog(this, "Sistema Acadêmico Java + MySQL"));
        btnJava.setBounds(385, 190, 68, 64);
        painel.add(btnJava);
    }

    private void adicionarBotoesImagemNotas(JPanel painel) {
        JButton btnConsultar = criarBotaoImagem("consultar.png", "Consultar aluno", e -> carregarAlunoNaAbaNotas(true));
        btnConsultar.setBounds(45, 220, 68, 64);
        painel.add(btnConsultar);

        JButton btnSalvar = criarBotaoImagem("salvar.png", "Salvar nota e faltas", e -> salvarNotaFalta());
        btnSalvar.setBounds(130, 220, 68, 64);
        painel.add(btnSalvar);

        JButton btnExcluir = criarBotaoImagem("excluir.png", "Excluir notas e faltas", e -> excluirNotasFaltas());
        btnExcluir.setBounds(215, 220, 68, 64);
        painel.add(btnExcluir);

        JButton btnLimpar = criarBotaoImagem("java.png", "Limpar campos", e -> limparCamposNotas());
        btnLimpar.setBounds(300, 220, 68, 64);
        painel.add(btnLimpar);

        JButton btnSair = criarBotaoImagem("sair.png", "Sair", e -> sair());
        btnSair.setBounds(385, 220, 68, 64);
        painel.add(btnSair);
    }

    private String[] gerarNotas() {
        String[] notas = new String[21];
        int pos = 0;
        for (double n = 0; n <= 10.0; n += 0.5) {
            notas[pos++] = String.format("%.1f", n).replace('.', ',');
        }
        return notas;
    }

    // =========================================================
    // VALIDAÇÃO DOS CAMPOS NA DIGITAÇÃO
    // =========================================================
    private void aplicarValidacoesCampos() {
        aplicarFiltroNumeros(txtRgm, "RGM", 8);
        aplicarFiltroLetras(txtNome, "Nome", 50);
        aplicarFiltroLimite(txtEmail, "Email", 100);
        aplicarFiltroLimite(txtEndereco, "Endereço", 80);

        aplicarFiltroNumeros(txtNotasRgm, "RGM", 8);
        aplicarFiltroNumeros(txtBoletimRgm, "RGM", 8);
        aplicarFiltroFaltas(txtFaltas, "Faltas", 15);
    }

    private void aplicarFiltroNumeros(JTextField campo, String nomeCampo, int limite) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroNumeros(this, nomeCampo, limite));
    }

    private void aplicarFiltroLetras(JTextField campo, String nomeCampo, int limite) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroLetras(this, nomeCampo, limite));
    }

    private void aplicarFiltroLimite(JTextField campo, String nomeCampo, int limite) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroLimite(this, nomeCampo, limite));
    }

    private void aplicarFiltroFaltas(JTextField campo, String nomeCampo, int maximo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroFaltas(this, nomeCampo, maximo));
    }

    // =========================================================
    // MUNICÍPIO AUTOMÁTICO PELO UF
    // =========================================================
    private void atualizarMunicipioPorUf() {
        if (cbUf == null || txtMunicipio == null || cbUf.getSelectedItem() == null) return;

        String uf = cbUf.getSelectedItem().toString();
        switch (uf) {
            case "SP":
                txtMunicipio.setText("São Paulo");
                break;
            case "RJ":
                txtMunicipio.setText("Rio de Janeiro");
                break;
            case "MG":
                txtMunicipio.setText("Belo Horizonte");
                break;
            case "PR":
                txtMunicipio.setText("Curitiba");
                break;
            case "SC":
                txtMunicipio.setText("Florianópolis");
                break;
            case "RS":
                txtMunicipio.setText("Porto Alegre");
                break;
            case "BA":
                txtMunicipio.setText("Salvador");
                break;
            case "GO":
                txtMunicipio.setText("Goiânia");
                break;
            case "DF":
                txtMunicipio.setText("Brasília");
                break;
            case "PE":
                txtMunicipio.setText("Recife");
                break;
            case "CE":
                txtMunicipio.setText("Fortaleza");
                break;
            default:
                txtMunicipio.setText("");
                break;
        }
    }

    // =========================================================
    // CRUD - ALUNOS
    // =========================================================
    private void salvarAluno() {
        if (!validarAluno()) return;

        try {
            alunoDAO.salvar(montarAlunoDaTela());
            JOptionPane.showMessageDialog(this, "Aluno cadastrado com sucesso!");
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Este RGM já está cadastrado.", "RGM duplicado", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            mostrarErroBanco("Erro ao salvar aluno", ex);
        }
    }

    private void alterarAluno() {
        if (!validarAluno()) return;

        try {
            int linhas = alunoDAO.alterar(montarAlunoDaTela());

            if (linhas > 0) {
                JOptionPane.showMessageDialog(this, "Aluno alterado com sucesso!");
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum aluno encontrado com este RGM.");
            }
        } catch (SQLException ex) {
            mostrarErroBanco("Erro ao alterar aluno", ex);
        }
    }

    private Aluno montarAlunoDaTela() {
        return new Aluno(
                txtRgm.getText().trim(),
                txtNome.getText().trim(),
                txtDataNasc.getText().trim(),
                txtCpf.getText().trim(),
                txtEmail.getText().trim(),
                txtEndereco.getText().trim(),
                txtMunicipio.getText().trim(),
                cbUf.getSelectedItem().toString(),
                txtCelular.getText().trim(),
                cbCurso.getSelectedItem().toString(),
                cbCampus.getSelectedItem().toString(),
                obterPeriodoSelecionado()
        );
    }

    private boolean consultarAluno(boolean mostrarMensagem) {
        if (txtRgm.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o RGM para consultar.");
            return false;
        }

        try {
            Aluno aluno = alunoDAO.consultarPorRgm(txtRgm.getText().trim());

            if (aluno != null) {
                carregarAlunoNosCampos(aluno);
                if (mostrarMensagem) {
                    JOptionPane.showMessageDialog(this, "Aluno encontrado!");
                }
                return true;
            }

            if (mostrarMensagem) {
                JOptionPane.showMessageDialog(this, "Aluno não encontrado.");
            }
        } catch (SQLException ex) {
            mostrarErroBanco("Erro ao consultar aluno", ex);
        }

        return false;
    }

    private void carregarAlunoNosCampos(Aluno aluno) {
        txtRgm.setText(aluno.getRgm());
        txtNome.setText(aluno.getNome());
        txtDataNasc.setText(aluno.getDataNascimento());
        txtCpf.setText(aluno.getCpf());
        txtEmail.setText(aluno.getEmail());
        txtEndereco.setText(aluno.getEndereco());
        txtMunicipio.setText(aluno.getMunicipio());
        cbUf.setSelectedItem(aluno.getUf());
        txtCelular.setText(aluno.getCelular());
        cbCurso.setSelectedItem(aluno.getCurso());
        cbCampus.setSelectedItem(aluno.getCampus());
        selecionarPeriodo(aluno.getPeriodo());
    }

    private void excluirAluno() {
        String rgm = txtRgm.getText().trim();
        if (rgm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o RGM do aluno que deseja excluir.");
            return;
        }

        int opcao = JOptionPane.showConfirmDialog(
                this,
                "Deseja excluir este aluno?\nAs notas e faltas também serão excluídas automaticamente.",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION
        );

        if (opcao != JOptionPane.YES_OPTION) return;

        try {
            int linhas = alunoDAO.excluir(rgm);

            if (linhas > 0) {
                JOptionPane.showMessageDialog(this, "Aluno excluído com sucesso!");
                limparCamposAluno();
                limparCamposNotas();
                limparBoletim();
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum aluno encontrado com este RGM.");
            }
        } catch (SQLException ex) {
            mostrarErroBanco("Erro ao excluir aluno", ex);
        }
    }

    // =========================================================
    // CRUD - NOTAS E FALTAS
    // =========================================================
    private void salvarNotaFalta() {
        String rgm = txtNotasRgm.getText().trim();
        if (rgm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o RGM do aluno.");
            return;
        }

        if (!carregarAlunoNaAbaNotas(false)) {
            JOptionPane.showMessageDialog(this, "Não existe aluno cadastrado com este RGM.");
            return;
        }

        int faltas;
        double nota;

        try {
            faltas = Integer.parseInt(txtFaltas.getText().trim());
            nota = converterNota(cbNota.getSelectedItem().toString());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite uma nota e quantidade de faltas válidas.");
            return;
        }

        if (nota < 0 || nota > 10) {
            JOptionPane.showMessageDialog(this, "A nota deve estar entre 0 e 10.");
            return;
        }

        if (faltas < 0 || faltas > 15) {
            JOptionPane.showMessageDialog(this, "A quantidade de faltas deve estar entre 0 e 15.");
            return;
        }

        NotaFalta notaFalta = new NotaFalta(
                rgm,
                cbDisciplina.getSelectedItem().toString(),
                cbSemestre.getSelectedItem().toString(),
                nota,
                faltas
        );

        try {
            notaFaltaDAO.salvar(notaFalta);
            JOptionPane.showMessageDialog(this, "Nota e faltas salvas com sucesso!");
        } catch (SQLException ex) {
            mostrarErroBanco("Erro ao salvar nota/falta", ex);
        }
    }

    private boolean carregarAlunoNaAbaNotas(boolean mostrarMensagem) {
        String rgm = txtNotasRgm.getText().trim();
        if (rgm.isEmpty()) {
            if (mostrarMensagem) JOptionPane.showMessageDialog(this, "Informe o RGM do aluno.");
            return false;
        }

        try {
            Aluno aluno = alunoDAO.consultarPorRgm(rgm);

            if (aluno != null) {
                txtNotasNome.setText(aluno.getNome());
                txtNotasCurso.setText(aluno.getCurso());
                if (mostrarMensagem) JOptionPane.showMessageDialog(this, "Aluno carregado na aba Notas e Faltas.");
                return true;
            }

            txtNotasNome.setText("");
            txtNotasCurso.setText("");
            if (mostrarMensagem) JOptionPane.showMessageDialog(this, "Aluno não encontrado.");
        } catch (SQLException ex) {
            mostrarErroBanco("Erro ao consultar aluno", ex);
        }

        return false;
    }

    private void excluirNotasFaltas() {
        String rgm = txtNotasRgm.getText().trim();
        if (rgm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o RGM do aluno.");
            return;
        }

        int opcao = JOptionPane.showConfirmDialog(
                this,
                "Deseja excluir todas as notas e faltas deste aluno?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION
        );

        if (opcao != JOptionPane.YES_OPTION) return;

        try {
            int linhas = notaFaltaDAO.excluirPorRgm(rgm);
            JOptionPane.showMessageDialog(this, "Registros excluídos: " + linhas);
        } catch (SQLException ex) {
            mostrarErroBanco("Erro ao excluir notas/faltas", ex);
        }
    }

    // =========================================================
    // BOLETIM
    // =========================================================
    private void gerarBoletim() {
        limparBoletim();

        String rgm = txtBoletimRgm.getText().trim();
        if (rgm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o RGM para gerar o boletim.");
            return;
        }

        try {
            Aluno aluno = alunoDAO.consultarPorRgm(rgm);

            if (aluno == null) {
                JOptionPane.showMessageDialog(this, "Aluno não encontrado.");
                return;
            }

            lblBoletimRgm.setText("RGM: " + aluno.getRgm());
            lblBoletimNome.setText("Nome: " + aluno.getNome());
            lblBoletimCurso.setText("Curso: " + aluno.getCurso());

            List<NotaFalta> notas = notaFaltaDAO.listarPorRgm(rgm);

            double somaNotas = 0;
            int somaFaltas = 0;
            int totalDisciplinas = 0;
            int reprovacoes = 0;

            for (NotaFalta notaFalta : notas) {
                String situacao = calcularSituacao(notaFalta.getNota(), notaFalta.getFaltas());

                if (!"Aprovado".equals(situacao)) {
                    reprovacoes++;
                }

                somaNotas += notaFalta.getNota();
                somaFaltas += notaFalta.getFaltas();
                totalDisciplinas++;

                modeloBoletim.addRow(new Object[]{
                        notaFalta.getDisciplina(),
                        notaFalta.getSemestre(),
                        formatarNota(notaFalta.getNota()),
                        notaFalta.getFaltas(),
                        situacao
                });
            }

            if (totalDisciplinas == 0) {
                lblBoletimStatus.setText("Status: aluno sem notas e faltas cadastradas.");
                lblBoletimStatus.setForeground(Color.BLACK);
                return;
            }

            double media = somaNotas / totalDisciplinas;
            if (reprovacoes == 0) {
                lblBoletimStatus.setText("Status: APROVADO | Média geral: " + formatarNota(media) + " | Faltas: " + somaFaltas);
                lblBoletimStatus.setForeground(new Color(0, 120, 0));
            } else {
                lblBoletimStatus.setText("Status: ATENÇÃO | Média geral: " + formatarNota(media) + " | Reprovações: " + reprovacoes);
                lblBoletimStatus.setForeground(new Color(180, 0, 0));
            }
        } catch (SQLException ex) {
            mostrarErroBanco("Erro ao gerar boletim", ex);
        }
    }

    private String calcularSituacao(double nota, int faltas) {
        if (nota >= 6.0 && faltas < 15) return "Aprovado";
        if (nota < 6.0 && faltas >= 15) return "Reprovado por nota/falta";
        if (nota < 6.0) return "Reprovado por nota";
        return "Reprovado por falta";
    }

    // =========================================================
    // VALIDAÇÕES E LIMPEZA
    // =========================================================
    private boolean validarAluno() {
        String rgm = txtRgm.getText().trim();
        String nome = txtNome.getText().trim();
        String email = txtEmail.getText().trim();
        String endereco = txtEndereco.getText().trim();
        String municipio = txtMunicipio.getText().trim();

        if (rgm.isEmpty() || nome.isEmpty() || email.isEmpty() || endereco.isEmpty() || municipio.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios do aluno.");
            return false;
        }

        if (!rgm.matches("\\d+") || rgm.length() > 8) {
            JOptionPane.showMessageDialog(this, "O RGM deve conter apenas números e no máximo 8 dígitos.");
            return false;
        }

        if (!nome.matches("[a-zA-ZÀ-ÿ\\s]+") || nome.length() > 50) {
            JOptionPane.showMessageDialog(this, "O nome deve conter apenas letras e no máximo 50 caracteres.");
            return false;
        }

        if (endereco.length() > 80) {
            JOptionPane.showMessageDialog(this, "O endereço deve ter no máximo 80 caracteres.");
            return false;
        }

        if (email.length() > 100 || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Digite um email válido com no máximo 100 caracteres.");
            return false;
        }

        if (!mascaraPossuiNumeros(txtDataNasc, 8)) {
            JOptionPane.showMessageDialog(this, "Preencha a data de nascimento corretamente.");
            return false;
        }

        if (!dataValida(txtDataNasc.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Data de nascimento inválida.");
            return false;
        }

        if (!mascaraPossuiNumeros(txtCpf, 11)) {
            JOptionPane.showMessageDialog(this, "Preencha o CPF corretamente.");
            return false;
        }

        if (!mascaraPossuiNumeros(txtCelular, 11)) {
            JOptionPane.showMessageDialog(this, "Preencha o celular corretamente.");
            return false;
        }

        return true;
    }

    private boolean mascaraPossuiNumeros(JFormattedTextField campo, int quantidade) {
        String numeros = campo.getText().replaceAll("\\D", "");
        return numeros.length() == quantidade;
    }

    private boolean dataValida(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            sdf.parse(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String obterPeriodoSelecionado() {
        if (rbMatutino.isSelected()) return "Matutino";
        if (rbVespertino.isSelected()) return "Vespertino";
        return "Noturno";
    }

    private void selecionarPeriodo(String periodo) {
        if ("Matutino".equalsIgnoreCase(periodo)) {
            rbMatutino.setSelected(true);
        } else if ("Vespertino".equalsIgnoreCase(periodo)) {
            rbVespertino.setSelected(true);
        } else {
            rbNoturno.setSelected(true);
        }
    }

    private double converterNota(String valor) {
        return Double.parseDouble(valor.trim().replace(',', '.'));
    }

    private String formatarNota(double nota) {
        return String.format("%.1f", nota).replace('.', ',');
    }

    private void limparCamposAluno() {
        txtRgm.setText("");
        txtNome.setText("");
        txtDataNasc.setText("");
        txtCpf.setText("");
        txtEmail.setText("");
        txtEndereco.setText("");
        cbUf.setSelectedIndex(0);
        atualizarMunicipioPorUf();
        txtCelular.setText("");
        cbCurso.setSelectedIndex(0);
        cbCampus.setSelectedIndex(0);
        rbNoturno.setSelected(true);
    }

    private void limparCamposNotas() {
        txtNotasRgm.setText("");
        txtNotasNome.setText("");
        txtNotasCurso.setText("");
        cbDisciplina.setSelectedIndex(0);
        cbSemestre.setSelectedIndex(0);
        cbNota.setSelectedIndex(0);
        txtFaltas.setText("");
    }

    private void limparBoletim() {
        lblBoletimRgm.setText("RGM: -");
        lblBoletimNome.setText("Nome: -");
        lblBoletimCurso.setText("Curso: -");
        lblBoletimStatus.setText("Status: -");
        lblBoletimStatus.setForeground(Color.BLACK);
        if (modeloBoletim != null) {
            modeloBoletim.setRowCount(0);
        }
    }

    private void mostrarErroBanco(String titulo, SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, titulo + ":\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void sair() {
        int opcao = JOptionPane.showConfirmDialog(this, "Deseja sair do sistema?", "Sair", JOptionPane.YES_NO_OPTION);
        if (opcao == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new SistemaAcademico().setVisible(true));
    }
}

