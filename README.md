# 🌾 Fazenda Idle 2.0 - Documentação Técnica

## Descrição do trabalho
- Esse projeto é um trabalho final para a matéria: EN05219 - PROGRAMACAO II (2025 .4 - T01) do Curso de Ciência da Computação da UFPA.
- Sobre a tutela do Professor Doutor CARLOS GUSTAVO RESQUE DOS SANTOS. 


> **Gerenciamento Estratégico Agrícola com Automação e Controle Duplo**

---

## 📚 Índice
- [1. 🏗️ Arquitetura de Classes](#1-️-arquitetura-de-classes)
- [2. 🎮 Lógica de Controle Dupla](#2-️-lógica-de-controle-dupla)
- [3. 🤖 Sistema de Máquinas](#3-️-sistema-de-máquinas)
- [4. 🌱 Sistema de Fertilizante](#4-️-sistema-de-fertilizante)
- [5. ⚙️ Classe Fazenda](#5-️-classe-fazenda)
- [6. 📊 Regras de Negócio](#6-️-regras-de-negócio)
- [7. 🎨 Implementação Gráfica com Swing](#7-️-implementação-gráfica-com-swing)

---

## 1. 🏗️ Arquitetura de Classes

### 🌿 Classe Vegetal
Define os atributos estáticos das plantas cultiváveis.

**Atributos:**
- `nome` 
- `nivelMinimo`
- `tempoBaseDias`
- `valorBaseVenda`

**Catálogo de Plantas:**
| Planta | Nível | Tempo | Venda |
|--------|-------|-------|-------|
| 🥬 Alface | 1 | 2 dias | R$ 15,00 |
| 🥕 Cenoura | 2 | 4 dias | R$ 40,00 |
| 🎃 Abóbora | 5 | 10 dias | R$ 150,00 |

### 🌍 Classe Solo
Gerencia slots de plantação, bônus e automação.

**Atributos:**
- `nivel` (1-10)
- `vegetalPlantado`
- `tempoRestante`
- `estaOcupado`
- `maquinasAtribuidas` (lista)
- `fertilizanteAtivo` (booleano)

**Bônus por Nível:**
- ✅ +20% valor de venda por nível
- ⚡ +10% velocidade de crescimento por nível

### 🚜 Sistema de Máquinas
Cada máquina deve ser comprada e atribuída a um solo específico.

| Máquina | Função | Modo de Uso |
|---------|--------|-------------|
| 🚜 Trator | Colheita e venda automática | Ativa/Desativa por solo |
| 🛠️ Arador | Plantio automático | Ativa/Desativa por solo |
| 💦 Irrigador | Aumenta valor e reduz tempo | Ativa/Desativa por solo |

### 🐔 Classe Animal (Nova)
Define animais e sua produção.

**Atributos:**
- `especie`
- `tempoProducao`
- `valorProduto`
- `custoManutencao`

**Espécies:**
| Animal | Produz | Custo |
|--------|--------|-------|
| 🐔 Galinha | Ovos | Limpeza + Comida |
| 🐑 Ovelha | Lã | Tratamento + Comida |
| 🐮 Vaca | Leite | Tratamento + Comida |

### 🏠 Classe Cercado (Nova)
Gerencia grupos de até 3 animais da mesma espécie.

**Lógica de Coleta:**
- Coleta gera lucro imediato
- Consome "meio dia" de tempo por cercado visitado

---

## 2. 🎮 Lógica de Controle Dupla

### 🤖 Modo Automático (Padrão)
Controlado pela **Classe PersonagemIA**

**Prioridades de Ação:**
1. 🔄 Colheita em solos prontos sem Trator
2. 🌱 Plantio em solos vazios sem Arador
3. 🐔 Coleta em cercados disponíveis
4. 💎 Aplicação de fertilizante (se configurado)

### 👤 Modo Manual
Jogador controla diretamente o personagem.

**Mecânicas Necessárias:**
- 🎯 Movimento com teclado (WASD/Setas)
- 🖱️ Áreas interativas com detecção de proximidade
- 📋 Menu de ações contextuais
- ⚙️ Controle individual por solo (máquinas/fertilizante)

**Alternância entre Modos:**
- 🔘 Botão "Auto/Manual" na interface
- 🔄 Transição instantânea
- 💾 Estado preservado

---

## 3. 🤖 Sistema de Máquinas

### 🛒 Aquisição
- Cada máquina comprada individualmente
- Fica disponível no inventário após compra
- Preço fixo por tipo de máquina

### ⚙️ Instalação por Solo
- Arrastar/soltar ou menu contextual
- Cada solo pode ter uma máquina de cada tipo
- Máquina pode ser realocada gratuitamente

### 🔧 Configuração
- Painel por solo para ativar/desativar máquinas
- Status visual das máquinas ativas
- Estratégia: priorizar solos de alto valor

---

## 4. 🌱 Sistema de Fertilizante

### 🛍️ Aquisição
- Comprado em lotes (ex: 10 aplicações)
- Estoque global compartilhado
- Preço pode variar por oferta/demanda

### ⚡ Uso Automático
- Ativado/desativado por solo
- Consome uma unidade por plantio
- Notificação quando estoque baixo

### ✋ Uso Manual
- Aplicação individual por solo
- Efeito imediato: reduz tempo + aumenta lucro
- Consome do estoque global

---

## 5. ⚙️ Classe Fazenda

**Atributos Principais:**
- `dinheiro` 💰
- `diasPassados` 📅
- `estoqueFertilizante` 🌱
- `inventarioMaquinas` 🚜
- `maquinasInstaladas` 🗺️

**Sistema de Tempo:**
- ⏰ Ciclo de dia = 15 segundos reais
- 🔄 Avança automaticamente

---

## 6. 📊 Regras de Negócio

| Item | Tipo | Frequência | Observações |
|------|------|------------|-------------|
| Manutenção Animal | Débito Automático | Diário (15s) | 🐔🐑🐮 |
| Upgrade de Solo | Investimento Único | Por Nível | 📈 Custo progressivo |
| Máquinas | Investimento Único | Por unidade | 🚜🛠️💦 |
| Instalação de Máquina | Gratuita | Por solo | 🔄 Realocável |
| Fertilizante | Consumível | Quando acaba | 🌱 Lotes de 10 |
| Manutenção de Máquinas | Débito Automático | Semanal | 🔧 Pequena taxa |

---

## 7. 🎨 Implementação Gráfica com Swing

### ✅ Vantagens
1. 🏗️ **Integração Nativa com NetBeans**
2. 📚 **Curva de Aprendizado Suave**
3. ⚡ **Performance Adequada para 2D**
4. 🎯 **Controle Total de Renderização**
5. 🔄 **Compatibilidade Universal**

### 🖼️ Sistema Visual

**Sprites e Animações:**
- 👤 Personagem: 4 direções + estados
- 🌿 Plantas: Semente → Broto → Madura
- 🐑 Animais: Estados visuais distintos
- 🚜 Máquinas: Animações quando ativas

**Interface:**
- 📊 HUD com recursos principais
- 🎛️ Painel de configuração por solo
- 🔍 Highlight em objetos interativos
- 🎨 Transições suaves entre modos

**Otimizações:**
- 🖼️ Double Buffering (sem flickering)
- 🔄 Sprite Pooling (melhor performance)
- 🎯 Renderização por regiões
- 🧵 Thread separada para renderização

---

## 🚀 Estratégias Recomendadas

1. **Início:** Foque em alface para fluxo de caixa rápido
2. **Expansão:** Compre máquinas para solos de maior nível
3. **Automação:** Configure tratores em cultivos longos (abóbora)
4. **Animais:** Adicione quando tiver fluxo de caixa estável
5. **Fertilizante:** Use em cultivos de alto valor para maximizar lucro

---

## 🎯 Próximos Passos

1. **Fase 1:** Implementar classes básicas (Vegetal, Solo, Fazenda)
2. **Fase 2:** Sistema de tempo e ciclo diário
3. **Fase 3:** Interface Swing básica
4. **Fase 4:** Sistema de controle duplo
5. **Fase 5:** Máquinas e automação
6. **Fase 6:** Animais e cercados
7. **Fase 7:** Polimentos e balanceamento

---

**Desenvolvido com ❤️ para entusiastas de jogos idle e agricultura!**

> *"Plante, automatize, colha e repita!"* 🌱🚜💰
