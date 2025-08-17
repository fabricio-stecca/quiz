# Quiz App Android

Um aplicativo de quiz Android desenvolvido em Kotlin com arquitetura MVVM, usando Jetpack Compose para a interface e Firebase para backend.

## 🚀 Funcionalidades

- **Autenticação**: Login e cadastro de usuários via Firebase Auth
- **Quiz Dinâmico**: Questões organizadas por categorias
- **Armazenamento Offline**: Sincronização entre Firebase e banco local (Room)
- **Histórico Pessoal**: Acompanhamento do desempenho do usuário
- **Dashboard**: Análise detalhada de performance
- **Ranking Global**: Classificação dos melhores usuários por pontos ou perguntas respondidas
- **Interface Moderna**: Desenvolvida com Material Design 3
- **Painel Administrativo**: Gestão de questões e categorias

## 🏗️ Arquitetura

O projeto segue a arquitetura MVVM (Model-View-ViewModel) com as seguintes camadas:

- **Presentation**: ViewModels e UI (Jetpack Compose)
- **Data**: Repositórios, DAOs e fontes de dados (Firebase + Room)
- **UI Theme**: Sistema de cores e tipografia consistente

## 🛠️ Tecnologias Utilizadas

- **Kotlin**: Linguagem principal
- **Jetpack Compose**: Interface do usuário moderna
- **Firebase**: Backend (Auth, Realtime Database)
- **Room**: Banco de dados local SQLite
- **Navigation Compose**: Navegação entre telas
- **Coroutines**: Programação assíncrona
- **StateFlow/Flow**: Gerenciamento de estado reativo
- **Material Design 3**: Design system

## 📱 Telas do Aplicativo

1. **Login/Cadastro**: Autenticação do usuário com validação
2. **Home**: Lista de categorias disponíveis e atividade recente
3. **Quiz**: Interface interativa de execução dos quizzes
4. **Histórico**: Visualização de quizzes anteriores com detalhes
5. **Dashboard**: Análise de desempenho e estatísticas pessoais
6. **Ranking**: Classificação global dos usuários (por pontos ou perguntas)
7. **Admin**: Painel para gerenciamento de questões (usuários admin)

## 🗄️ Estrutura do Banco de Dados

### Entidades Room (Local)
- **Question**: Questões do quiz (id, text, options, correctAnswer, category)
- **QuizSession**: Sessões realizadas (id, userId, score, accuracy, duration)
- **User**: Dados dos usuários (id, nickname, email, isAdmin)

### Firebase Realtime Database
```
quiz-app/
├── questions/
│   ├── conhecimentos-gerais/
│   ├── historia/
│   ├── ciencias/
│   ├── geografia/
│   └── esportes/
├── users/
└── quiz_sessions/
```

## 🚀 Como Executar

1. **Clone o repositório**
   ```bash
   git clone [url-do-repositorio]
   cd quiz
   ```

2. **Configure o Firebase**:
   - Crie um projeto no Firebase Console
   - Ative Authentication (Email/Password)
   - Configure Realtime Database
   - Baixe o arquivo `google-services.json` e coloque em `app/`

3. **Execute o projeto** no Android Studio
   - Abra o projeto no Android Studio
   - Sync do Gradle
   - Execute no emulador ou dispositivo

## 📊 Funcionalidades Detalhadas

### Sistema de Quiz
- Questões organizadas por categoria
- Pontuação baseada em acertos (1 ponto por questão)
- Feedback visual em tempo real
- Suporte para múltiplas escolhas

### Sincronização de Dados
- Download automático de questões do Firebase
- Armazenamento local para uso offline
- Sincronização automática de resultados
- Backup de dados na nuvem

### Análise de Performance
- Estatísticas detalhadas: total de quizzes, precisão média, pontos
- Histórico completo com filtros
- Dashboard personalizado
- Ranking comparativo entre usuários

### Sistema de Usuários
- Autenticação segura com Firebase Auth
- Perfis personalizados com nickname
- Níveis de permissão (usuário/admin)
- Proteção de dados pessoais

## 🎯 Dados de Exemplo

O aplicativo inclui 25+ questões de exemplo nas seguintes categorias:
- **Conhecimentos Gerais**: Questões variadas de cultura geral
- **História**: Eventos históricos mundiais e do Brasil
- **Ciências**: Física, química, biologia e matemática
- **Geografia**: Países, capitais, rios e montanhas
- **Esportes**: Modalidades esportivas e eventos

## 🔧 Manutenção e Limpeza

O projeto foi otimizado removendo:
- ✅ Arquivos duplicados e não utilizados (`*New.kt`, backups)
- ✅ Imports desnecessários
- ✅ Recursos não referenciados (strings, cores)
- ✅ Testes exemplo padrão do Android
- ✅ Código morto e comentários obsoletos

### Estrutura Limpa
```
app/src/main/java/com/example/quiz/
├── MainActivity.kt
├── QuizApplication.kt
├── AdminBootstrap.kt
├── data/
│   ├── dao/
│   ├── database/
│   ├── model/
│   └── repository/
├── presentation/
│   ├── navigation/
│   ├── screen/
│   └── viewmodel/
└── ui/theme/
```

## 🐛 Debug e Troubleshooting

Consulte o arquivo [DEBUG_INSTRUCTIONS.md](DEBUG_INSTRUCTIONS.md) para:
- Configuração do Firebase
- Resolução de problemas comuns
- Logs de debug
- Procedimentos de teste

## 👥 Contribuições

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

---

**Status do Projeto**: ✅ Funcional e otimizado  
**Última atualização**: Agosto 2025  
**Versão**: 1.0.0
