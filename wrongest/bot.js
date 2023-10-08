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

TOKEN= [NzU1ODc5OTIyNjY4NDA0Nzk2.GK4Po8.3Ao_ywcqfUoqsK5TBMF5k_OKZbdNZL4jFQKCz0]
