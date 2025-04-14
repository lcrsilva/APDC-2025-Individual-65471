Operações Implementadas
	-	OP1: Criação de Conta de Utilizador
	-	OP2: Login
	-	OP3: Alteração de Role
	-	OP4: Alteração de Estado da Conta
	-	OP5: Remoção de Conta de Utilizador
	-	OP6: Listar Utilizadores
	-	OP7: Alteração de Atributos de Conta
	-	OP8: Alteração de Password
	-	OP9: Logout
	-	OP10: Criar Folha de Obra

 Testar:
Clone:
git clone APDC-2025-Individual-65471
cd <project-folder>

Build:
mvn clean package

Deploy Local:
mvn appengine:run

Deploy Remoto:
mvn package appengine:deploy -Dapp.deploy.projectId=individual-456011 -Dapp.deploy.version=1
