# Compatibilidade

Alvos desta primeira versão:

| Minecraft | Fabric Loader | Fabric API | Forge | Java do jogo |
| --- | --- | --- | --- | --- |
| 1.12.2 | Não disponível oficialmente | — | 14.23.5.2864 | 8 |
| 1.16.5 | 0.16.10+ | 0.42.0+1.16 | 36.2.34 | 8 |
| 1.18.2 | 0.16.10+ | 0.77.0+1.18.2 | 40.3.0 | 17 |
| 1.20.1 | 0.16.10+ | 0.92.6+1.20.1 | 47.4.28 | 17 |
| 1.21.1 | 0.16.10+ | 0.116.6+1.21.1 | 52.1.20 | 21 |

O núcleo partilhado é compatível com Java 8. Cada módulo tem código de menus e ligação específico da versão do Minecraft e do loader. Não existe um JAR universal.

A compilação é uma verificação separada do funcionamento em jogo. **Todos os builds ainda precisam de teste manual com dois clientes Minecraft**, incluindo redes distintas. O teste automatizado do núcleo usa dois processos Tailcat v0.7.0 e uma ligação TCP real; não simula o protocolo Minecraft.

Versões intermédias e posteriores a 1.21.1 ainda não estão incluídas. O suporte mais recente será acrescentado depois da validação destas versões principais.

Fontes: https://fabricmc.net/ ; https://files.minecraftforge.net/ ; https://github.com/tailscale/tailcat .

## LinceLink 0.1.6

Os nove módulos têm a nova marca e o logo. Os JARs começam por `lincelink-`. Instalação automática, ping no Tab, Direta/DERP e interface nos 145 idiomas estão no build **Forge 1.20.1**. Os restantes módulos mantêm as funcionalidades base e o executável Tailcat instalado manualmente. Remover o JAR antigo antes de instalar o novo, pois representam o mesmo mod.
