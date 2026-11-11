package App.view.mensalidades;

import App.model.Mensalidade;
import App.model.StatusMensalidade;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TabelaMensalidadesModel extends AbstractTableModel {

    public static final int COL_STATUS = 5;

    private static final String[] COLUNAS = {
        "Nr. Estudante", "Aluno", "Turma", "Mês Ref.", "Valor", "Status"
    };
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MM/yyyy");

    private List<Mensalidade> mensalidades = new ArrayList<>();

    @Override
    public int getRowCount() {
        return mensalidades.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUNAS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Mensalidade m = mensalidades.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> m.getAluno().getNumeroEstudante();
            case 1 -> m.getAluno().getNome();
            case 2 -> m.getAluno().getTurma() != null ? m.getAluno().getTurma().getNome() : "";
            case 3 -> m.getMesReferencia().format(FORMATO_MES);
            case 4 -> String.format(Locale.US, "%.2f MT", m.getValor());
            case 5 -> rotulo(m.getStatus());
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void atualizarDados(List<Mensalidade> novaLista) {
        this.mensalidades = novaLista;
        fireTableDataChanged();
    }

    public Mensalidade getMensalidadeNaLinha(int row) {
        return mensalidades.get(row);
    }

    public static String rotulo(StatusMensalidade status) {
        return switch (status) {
            case PENDENTE -> "Pendente";
            case PAGO -> "Pago";
            case ATRASADO -> "Atrasado";
        };
    }
}