# 🌾 Fazenda Idle 2.0 - Documentação Técnica

> **Trabalho Final - Disciplina EN05219 - PROGRAMAÇÃO II**

---

## 📚 Índice
- [1. 🎓 Informações Acadêmicas](#1-🎓-informações-acadêmicas)
- [2. 👥 Equipe de Desenvolvimento](#2-👥-equipe-de-desenvolvimento)
- [3. 🎯 Objetivos do Projeto](#3-🎯-objetivos-do-projeto)
- [4. 🏗️ Arquitetura de Classes](#4-🏗️-arquitetura-de-classes)
- [5. 🎮 Lógica de Controle Dupla](#5-🎮-lógica-de-controle-dupla)
- [6. 🤖 Sistema de Máquinas e Consumíveis](#6-🤖-sistema-de-máquinas-e-consumíveis)
- [7. ⚙️ Classe Fazenda](#7-⚙️-classe-fazenda)
- [8. 📊 Regras de Negócio](#8-📊-regras-de-negócio)
- [9. 🎨 Implementação Gráfica com Swing](#9-🎨-implementação-gráfica-com-swing)
- [10. 📅 Cronograma e Metodologia](#10-📅-cronograma-e-metodologia)

---

## 1. 🎓 Informações Acadêmicas

Este projeto foi desenvolvido como **trabalho final** para a disciplina:

### **EN05219 - PROGRAMACAO II**  
**Período:** 2025.4 - T01  
**Curso:** Ciência da Computação  
**Instituição:** Universidade Federal do Pará (UFPA)

## 👨‍🏫 Orientação
- **Professor Responsável:** Dr. Carlos Gustavo Resque dos Santos

---

## 2. 👥 Equipe de Desenvolvimento

| Desenvolvedor | Responsabilidades Principais |
|---------------|------------------------------|
| **Enya Clara Elizabeth da Silva Araujo** | 🎨 **Interface Gráfica**<br>🖼️ **Sistema de Sprites e Animações**<br>🎭 **Efeitos Visuais e Transições** |
| **Kaleo Nabor Pimentel da Cunha** | ⚙️ **Lógica das Classes e Objetos**<br>🔄 **Sistemas de Automação**<br>📊 **Mecânicas de Jogo e Balanceamento** |

---

## 3. 🎯 Objetivos do Projeto

### 📚 Objetivos Acadêmicos
- Aplicar os conceitos de **Programação Orientada a Objetos** aprendidos na disciplina
- Desenvolver um projeto completo com **arquitetura modular** e **boas práticas de código**
- Implementar **interface gráfica interativa** utilizando Swing
- Trabalhar em equipe com **divisão clara de responsabilidades**

### 🎮 Objetivos do Jogo
- Criar um **jogo idle/gestão** funcional e divertido
- Implementar **sistema de automação progressiva**
- Desenvolver **gráficos 2D animados** e responsivos
- Garantir **experiência de usuário fluida** e intuitiva

---

## 4. 🏗️ Arquitetura de Classes

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
- `estaUsandoFertilizante` (booleano)

**Bônus por Nível:**
- ✅ +20% valor de venda por nível
- ⚡ +10% velocidade de crescimento por nível

### 🐔 Classe Animal
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

### 🏠 Classe Cercado
Gerencia grupos de até 3 animais da mesma espécie.

**Lógica de Coleta:**
- Coleta gera lucro imediato
- Consome "meio dia" de tempo por cercado visitado

---

## 5. 🎮 Lógica de Controle Dupla

### 🤖 Modo Automático (Padrão)
Controlado pela **Classe PersonagemIA**

**Prioridades de Ação:**
1. 🔄 Colheita em solos prontos sem Trator
2. 🌱 Plantio em solos vazios sem Arador
3. 🐔 Coleta em cercados disponíveis
4. 💎 Aplicação de fertilizante (se configurado e disponível)

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

## 6. 🤖 Sistema de Máquinas e Consumíveis

### 🚜 Sistema de Máquinas Permanentes
Cada máquina deve ser comprada individualmente e atribuída a um solo específico.

| Máquina | Função | Atribuição | Custo |
|---------|--------|------------|-------|
| 🚜 **Trator** | Colheita e venda automática | Por solo (1 por solo) | R$ 300,00 |
| 🛠️ **Arador** | Plantio automático da última semente | Por solo (1 por solo) | R$ 250,00 |
| 💦 **Irrigador** | +25% valor, -15% tempo | Por solo (1 por solo) | R$ 400,00 |

**Características:**
- ✅ Compra única, fica no inventário
- 🔧 Instalação por arrastar/soltar ou menu
- 🔄 Pode ser realocada entre solos
- ⚙️ Ativa/desativa individualmente por solo

### 🌱 Sistema de Fertilizante (Consumível)

**Mecânica de Funcionamento:**
1. **Aquisição:**
   - Comprado em **lotes de 10 aplicações** na loja
   - Preço: R$ 150,00 por lote
   - Estoque global compartilhado entre todos os solos

2. **Atribuição a Solos:**
   - Cada solo pode ter o fertilizante **ativado/desativado**
   - Quando ativado, mostra indicador visual no solo
   - Configuração individual por solo

3. **Uso Automático:**
   - Quando um solo com fertilizante ativado é **plantado**
   - Consome **1 aplicação** do estoque global automaticamente
   - Efeito aplicado naquela planta específica

4. **Efeitos do Fertilizante:**
   - ⏰ **-40%** no tempo de crescimento
   - 💰 **+50%** no valor de venda
   - 🎨 Efeito visual especial na planta

5. **Gestão de Estoque:**
   - Quando o estoque chega a **0**, não há mais aplicações automáticas
   - Solos configurados continuam "ativados", mas não consomem
   - Notificação visual quando estoque está baixo (<3)
   - Necessidade de reabastecimento manual na loja

### ⚙️ Configuração por Solo

**Painel de Controle do Solo:**

| Máquina | Status |
|---------|--------|
| 🚜 Trator | [✅ Ativado] [❌ Desativado] |
| 🛠️ Arador | [✅ Ativado] [❌ Desativado] |
| 💦 Irrigador | [✅ Ativado] [❌ Desativado] |
| 🌱 Fertilizante | [✅ Ativado] [❌ Desativado] |

Estoque Fertilizante: 10/10 aplicações

**Opções de Fertilizante:**
- **✅ Ativado:** Usa do estoque automaticamente a cada plantio ate acabar o estoque
- **❌ Desligado:** Nunca usa fertilizante neste solo/Quando zero desativa
- **🔄 Botão lateral** Renova o estoque (Logica de desconto para completar o estoque quando não zerado)

---

## 7. ⚙️ Classe Fazenda

**Atributos Principais:**
- `dinheiro` 💰 (saldo atual)
- `diasPassados` 📅 (progresso temporal)
- `estoqueFertilizante` 🌱 (aplicações disponíveis)
- `inventarioMaquinas` 🚜 (quantidade de cada máquina disponível)
- `maquinasInstaladas` 🗺️ (mapeamento máquina→solo)
- `configuracoesSolo` ⚙️ (configurações individuais por solo)

**Sistema de Tempo:**
- ⏰ Ciclo de dia = 15 segundos reais
- 🔄 Avança automaticamente
- 📅 Eventos diários processados ao final de cada ciclo

---

## 8. 📊 Regras de Negócio

| Item | Tipo | Frequência | Custo/Valor |
|------|------|------------|-------------|
| **Máquinas** | Investimento Único | Por unidade | 🚜 R$ 300<br>🛠️ R$ 250<br>💦 R$ 400 |
| **Fertilizante** | Consumível | Lotes de 10 | 🌱 R$ 150/lote |
| **Manutenção Animal** | Débito Automático | Diário (15s) | 🐔 R$ 5<br>🐑 R$ 10<br>🐮 R$ 20 |
| **Upgrade de Solo** | Investimento Único | Por Nível | 📈 R$ 100 × nível |
| **Manutenção Máquinas** | Débito Automático | Semanal | 🔧 R$ 10/máquina |

**Economia do Fertilizante:**
- Cada aplicação custa efetivamente **R$ 15,00**
- Deve render pelo menos **R$ 30,00** extra para valer a pena
- Estratégico em cultivos de alto valor (Abóbora: R$ 150 → R$ 225)

---

## 9. 🎨 Implementação Gráfica com Swing

### ✅ Vantagens
1. 🏗️ **Integração Nativa com NetBeans**
2. 📚 **Curva de Aprendizado Suave**
3. ⚡ **Performance Adequada para 2D**
4. 🎯 **Controle Total de Renderização**
5. 🔄 **Compatibilidade Universal**

### 🖼️ Sistema Visual

**Indicadores de Máquinas/Fertilizante:**
- Ícones flutuantes acima de cada solo
- Cores: Verde (ativo), Cinza (inativo), Vermelho (sem estoque)
- Tooltips com status detalhado

**Animações Especiais:**
- 💨 Partículas ao aplicar fertilizante
- 🌈 Brilho nas plantas com fertilizante ativo
- 🔄 Rotação sutil nas máquinas ativas

**Interface de Configuração:**
- Painel flutuante ao clicar em um solo
- Controles deslizantes para ativar/desativar
- Barra de progresso do estoque de fertilizante
- Botão de compra rápida quando estoque baixo

---

## 10. 📅 Cronograma e Metodologia

### 📋 Divisão de Tarefas Detalhada

**Enya Clara** 🎨
1. **Interface Gráfica (Swing)**
   - Design e implementação da janela principal
   - Sistema de HUD (Heads-Up Display)
   - Menus e painéis de configuração
   - Sistema de diálogos e notificações

2. **Sistema Visual e Animações**
   - Sprite sheets para personagens e elementos
   - Animações de crescimento das plantas
   - Transições entre estados visuais
   - Efeitos especiais (coleta, plantio, etc.)

**Kaleo Nabor** ⚙️
1. **Arquitetura do Sistema**
   - Design e implementação das classes principais
   - Sistema de gerenciamento de estado do jogo
   - Sistema de eventos e notificações internas

2. **Mecânicas de Jogo**
   - Sistema de tempo e ciclo diário
   - Lógica de cultivo e colheita
   - Sistema econômico (compra/venda)
   - Progressão e balanceamento

### 📊 Cronograma de Desenvolvimento

| Fase | Período | Atividades |
|------|---------|------------|
| **1. Planejamento** | Semana 1 | - Definição de escopo<br>- Design das classes<br>- Coleta de assets visuais |
| **2. Implementação Base** | Semanas 2-3 | - Classes principais<br>- Sistema gráfico básico<br>- Mecânicas fundamentais |
| **3. Integração** | Semana 4 | - Conexão interface-lógica<br>- Testes iniciais<br>- Correção de bugs |
| **4. Polimento** | Semana 5 | - Animações finais<br>- Balanceamento<br>- Testes de usabilidade |
| **5. Entrega** | Semana 6 | - Documentação final<br>- Apresentação<br>- Código final |

### 🤝 Metodologia de Trabalho
- **Reuniões semanais** para sincronização
- **GitHub** para controle de versão
- **Pair programming** para componentes complexos
- **Testes unitários** para lógica de negócio
- **Testes de usabilidade** para interface

### 🛠️ Ferramentas Utilizadas
- **IDE:** NetBeans / IntelliJ IDEA
- **Controle de Versão:** Git + GitHub
- **Design:** Aseprite / Photoshop (para sprites)
- **Documentação:** Markdown

---

## 🏆 Competências Desenvolvidas

### 💻 Técnicas
- Programação Java avançada
- Desenvolvimento de jogos 2D
- Interface gráfica com Swing
- Arquitetura de software modular

### 👥 Pessoais
- Trabalho em equipe
- Gestão de tempo
- Resolução de problemas
- Comunicação técnica

---

## 🚀 Estratégias de Jogo Recomendadas

1. **Fase Inicial (Dias 1-5):**
   - Plante apenas 🥬 Alface para fluxo rápido
   - Economize para primeiro 🚜 Trator
   - Compre primeiro lote de 🌱 Fertilizante

2. **Fase de Expansão (Dias 6-15):**
   - Automatize solos de nível 3+ com tratores
   - Use fertilizante apenas em 🎃 Abóbora
   - Adquira animais para renda passiva

3. **Fase Avançada (Dias 16+):**
   - Todos os solos com trator + arador
   - Fertilizante em todos os cultivos
   - Balanceie entre produção vegetal e animal

---

## 📁 Estrutura do Projeto Sugerida

fazenda-idle-2.0/
├── src/
│ ├── model/
│ │ ├── Vegetal.java
│ │ ├── Solo.java
│ │ ├── Animal.java
│ │ ├── Cercado.java
│ │ ├── Fazenda.java
│ │ └── Maquina.java
│ ├── view/
│ │ ├── GamePanel.java
│ │ ├── HUD.java
│ │ ├── SpriteManager.java
│ │ └── AnimationEngine.java
│ ├── controller/
│ │ ├── GameController.java
│ │ ├── PlayerController.java
│ │ └── AIController.java
│ └── util/
│ ├── Constants.java
│ └── ResourceLoader.java
├── assets/
│ ├── sprites/
│ │ ├── plants/
│ │ ├── animals/
│ │ ├── character/
│ │ └── machines/
│ ├── ui/
│ └── effects/
└── docs/
├── README.md
└── diagramas/

---

## 🙏 Agradecimentos

Agradecemos ao **Prof. Dr. Carlos Gustavo Resque dos Santos** pela orientação e aos **colegas de turma** pelo apoio durante o desenvolvimento deste projeto.

---
*Documento atualizado em: Janeiro de 2026*  
*Universidade Federal do Pará - Ciência da Computação*
