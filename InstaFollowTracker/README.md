# Seguidores Tracker

App Android (Kotlin + Jetpack Compose) para acompanhar **quem deixou de te
seguir** e **quem passou a te seguir** no Instagram, além de mostrar quem
você segue que não te segue de volta.

## Por que o app funciona por importação, e não por login automático

O Instagram não oferece uma API pública para consultar a lista completa de
seguidores/seguindo de uma conta pessoal. Apps que fazem esse tipo de
"rastreamento" via login automatizado usam APIs privadas não documentadas,
o que **viola os Termos de Uso do Instagram** e coloca a conta em risco de
bloqueio, além de exigir guardar sua senha em um servidor de terceiros.

Este app evita os dois problemas: ele lê o **export oficial de dados** que
o próprio Instagram gera (Configurações → Central de Contas → Suas
informações e permissões → Baixar suas informações), guarda um "retrato"
(snapshot) local a cada importação e compara com o snapshot anterior para
calcular quem saiu e quem entrou. Nenhuma senha é pedida, nenhum dado sai
do aparelho — o app não usa internet.

Isso também explica a única limitação real: para ver comparações você
precisa importar seus dados de tempos em tempos (ex.: uma vez por semana),
em vez de ter alertas em tempo real.

## Funcionalidades

- **Início**: resumo com total de seguidores/seguindo, novos seguidores,
  quem deixou de seguir e quem não segue de volta.
- **Deixaram de seguir** / **Novos seguidores**: listas comparando a
  importação mais recente com a anterior; toque em alguém para abrir o
  perfil no Instagram.
- **Não seguem de volta**: quem você segue e não é seu seguidor, calculado
  a partir da importação mais recente.
- **Importar**: aceita o `.zip` baixado direto do Instagram ou os arquivos
  `followers_1.json` / `following.json` já extraídos.
- **Histórico**: lista todas as importações feitas, com opção de excluir.

## Como exportar seus dados do Instagram

1. No app do Instagram: **Configurações → Central de Contas → Suas
   informações e permissões → Baixar suas informações**.
2. Selecione sua conta, escolha **apenas "Seguidores e pessoas que você
   segue"** e o formato **JSON** (não HTML).
3. Solicite o download. O Instagram avisa por e-mail/notificação quando o
   arquivo `.zip` estiver pronto (pode levar de minutos a poucas horas).
4. Baixe o `.zip` no celular e abra o Seguidores Tracker → aba **Importar**
   → **Selecionar arquivo .zip exportado**.
5. Repita esse processo periodicamente — a cada nova importação o app
   compara com a anterior automaticamente.

## Arquitetura

- **Kotlin + Jetpack Compose (Material 3)**, com suporte a cor dinâmica
  (Material You) no Android 12+.
- **MVVM**: `ViewModel` por tela + `StateFlow`, sem dependências externas de
  DI (injeção manual via a classe `Application`).
- **Room** para persistir os snapshots (`SnapshotEntity`) e os perfis de
  cada snapshot (`ProfileRecordEntity`).
- **`InstagramExportParser`**: lê o `.zip` (via `java.util.zip`) ou os
  arquivos JSON individuais (via `org.json`, sem dependências externas),
  tolerante a variações no formato do export do Instagram.
- **`DiffCalculator`**: lógica pura (sem Android) que calcula novos
  seguidores, quem deixou de seguir e quem não segue de volta — coberta por
  testes unitários em `app/src/test`.
- Nenhuma permissão de internet ou armazenamento é declarada: a leitura dos
  arquivos usa o Storage Access Framework (`ACTION_OPEN_DOCUMENT`), que já
  concede acesso ao arquivo selecionado sem permissão adicional.

```
app/src/main/java/com/gustavobarreto/instafollowtracker/
├── data/
│   ├── model/        InstaProfile, ListType
│   ├── db/           Room: entities, DAOs, AppDatabase
│   ├── parser/        InstagramExportParser (.zip / .json)
│   └── repository/   FollowersRepository
├── domain/            DiffCalculator (lógica pura)
└── ui/
    ├── theme/         Cores, tipografia, Material 3
    ├── navigation/     Destinos da bottom bar
    ├── components/    StatCard, UserListItem, EmptyState
    └── screens/       dashboard, userlist, importdata, history
```

## Como compilar

Pré-requisitos: Android Studio (Koala ou mais recente) ou o SDK do Android
com `compileSdk 34` instalado.

```bash
./gradlew assembleDebug   # gera o APK de debug
./gradlew test            # roda os testes unitários (parser + diff)
```

Depois é só instalar o APK gerado em `app/build/outputs/apk/debug/` no seu
Galaxy S24 Ultra (Android 14+), ou rodar direto do Android Studio com o
aparelho conectado via USB (modo desenvolvedor + depuração USB ativados).

> Este projeto foi desenvolvido em um ambiente sem acesso ao repositório
> Maven do Google (`dl.google.com`), então a compilação completa do módulo
> Android (que depende de AndroidX/Compose) não pôde ser executada nesta
> sessão. A lógica de negócio pura (`DiffCalculator` e
> `InstagramExportParser`, incluindo os testes unitários) foi extraída e
> compilada/testada isoladamente com sucesso. Ao abrir o projeto no Android
> Studio, com acesso normal à internet, o `./gradlew assembleDebug` deve
> funcionar sem ajustes.

## Privacidade

- Sem permissão de internet: o app não consegue enviar dados para lugar
  nenhum, mesmo que quisesse.
- Sem login, sem senha, sem coleta de dados pessoais além dos arquivos que
  você mesmo escolhe importar.
- Todos os dados ficam no banco de dados local (Room/SQLite) do app; excluir
  o app ou uma importação específica na aba Histórico apaga os dados.
