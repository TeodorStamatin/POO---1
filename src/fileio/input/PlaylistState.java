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
    Boolean waitSong = false;
    private String owner;

    // Constructor
    public PlaylistState(String selectedPlaylistName, boolean isShuffleEnabled, boolean isPaused, String repeatState, int lastTimestamp, int initialTimestamp, List<SongInput> tracks, String owner) {
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
    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }
    public void setCurrentTrackIndex(int currentTrackIndex) {
        this.currentTrackIndex = currentTrackIndex;
    }

    public List<SongInput> gettracks() {
        return tracks;
    }

    public String getOwner() {
        return owner;
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

            if(isShuffleEnabled) {
                while(elapsedTime >= timeRemaining) {
                    if(waitSong) {
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
                            if(repeatState.equals("Repeat All")) {
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
                while(elapsedTime >= timeRemaining) {
                    if(waitSong) {
                        waitSong = false;
                        elapsedTime -= timeRemaining;
                        currentTrackIndex++;
                        if(currentTrackIndex >= tracks.size()) {
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

        if(!isShuffleEnabled) {
            savedTrackIndex = currentTrackIndex + 1;
        }

        this.shuffleOrder = shuffleOrder;
        waitSong = true;
    }
    public void shuffleOff() {

        waitSong = true;
        currentShuffleIndex = currentTrackIndex;
        currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
    }
    public void reset() {
        currentTrackIndex = 0;
        isShuffleEnabled = false;
        updateCurrentTrackInfo();
    }
    public boolean next(int timestamp) {
        lastTimestamp = timestamp;
        initialTimestamp = timestamp;
        if(isShuffleEnabled) {
            currentShuffleIndex++;
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
                if(repeatState.equals("Repeat All")) {
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

    public void prev(int timestamp) {
        lastTimestamp = timestamp;
        calculateTimeRemaining();
        if(timeRemaining == duration) {
            if(isShuffleEnabled) {
                currentShuffleIndex--;
                if(currentShuffleIndex < 0) {
                    currentShuffleIndex = 0;
                    currentTrackIndex = shuffleOrder.get(currentShuffleIndex);
                }
            } else {
                currentTrackIndex--;
                if(currentTrackIndex < 0) {
                    currentTrackIndex = 0;
                }
            }
        }
        initialTimestamp = timestamp;
        if(isPaused == true) {
            isPaused = false;
        }
        updateCurrentTrackInfo();
    }
}
