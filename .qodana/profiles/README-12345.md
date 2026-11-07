# 125309 - Diogo Miranda

# Qodana - Long Parameter List

## Cheiro escolhido

Long Parameter List

## Tabela de mapeamento

| Cheiro (Fowler) | Inspeção no IDE | ID da inspeção | Métrica / Limiar | Limitações |
|-----------------|----------------|----------------|------------------|------------|
| Long Parameter List | Method with too many parameters | MethodWithTooManyParameters | Número de parâmetros (limite = 5) | Utiliza um limite absoluto; não permite combinar várias métricas como no livro |

## Estratégia utilizada

Foi utilizada a inspeção `MethodWithTooManyParameters` do Qodana para detetar métodos e construtores com um número excessivo de parâmetros.

## Limitações

A estratégia apresentada em *Object-Oriented Metrics in Practice* combina múltiplas métricas e utiliza limiares relativos ao sistema analisado. A inspeção do Qodana permite apenas definir um limite absoluto para o número de parâmetros, constituindo assim uma aproximação da estratégia original.

## Execução com falha

Adicionar link após testar o workflow.

## Execução com sucesso

Adicionar link após corrigir a violação e voltar a executar o workflow.

## Reflexão

O Qodana permite identificar facilmente métodos com listas excessivas de parâmetros, ajudando a melhorar a legibilidade e manutenção do código. A sensibilidade dos resultados depende diretamente do valor-limite escolhido. Um valor demasiado baixo pode originar falsos positivos, enquanto um valor demasiado elevado pode não detetar situações problemáticas.