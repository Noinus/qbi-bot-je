import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.entities.Activity;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main extends ListenerAdapter {
  public static void main(String[] args) throws Exception {
    String token = args[0];

    JDABuilder builder = JDABuilder.createDefault(token)
            .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
            .setBulkDeleteSplittingEnabled(false)
            .addEventListeners(new Main())
            .addEventListeners(new MessageListener())
            .setActivity(Activity.playing("Being the best bot"));

    builder.build();
  }

  private final AudioPlayerManager playerManager;
  private final Map<Long, GuildMusicManager> musicManagers;

  private Main() {
    this.musicManagers = new HashMap<>();

    this.playerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(playerManager);
    AudioSourceManagers.registerLocalSource(playerManager);
  }

  private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
    long guildId = Long.parseLong(guild.getId());
    GuildMusicManager musicManager = musicManagers.get(guildId);

    if (musicManager == null) {
      musicManager = new GuildMusicManager(playerManager);
      musicManagers.put(guildId, musicManager);
    }

    guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

    return musicManager;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    String[] command = event.getMessage().getContentRaw().split(" ", 2);

    if ("j.play".equals(command[0]) && "sanah".equals(command[1]))
    {
      event.getChannel().sendMessage("Sanah playlist added").queue();
      loadAndPlayPlaylist((TextChannel) event.getChannel(), "C:\\Users\\Kamil\\Documents\\Java\\sanah.csv");
    }
    else if ("j.play".equals(command[0]) && "kpop".equals(command[1]))
    {
      event.getChannel().sendMessage("kpop playlist added").queue();
      loadAndPlayPlaylist((TextChannel) event.getChannel(), "C:\\Users\\Kamil\\Documents\\Java\\test.csv");
    }
    else if ("j.play".equals(command[0]) && command.length == 2)
    {
      loadAndPlay((TextChannel) event.getChannel(), command[1]);
    }
    else if ("j.skip".equals(command[0])) {
      skipTrack((TextChannel) event.getChannel());
    }
    else if ("j.queue".equals(command[0])){
      printQueue((TextChannel) event.getChannel());
    }

    super.onMessageReceived(event);
  }

  private void loadAndPlay(final TextChannel channel, final String trackUrl) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

    playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

        play(channel.getGuild(), musicManager, track);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack firstTrack = playlist.getSelectedTrack();

        if (firstTrack == null) {
          firstTrack = playlist.getTracks().get(0);
        }

        channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

        play(channel.getGuild(), musicManager, firstTrack);
      }

      @Override
      public void noMatches() {
        channel.sendMessage("Nothing found by " + trackUrl).queue();
      }

      @Override
      public void loadFailed(FriendlyException exception) {
        channel.sendMessage("Could not play: " + exception.getMessage()).queue();
      }
    });
  }

  private void loadAndPlayPlaylist(final TextChannel channel, final String filePath)
  {
    ArrayList<Song> loadedPlaylist = WrapperLoader.loadFromFile(filePath);
    Collections.shuffle(loadedPlaylist);
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    String trackUrl;

    int j = 1;
    //channel.sendMessage("Adding to queue:").queue();
    for(Song i : loadedPlaylist) {
      trackUrl = i.getUrl();

      int finalJ = j;
      playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
        @Override
        public void trackLoaded(AudioTrack track) {
          //channel.sendMessage(finalJ + ". " + track.getInfo().title).queue();
          play(channel.getGuild(), musicManager, track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
          AudioTrack firstTrack = playlist.getSelectedTrack();

          if (firstTrack == null) {
            firstTrack = playlist.getTracks().get(0);
          }

          play(channel.getGuild(), musicManager, firstTrack);
        }

        @Override
        public void noMatches() {
        }

        @Override
        public void loadFailed(FriendlyException exception) {
        }
      });
      j++;
    }
  }

  private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
    connectToFirstVoiceChannel(guild.getAudioManager());

    musicManager.scheduler.queue(track);
  }

  private void printQueue(TextChannel channel) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    AudioTrack[] listOfTracks = musicManager.scheduler.getListOfTracks();

    StringBuilder message = new StringBuilder();

    if(listOfTracks.length > 5)
    {
      for (int i=0; i<5; i++)
      {
        message.append(i + 1).append(". ").append(listOfTracks[i].getInfo().title).append("\n");
        //channel.sendMessage(i+1 + ". " + listOfTracks[i].getInfo().title).queue();
      }
      message.append("... and ").append(listOfTracks.length - 5).append(" others");
      //channel.sendMessage("... and " + (listOfTracks.length-5) + " others").queue();
    }
    else {
      for (int i = 0; i < listOfTracks.length; i++) {
        message.append(i + 1).append(". ").append(listOfTracks[i].getInfo().title).append("\n");
        //channel.sendMessage(i+1 + ". " + listOfTracks[i].getInfo().title).queue();
      }
    }
    channel.sendMessage(message.toString()).queue();
  }

  private void skipTrack(TextChannel channel) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    musicManager.scheduler.nextTrack();

    channel.sendMessage("Skipped to next track.").queue();
  }

  private static void connectToFirstVoiceChannel(AudioManager audioManager) {
    if (!audioManager.isConnected()) {
      for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
        audioManager.openAudioConnection(voiceChannel);
        break;
      }
    }
  }
}