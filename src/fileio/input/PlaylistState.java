package fileio.input;

import java.util.List;

public class PlaylistState {

    private String selectedPlaylistName;
    private boolean isShuffleEnabled;
    private boolean isPaused;
    private String repeatState;
    private int lastTimestamp;
    private int initialTimestamp;
    private List<SongInput> tracks;
    private int currentTrackIndex = 0;
    private int timeRemaining;
    private int duration;
    private int savedTrackIndex;
    private List<Integer> shuffleOrder;
    private int currentShuffleIndex = 0;
    private Boolean waitSong = false;
    private String owner;

    // Constructor
    public PlaylistState(final String selectedPlaylistName, final boolean isShuffleEnabled,
                         final boolean isPaused, final String repeatState,
                         final int lastTimestamp, final int initialTimestamp,
                         final List<SongInput> tracks, final String owner) {
        this.selectedPlaylistName = selectedPlaylistName;
        this.isShuffleEnabled = isShuffleEnabled;
        this.isPaused = isPaused;
        this.repeatState = repeatState;
        this.lastTimestamp = lastTimestamp;
        this.initialTimestamp = initialTimestamp;
        this.tracks = tracks;
        this.owner = owner;
        updateCurrentTrackInfo();
    }

    /**
     * Getter for paused state
     * @return
     */
    public boolean isPaused() {
        return isPaused;
    }
    /**
     * Getter selected playlist name
     * @return
     */
    public String getSelectedPlaylistName() {
        return selectedPlaylistName;
    }
    /**
     * Getter for the repeat state
     * @return
     */
    public String getRepeatState() {
        return repeatState;
    }
    /**
     * Getter for all the tracks
     * @return
     */
    public List<SongInput> gettracks() {
        return tracks;
    }
    /**
     * Getter for owner of the playlist
     * @return
     */
    public String getOwner() {
        return owner;
    }
    /**
     * Getter for shuffle state
     * @return
     */
    public boolean isShuffleEnabled() {
        return isShuffleEnabled;
    }
    /**
     * Setter for shuffle state
     * @return
     */
    public void setShuffleEnabled(final boolean shuffleEnabled) {
        isShuffleEnabled = shuffleEnabled;
    }
    /**
     * Setter for paused state
     * @return
     */
    public void setPaused(final boolean paused) {
        isPaused = paused;
    }
    /**
     * Setter for repeat state
     * @return
     */
    public void setRepeatState(final String repeatState) {
        this.repeatState = repeatState;
    }
    /**
     * Setter for last timestamp
     * @return
     */
    public void setLastTimestamp(final int lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }
    /**
     * Setter for initial timestamp
     * @return
     */
    public void setInitialTimestamp(final int initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }
    /**
     * Method to update the current track info
     * @return
     */
    public void updateCurrentTrackInfo() {
        duration = tracks.get(currentTrackIndex).getDuration();
        timeRemaining = tracks.get(currentTrackIndex).getDuration();
    }
    /**
     * Method to calculate the time remaining
     * @return
     */
    public int calculateTimeRemaining() {
        if (isPaused) {
            return timeRemaining;
        } else {
            int elapsedTime = lastTimestamp - initialTimestamp;

            if (isShuffleEnabled) {
                while (elapsedTime >= timeRemaining) {
                    if (waitSong) {
                        waitSong = false;
                        elapsedTime -= timeRemaining;
                        currentShuffleIndex = savedTrackIndex;
                        currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                        updateCurrentTrackInfo();
                    } else {
                        elapsedTime -= timeRemaining;
                        currentShuffleIndex++;
                        currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                        if (repeatState.equals("Repeat Current Song")) {
                            currentShuffleIndex--;
                            currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                        }
                        if (currentShuffleIndex >= tracks.size()) {
                            if (repeatState.equals("Repeat All")) {
                                currentShuffleIndex = 0;
                                currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                            } else {
                                currentTrackIndex = 0;
                                isShuffleEnabled = false;
                                return 0;
                            }
                        }
                        updateCurrentTrackInfo();
                    }
                }
            } else {
                while (elapsedTime >= timeRemaining) {
                    if (waitSong) {
                        waitSong = false;
                        elapsedTime -= timeRemaining;
                        currentTrackIndex++;
                        if (currentTrackIndex >= tracks.size()) {
                            currentTrackIndex = 0;
                            return 0;
                        }
                        updateCurrentTrackInfo();
                    } else {
                        elapsedTime -= timeRemaining;
                        currentTrackIndex++;
                        if (repeatState.equals("Repeat Current Song")) {
                            currentTrackIndex--;
                        }
                        if (currentTrackIndex >= tracks.size()) {
                            if (repeatState.equals("Repeat All")) {
                                currentTrackIndex = 0;
                            } else {
                                currentTrackIndex = 0;
                                return 0;
                            }
                        }
                        updateCurrentTrackInfo();
                    }
                }
            }
            return Math.max(0, timeRemaining - elapsedTime);
        }
    }
    /**
     * Setter for the time remaining
     * @return
     */
    public void setTimeRemaining(final int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
    /**
     * Getter for the time remaining
     * @return
     */
    public int getTimeRemaining() {
        return timeRemaining;
    }
    /**
     * Getter for the name of the current track
     * @return
     */
    public String getSelectedTrackName() {
        return tracks.get(currentTrackIndex).getName();
    }
    /**
     * Method that makes the necessary changes when the shuffle is enabled
     * @return
     */
    public void shuffleOn(final List<Integer> shuffleOrder) {

        if (!isShuffleEnabled) {
            savedTrackIndex = currentTrackIndex + 1;
        }

        this.shuffleOrder = shuffleOrder;
        waitSong = true;
    }
    /**
     * Method that makes the necessary changes when the shuffle is disabled
     * @return
     */
    public void shuffleOff() {

        waitSong = true;
        currentShuffleIndex = currentTrackIndex;
        currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
    }
    /**
     * Method that resets the playlist
     * @return
     */
    public void reset() {
        currentTrackIndex = 0;
        isShuffleEnabled = false;
        updateCurrentTrackInfo();
    }
    /**
     * Method for the next song
     * @return
     */
    public boolean next(final int timestamp) {
        lastTimestamp = timestamp;
        initialTimestamp = timestamp;
        if (isShuffleEnabled) {
            currentShuffleIndex++;
            if (repeatState.equals("Repeat Current Song")) {
                currentShuffleIndex--;
                currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
            }
            if (currentShuffleIndex >= tracks.size()) {
                if (repeatState.equals("Repeat All")) {
                    currentShuffleIndex = 0;
                    currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                } else {
                    currentTrackIndex = 0;
                    isShuffleEnabled = false;
                    isPaused = true;
                    return true;
                }
            }
            updateCurrentTrackInfo();
        } else {
            currentTrackIndex++;
            if (repeatState.equals("Repeat Current Song")) {
                currentTrackIndex--;
            }
            if (currentTrackIndex >= tracks.size()) {
                if (repeatState.equals("Repeat All")) {
                    currentTrackIndex = 0;
                } else {
                    currentTrackIndex = 0;
                    isPaused = true;
                    return true;
                }
            }
            updateCurrentTrackInfo();
        }
        return false;
    }
    /**
     * Method for the previous song
     * @return
     */
    public void prev(final int timestamp) {
        lastTimestamp = timestamp;
        calculateTimeRemaining();
        if (timeRemaining == duration) {
            if (isShuffleEnabled) {
                currentShuffleIndex--;
                if (currentShuffleIndex < 0) {
                    currentShuffleIndex = 0;
                    currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                }
            } else {
                currentTrackIndex--;
                if (currentTrackIndex < 0) {
                    currentTrackIndex = 0;
                }
            }
        }
        initialTimestamp = timestamp;
        if (isPaused) {
            isPaused = false;
        }
        updateCurrentTrackInfo();
    }
}
