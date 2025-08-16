# Quiz App Android

Um aplicativo de quiz Android desenvolvido em Kotlin com arquitetura MVVM, usando Jetpack Compose para a interface e Firebase para backend.

## 🚀 Funcionalidades

- **Autenticação**: Login e cadastro de usuários via Firebase Auth
- **Quiz Dinâmico**: Questões organizadas por categorias
- **Armazenamento Offline**: Sincronização entre Firebase e banco local (Room)
- **Histórico Pessoal**: Acompanhamento do desempenho do usuário
- **Dashboard**: Análise detalhada de performance
- **Ranking Global**: Classificação dos melhores usuários
- **Interface Moderna**: Desenvolvida com Material Design 3

## 🏗️ Arquitetura

O projeto segue a arquitetura MVVM (Model-View-ViewModel) com as seguintes camadas:

- **Presentation**: ViewModels e UI (Jetpack Compose)
- **Domain**: Casos de uso e modelos de domínio
- **Data**: Repositórios, APIs e fontes de dados

## 🛠️ Tecnologias Utilizadas

- **Kotlin**: Linguagem principal
- **Jetpack Compose**: Interface do usuário
- **Firebase**: Backend (Auth, Realtime Database)
- **Room**: Banco de dados local
- **Hilt**: Injeção de dependências
- **Navigation Compose**: Navegação
- **Coroutines**: Programação assíncrona
- **StateFlow**: Gerenciamento de estado reativo

## 📱 Telas do Aplicativo

1. **Login/Cadastro**: Autenticação do usuário
2. **Home**: Lista de categorias disponíveis e atividade recente
3. **Quiz**: Interface de execução dos quizzes
4. **Histórico**: Visualização de quizzes anteriores
5. **Dashboard**: Análise de desempenho e estatísticas
6. **Ranking**: Classificação global dos usuários

## 🗄️ Estrutura do Banco de Dados

### Entidades Room
- **Question**: Questões do quiz
- **QuizSession**: Sessões de quiz realizadas
- **User**: Dados dos usuários

### Firebase Realtime Database
```
quiz-app/
├── questions/
│   ├── categoria1/
│   │   ├── question1
│   │   └── question2
│   └── categoria2/
├── users/
└── quiz_sessions/
```

## 🚀 Como Executar

1. **Clone o repositório**
2. **Configure o Firebase**:
   - Crie um projeto no Firebase Console
   - Adicione o arquivo `google-services.json` na pasta `app/`
   - Configure Authentication e Realtime Database
3. **Execute o projeto** no Android Studio

## 📊 Funcionalidades Detalhadas

### Sistema de Quiz
- Questões organizadas por categoria
- Pontuação baseada em acertos
- Timer por sessão
- Feedback em tempo real

### Sincronização de Dados
- Download automático de questões do Firebase
- Armazenamento local para uso offline
- Sincronização de resultados com a nuvem

### Análise de Performance
- Estatísticas detalhadas de desempenho
- Histórico completo de quizzes
- Comparação de performance ao longo do tempo
- Ranking global de usuários

## 🎯 Dados de Exemplo

O aplicativo inclui questões de exemplo nas seguintes categorias:
- Conhecimentos Gerais
- História
- Ciências
- Geografia
- Esportes

## 👥 Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests.

## 📄 Licença

Este projeto está sob a licença MIT.
