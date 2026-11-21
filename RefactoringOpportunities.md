# Oportunidades de Refabricação

| Local (Classe::Método) | Cheiro no Código (Smell) | Refabricação a aplicar | Nº do Aluno |
| :--- | :--- | :--- | :--- |
| `Tasks::menu` (CMD_HISTORY) | Long Method | Extract Method | 129494 |
| `Tasks::menu` (CMD_EXPORTPDF) | Long Method | Extract Method | 129494 |
| `Tasks::menu` (CMD_SIMULATE) | Long Method | Extract Method | 129494 |
| `Tasks::chooseLanguage` | Nomes pouco claros (Bad Naming) | Rename | 129494 |
| `Tasks::menu` (CMD_NEWFLEET) | Long Method | Extract Method | 129494 |
|Ship::getAdjacentPositions | Long Method	|	Extract Method		|	125309
|Position::isAdjacentTo	|	Magic Number	|	Introduce Constant	|	125309
|Move::processEnemyFire	|	Long Method	|	Extract Method		|	125309
|Move::buildResponse 	|	Long Parameter List	| Introduce Parameter Object	| 125309
|Position::adjacentPositions |	Long Method	|	Extract Method	|		125309
| `CallbackClient::sendShots` | Long Method | Extract Method (getRequest) | 124579 |
| `CallbackClient::recordAiMove` | Complex Expression | Extract Variable (nextMoveNumber) | 124579 |
| `GameSession::markAiWins` | Magic String | Extract Constant (AI_WINS) | 124579 |
| `GameSession::markStudentWins` | Magic String | Extract Constant (STUDENT_WINS) | 124579 |
| `CallbackClient::recordAiMove` | Complex Expression | Extract Variable (result) | 124579 |