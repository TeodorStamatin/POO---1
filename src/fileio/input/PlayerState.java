package fileio.input;


import java.time.Duration;

public class PlayerState {
    private String selectedSongName;
    private boolean isShuffleEnabled;
    private boolean isPaused;
    private String repeatState;

    public int getLastTimestamp() {
        return lastTimestamp;
    }

    private int lastTimestamp;
    private int duration;
    private int timeRemaining;

    public int getInitialTimestamp() {
        return initialTimestamp;
    }

    private int initialTimestamp;

    // Constructor
    public PlayerState(String selectedSongName, boolean isShuffleEnabled, boolean isPaused, String repeatState, int lastTimestamp, int duration, int timeRemaining, int initialTimestamp) {
        this.selectedSongName = selectedSongName;
        this.isShuffleEnabled = isShuffleEnabled;
        this.isPaused = isPaused;
        this.repeatState = repeatState;
        this.lastTimestamp = lastTimestamp;
        this.duration = duration;
        this.timeRemaining = timeRemaining;
        this.initialTimestamp = initialTimestamp;
    }
    public String getSelectedSongName() {
        return selectedSongName;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }
    public String getRepeatState() {
        return repeatState;
    }
    public boolean isShuffleEnabled() {
        return isShuffleEnabled;
    }

    public boolean isPaused() {
        return isPaused;
    }

    // Setters
    public void setSelectedSongName(String selectedSongName) {
        this.selectedSongName = selectedSongName;
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

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public void setInitialTimestamp(int initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }
    private void updateCurrentSongDuration() {
        timeRemaining = duration;
    }
    public int calculateTimeRemaining() {
        if (isPaused) {
            return timeRemaining;
        } else {
            int elapsedTime = lastTimestamp - initialTimestamp;
            while(elapsedTime > timeRemaining) {
                if(repeatState.equals("No Repeat")) {
                    return 0;
                } else if(repeatState.equals("Repeat Infinite")) {
                    elapsedTime -= timeRemaining;
                } else if(repeatState.equals("Repeat Once")) {
                    elapsedTime -= timeRemaining;
                    repeatState = "No Repeat";
                }
                updateCurrentSongDuration();
            }
            return Math.max(0, timeRemaining - elapsedTime);
        }
    }
}