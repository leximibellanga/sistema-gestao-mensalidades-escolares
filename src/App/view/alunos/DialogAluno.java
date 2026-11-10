package App.view.alunos;

import App.model.Aluno;
import App.model.Turma;
import App.service.AlunoService;
import App.service.TurmaService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DialogAluno extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlunoService alunoService = new AlunoService();
    private final TurmaService turmaService = new TurmaService();
    private final Aluno aluno; // null = novo aluno
    private boolean salvo = false;

    private final JTextField txtNumero = new JTextField(22);
    private final JTextField txtNome = new JTextField(22);
    private final JTextField txtEncarregado = new JTextField(22);
    private final JTextField txtContacto = new JTextField(22);
    private final JComboBox<Turma> cmbTurma = new JComboBox<>();
    private final JTextField txtData = new JTextField(22);

    public DialogAluno(Frame owner, Aluno aluno) {
        super(owner, aluno == null ? "Novo Aluno" : "Editar Aluno", true);
        this.aluno = aluno;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        adicionarLinha(form, 0, "Nr. Estudante:", txtNumero);
        adicionarLinha(form, 1, "Nome:", txtNome);
        adicionarLinha(form, 2, "Encarregado:", txtEncarregado);
        adicionarLinha(form, 3, "Contacto:", txtContacto);
        adicionarLinha(form, 4, "Turma:", cmbTurma);
        adicionarLinha(form, 5, "Data de Matrícula:", txtData);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botoes.add(btnCancelar);
        botoes.add(btnSalvar);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnSalvar);

        carregarTurmas();
        if (aluno != null) {
            preencher(aluno);
        } else {
            txtData.setText(LocalDate.now().format(FORMATO_DATA));
        }

        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean foiSalvo() {
        return salvo;
    }

    private void adicionarLinha(JPanel form, int linha, String rotulo, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = linha;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, 0, 6, 14);
        form.add(new JLabel(rotulo), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);
        form.add(campo, c);
    }

    private void carregarTurmas() {
        try {
            for (Turma t : turmaService.listarTodas()) {
                cmbTurma.addItem(t);
            }
        } catch (RuntimeException e) {
            erro("Erro ao carregar as turmas: " + e.getMessage());
        }
    }

    private void preencher(Aluno a) {
        txtNumero.setText(a.getNumeroEstudante());
        txtNome.setText(a.getNome());
        txtEncarregado.setText(a.getEncarregado());
        txtContacto.setText(a.getContacto());
        txtData.setText(a.getDataMatricula().format(FORMATO_DATA));

        for (int i = 0; i < cmbTurma.getItemCount(); i++) {
            if (cmbTurma.getItemAt(i).getId() == a.getTurma().getId()) {
                cmbTurma.setSelectedIndex(i);
                break;
            }
        }
    }

    private void salvar() {
        LocalDate data;
        try {
            data = LocalDate.parse(txtData.getText().trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            erro("Data de matrícula inválida. Use o formato dd/MM/aaaa.");
            return;
        }

        // Objeto novo: se a gravação falhar, a linha da tabela não fica alterada
        Aluno a = new Aluno();
        if (aluno != null) {
            a.setId(aluno.getId());
        }
        a.setNumeroEstudante(txtNumero.getText().trim());
        a.setNome(txtNome.getText().trim());
        a.setEncarregado(txtEncarregado.getText().trim());
        a.setContacto(txtContacto.getText().trim());
        a.setTurma((Turma) cmbTurma.getSelectedItem());
        a.setDataMatricula(data);

        try {
            if (aluno == null) {
                alunoService.cadastrar(a);
            } else {
                alunoService.atualizar(a);
            }
            salvo = true;
            dispose();
        } catch (IllegalArgumentException e) {
            erro(e.getMessage());
        } catch (RuntimeException e) {
            erro("Não foi possível gravar o aluno.\n"
               + "Verifique se o número de estudante já existe.\n\n" + e.getMessage());
        }
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}