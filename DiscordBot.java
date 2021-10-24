import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.AllowedMentions;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class DiscordBot {

    private static final List<String> ignoreUsers = Arrays.asList("235148962103951360", "897566211074842624"); // get the id of the user whose posts you want to ignore by doing \ @User in a private channel or use developer mode in Discord
    // you will want to include the id of your bot here so it doesn't respond to its own posts
    // get your bot's id from the developer portal - it's the application id as well as the client id.

    private static final List<String> greetings = Arrays.asList("hello", "bonjour", "hola");

    private static boolean startsWithO(String sentence) { //  to detect if a post starts with a word made up of just the letter o.
        if (sentence.startsWith("o")) {
            IntStream word = !sentence.contains(" ") ? sentence.chars() : sentence.substring(0, sentence.indexOf(" ")).chars();
            return word.distinct().count() <= 1;
        }
        return false;
    }

    private static String removeEmojisAndUsernames(String input) {  // sanitize the input
        String[] words = input.split(" ");
        for (String word : words) {
            if (word.contains("<") && word.contains(">")) { // emojis and user ids are encased in these characters
                String id = word.substring(word.indexOf("<"), word.indexOf(">")); // extract the emoji/userid
                input = input.replace(id, " "); // replace it in the input, essentially ignoring it
            }

        }
        return input;
    }

    private static boolean validUser(String postAuthorId) { // don't respond to posts from specified users
        return !ignoreUsers.contains(postAuthorId);
    }

    private final static String token = "replace this with your token"; // without this the code cannot connect to your bot

    public static void main(String[] args) {

        GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

        client.getEventDispatcher().on(ReadyEvent.class)  // when the connection is established
                .subscribe(event -> {
                    User self = event.getSelf();
                    System.out.printf("Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator());
                });

        // a user may DM a bot and use this keyword at the start of their post to send anonymous feedback
        client.getEventDispatcher().on(MessageCreateEvent.class, event -> {
            if (event.getMessage().getContent().startsWith("!anonfeedback")) {
                return client.getChannelById(Snowflake.of("856418867525582849")) // replace this channel id with one from your server (preferably a private channel with limited access)
                        .ofType(MessageChannel.class)
                        .flatMap(channel -> channel.createMessage(spec -> spec.setContent("Anon feedback: " + event.getMessage().getContent() + "|| ChannelId:" + event.getMessage().getChannelId().asLong() + ". ReplyId:" + event.getMessage().getId().asLong() + ".||")));
            }
            return Mono.empty();
        }).subscribe();

        // reply to the anonymous feedback posted by the bot and start your reply with this keyword of '!replytoanonfeedback'
        // the bot will transmit your reply to the user who sent the anonymous feedback while keeping their identity secret
        client.getEventDispatcher().on(MessageCreateEvent.class, event -> {
            if (event.getMessage().getContent().startsWith("!replytoanonfeedback")) {
                String replyToSend = event.getMessage().getContent();
                String anonMessage = event.getMessage().getReferencedMessage().get().getContent();
                String originalChannelId = anonMessage.substring(anonMessage.indexOf("|| ChannelId:")+13,anonMessage.indexOf(". ReplyId:"));
                String originalMessageId = anonMessage.substring(anonMessage.indexOf(". ReplyId:")+10,anonMessage.indexOf(".||"));
                return client.getChannelById(Snowflake.of(originalChannelId))
                        .ofType(MessageChannel.class)
                        .flatMap(channel -> channel.createMessage(spec -> {
                            spec.setContent(replyToSend);
                            spec.setMessageReference(Snowflake.of(originalMessageId));
                        }));
            }
            return Mono.empty();
        }).subscribe();

        // if post starts with !greetme then reply to user with a greeting in a random language while ignoring posts of certain users and the bot itself
        client.getEventDispatcher().on(MessageCreateEvent.class, event -> {
            if (validUser(event.getMember().get().getId().toString()) && event.getMessage().getContent().startsWith("!greetme")) {
                return event.getMessage().getChannel()
                        .flatMap(channel -> channel.createMessage(spec -> {
                            spec.setContent(event.getMember().get().getDisplayName() + ", " + greetings.get(new Random().ints(1, 0, greetings.size()).findFirst().getAsInt()));
                            spec.setAllowedMentions(AllowedMentions.builder().build());
                            spec.setMessageReference(event.getMessage().getId());
                        }));
            }
            return Mono.empty();
        }).subscribe();

        // exposes the greetings available
        client.getEventDispatcher().on(MessageCreateEvent.class, event -> {
            if (validUser(event.getMember().get().getId().toString()) && event.getMessage().getContent().startsWith("!showmegreetings")) {
                return event.getMessage().getChannel()
                        .flatMap(channel -> channel.createMessage(spec -> {
                            spec.setContent(String.join(", ", greetings));
                            spec.setMessageReference(event.getMessage().getId());
                        }));
            }
            return Mono.empty();
        }).subscribe();

        // if a post mentions your bot, the bot will appear and acknowledge
        client.getEventDispatcher().on(MessageCreateEvent.class, event -> {
            if (validUser(event.getMember().get().getId().toString()) && event.getMessage().getContent().contains("<@!897566211074842624>")) {
                return event.getMessage().getChannel()
                        .flatMap(channel -> channel.createMessage(spec -> {
                            spec.setContent(event.getMember().get().getNicknameMention() + " has summoned me.");
                            spec.setAllowedMentions(AllowedMentions.builder().allowUser(event.getMember().get().getId()).build());
                            spec.setMessageReference(event.getMessage().getId());
                        }));
            }
            return Mono.empty();
        }).subscribe();

        // if post starts with a word made up of the letter 'o' then add the 'eyes' standard emoji
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(event -> {
                    if(event.getMember().isPresent()) {
                        return validUser(event.getMember().get().getId().toString());
                    }
                    return true;
                })                .map(MessageCreateEvent::getMessage)
                .filter(message -> startsWithO(message.getContent()))
                .flatMap(message -> message.addReaction(ReactionEmoji.unicode("\uD83D\uDC40")))
                .subscribe();


        // if post contains the word 'hug' (after sanitizing) then add a custom animated emoji
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(event -> {
                    if(event.getMember().isPresent()) {
                        return validUser(event.getMember().get().getId().toString());
                    }
                    return true;
                })
                .map(MessageCreateEvent::getMessage)
                .filter(message -> removeEmojisAndUsernames(message.getContent()).contains("hug"))
                .flatMap(message -> message.addReaction(ReactionEmoji.of(856885630507548702L, "aww", true)))
                //to get the id of a custom emoji, right click it and select copy link. You can find the id inside the URL copied.
                .subscribe();


        client.onDisconnect().

                block();

    }
}