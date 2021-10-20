import discord
import random

token = 'replace this with your token' # without this the code cannot connect to your bot

client = discord.Client()

ignoreUsers = [235148962103951360] # get the id of the user whose posts you want to ignore by doing \ @User in a private channel or use developer mode in Discord

greetings = ['hello','bonjour','hola']

def startsWithO(sentence): # to detect if a post starts with a word made up of just the letter o.
    if sentence.startswith('o'):
        if ' ' in sentence: # if post has more than one word
            sentence = sentence.split(' ',1) # split into two parts at the first space
            word = sentence[0] # grab the first word only
        else:
            word = sentence
        if word.count('o') == len(word):
            return True
    return False

def removeEmojisAndUsernames(input): # sanitize the input
    words = input.split(' ')
    for w in words:
        if '<' in w and '>' in w: # emojis and user ids are encased in these characters
            id = w[w.index('<'):w.index('>')] # extract the emoji/userid
            input = input.replace(id, '') # replace it in the input, essentially ignoring it
    return input

def ignoreCertainUsers(postAuthorId): # don't respond to posts from specified users
    if postAuthorId in ignoreUsers:
        return True
    return False

@client.event
async def on_ready():   # when the connection is established
    print('We have logged in as {0.user}'.format(client))

@client.event
async def on_message(message):  # when any message is posted on a channel the bot has access to within the server
    if message.author == client.user or ignoreCertainUsers(message.author.id): #bot shouldn't react to its own posts or those of specified users
        return

    # since these keywords are preceded by a special character '!' and must be at the start of the post, we can merely lowercase the input and evaluate

    if message.content.lower().startswith('!anonfeedback'): # a user may DM a bot and use this keyword at the start of their post to send anonymous feedback
        await client.get_channel(856418867525582849).send('Anon feedback: '+message.content+'|| ChannelId:'+str(message.channel.id)+'. ReplyId:'+str(message.id)+'.||')
        # replace this channel id with one from your server (preferably a private channel with limited access)

    if message.content.lower().startswith('!replytoanonfeedback'): # reply to the anonymous feedback posted by the bot and start your reply with this keyword of '!replytoanonfeedback'
    # the bot will transmit your reply to the user who sent the anonymous feedback while keeping their identity secret
        anonMessage = (await client.get_channel(856418867525582849).fetch_message(message.reference.message_id))
        originalMessageId = anonMessage.content.split('ReplyId:')
        originalMessageId = originalMessageId[1]
        originalMessageId = originalMessageId[:originalMessageId.index('.')]
        originalChannelId = anonMessage.content.split('ChannelId:')
        originalChannelId = originalChannelId[1]
        originalChannelId = originalChannelId[:originalChannelId.index('.')]
        await (await (await client.fetch_channel(originalChannelId)).fetch_message(originalMessageId)).reply(message.content)

    if message.content.lower().startswith('!greetme'): #reply to user with a greeting in a random language
        await message.reply(message.author.display_name+', '+greetings[random.randint(0, len(greetings))], mention_author=True)

    if message.content.lower().startswith('!showmegreetings'): # exposes the greetings available
            await message.reply(str(greetings))

    if '<@!897566211074842624>' in message.content.lower(): # if a post mentions your bot, the bot will appear and acknowledge
        # get your bot's id from the developer portal - it's the application id as well as the client id.
        await message.channel.send(message.author.mention+' has summoned me.')

    if startsWithO(message.content.lower()): # if a post starts with ooooo of any length
        await message.add_reaction('👀') # this is an example of adding a standard unicode emoji reaction to a post

    post = removeEmojisAndUsernames(message.content.lower()) # strip the message of any emoji names and user names

    # this is helpful if you want detect words that are not explicitly summoning your bot (no symbol before them, not at the start of a post)

    if 'hug' in post:
        await message.add_reaction('<:aww:856885630507548702>') # this is an example of adding a custom emoji to a post (works for both animated and non-animated)
        # to get the id of a custom emoji, right click it and select copy link. You can find the id inside the URL copied.

    # emojis can be used inside replies as well!

client.run(token) # connect to the bot