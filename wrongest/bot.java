@bot.slash_command(description="Responde a 'World'")
async def hello(inter):
    await inter.response.send_message("World")
