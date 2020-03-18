# regras-fiscais

- Criar banco de dados postgres chamado "fiscal"
- Usar Java 11
- POI consome muito heap iniciar com -Xms1024m -Xmx10000m
- Pasta SQL tem o sql que le corretamente as bases para extrair as regras

Serviços:
/fiscal/arquivos/update
- Atualiza todos os arquivos no diretório documentos


fiscal/regras/update
- Antes de atualizar as regras, va no diretório e remova as versões mais antigas e também o de MT, que esta corrompido.
- Le todos os arquivos do diretório documentos e atualiza as tabelas
- Os arquivos já importados são ignorados

fiscal/regras/versao
Opcional: Atualiza no banco a ultima versão válida das regras



