# Proiect GlobalWaves  - Etapa 1

<div align="center"><img src="https://tenor.com/view/listening-to-music-spongebob-gif-8009182.gif" width="300px"></div>

#### Assignment Link: [https://ocw.cs.pub.ro/courses/poo-ca-cd/teme/proiect/etapa1](https://ocw.cs.pub.ro/courses/poo-ca-cd/teme/proiect/etapa1)


## Skel Structure

* src/
  * checker/ - checker files
  * fileio/ - contains classes used to read data from the json files + classes made by myself
  * main/
      * Main - the Main class runs the checker on your implementation. Add the entry point to your implementation in it. Run Main to test your implementation from the IDE or from command line.
      * Test - run the main method from Test class with the name of the input file from the command line and the result will be written
        to the out.txt file. Thus, you can compare this result with ref.
* input/ - contains the tests and library in JSON format
* ref/ - contains all reference output for the tests in JSON format

## Tests results:
1. test01_searchBar_songs_podcasts - 4p
2. test02_playPause_song - 4p
3. test03_like_create_addRemove - 4p
4. test04_like_create_addRemove_error - 4p
5. test05_playPause_playlist_podcast - 4p
6. test06_playPause_error - 4p
7. test07_repeat - 4p
8. test08_repeat_error - 4p
9. test09_shuffle - 4p
10. test10_shuffle_error - 4p
11. test11_next_prev_forward_backward - 4p
12. test12_next_prev_forward_backward_error - 4p
13. test13_searchPlaylist_follow -  4p
14. test14_searchPlaylist_follow_error - 4p
15. test15_statistics - 0p
16. test16_complex - 0p
17. test17_complex - 0p

Final : 76p

<div align="center"><img src="https://media.tenor.com/qOI3iBvktYcAAAAd/giga-chad.gif"width="500px"></div>

# Remarks about the project:

  I used a class where I take all the commands and process them. This class is called Command. I also used a class named
UserInformation where I store all the informations about every users: in addition to the information from the library,
I also store the playlists. I wanted it to work like a database, so I can easily access the information about a user.
  
  In the main function, I extract all the users from the library and I create a list of UserInformation objects. Then, I
read the input files and I use a switch based on the command to call the function that processes the command.

### handleSearchCommand Method:

  Extracts information such as username, timestamp, filters and type from the commandNode. Then, I check if I already
had a loaded playlist, song or podcast and I update the time remaining. Then I use a helper function to handle the
filters and I return the resulting list of songs, podcasts or playlists.

### handleSelectCommand Method:

  Extracts information such as username, timestamp, and itemNumber from the commandNode. Then i adjust the selected index
to start from 0. Checks if the search results list is not null and if the index is valid. Then, I check what type of
search I performed and I update the current item. I also update the flag named selected to 1 or 0 depending on the index 
and results vector, to use it later.

### handleLoadCommand Method:

  Extracts information from the commandNode. Then I check if the last command was select. If it was, I check if the
selected object is either a song, podcast or playlist. If the object was already loaded, I update the timestamps and I
put it into the unpaused state. If i load a new object, I create a new object of it`s type. I pause all the other
objects and I update the selected, searched, and loaded flags.

### handlePlayPauseCommand Method:

  Checks the loaded state of the media player for different types of content (song, playlist, podcast) and changes the 
state of the player to !state (pause or play). I always update the timestamp and the time remaining.

### handleRepeatCommand Method:

  Extracts information and checks the loaded state of the media player for different types of content (song, podcast,
playlist). If a song is loaded, it updates the repeat state based on the current state and the repeat mode requested by
the user using a helper function that sets the repeat mode based of a number. It does a similar thing for podcasts and
playlists.

### handleStatusCommand Method:

  If a song is loaded, it creates an output JSON containing information about the song's status, including the song's
name, remaining time, repeat state, shuffle state, and playback state. If we have a podcast, it displays the current
episode`s name, and if we have a playlist, it displays the name of the current song. It calculates the time remaining
based on the timestamp every time and resets the initial timestamp.

### handleShuffleCommand Method:

  Checks if a source is loaded before attempting to use the shuffle function and if the loaded source is a playlist.
If a playlist is loaded and the command has a "seed," it shuffles the playlist based on the provided seed using a new
list of indices I use to update the current song index. Otherwise, it deactivates the shuffle function.

### handleCreatePlaylistCommand Method:

  Retrieves user information from the userInformationList based on the provided username and checks if the user already
has a playlist with the same name. If the playlist does not already exist, it creates a new playlist for the user with
the specified name, an initial visibility status (public), and the owner's username.

### handleAddRemoveInPlaylistCommand Method:

  Ensures that the loaded source is a song; if not, it returns an error message. Checks if the specified playlist exists
based on the provided playlistId and retrieves the playlist. First, it checks whether the song is already in the
playlist and if the song is in the playlist, it removes the song. If not, it adds the song to the playlist.

#### getUserByUsername Method: (helper function)

  Iterates through the userInformationList to find a user with a matching username. For each user in the list, it checks
if the username matches the provided username. If a match is found, it returns the corresponding UserInformation object
and if not, it returns null.

### handleLikeCommand Method:

  Retrieves the UserInformation object corresponding to the provided username from the userInformationList, checks the
type of loaded source, and if the song is already liked by the user, it removes the like. If not, it adds the like.

### handlePrefferedSongs Method:

  Retrieves the UserInformation object corresponding to the provided username from the userInformationList and gets the
list of liked songs from the user.

### handleShowPlaylistsCommand Method:

  Retrieves the UserInformation object corresponding to the provided username from the userInformationList and gets the
list of playlists from the user and iterates over each playlist, creating a JSON node for each with details such as
name, songs, visibility, and followers.

#### convertListToJsonArray Method: (helper function)

  Creates a new ArrayNode using JsonNodeFactory and iterates over each SongInput item in the provided list and adds the
name of each song to the JSON array.

### handleNextCommand Method:

  Checks if a source (song, podcast, or playlist) is loaded. If a source is loaded, it calls the coresponing method for
each type of source. Checks if the source has reached the end and if it did, it unloads it.

### handlePrevCommand Method:

If a source is loaded, it goes to the previous track, and if it is the first track, it goes from the beginning.

### handleForwardCommand Method:

  Calls the forward method on the currentPodcastState to skip 90 seconds. If the forward operation causes the podcast to
stop, sets loadedPodcast to false.

### handleBackwardCommand Method:

  Calls the backward method on the currentPodcastState to rewind 90 seconds. If we currently are at the first episode,
it goes from the beginning.

### handleFollowCommand Method:

  Checks if the current source is not a playlist (both currentSong and currentPodcast are not null). Checks if a source
is selected and marked for follow using the selectedForFollow flag. If the source is selected and marked for follow,
unsets the selectedForFollow flag and checks if a playlist is loaded. It xearches for the playlist to follow among the
playlists of the user to follow. Checks if the user is already following the playlist. If true, decreases the followers
count and unfollows the playlist. If false, increases the followers count and follows the playlist.

### handleSwitchVisibilityCommand Method:

  Retrieves information about the user and the user who owns the current playlist. Searches for the current playlist
among the playlists of the user who owns it and checks the current visibility status of the playlist. If public, sets
it to private and vice versa.

<div align="center"><img src="https://media.tenor.com/dNUP3nUH8gkAAAAd/giga-chad-average-enjoyer.gif"width="500px"></div>


# NOTE : 
 acum pe vm checker sunt 72 de pct, imi pica testul 12, prev next error.
 a trebuit sa fac o modificare la cod, la clasa PodcastState. Acum, cu fisierul de pe, arata asa :
```agsl
/**
     * Method for the forward 90 seconds
     * @return
     */
    public boolean forward(final int timestamp) {
        lastTimestamp = timestamp;
        calculateTimeRemaining();
        if (timeRemaining > 90) {
            timeRemaining -= 90;
            return false;
        } else {
            return next(timestamp);
        }
    }
    /**
     * Method for the backward 90 seconds
     * @return
     */
    public void backward(final int timestamp) {
        lastTimestamp = timestamp;
        calculateTimeRemaining();
        if (episodes.get(currentEpisodeIndex).getDuration() - timeRemaining
                > 90) {
            timeRemaining += 90;
        } else {
            prev(timestamp);
        }
    }
```

Aceasta varianta are 72 de pct de pe checker. Totusi, eu pe local am urmatoarea varianta, cu care iau 76, adica
imi trece testul 12:

```agsl
/**
     * Method for the forward 90 seconds
     * @return
     */
    public boolean forward(final int timestamp) {
        lastTimestamp = timestamp;
        calculateTimeRemaining();
        if (timeRemaining > 90) {
            timeRemaining += 90;
            return false;
        } else {
            return next(timestamp);
        }
    }
    /**
     * Method for the backward 90 seconds
     * @return
     */
    public void backward(final int timestamp) {
        lastTimestamp = timestamp;
        calculateTimeRemaining();
        if (episodes.get(currentEpisodeIndex).getDuration() - timeRemaining
                > 90) {
            timeRemaining -= 90;
        } else {
            prev(timestamp);
        }
    }
```