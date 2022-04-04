public class Song {
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public void print()
    {
        System.out.print(author + "\t");
        System.out.print(title + "\t");
        System.out.print(url + "\t");
        System.out.print(played + "\t");
        System.out.println();
    }
    public String toString()
    {
        return author+ "\t" + title+ "\t" + url + "\t" + played;
    }

    String author;
    String title;
    String url;
    int played;
}
