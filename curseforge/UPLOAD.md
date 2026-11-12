# Atualizar o projeto existente no CurseForge

Projeto: https://www.curseforge.com/minecraft/mc-mods/lincelink

A página enviada com `/preview` é a pré-visualização. Usa o endereço acima nos links públicos. Esta preparação não entra na tua conta nem envia ficheiros ao CurseForge.

## Página do projeto

- Nome: **LinceLink**.
- Resumo: copiar `SUMMARY.txt` do kit (inglês); o mesmo texto está em `summary` de `project.json`.
- Descrição: copiar `DESCRIPTION.md` para o editor Markdown; inglês primeiro, português depois.
- Licença: **MIT License**.
- Código-fonte: https://github.com/Alexandre1116/LinceLink
- Issues: https://github.com/Alexandre1116/LinceLink/issues
- Categorias sugeridas: **Server Utility**, e **Utility & QoL** como adicional. Usa apenas categorias que correspondam ao conteúdo.
- Ambiente, se o portal mostrar esta opção: **Client**. Instalar nos clientes do anfitrião e convidados, não num servidor dedicado.
- Logo: `logo.png` no kit (o original quadrado de 1254×1254). O guia oficial aceita PNG quadrado com pelo menos 400×400 e indica redimensionamento de imagens maiores; no editor, confirma o resultado final 400×400 e guarda o avatar.

O tipo **Alpha** dos ficheiros é independente da opção **Experimental** do projeto. Não é necessário ativar Experimental para publicar ficheiros alpha; essa opção pode impedir a sincronização normal com a aplicação CurseForge.

## Ficheiros

Extrai o kit ZIP primeiro. **Não envies o kit inteiro como ficheiro do mod e não o importes como modpack.** Envia cada JAR de `files/` individualmente no projeto já criado.

Para cada JAR, consulta `FILES.md` e a entrada correspondente em `files.json`:

1. Seleciona o JAR exato.
2. Preenche o nome de apresentação indicado.
3. Marca apenas a versão exata do Minecraft, o loader e a versão Java indicados.
4. Marca **Alpha** como tipo de lançamento.
5. Copia `CHANGELOG.md` como changelog em Markdown.
6. Nos ficheiros **Fabric**, adiciona **Fabric API** como **Required Dependency**. Nos ficheiros **Forge**, não adiciones Fabric API.
7. Confirma que não marcaste NeoForge, Quilt, versões intermédias ou outros sistemas não testados.
8. Envia para a revisão normal da plataforma.

O `files.json` é um plano de upload legível, não um payload da API CurseForge: os nomes das versões não são IDs numéricos de API. O script não inventa IDs nem faz uploads.

## Informação funcional e moderação

A descrição distingue as funcionalidades base dos extras exclusivos de Forge 1.20.1. Explica também a dependência do executável Tailcat v0.7.0 e o download automático deste componente nesse build. Mantém essa informação visível para os utilizadores. `MODERATOR-NOTES.md` resume o comportamento para a revisão técnica, se necessário.

A página de descrição não inclui links diretos externos para descarregar JARs ou executáveis. Usa os ficheiros do próprio projeto e os campos de código-fonte/issues para os links correspondentes.

Não anunciar estes ficheiros como estáveis: ainda há testes gráficos, entre sistemas operativos e sessões completas por concluir. A aprovação final depende da revisão do CurseForge, não deste kit.

Para a galeria, usa capturas reais do jogo conforme `GALLERY.md`; não publiques códigos de convite reais nem imagens que simulem funcionalidades ainda não verificadas.

## Regenerar o kit

Depois de compilar os nove módulos:

```sh
python3 tools/package_curseforge.py --version 0.1.7
```

O script valida os JARs, licencia, metadados, ícones e dependências e cria `release-artifacts/LinceLink-CurseForge-0.1.7.zip`. O conteúdo inclui JARs separados, textos e metadados para atualização do projeto.

## Fontes oficiais consultadas

- Guia de submissão, logo e campos: https://support.curseforge.com/support/solutions/articles/9000199552-project-submission-guide-and-tips
- Políticas, inglês primeiro e changelog: https://support.curseforge.com/support/solutions/articles/9000197279
- Estados de projeto/ficheiros e revisão: https://support.curseforge.com/support/solutions/articles/9000197905-project-statuses-101
