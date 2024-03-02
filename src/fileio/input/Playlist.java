package fileio.input;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;
    private int visibilityStatus = 1;
    private List<SongInput> songs;
    private String owner;
    private int followers = 0;

    public Playlist(final String name, final int visibilityStatus, final String owner) {
        this.name = name;
        this.visibilityStatus = visibilityStatus;
        this.songs = new ArrayList<>();
        this.owner = owner;
    }

    /**
     * Setter for the visibility status
     * @param visibilityStatus
     */
    public void setVisibilityStatus(final int visibilityStatus) {
        this.visibilityStatus = visibilityStatus;
    }
    /**
     * Getter for the number of followers
     * @return
     */
    public int getFollowers() {
        return followers;
    }
    /**
     * Increase the number of followers
     */
    public void increaseFollowers() {
        followers++;
    }
    /**
     * Decrease the number of followers
     */
    public void decreaseFollowers() {
        followers--;
    }
    /**
     * Getter for the name of the playlist
     * @return
     */
    public String getName() {
        return name;
    }
    /**
     * Getter for the owner of the playlist
     * @return
     */
    public String getOwner() {
        return owner;
    }
    /**
     * Getter for the visibility status
     * @return
     */
    public int getVisibilityStatus() {
        return visibilityStatus;
    }
    /**
     * Getter for the songs
     * @return
     */
    public List<SongInput> getSongs() {
        return songs;
    }
    /**
     * Add a song to the playlist
     * @param songName
     */
    public void addSong(final SongInput songName) {
        songs.add(songName);
    }
    /**
     * Remove a song from the playlist
     * @param songName
     */
    public void removeSong(final SongInput songName) {
        songs.remove(songName);
    }
}
