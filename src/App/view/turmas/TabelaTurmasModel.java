package App.view.turmas;

import App.model.Turma;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TabelaTurmasModel extends AbstractTableModel {

    private static final String[] COLUNAS = {"ID", "Nome", "Valor Mensalidade"};

    private List<Turma> turmas = new ArrayList<>();

    @Override
    public int getRowCount() {
        return turmas.size();
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
        Turma t = turmas.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> t.getId();
            case 1 -> t.getNome();
            case 2 -> String.format(Locale.US, "%.2f MT", t.getValorMensalidade());
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void atualizarDados(List<Turma> novaLista) {
        this.turmas = novaLista;
        fireTableDataChanged();
    }

    public Turma getTurmaNaLinha(int row) {
        return turmas.get(row);
    }
}