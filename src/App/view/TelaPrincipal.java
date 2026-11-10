package App.view;

import App.view.alunos.PainelAlunos;
import App.view.turmas.PainelTurmas;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class TelaPrincipal extends JFrame {

    public static final String CARD_ALUNOS = "ALUNOS";
    public static final String CARD_TURMAS = "TURMAS";
    public static final String CARD_MENSALIDADES = "MENSALIDADES";
    public static final String CARD_RELATORIOS = "RELATORIOS";

    private static final Color COR_SIDEBAR = new Color(45, 45, 48);
    private static final Color COR_SIDEBAR_HOVER = new Color(62, 62, 66);
    private static final Color COR_SIDEBAR_ATIVO = new Color(85, 85, 90);
    private static final Color COR_CONTEUDO = new Color(238, 238, 238);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pnlConteudo = new JPanel(cardLayout);
    private final Map<String, JButton> botoesMenu = new LinkedHashMap<>();
    private String cardAtivo;

    public TelaPrincipal() {
        super("SGME - Sistema de Gestão de Mensalidades Escolares");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(criarSidebar(), BorderLayout.WEST);
        add(criarAreaConteudo(), BorderLayout.CENTER);

        mostrarCard(CARD_TURMAS); // ecrã inicial
    }

    private JPanel criarSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel logo = new JLabel("SGME");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));
        logo.setBorder(BorderFactory.createEmptyBorder(28, 24, 28, 24));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);

        sidebar.add(criarBotaoMenu("Alunos", CARD_ALUNOS));
        sidebar.add(criarBotaoMenu("Turmas", CARD_TURMAS));
        sidebar.add(criarBotaoMenu("Mensalidades", CARD_MENSALIDADES));
        sidebar.add(criarBotaoMenu("Relatórios", CARD_RELATORIOS));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton criarBotaoMenu(String texto, String card) {
        JButton btn = new JButton(texto);
        btn.setUI(new BasicButtonUI()); // garante que a cor de fundo funciona em qualquer Look and Feel
        btn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COR_SIDEBAR);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setPreferredSize(new Dimension(220, 46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!card.equals(cardAtivo)) {
                    btn.setBackground(COR_SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!card.equals(cardAtivo)) {
                    btn.setBackground(COR_SIDEBAR);
                }
            }
        });
        btn.addActionListener(e -> mostrarCard(card));

        botoesMenu.put(card, btn);
        return btn;
    }

    private JPanel criarAreaConteudo() {
        pnlConteudo.setBackground(COR_CONTEUDO);

        // Painéis provisórios: vamos substituir cada linha pelo painel real
        pnlConteudo.add(new PainelAlunos(), CARD_ALUNOS);
        pnlConteudo.add(new PainelTurmas(), CARD_TURMAS);
        pnlConteudo.add(criarPlaceholder("Mensalidades"), CARD_MENSALIDADES);
        pnlConteudo.add(criarPlaceholder("Relatórios"), CARD_RELATORIOS);

        return pnlConteudo;
    }

    private JPanel criarPlaceholder(String titulo) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_CONTEUDO);

        JLabel lbl = new JLabel("Ecrã de " + titulo + " (em construção)", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lbl.setForeground(new Color(110, 110, 110));
        painel.add(lbl, BorderLayout.CENTER);

        return painel;
    }

    private void mostrarCard(String card) {
        cardLayout.show(pnlConteudo, card);
        cardAtivo = card;
        botoesMenu.forEach((chave, btn) ->
                btn.setBackground(chave.equals(card) ? COR_SIDEBAR_ATIVO : COR_SIDEBAR));
    }
}