package App.view.turmas;

import App.model.Turma;
import App.service.TurmaService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.util.Locale;

public class DialogTurma extends JDialog {

    private final TurmaService turmaService = new TurmaService();
    private final Turma turma; // null = nova turma
    private boolean salvo = false;

    private final JTextField txtNome = new JTextField(20);
    private final JTextField txtValor = new JTextField(20);

    public DialogTurma(Frame owner, Turma turma) {
        super(owner, turma == null ? "Nova Turma" : "Editar Turma", true);
        this.turma = turma;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        adicionarLinha(form, 0, "Nome da Turma:", txtNome);
        adicionarLinha(form, 1, "Valor da Mensalidade (MT):", txtValor);

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

        if (turma != null) {
            txtNome.setText(turma.getNome());
            txtValor.setText(String.format(Locale.US, "%.2f", turma.getValorMensalidade()));
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

    private void salvar() {
        double valor;
        try {
            // aceita "6300", "6300.50" e "6300,50"
            String texto = txtValor.getText().trim().replace(",", ".");
            valor = Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            erro("Valor da mensalidade inválido. Use apenas números (ex: 6300.00).");
            return;
        }

        // Objeto novo: se a gravação falhar, a linha da tabela não fica alterada
        Turma t = new Turma();
        if (turma != null) {
            t.setId(turma.getId());
        }
        t.setNome(txtNome.getText().trim());
        t.setValorMensalidade(valor);

        try {
            if (turma == null) {
                turmaService.cadastrar(t);
            } else {
                turmaService.atualizar(t);
            }
            salvo = true;
            dispose();
        } catch (IllegalArgumentException e) {
            erro(e.getMessage());
        } catch (RuntimeException e) {
            erro("Não foi possível gravar a turma.\n\n" + e.getMessage());
        }
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    void setVisible(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}