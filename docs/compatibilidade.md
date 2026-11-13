# Compatibilidade

Cada JAR é específico da versão do Minecraft e do loader. Não existe um JAR
universal.

| Minecraft Java | Forge | Fabric Loader / API | NeoForge | Java do jogo |
| --- | --- | --- | --- | --- |
| 1.12.2 | 14.23.5.2864 | — | — | 8 |
| 1.16.5 | 36.2.34 | 0.16.10+ / 0.42.0+1.16 | — | 8 |
| 1.18.2 | 40.3.0 | 0.16.10+ / 0.77.0+1.18.2 | — | 17 |
| 1.20.1 | 47.4.28 | 0.16.10+ / 0.92.6+1.20.1 | Coming soon | 17 |
| 1.21.1 | 52.1.20 | 0.16.10+ / 0.116.6+1.21.1 | 21.1.265+ | 21 |

O núcleo partilhado é compatível com Java 8. Os módulos incluem interfaces
específicas do Minecraft e do loader. Fabric requer a versão de Fabric API
indicada na tabela.

## Funcionalidades

- Os dez builds suportados podem instalar Tailcat v0.7.0 automaticamente em
  Windows e Linux x64/ARM64, se faltar. macOS e outras arquiteturas requerem
  instalação manual.
- Forge 1.20.1 inclui comandos de servidor, ping e estado de percurso Direto /
  DERP no Tab e traduções nativas nas 145 línguas do Minecraft 1.20.1.
- Os restantes builds têm interface em inglês e português; idiomas sem recurso
  próprio usam inglês.
- NeoForge está disponível apenas para 1.21.1. NeoForge 1.20.1 continua
  planeado.

## Âmbito de validação

Uma compilação confirma que o JAR pode ser construído; não prova a interação
dentro do jogo. Os checks documentados cobrem builds selecionados. A ligação
com dois clientes Minecraft não foi testada nesta versão: o proprietário fará
esse teste posteriormente com amigos. Os percursos não testados por versão
estão listados nas [notas de validação](validation-working.md).

O teste de transporte usa dois processos Tailcat v0.7.0 e uma ligação TCP real;
não simula o protocolo do servidor Minecraft.

Fontes: https://fabricmc.net/ ; https://files.minecraftforge.net/ ;
https://github.com/tailscale/tailcat .
