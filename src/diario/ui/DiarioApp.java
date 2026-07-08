package diario.ui;

import diario.model.*;
import diario.storage.Armazenamento;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class DiarioApp extends JFrame {
    private Armazenamento storage;
    private DefaultListModel<String> modeloCadernos;
    private DefaultListModel<String> modeloPaginas;
    private JList<String> listaCadernos;
    private JList<String> listaPaginas;
    private JTextArea areaTexto;
    private Caderno cadernoAtual;
    private Pagina paginaAtual;

    public DiarioApp() {
        // Define diretório padrão: ~/.diario
        String home = System.getProperty("user.home");
        storage = new Armazenamento(Path.of(home, "diario_App"));
        initComponents();
        carregarCadernos();
    }

    private void initComponents() {
        setTitle("Diário Pessoal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Painel esquerdo (cadernos e páginas)
        JPanel painelEsquerdo = new JPanel(new GridLayout(2,1));
        // --- Lista de cadernos ---
        modeloCadernos = new DefaultListModel<>();
        listaCadernos = new JList<>(modeloCadernos);
        listaCadernos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String nome = listaCadernos.getSelectedValue();
                if (nome != null) {
                    cadernoAtual = storage.carregarCaderno(nome);
                    atualizarPaginas();
                }
            }
        });
        JScrollPane scrollCadernos = new JScrollPane(listaCadernos);
        scrollCadernos.setBorder(BorderFactory.createTitledBorder("Cadernos"));
        painelEsquerdo.add(scrollCadernos);

        // --- Lista de páginas ---
        modeloPaginas = new DefaultListModel<>();
        listaPaginas = new JList<>(modeloPaginas);
        listaPaginas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = listaPaginas.getSelectedIndex();
                if (cadernoAtual != null && idx >= 0 && idx < cadernoAtual.getPaginas().size()) {
                    paginaAtual = cadernoAtual.getPaginas().get(idx);
                    areaTexto.setText(paginaAtual.getConteudo());
                }
            }
        });
        JScrollPane scrollPaginas = new JScrollPane(listaPaginas);
        scrollPaginas.setBorder(BorderFactory.createTitledBorder("Páginas"));
        painelEsquerdo.add(scrollPaginas);

        add(painelEsquerdo, BorderLayout.WEST);

        // Área central (texto)
        areaTexto = new JTextArea();
        areaTexto.setBorder(BorderFactory.createTitledBorder("Conteúdo"));
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        // Painel inferior (botões)
        JPanel botoes = new JPanel(new FlowLayout());
        JButton btnNovoCaderno = new JButton("Novo Caderno");
        JButton btnNovaPagina = new JButton("Nova Página");
        JButton btnSalvar = new JButton("Salvar");
        JButton btnRenomear = new JButton("Renomear");
        JButton btnExcluir = new JButton("Excluir");

        btnNovoCaderno.addActionListener(e -> novoCaderno());
        btnNovaPagina.addActionListener(e -> novaPagina());
        btnSalvar.addActionListener(e -> salvarPagina());
        btnRenomear.addActionListener(e -> renomear());
        btnExcluir.addActionListener(e -> excluir());

        botoes.add(btnNovoCaderno);
        botoes.add(btnNovaPagina);
        botoes.add(btnSalvar);
        botoes.add(btnRenomear);
        botoes.add(btnExcluir);
        add(botoes, BorderLayout.SOUTH);
    }

    private void carregarCadernos() {
        modeloCadernos.clear();
        List<String> nomes = storage.listarCadernos();
        for (String n : nomes) modeloCadernos.addElement(n);
    }

    private void atualizarPaginas() {
        modeloPaginas.clear();
        if (cadernoAtual != null) {
            for (Pagina p : cadernoAtual.getPaginas()) {
                modeloPaginas.addElement(p.getTitulo());
            }
        }
    }

    private void novoCaderno() {
        String nome = JOptionPane.showInputDialog(this, "Nome do caderno:");
        if (nome != null && !nome.trim().isEmpty()) {
            Caderno novo = new Caderno(nome);
            storage.salvarCaderno(novo); // Salva o caderno (cria a pasta)
            carregarCadernos();
        }
    }

    private void novaPagina() {
        if (cadernoAtual == null) {
            JOptionPane.showMessageDialog(this, "Selecione um caderno primeiro.");
            return;
        }
        String titulo = JOptionPane.showInputDialog(this, "Título da página:");
        if (titulo != null && !titulo.trim().isEmpty()) {
            Pagina p = new Pagina(titulo);
            cadernoAtual.adicionarPagina(p);
            storage.salvarPagina(cadernoAtual, p);
            atualizarPaginas();
        }
    }

    private void salvarPagina() {
        if (paginaAtual != null) {
            paginaAtual.setConteudo(areaTexto.getText());
            storage.salvarPagina(cadernoAtual, paginaAtual);
            JOptionPane.showMessageDialog(this, "Página salva.");
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma página para salvar.");
        }
    }

    private void renomearPaginaSelecionada(int idx) {
        Pagina p = cadernoAtual.getPaginas().get(idx);
        String novoTitulo = JOptionPane.showInputDialog(this, "Novo título:", p.getTitulo());
        if (novoTitulo == null || novoTitulo.trim().isEmpty()) return;

        boolean ok = storage.renomearPagina(cadernoAtual, p, novoTitulo);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Já existe uma página com esse título.");
            return;
        }
        p.setTitulo(novoTitulo);
        atualizarPaginas();
    }

    private void renomearCadernoSelecionado(String nomeCaderno) {
        String novoNome = JOptionPane.showInputDialog(this, "Novo nome do caderno:", nomeCaderno);
        if (novoNome == null || novoNome.trim().isEmpty()) return;

        boolean ok = storage.renomearCaderno(nomeCaderno, novoNome);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Já existe um caderno com esse nome.");
            return;
        }

        if (cadernoAtual != null && cadernoAtual.getNome().equals(nomeCaderno)) {
            cadernoAtual.setNome(novoNome);
        }
        carregarCadernos();
        listaCadernos.setSelectedValue(novoNome, true);
    }

    private void excluir() {
        int idxPagina = listaPaginas.getSelectedIndex();
        if (cadernoAtual != null && idxPagina >= 0) {
            excluirPaginaSelecionada(idxPagina);
            return;
        }

        String nomeCaderno = listaCadernos.getSelectedValue();
        if (nomeCaderno != null) {
            excluirCadernoSelecionado(nomeCaderno);
            return;
        }

        JOptionPane.showMessageDialog(this, "Selecione um caderno ou uma página para excluir.");
    }

    private void renomear() {
        int idxPagina = listaPaginas.getSelectedIndex();
        if (cadernoAtual != null && idxPagina >= 0) {
            renomearPaginaSelecionada(idxPagina);
            return;
        }

        String nomeCaderno = listaCadernos.getSelectedValue();
        if (nomeCaderno != null) {
            renomearCadernoSelecionado(nomeCaderno);
            return;
        }

        JOptionPane.showMessageDialog(this, "Selecione um caderno ou uma página para renomear.");
    }

    private void excluirPaginaSelecionada(int idx) {
        Pagina p = cadernoAtual.getPaginas().get(idx);
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Excluir a página \"" + p.getTitulo() + "\"?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (confirmacao != JOptionPane.YES_OPTION) return;

        storage.excluirPagina(cadernoAtual, p);
        cadernoAtual.removerPagina(p);
        if (paginaAtual == p) {
            paginaAtual = null;
            areaTexto.setText("");
        }
        atualizarPaginas();
    }

    private void excluirCadernoSelecionado(String nomeCaderno) {
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Excluir o caderno \"" + nomeCaderno + "\" e todas as suas páginas?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (confirmacao != JOptionPane.YES_OPTION) return;

        storage.excluirCaderno(nomeCaderno);
        if (cadernoAtual != null && cadernoAtual.getNome().equals(nomeCaderno)) {
            cadernoAtual = null;
            paginaAtual = null;
            areaTexto.setText("");
            modeloPaginas.clear();
        }
        carregarCadernos();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DiarioApp().setVisible(true);
        });
    }
}