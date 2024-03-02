package fileio.input;

public class PlayerState {
    /**
     * The name of the song that is currently selected
     */
    private String selectedSongName;
    /**
     * The shuffle state of the player
     */
    private boolean isShuffleEnabled;
    /**
     * The pause state of the player
     */
    private boolean isPaused;
    /**
     * The repeat state of the player
     */
    private String repeatState;
    /**
     * The last timestamp of the player
     */
    private int lastTimestamp;
    /**
     * The duration of the song
     */
    private int duration;
    /**
     * The time remaining of the song
     */
    private int timeRemaining;
    /**
     * The initial timestamp of the player
     */
    private int initialTimestamp;

    public PlayerState(final String selectedSongName, final boolean isShuffleEnabled, final
    boolean isPaused, final String repeatState, final int lastTimestamp, final int duration,
                       final int timeRemaining, final int initialTimestamp) {
        this.selectedSongName = selectedSongName;
        this.isShuffleEnabled = isShuffleEnabled;
        this.isPaused = isPaused;
        this.repeatState = repeatState;
        this.lastTimestamp = lastTimestamp;
        this.duration = duration;
        this.timeRemaining = timeRemaining;
        this.initialTimestamp = initialTimestamp;
    }

    /**
     * Constructor for the player state
     * @return
     */
    public String getSelectedSongName() {
        return selectedSongName;
    }

    /**
     * Getter for the last timestamp
     * @return
     */
    public int getTimeRemaining() {
        return timeRemaining;
    }
    /**
     * Getter for the last timestamp
     * @return
     */
    public String getRepeatState() {
        return repeatState;
    }
    /**
     * Getter for the last timestamp
     * @return
     */
    public boolean isPaused() {
        return isPaused;
    }
    /**
     * Getter for the last timestamp
     * @return
     */
    public void setSelectedSongName(final String selectedSongName) {
        this.selectedSongName = selectedSongName;
    }
    /**
     * Getter for the last timestamp
     * @return
     */
    public void setPaused(final boolean paused) {
        isPaused = paused;
    }
    /**
     * Getter for the last timestamp
     * @return
     */
    public void setRepeatState(final String repeatState) {
        this.repeatState = repeatState;
    }
    /**
     * Getter for the last timestamp
     * @return
     */
    public void setLastTimestamp(final int lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }
    /**
     * Getter for the last timestamp
     * @return
     */
    public void setTimeRemaining(final int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
    /**
     * Getter for the last timestamp
     * @return
     */
    public void setInitialTimestamp(final int initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }
    /**
     * Method that updates the current song duration
     * @return
     */
    private void updateCurrentSongDuration() {
        timeRemaining = duration;
    }
    /**
     * Method that updates the time remaining
     * @return
     */
    public int calculateTimeRemaining() {
        if (isPaused) {
            return timeRemaining;
        } else {
            int elapsedTime = lastTimestamp - initialTimestamp;
            while (elapsedTime > timeRemaining) {
                if (repeatState.equals("No Repeat")) {
                    return 0;
                } else if (repeatState.equals("Repeat Infinite")) {
                    elapsedTime -= timeRemaining;
                } else if (repeatState.equals("Repeat Once")) {
                    elapsedTime -= timeRemaining;
                    repeatState = "No Repeat";
                }
                updateCurrentSongDuration();
            }
            return Math.max(0, timeRemaining - elapsedTime);
        }
    }
    /**
     * Method for the next song
     * @return
     */
    public boolean next(final int timestamp) {
        initialTimestamp = timestamp;
        lastTimestamp = timestamp;
        if (repeatState.equals("No Repeat")) {
            return true;
        } else if (repeatState.equals("Repeat Once")) {
            repeatState = "No Repeat";
        }
        updateCurrentSongDuration();
        return false;
    }
    /**
     * Method for the previous song
     * @return
     */
    public void prev(final int timestamp) {
        initialTimestamp = timestamp;
        lastTimestamp = timestamp;
        if (isPaused) {
            isPaused = false;
        }
        updateCurrentSongDuration();
    }
}
