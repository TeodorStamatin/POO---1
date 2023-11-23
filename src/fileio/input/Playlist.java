package fileio.input;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;

    public void setVisibilityStatus(int visibilityStatus) {
        this.visibilityStatus = visibilityStatus;
    }

    private int visibilityStatus = 1;
    private List<SongInput> songs; // make that a list of SongInput
    private String owner;

    public int getTimeCreated() {
        return timeCreated;
    }

    private int timeCreated;
    public int getFollowers() {
        return followers;
    }

    private int followers = 0;

    // Constructor
    public Playlist(String name, int visibilityStatus, String owner, int timeCreated) {
        this.name = name;
        this.visibilityStatus = visibilityStatus;
        this.songs = new ArrayList<>();
        this.owner = owner;
        this.timeCreated = timeCreated;
    }

    // Getters
    public void increaseFollowers() {
        followers++;
    }
    public void decreaseFollowers() {
        followers--;
    }
    public String getName() {
        return name;
    }
    public String getOwner() {
        return owner;
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
