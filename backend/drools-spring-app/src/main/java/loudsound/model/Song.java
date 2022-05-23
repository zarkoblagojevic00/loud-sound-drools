package loudsound.model;

import loudsound.controllers.dtos.NewSongDTO;

import java.util.Date;
import java.util.UUID;

public class Song {
    public enum Status {
        POPULAR, OK, BORING
    }

    public enum Genre {
        ROCK, METAL, JAZZ, BLUES, RAP, FOLK
    }

    private final String id;
    private final String artist;
    private final String title;
    private final Date created;
    private final long length;
    private long likesNumber;
    private long timesListenedNumber;
    private long timesSkippedNumber;
    private Status status;
    private final Genre genre;

    public Song(NewSongDTO newSong) {
        this.id = UUID.randomUUID().toString();
        this.artist = newSong.getArtist();
        this.title = newSong.getTitle();
        this.length = newSong.getDuration();
        this.genre = newSong.getGenre();

        this.created = new Date();
        this.status = Status.OK;
    }

    public String getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public Date getCreated() {
        return created;
    }

    public long getLength() {
        return length;
    }

    public long getLikesNumber() {
        return likesNumber;
    }

    public void like() {
        this.likesNumber++;
    }

    public long getTimesListenedNumber() {
        return timesListenedNumber;
    }

    public void listen() {
        this.timesListenedNumber++;
    }

    public long getTimesSkippedNumber() {
        return timesSkippedNumber;
    }

    public void skip() {
        this.timesSkippedNumber++;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Genre getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id='" + id + '\'' +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", created=" + created +
                ", duration=" + length +
                ", likesNumber=" + likesNumber +
                ", timesListenedNumber=" + timesListenedNumber +
                ", timesSkippedNumber=" + timesSkippedNumber +
                ", status=" + status +
                ", genre=" + genre +
                '}';
    }
}
