import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;


public class MessageListener extends ListenerAdapter {
    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();
        if (msg.getContentRaw().equals("!ping")) {
            long time = System.currentTimeMillis();
            channel.sendMessage("Pong!") /* => RestAction<Message> */
                    .queue(response /* => Message */ -> {
                        response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                    });
        } else if (msg.getContentRaw().equals("Turko bot")) {
            int songNumber = getRandomNumber(0, 6);
            String songURL = switch (songNumber) {
                case 0 -> "https://www.youtube.com/watch?v=R9At2ICm4LQ";
                case 1 -> "https://www.youtube.com/watch?v=WyiIGEHQP8o";
                case 2 -> "https://www.youtube.com/watch?v=vHS9E6JFja8";
                case 3 -> "https://www.youtube.com/watch?v=c9RzZpV460k";
                case 4 -> "https://www.youtube.com/watch?v=J_CFBjAyPWE";
                case 5 -> "https://www.youtube.com/watch?v=XGdbaEDVWp0";
                default -> "https://www.youtube.com/watch?v=QslJYDX3o8s";
            };
            channel.sendMessage(songURL).queue();
        } else if (msg.getContentRaw().equals("Barka")) {
            String songURL = "https://www.youtube.com/watch?v=0qzLRlQFFQ4";
            channel.sendMessage(songURL).queue();
        }
    }
}