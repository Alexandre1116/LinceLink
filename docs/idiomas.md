# Idiomas do build Forge 1.20.1 / 0.1.6

A interface reutiliza componentes traduzíveis do Minecraft, em vez de duplicar traduções do jogo ou enviar textos para serviços externos. Assim, os rótulos acompanham o idioma do cliente e os resource packs que alteram as traduções do próprio Minecraft.

O catálogo foi obtido do `pack.mcmeta` dos assets oficiais 1.20.1: 145 idiomas, incluindo en_us. Cada uma das 24 chaves usadas pelo mod existe nos ficheiros nativos desses 145 idiomas. Foram verificados também os argumentos de formatação. O [relatório JSON](idiomas.json) contém a lista de chaves, idiomas, direções de escrita e URL do índice oficial utilizado.

A mudança de idioma é tratada pelo jogo. Os componentes dos títulos, botões, mensagens e Tab não guardam uma tradução portuguesa fixa. O progresso em background é guardado como dados; só é traduzido quando aparece no ecrã. Erros conhecidos recebem mensagens traduzíveis, com caminhos/versões/códigos como dados; os diagnósticos completos mantêm-se no log para depuração.

As frases coincidem com as do Minecraft: abrir em LAN, entrar no servidor, copiar para a área de transferência, desligar, voltar, download e mensagens de erro. A marca LinceLink identifica o fluxo do mod. Colar é apresentado como o atalho de teclado da plataforma. DERP, LinceLink, Tailcat e unidades como ms são identificadores técnicos, não traduções em falta.

Os labels do Tab também usam as traduções nativas: ligação direta, local, desconhecido e LinceLink: não. O valor e o percurso continuam a representar os mesmos dados da versão anterior. O protocolo dos convites e do canal de estados não mudou.

## Repetir a auditoria

Requer Python 3 e acesso aos assets oficiais Mojang:

```sh
python3 tools/check_locales.py
```

Opcionalmente, `--client-jar /caminho/client.jar` permite reutilizar o JAR original Minecraft 1.20.1; `--cache /caminho/cache` reutiliza os assets. Os hashes dos downloads são verificados. Os ficheiros completos de traduções do Minecraft não são copiados para este repositório nem redistribuídos no mod.

Os testes de componentes são executados por `./gradlew -p forge-1.20.1 build`. Estes testes e a auditoria não substituem uma inspeção visual das fontes, do layout e da escrita RTL dentro do Minecraft.
