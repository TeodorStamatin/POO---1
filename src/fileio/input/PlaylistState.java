package fileio.input;

import java.util.List;

public class PlaylistState {
    public String getSelectedPlaylistName() {
        return selectedPlaylistName;
    }

    private String selectedPlaylistName;

    public boolean isShuffleEnabled() {
        return isShuffleEnabled;
    }

    private boolean isShuffleEnabled;

    public boolean isPaused() {
        return isPaused;
    }

    private boolean isPaused;

    public String getRepeatState() {
        return repeatState;
    }

    private String repeatState;
    private int lastTimestamp;
    private int initialTimestamp;

    private List<SongInput> tracks;
    private int currentTrackIndex = 0;
    private int timeRemaining;
    private int duration;
    int savedTrackIndex;
    int savedTimeRemaining;
    List<Integer> shuffleOrder;
    int currentShuffleIndex = 0;

    // Constructor
    public PlaylistState(String selectedPlaylistName, boolean isShuffleEnabled, boolean isPaused, String repeatState, int lastTimestamp, int initialTimestamp, List<SongInput> tracks) {
        this.selectedPlaylistName = selectedPlaylistName;
        this.isShuffleEnabled = isShuffleEnabled;
        this.isPaused = isPaused;
        this.repeatState = repeatState;
        this.lastTimestamp = lastTimestamp;
        this.initialTimestamp = initialTimestamp;
        this.tracks = tracks;
        updateCurrentTrackInfo();
    }
    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }
    public void setCurrentTrackIndex(int currentTrackIndex) {
        this.currentTrackIndex = currentTrackIndex;
    }

    public List<SongInput> gettracks() {
        return tracks;
    }

    // Setters
    public void setSelectedPlaylistName(String selectedPlaylistName) {
        this.selectedPlaylistName = selectedPlaylistName;
    }

    public void setShuffleEnabled(boolean shuffleEnabled) {
        isShuffleEnabled = shuffleEnabled;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public void setRepeatState(String repeatState) {
        this.repeatState = repeatState;
    }

    public void setLastTimestamp(int lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public void setInitialTimestamp(int initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }

    public void updateCurrentTrackInfo() {
        duration = tracks.get(currentTrackIndex).getDuration();
        timeRemaining = tracks.get(currentTrackIndex).getDuration();
    }

    public int calculateTimeRemaining() {
        if (isPaused) {
            return timeRemaining;
        } else {
            int elapsedTime = lastTimestamp - initialTimestamp;
            // check if you need to go to the next episode
            if(isShuffleEnabled) {
                // TO DO check if current track index == saved track index. if 1, then finish the current song
                // if 0, go to shuffle tracks.
                while(elapsedTime >= timeRemaining) {
                    elapsedTime -= timeRemaining;
                    currentShuffleIndex++;
                    currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                    if (repeatState.equals("Repeat Current Song")) {
                        currentShuffleIndex--;
                        currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                    }
                    if (currentShuffleIndex >= tracks.size()) {
                        if(repeatState.equals("Repeat All")) {
                            currentShuffleIndex = 0;
                            currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                        } else {
                            currentTrackIndex = 0;
                            return 0;
                        }
                    }
                    updateCurrentTrackInfo();
                }
            } else {
                // check if 
                while(elapsedTime >= timeRemaining) {
                    elapsedTime -= timeRemaining;
                    currentTrackIndex++;
                    if (repeatState.equals("Repeat Current Song")) {
                        currentTrackIndex--;
                    }
                    if (currentTrackIndex >= tracks.size()) {
                        if(repeatState.equals("Repeat All")) {
                            currentTrackIndex = 0;
                        } else {
                            currentTrackIndex = 0;
                            return 0;
                        }
                    }
                    updateCurrentTrackInfo();
                }
            }
            return Math.max(0, timeRemaining - elapsedTime);
        }
    }
    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
    public int getTimeRemaining() {
        return timeRemaining;
    }

    public String getSelectedTrackName() {
        return tracks.get(currentTrackIndex).getName();
    }
    public void shuffleOn(List<Integer> shuffleOrder) {
        // TO DO implement logic for calculate time remaining above
        // saved time remaining would not be needed anymore
        savedTrackIndex = currentTrackIndex;
        savedTimeRemaining = timeRemaining;
        this.shuffleOrder = shuffleOrder;
        currentShuffleIndex = 0;
        currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
        List<SongInput> temp = gettracks();
        timeRemaining = temp.get(currentTrackIndex).getDuration();
    }
    public void shuffleOff() {
        // TO DO impelment logic so that the last shuffled song plays until the end
        // it should be current track index = saved track index, and implement the logic for calculate time remaining above
        currentTrackIndex = savedTrackIndex;
        timeRemaining = savedTimeRemaining;
    }
}
