package App;

import App.dao.DashboardDAO;

public class SistemaGestaoMensalidadesEscolares {

    public static void main(String[] args) {
        System.out.println("SGME");
        DashboardDAO dao = new DashboardDAO();
        System.out.println(dao.countAlunos());
    }
}
