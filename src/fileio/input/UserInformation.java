package fileio.input;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class UserInformation {
    private String username;
    private int age;
    private String city;
    private List<String> likedSongs;
    private List<Playlist> playlists;
    private Set<String> followedPlaylists;

    // Constructor
    public UserInformation(String username, int age, String city) {
        this.username = username;
        this.age = age;
        this.city = city;
        this.likedSongs = new ArrayList<>();
        this.playlists = new ArrayList<>();
        this.followedPlaylists = new HashSet<>();
    }

    // Getters and setters for basic user information

    public String getUsername() {
        return username;
    }

    public int getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    // Liked songs methods

    public List<String> getLikedSongs() {
        return likedSongs;
    }

    public void likeSong(String songName) {
        likedSongs.add(songName);
    }

    public void unlikeSong(String songName) {
        likedSongs.remove(songName);
    }

    // Playlist methods

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    //getter for a certain playlist songs

    public void createPlaylist(String playlistName, int visibilityStatus) {
        Playlist playlist = new Playlist(playlistName, visibilityStatus);
        playlists.add(playlist);
    }

    public void addSongToPlaylist(String playlistName, SongInput songName) {
        Playlist playlist = findPlaylistByName(playlistName);
        if (playlist != null) {
            playlist.addSong(songName);
        }
    }

    public void removeSongFromPlaylist(String playlistName, SongInput songName) {
        Playlist playlist = findPlaylistByName(playlistName);
        if (playlist != null) {
            playlist.removeSong(songName);
        }
    }

    // Followed playlists methods

    public Set<String> getFollowedPlaylists() {
        return followedPlaylists;
    }

    public void followPlaylist(String playlistName) {
        followedPlaylists.add(playlistName);
    }

    public void unfollowPlaylist(String playlistName) {
        followedPlaylists.remove(playlistName);
    }

    // Helper method to find a playlist by name
    private Playlist findPlaylistByName(String playlistName) {
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(playlistName)) {
                return playlist;
            }
        }
        return null;
    }
    public boolean playlistExists(String playlistName) {
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(playlistName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsSong(Playlist playlist, SongInput songName) {
        return playlist.getSongs().contains(songName);
    }

    public int getFollowers(List<UserInformation> userInformationList, Playlist playlist) {
        int followersCount = 0;

        for (UserInformation user : userInformationList) {
            if (user.isFollowingPlaylist(playlist)) {
                followersCount++;
            }
        }

        return followersCount;
    }

    private boolean isFollowingPlaylist(Playlist playlist) {
        for (String followedPlaylistName : followedPlaylists) {
            if (followedPlaylistName.equals(playlist.getName())) {
                return true;
            }
        }
        return false;
    }

}
