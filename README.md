# Discord_Bot
An abstracted public version of a private Discord Bot I've written. 

Features include:
* Letting users submit anonymous feedback by DMing the bot with a command
* Letting an admin/mod reply to this feedback while maintaining anonymity
* Replying to a user with their display name and a random greeting phrase
* Respond if your bot is mentioned (with an @)
* Add (both standard and custom) emoji reactions to posts based on keywords
* Sanitize input (ignore emojinames and usernames when looking for keywords in posts)

I've provided the code in two languages: _Java_ and _Python_. I started out writing my bot in Java, but switched to Python for memory reasons. The Java version was using a lot more RAM (160MB vs 25 MB). The Python version was therefore easier to host remotely for free.

Hosting remotely is important to ensure the bot is up 24/7. You will want to do all your beta testing on your computer first though.

**Minimum Requirements** (higher versions are fine too): \
For Java: Java 8 + Maven 2 \
For Python: Python 3.5.3 + discord.py 1.7.3 + random2.py 1.0.1 \
&nbsp;&nbsp;&nbsp;&nbsp;See [Discord.py Docs](https://discordpy.readthedocs.io/en/latest/intro.html) for pip commands.

In order to use this functionality for your own bot:
1. Create a Bot Application on the Discord Developer portal and obtain a bot token. You can follow [this guide](https://www.writebots.com/discord-bot-token/).
   * permissions needed: Read Message History, Send Messages, Add Reactions
2. Replace the 'token' string variable with your bot's token.
3. Run the program. If using Java, make sure to ignore the python file when you build the jar.