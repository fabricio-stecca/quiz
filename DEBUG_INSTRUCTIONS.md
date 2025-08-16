# Instruções de Debug - Quiz App

## Problemas Corrigidos:

### 1. ✅ Inicialização do Firebase
- Adicionado `FirebaseApp.initializeApp(this)` no `QuizApplication`
- Adicionado logs de debug para verificar inicialização

### 2. ✅ Configuração do Firebase Database
- Configurado URL específico do projeto: `https://quiz-1a8d5-default-rtdb.firebaseio.com/`
- Adicionado fallback para instância padrão em caso de erro
- Habilitada persistência offline

### 3. ✅ Tratamento de Erros
- Adicionado try-catch em todos os métodos críticos
- Logs detalhados no `UserRepository`
- Tratamento robusto de erros no `AuthViewModel`

### 4. ✅ Navegação Segura
- Adicionado valores iniciais no `collectAsState()`
- Tratamento de erro na navegação do LoginScreen
- Navegação com `remember` para evitar recomposições desnecessárias

### 5. ✅ Compilação
- Corrigidos todos os ícones deprecados
- Corrigido `LinearProgressIndicator` deprecado
- Build bem-sucedido sem erros

## Se o App Ainda Crashar:

### 1. **Verificar Logs no Android Studio:**
```
Logcat -> Filtro: "QuizApplication" ou "UserRepository"
```

### 2. **Possíveis Causas:**
- **Problemas de Rede**: Verificar se o dispositivo tem acesso à internet
- **Firebase Configuration**: Verificar se o `google-services.json` está correto
- **Permissões**: Verificar se as permissões de internet estão no manifest

### 3. **Como Debugar:**
1. Abrir Android Studio
2. Conectar dispositivo/emulador
3. Executar o app em modo debug
4. Verificar Logcat para mensagens de erro
5. Procurar por mensagens com tags "QuizApplication", "UserRepository", "AuthViewModel"

### 4. **Soluções Alternativas:**
- **Modo Offline**: O app deve funcionar mesmo sem conexão Firebase
- **Reset do Estado**: Limpar dados do app nas configurações do dispositivo
- **Recompilação**: `.\gradlew.bat clean assembleDebug`

### 5. **Logs Importantes a Procurar:**
- `Firebase initialized successfully` - Confirma inicialização do Firebase
- `User signed in: true/false` - Estado de autenticação
- `Error` seguido de stack trace - Localiza problemas específicos

## Funcionalidades Implementadas:
- ✅ Autenticação Firebase completa
- ✅ Database local com Room
- ✅ Sincronização Firebase Database
- ✅ Navegação entre telas
- ✅ Sistema de quiz com pontuação
- ✅ Histórico de sessões
- ✅ Dashboard com estatísticas
- ✅ Ranking global
- ✅ 25 questões em 5 categorias

## Estado Atual:
🟢 **App compilando com sucesso**
🟢 **Todas as dependências resolvidas**
🟢 **Tratamento de erros implementado**
🟢 **Logs de debug adicionados**

O app está pronto para execução e deve funcionar corretamente.
