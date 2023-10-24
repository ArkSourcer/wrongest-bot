### Configuraciones
Enfocado a describir los parametros confusos encontrados en ShardDustryCore.json

`discordGuild` contiene la id del servidor en el que el bot trabajará, solo puede ser uno

`joinMessage` contiene el formato con el que se mostrarán los mensajes en discord al unirse dentro del juego, agrega {p} para mostrar el nombre del jugador y {pc} para mostrar la cantidad de jugadores

`chatMessage` contiene el formato con el que se mostrarán los mensajes en discord al mandar un mensaje en el chat del juego, agrega {p} para mostrar el nombre del jugador y {m} para mostrar el mensaje que ha enviado

`leaveMessage` contiene el formato con el que se mostrarán los mensajes en discord al salir del juego, agrega {p} para mostrar el nombre del jugador y {pc} para mostrar la cantidad de jugadores

`discordMessage` contiene el formato con el que se mostrarán los mensajes dentro del juego al mandar un mensaje en el canal correspondiente de discord, agrega {a} para mostrar el nombre del autor y {m} para mostrar su mensaje

`slashCommandAllowedRole` contiene la id del rol que permitirá hacer uso de los comandos slash especiales

`commandChannelID` contiene la id del canal donde las respuestas de comandos slash serán enviadas

`connectionList` contendrá un arreglo de id's de canales que se considerarán interconectados con el juego, es decir, en los que los mensajes enviados llegarán a mindustry y viceversa.

`serverIDList` contendrá un arreglo de id's o nombres de servidores, esto permitirá el uso de comandos slash al coincidir el id de este arreglo con el parametro propio en cada servidor
