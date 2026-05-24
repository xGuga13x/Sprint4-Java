package dnn.modelo;

public class Paciente extends Pessoa {

    private int idPaciente;
    private String programa, turnoPreferencial, convenio, observacoes;
    private double rendaFamiliar, distanciaKm;

    public Paciente() {
    }

    public Paciente(String cpf, String nome, String programa,
                    double rendaFamiliar, double distanciaKm, String turnoPreferencial) {
        super(cpf, nome, null, null, null, null, null, null, null, null, null);
        this.programa = programa;
        this.rendaFamiliar = rendaFamiliar;
        this.distanciaKm = distanciaKm;
        this.turnoPreferencial = turnoPreferencial;
    }

    // Programa deve ser um dos valores permitidos
    public boolean programaValido() {
        return "DENTISTAS_DO_BEM".equals(programa) || "APOLONICAS_DO_BEM".equals(programa);
    }

    // Renda não pode ser negativa
    public boolean rendaValida() {
        return rendaFamiliar >= 0;
    }

    // Distância não pode ser negativa
    public boolean distanciaValida() {
        return distanciaKm >= 0;
    }

    // Turno deve ser um dos valores permitidos
    public boolean turnoValido() {
        return turnoPreferencial == null
                || "MANHA".equals(turnoPreferencial)
                || "TARDE".equals(turnoPreferencial)
                || "NOITE".equals(turnoPreferencial);
    }

    @Override
    public String toString() {
        return String.format("Paciente{id=%d, nome='%s', programa='%s'}", idPaciente, getNome(), programa);
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int v) {
        idPaciente = v;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String v) {
        programa = v;
    }

    public String getTurnoPreferencial() {
        return turnoPreferencial;
    }

    public void setTurnoPreferencial(String v) {
        turnoPreferencial = v;
    }

    public String getConvenio() {
        return convenio;
    }

    public void setConvenio(String v) {
        convenio = v;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String v) {
        observacoes = v;
    }

    public double getRendaFamiliar() {
        return rendaFamiliar;
    }

    public void setRendaFamiliar(double v) {
        rendaFamiliar = v;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double v) {
        distanciaKm = v;
    }
}
