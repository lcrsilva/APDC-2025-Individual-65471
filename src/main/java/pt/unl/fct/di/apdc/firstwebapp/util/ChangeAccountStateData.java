package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAccountStateData {
    public AuthToken authToken;      // Token de autenticação do usuário que está fazendo a requisição
    public String targetUsername;    // Nome de usuário da conta a ser alterada
    public String newState;          // Novo estado da conta: "ATIVADA" ou "DESATIVADA"

    // Construtor padrão
    public ChangeAccountStateData() {
    }

    // Construtor com parâmetros
    public ChangeAccountStateData(AuthToken authToken, String targetUsername, String newState) {
        this.authToken = authToken;
        this.targetUsername = targetUsername;
        this.newState = newState;
    }

    // Getters e Setters
    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }
}