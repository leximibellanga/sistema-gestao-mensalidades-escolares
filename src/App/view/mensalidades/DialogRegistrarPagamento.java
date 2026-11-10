package App.view.mensalidades;

import App.model.Mensalidade;
import App.model.Pagamento;
import App.service.MensalidadeService;

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
import java.util.Locale;

public class DialogRegistrarPagamento extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MM/yyyy");

    private final MensalidadeService service = new MensalidadeService();
    private final Mensalidade mensalidade;
    private Pagamento pagamento; // null = nada foi registado

    private final JTextField txtValor = new JTextField(20);
    private final JTextField txtData = new JTextField(20);
    private final JComboBox<String> cmbMetodo = new JComboBox<>(
            new String[]{"Numerário", "M-Pesa", "e-Mola", "Transferência bancária"});

    public DialogRegistrarPagamento(Frame owner, Mensalidade mensalidade) {
        super(owner, "Registar Pagamento", true);
        this.mensalidade = mensalidade;

        txtValor.setText(String.format(Locale.US, "%.2f", mensalidade.getValor()));
        txtValor.setEditable(false); // pagamento integral
        txtData.setText(LocalDate.now().format(FORMATO_DATA));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        adicionarLinha(form, 0, "Aluno:", new JLabel(mensalidade.getAluno().getNome()));
        adicionarLinha(form, 1, "Mês de referência:",
                new JLabel(mensalidade.getMesReferencia().format(FORMATO_MES)));
        adicionarLinha(form, 2, "Valor a pagar (MT):", txtValor);
        adicionarLinha(form, 3, "Data do pagamento:", txtData);
        adicionarLinha(form, 4, "Método:", cmbMetodo);

        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar = new JButton("Cancelar");
        btnConfirmar.addActionListener(e -> confirmar());
        btnCancelar.addActionListener(e -> dispose());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botoes.add(btnCancelar);
        botoes.add(btnConfirmar);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnConfirmar);

        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Devolve o pagamento registado, ou null se o utilizador cancelou. */
    public Pagamento getPagamento() {
        return pagamento;
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

    private void confirmar() {
        LocalDate data;
        try {
            data = LocalDate.parse(txtData.getText().trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            erro("Data inválida. Use o formato dd/MM/aaaa.");
            return;
        }

        try {
            pagamento = service.registrarPagamento(
                    mensalidade, mensalidade.getValor(), data,
                    (String) cmbMetodo.getSelectedItem());
            dispose();
        } catch (IllegalArgumentException | IllegalStateException e) {
            erro(e.getMessage());
        } catch (RuntimeException e) {
            erro("Não foi possível registar o pagamento.\n\n" + e.getMessage());
        }
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}