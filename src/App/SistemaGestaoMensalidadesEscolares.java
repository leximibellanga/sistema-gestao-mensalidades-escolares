package App;

import App.util.ConexaoBD;

public class SistemaGestaoMensalidadesEscolares {

    public static void main(String[] args) {
        System.out.println("Testando a aplicacao.");
        
        // testar conexao
        System.out.println(ConexaoBD.getConexao());
    }
}
