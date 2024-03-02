package fileio.input;

import checker.CheckerConstants;

import java.util.ArrayList;

public class PodcastState {

    private String selectedPodcastName;
    private boolean isPaused;
    private String repeatState;
    private int lastTimestamp;
    private int initialTimestamp;
    private ArrayList<EpisodeInput> episodes;
    private int currentEpisodeIndex = 0;
    private int timeRemaining;

    public PodcastState(final String selectedPodcastName, final boolean isPaused,
                        final String repeatState, final int lastTimestamp,
                        final int initialTimestamp,
                        final ArrayList<EpisodeInput> episodeNames) {
        this.selectedPodcastName = selectedPodcastName;
        this.isPaused = isPaused;
        this.repeatState = repeatState;
        this.lastTimestamp = lastTimestamp;
        this.initialTimestamp = initialTimestamp;
        this.episodes = episodeNames;
        updateCurrentEpisodeInfo();
    }
    /**
     * Getter for the selected podcast name
     * @return
     */
    public String getSelectedPodcastName() {
        return selectedPodcastName;
    }
    /**
     * Getter for time remaining
     * @return
     */
    public int getTimeRemaining() {
        return timeRemaining;
    }
    /**
     * Getter for the repeat state
     * @return
     */
    public String getRepeatState() {
        return repeatState;
    }
    /**
     * Getter for paused state
     * @return
     */
    public boolean isPaused() {
        return isPaused;
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
     * Method to update the current episode info
     * @return
     */
    private void updateCurrentEpisodeInfo() {
        timeRemaining = episodes.get(currentEpisodeIndex).getDuration();
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
            while (elapsedTime >= timeRemaining) {
                elapsedTime -= timeRemaining;
                currentEpisodeIndex++;
                if (repeatState.equals("Repeat Once")) {
                    currentEpisodeIndex--;
                    repeatState = "No Repeat";
                }
                if (currentEpisodeIndex >= episodes.size()) {
                    if (repeatState.equals("No Repeat")) {
                        currentEpisodeIndex = 0;
                        return 0;
                    } else if (repeatState.equals("Repeat Infinite")) {
                        currentEpisodeIndex = 0;
                    }
                }
                updateCurrentEpisodeInfo();
            }
            return Math.max(0, timeRemaining - elapsedTime);
        }
    }
    /**
     * Setter for time remaining
     * @return
     */
    public void setTimeRemaining(final int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
    /**
     * Getter for the current episode name
     * @return
     */
    public String getSelectedEpisodeName() {
        return episodes.get(currentEpisodeIndex).getName();
    }
    /**
     * Method for the next episode
     * @return
     */
    public boolean next(final int timestamp) {
        initialTimestamp = timestamp;
        lastTimestamp = timestamp;
        currentEpisodeIndex++;
        if (repeatState.equals("Repeat Once")) {
            currentEpisodeIndex--;
            repeatState = "No Repeat";
        }
        if (currentEpisodeIndex >= episodes.size()) {
            if (repeatState.equals("No Repeat")) {
                currentEpisodeIndex = 0;
                return true;
            } else if (repeatState.equals("Repeat Infinite")) {
                currentEpisodeIndex = 0;
            }
        }
        updateCurrentEpisodeInfo();
        return false;
    }
    /**
     * Method for the previous episode
     * @return
     */
    public void prev(final int timestamp) {
        initialTimestamp = timestamp;
        lastTimestamp = timestamp;
        if (isPaused) {
            isPaused = false;
        }
        updateCurrentEpisodeInfo();
    }
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
}
