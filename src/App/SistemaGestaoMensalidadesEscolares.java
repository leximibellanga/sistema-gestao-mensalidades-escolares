package App;

import App.dao.TurmaDAO;
import App.model.Turma;
import App.util.ConexaoBD;

public class SistemaGestaoMensalidadesEscolares {

    public static void main(String[] args) {
        System.out.println("Testando a aplicacao.");
        
        System.out.println("======= Testar conexao =====");
        System.out.println(ConexaoBD.getConexao());
        
        System.out.println("\n\n");
        System.out.println("====== INSERIR TURMA =========");
        Turma t1 = new Turma("INFORMATICA", 6300);
        Turma t2 = new Turma("GESTAO", 5700);
        Turma t3 = new Turma("DIREITO", 5900);
        Turma t4 = new Turma("CONTABILIDADE", 5800);
        TurmaDAO tBD = new TurmaDAO();
        tBD.inserir(t1);
        tBD.inserir(t2);
        tBD.inserir(t3);
        tBD.inserir(t4);
        
    }
}
