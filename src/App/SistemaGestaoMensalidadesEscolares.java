package App;

import App.dao.MensalidadeDAO;
import java.time.LocalDate;
import java.time.Month;

public class SistemaGestaoMensalidadesEscolares {

    public static void main(String[] args) {
        System.out.println("SGME");
        
        MensalidadeDAO bdMensal = new MensalidadeDAO();
        System.out.println("\n\nEm atraso: \t" + bdMensal.listarEmAtraso());
        System.out.println("\n\nPendentes com data vencida: \t" + bdMensal.listarPendentesComDataVencida());
        System.out.println("\n\nEm Por aluno [5]: \t" + bdMensal.listarPorAluno(6));
        System.out.println("\n\nPor mes [agosto]: \t" + bdMensal.listarPorMesReferencia(LocalDate.of(2026, Month.MARCH, 1)));
    }
}
