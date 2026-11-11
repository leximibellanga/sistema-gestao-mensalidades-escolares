package App.view.alunos;

import App.model.Aluno;
import App.service.AlunoService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PainelAlunos extends JPanel {

    private static final Color COR_FUNDO = new Color(238, 238, 238);

    private final AlunoService alunoService = new AlunoService();
    private final TabelaAlunosModel tabelaModel = new TabelaAlunosModel();
    private final JTable tblAlunos = new JTable(tabelaModel);

    public PainelAlunos() {
        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);

        add(criarTopo(), BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);

        carregarTabela();
    }

    private JPanel criarTopo() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        barra.setBackground(new Color(214, 214, 214));
        barra.add(criarBotao("Novo Aluno", e -> novoAluno()));
        barra.add(criarBotao("Atualizar Lista", e -> carregarTabela()));
        barra.add(criarBotao("Editar Aluno", e -> editarAluno()));
        barra.add(criarBotao("Remover Aluno", e -> removerAluno()));

        JLabel titulo = new JLabel("Gestão de Alunos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 20, 12, 20));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(barra, BorderLayout.NORTH);
        topo.add(titulo, BorderLayout.CENTER);
        return topo;
    }

    private JPanel criarTabela() {
        tblAlunos.setRowHeight(30);
        tblAlunos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAlunos.setShowVerticalLines(false);
        tblAlunos.setFillsViewportHeight(true);
        tblAlunos.getTableHeader().setReorderingAllowed(false);
        tblAlunos.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Linhas alternadas (zebra) e espaçamento nas células
        tblAlunos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionada, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, selecionada, false, linha, coluna);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!selecionada) {
                    setBackground(linha % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
                return this;
            }
        });

        int[] larguras = {120, 240, 200, 120, 100, 120};
        for (int i = 0; i < larguras.length; i++) {
            tblAlunos.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Duplo clique numa linha abre a edição
        tblAlunos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblAlunos.getSelectedRow() >= 0) {
                    editarAluno();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblAlunos);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170)));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JButton criarBotao(String texto, ActionListener acao) {
        JButton btn = new JButton(texto);
        btn.setFocusPainted(false);
        btn.addActionListener(acao);
        return btn;
    }

    // ---------- Ações ----------

    private void carregarTabela() {
        try {
            List<Aluno> alunos = alunoService.listarTodos();
            tabelaModel.atualizarDados(alunos);
        } catch (RuntimeException e) {
            erro("Erro ao carregar os alunos:\n" + e.getMessage());
        }
    }

    private void novoAluno() {
        DialogAluno dialog = new DialogAluno(janelaPai(), null);
        dialog.setVisible(true);
        if (dialog.foiSalvo()) {
            carregarTabela();
        }
    }

    private void editarAluno() {
        Aluno aluno = alunoSelecionado();
        if (aluno == null) {
            return;
        }
        DialogAluno dialog = new DialogAluno(janelaPai(), aluno);
        dialog.setVisible(true);
        if (dialog.foiSalvo()) {
            carregarTabela();
        }
    }

    private void removerAluno() {
        Aluno aluno = alunoSelecionado();
        if (aluno == null) {
            return;
        }
        int opcao = JOptionPane.showConfirmDialog(this,
                "Remover o aluno \"" + aluno.getNome() + "\"?",
                "Confirmar remoção", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            alunoService.remover(aluno.getNumeroEstudante());
            carregarTabela();
        } catch (RuntimeException e) {
            erro("Não foi possível remover o aluno.\n"
               + "Ele pode ter mensalidades associadas.\n\n" + e.getMessage());
        }
    }

    // ---------- Auxiliares ----------

    private Aluno alunoSelecionado() {
        int linha = tblAlunos.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um aluno na tabela.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return tabelaModel.getAlunoNaLinha(linha);
    }

    private Frame janelaPai() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}