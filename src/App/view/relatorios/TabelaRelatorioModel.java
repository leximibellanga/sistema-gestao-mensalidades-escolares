package App.view.relatorios;

import App.model.Relatorio;

import javax.swing.table.AbstractTableModel;

/** TableModel genérico: mostra qualquer Relatorio (as colunas mudam com o relatório). */
public class TabelaRelatorioModel extends AbstractTableModel {

    private Relatorio relatorio;

    public void setRelatorio(Relatorio relatorio) {
        this.relatorio = relatorio;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        if (relatorio == null) {
            return 0;
        }
        return relatorio.getLinhas().size() + (relatorio.getLinhaTotal() != null ? 1 : 0);
    }

    @Override
    public int getColumnCount() {
        return relatorio == null ? 0 : relatorio.getColunas().length;
    }

    @Override
    public String getColumnName(int column) {
        return relatorio == null ? "" : relatorio.getColunas()[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object[] valores = rowIndex < relatorio.getLinhas().size()
                ? relatorio.getLinhas().get(rowIndex)
                : relatorio.getLinhaTotal();
        return Relatorio.formatar(valores[columnIndex]);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public boolean isLinhaTotal(int row) {
        return relatorio != null && relatorio.getLinhaTotal() != null
                && row == relatorio.getLinhas().size();
    }

    public boolean isColunaNumerica(int column) {
        return relatorio != null && relatorio.isColunaNumerica(column);
    }
}