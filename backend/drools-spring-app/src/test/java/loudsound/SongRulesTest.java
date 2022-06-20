package loudsound;

import loudsound.events.song.SongEnteredTopNEvent;
import loudsound.events.song.SongLikedEvent;
import loudsound.events.song.SongListeningEndedEvent;
import loudsound.events.song.SongListeningStartedEvent;
import loudsound.model.Song;
import loudsound.setup.SongRulesKieStateInitializer;
import loudsound.setup.TestKieSessionFactory;
import loudsound.stateinitializer.KieStateInitializer;
import org.drools.core.ClassObjectFilter;
import org.drools.core.time.SessionPseudoClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SongRulesTest {
    private final static Logger logger = LoggerFactory.getLogger(SongRulesTest.class);
    private KieSession kieSession;
    private SessionPseudoClock kieClock;

    @BeforeEach
    public void initEach() {
        System.out.println("BeforeEach initEach() method called");
        kieSession = TestKieSessionFactory.getSession(logger);
        kieClock = kieSession.getSessionClock();
    }

    @DisplayName("Song is liked once")
    @Test
    public void songIsLikedOnce() {
        //  arrange
        KieStateInitializer initializer = SongRulesKieStateInitializer.getOkSongToBeDeclaredBoringInitializer();
        initializer.initializeState(kieSession);
        Song song = getSongFromSession(initializer);
        long likesNumber = song.getLikesNumber();

        //  act
        kieSession.insert(new SongLikedEvent("T", new Date(kieClock.getCurrentTime()), SongRulesKieStateInitializer.SONG_ID));
        kieSession.fireAllRules();

        //  assert
        assertEquals(likesNumber + 1, song.getLikesNumber());
    }

    @DisplayName("Song is listened once")
    @Test
    public void songIsListenedOnce() {
        //  arrange
        KieStateInitializer initializer = SongRulesKieStateInitializer.getOkSongToBeDeclaredBoringInitializer();
        initializer.initializeState(kieSession);
        Song song = getSongFromSession(initializer);
        long timesListened = song.getTimesListenedNumber();

        //  act
        insertListeningEventsWithTimeApart(10);

        //  assert
        assertEquals(timesListened + 1, song.getTimesListenedNumber());
    }

    @DisplayName("Song is skipped once")
    @Test
    public void songIsSkippedOnce() {
        //  arrange
        KieStateInitializer initializer = SongRulesKieStateInitializer.getOkSongToBeDeclaredBoringInitializer();
        initializer.initializeState(kieSession);
        Song song = getSongFromSession(initializer);
        long timesSkipped = song.getTimesSkippedNumber();

        //  act
        insertListeningEventsWithTimeApart(2);

        //  assert
        assertEquals(timesSkipped + 1, song.getTimesSkippedNumber());
    }

    @DisplayName("Song is declared BORING based on times listened to times skipped ratio")
    @Test
    public void songIsDeclaredBoring() {
        //  arrange
        KieStateInitializer initializer = SongRulesKieStateInitializer.getOkSongToBeDeclaredBoringInitializer();
        initializer.initializeState(kieSession);

        //  act
        insertListeningEventsWithTimeApart(3);

        //  assert
        assertSongStatus(initializer, Song.Status.BORING);
    }

    @DisplayName("Song is redeclared OK based on times listened and skipped in past 48h")
    @Test
    public void songIsRedeclaredOk() {
        //  arrange
        KieStateInitializer initializer = SongRulesKieStateInitializer.getBoringSongToBeRedeclaredOkInitializer();
        initializer.initializeState(kieSession);

        //  act
        for (int i= 0; i < 9; i++) {
            insertListeningEventsWithTimeApart(10);
        }

        //  assert
        assertSongStatus(initializer, Song.Status.OK);
    }

    @DisplayName("Song reached top N when there are more than N songs")
    @Test
    public void songReachedTopN() {
        KieStateInitializer initializer = SongRulesKieStateInitializer.getSongsWithOneSongAboutToReachTopN();
        initializer.initializeState(kieSession);

        insertListeningEventsWithTimeApart(10);
        assertOvertakingWasCorrect();
    }

    @DisplayName("Song is declared POPULAR")
    @Test
    public void songIsDeclaredPopular() {
        //  arrange
        KieStateInitializer initializer = SongRulesKieStateInitializer.getPopularSongInitializer();
        initializer.initializeState(kieSession);

        //  act
//        kieSession.insert(new SongLikedEvent("T", SongRulesKieStateInitializer.SONG_ID));
        kieSession.fireAllRules();

        //  assert
        assertSongStatus(initializer, Song.Status.POPULAR);
    }

    @DisplayName("Song is revoked POPULAR status after some time")
    @Test
    public void songIsRevokedPopularStatus() {
        //  arrange
        KieStateInitializer initializer = SongRulesKieStateInitializer.getPopularSongInitializer();
        initializer.initializeState(kieSession);

        //  act
        kieClock.advanceTime(25, TimeUnit.HOURS);
        kieSession.fireAllRules();

        //  assert
        assertSongStatus(initializer, Song.Status.OK);
    }


    private void assertOvertakingWasCorrect() {
        Collection<SongEnteredTopNEvent> events = (Collection<SongEnteredTopNEvent>) kieSession.getObjects(new ClassObjectFilter(SongEnteredTopNEvent.class));
        assertEquals(4, events.size());

        assertSongAboutToReachTopNReachedTopN(events);

        assertCorrectSongWasOvertaken(events);
    }

    private void assertSongAboutToReachTopNReachedTopN(Collection<SongEnteredTopNEvent> events) {
        assertTrue(events.stream().anyMatch(e -> e.getSongId().equals(SongRulesKieStateInitializer.SONG_ID) && !e.isRevoked()));
    }

    private void assertCorrectSongWasOvertaken(Collection<SongEnteredTopNEvent> events) {
        String revokedSongId = events.stream()
                .filter(SongEnteredTopNEvent::isRevoked)
                .findFirst().get().getSongId();

        Collection<Song> songs = getSongsFromSession();
        Song revokedSong = songs.stream()
                .filter(s -> s.getId().equals(revokedSongId))
                .findFirst().get();

        assertNoSongsWithTimesListenedLessThanRevokedSong(songs, revokedSong);
    }

    private void assertNoSongsWithTimesListenedLessThanRevokedSong(Collection<Song> songs, Song revokedSong) {
        assertFalse(songs.stream().anyMatch(s-> s.getTimesListenedNumber() < revokedSong.getTimesListenedNumber()));
    }

    private Collection<Song> getSongsFromSession() {
        return (Collection<Song>) kieSession.getObjects(new ClassObjectFilter(Song.class));
    }

    private void insertListeningEventsWithTimeApart(int timeInSeconds) {
        kieSession.insert(new SongListeningStartedEvent("T", new Date(kieClock.getCurrentTime()), SongRulesKieStateInitializer.SONG_ID));
        kieClock.advanceTime(timeInSeconds, TimeUnit.SECONDS);
        kieSession.insert(new SongListeningEndedEvent("T", new Date(kieClock.getCurrentTime()), SongRulesKieStateInitializer.SONG_ID));
        kieSession.fireAllRules();
    }

    private void assertSongStatus(KieStateInitializer initializer, Song.Status status) {
        Song song = getSongFromSession(initializer);
        assertEquals(status, song.getStatus());
    }

    private Song getSongFromSession(KieStateInitializer initializer) {
        FactHandle handle = initializer.getSongFactHandle(
                SongRulesKieStateInitializer.ARTIST,
                SongRulesKieStateInitializer.SONG_ID);
        Song song = (Song) kieSession.getObject(handle);
        return song;
    }
}
