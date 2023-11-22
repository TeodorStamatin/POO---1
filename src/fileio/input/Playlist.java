package fileio.input;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;
    private int visibilityStatus;
    private List<SongInput> songs; // make that a list of SongInput

    // Constructor
    public Playlist(String name, int visibilityStatus) {
        this.name = name;
        this.visibilityStatus = visibilityStatus;
        this.songs = new ArrayList<>();
    }

    // Getters

    public String getName() {
        return name;
    }

    public int getVisibilityStatus() {
        return visibilityStatus;
    }

    public List<SongInput> getSongs() {
        return songs;
    }

    // Methods to add and remove songs

    public void addSong(SongInput songName) {
        songs.add(songName);
    }

    public void removeSong(SongInput songName) {
        songs.remove(songName);
    }
}
