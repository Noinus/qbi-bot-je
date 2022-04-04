import com.opencsv.CSVReader;
import org.json.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WrapperLoader {

    static public void loadFromReddit() throws IOException {
        String dicPath = "C:\\Users\\Kamil\\Documents\\Java\\GG.txt";
        String dictionary = Files.readString(Path.of(dicPath));

        ArrayList<Song> listOfSongs = new ArrayList<Song>();

        JSONObject obj = JsonReader.readJsonFromUrl("https://www.reddit.com/r/kpop/new.json?sort=new");
        JSONArray arr = null;
        if (obj != null) {
            arr = obj.getJSONObject("data").getJSONArray("children");
            for (int i = 0; i < arr.length(); i++) {
                if (Objects.equals(arr.getJSONObject(i).getJSONObject("data").getString("link_flair_text"), "[MV]")) {
                    System.out.println("////////////// New song found! //////////////");

                    Song song = new Song();

                    String title = arr.getJSONObject(i).getJSONObject("data").getString("title");
                    System.out.println(title);
                    String[] tokens = title.split(" - ");
                    song.setAuthor(tokens[0]);
                    song.setTitle(tokens[1]);
                    String url = arr.getJSONObject(i).getJSONObject("data").getString("url_overridden_by_dest");
                    song.setUrl(url);
                    song.setPlayed(1);
                    song.print();
                    if (dictionary.toUpperCase().contains(song.getAuthor().toUpperCase()))
                    {
                        System.out.println("Girl group found :D");
                        listOfSongs.add(song);
                    }
                    else
                    {
                        System.out.println("This author is not in GG database :/");
                    }
                }
            }
            if (listOfSongs.isEmpty()) {
                System.out.println("########### No new song found :( ###########");
            }
        }
        else {
            System.out.println("%%%%%%%%%% Connection error :( %%%%%%%%%%");
        }
    }


    static ArrayList<Song> loadFromFile(String filePath)
    {
        ArrayList<Song> listOfSongs = new ArrayList<Song>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> list = reader.readAll();
            for (String[] row : list) {
                Song song = new Song();
                song.setAuthor(row[0]);
                song.setTitle(row[1]);
                song.setUrl(row[2]);
                song.setPlayed(Integer.parseInt(row[3]));
                //song.print();
                listOfSongs.add(song);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listOfSongs;
    }
}
