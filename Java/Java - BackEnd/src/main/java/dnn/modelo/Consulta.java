package dnn.modelo;

public class Consulta {

    private int idConsulta, idPaciente;
    private String cpfPaciente, croDentista, cpfDentista;
    private String dataConsulta, horario, turno, status, tipo, observacoes;
    private double distanciaKm;
    private String nomePaciente, nomeDentista;

    public Consulta() {
    }

    // Status deve ser um dos valores permitidos
    public boolean statusValido() {
        return status != null &&
                ("AGENDADA".equals(status) || "REALIZADA".equals(status)
                        || "CANCELADA".equals(status) || "FALTA".equals(status));
    }

    // Turno deve ser um dos valores permitidos
    public boolean turnoValido() {
        return turno == null
                || "MANHA".equals(turno)
                || "TARDE".equals(turno)
                || "NOITE".equals(turno);
    }

    // Data obrigatória
    public boolean dataValida() {
        return dataConsulta != null && !dataConsulta.isBlank();
    }

    // Distância não pode ser negativa
    public boolean distanciaValida() {
        return distanciaKm >= 0;
    }

    @Override
    public String toString() {
        return String.format("Consulta{id=%d, paciente='%s', data='%s', status='%s'}",
                idConsulta, nomePaciente, dataConsulta, status);
    }

    public int getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(int v) {
        idConsulta = v;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int v) {
        idPaciente = v;
    }

    public String getCpfPaciente() {
        return cpfPaciente;
    }

    public void setCpfPaciente(String v) {
        cpfPaciente = v;
    }

    public String getCroDentista() {
        return croDentista;
    }

    public void setCroDentista(String v) {
        croDentista = v;
    }

    public String getCpfDentista() {
        return cpfDentista;
    }

    public void setCpfDentista(String v) {
        cpfDentista = v;
    }

    public String getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(String v) {
        dataConsulta = v;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String v) {
        horario = v;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String v) {
        turno = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String v) {
        tipo = v;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String v) {
        observacoes = v;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double v) {
        distanciaKm = v;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public void setNomePaciente(String v) {
        nomePaciente = v;
    }

    public String getNomeDentista() {
        return nomeDentista;
    }

    public void setNomeDentista(String v) {
        nomeDentista = v;
    }
}
