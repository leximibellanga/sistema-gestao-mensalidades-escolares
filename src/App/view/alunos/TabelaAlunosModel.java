package App.view.alunos;

import App.model.Aluno;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TabelaAlunosModel extends AbstractTableModel {

    private static final String[] COLUNAS = {
        "Nr. Estudante", "Nome", "Encarregado", "Contacto", "Turma", "Data Matrícula"
    };
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private List<Aluno> alunos = new ArrayList<>();

    @Override
    public int getRowCount() {
        return alunos.size();
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
        Aluno a = alunos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> a.getNumeroEstudante();
            case 1 -> a.getNome();
            case 2 -> a.getEncarregado();
            case 3 -> a.getContacto();
            case 4 -> a.getTurma() != null ? a.getTurma().getNome() : "";
            case 5 -> a.getDataMatricula() != null ? a.getDataMatricula().format(FORMATO_DATA) : "";
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void atualizarDados(List<Aluno> novaLista) {
        this.alunos = novaLista;
        fireTableDataChanged();
    }

    public Aluno getAlunoNaLinha(int row) {
        return alunos.get(row);
    }
}