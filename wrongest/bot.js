const Discord = require("discord.js")
const client = new Discord.Client()

client.on("ready", () => {
  console.log(`Logged in as ${client.user.tag}!`)
})

//respuesta//

client.on("message", msg => {
  if (msg.content === "ne") {
    msg.reply("gro");
  }
})

client.login(process.env.TOKEN)
