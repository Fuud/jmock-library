package example.qcon;

import org.jmock.ExpectationsExt;
import org.jmock.integration.junit3.MockObjectTestCase;

public class DJTests extends MockObjectTestCase {
    Playlist playlist = mock(Playlist.class);
    MediaControl mediaControl = mock(MediaControl.class);
    
    DJ dj = new DJ(playlist, mediaControl);
    
    private static final String LOCATION_A = "location-a";
    private static final String TRACK_A = "track-a";
    
    private static final String LOCATION_B = "location-b";
    private static final String TRACK_B = "track-b";
    
    @Override
    public void setUp() {
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            allowing (playlist).hasTrackFor(LOCATION_A); will(ExpectationsExt.returnValue(true));
            allowing (playlist).trackFor(LOCATION_A); will(ExpectationsExt.returnValue(TRACK_A));
            allowing (playlist).hasTrackFor(LOCATION_B); will(ExpectationsExt.returnValue(true));
            allowing (playlist).trackFor(LOCATION_B); will(ExpectationsExt.returnValue(TRACK_B));
            allowing (playlist).hasTrackFor(with(ExpectationsExt.any(String.class))); will(ExpectationsExt.returnValue(false));
        }});
    }
    
    public void testStartsPlayingTrackForCurrentLocationWhenLocationFirstDetected() {
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            oneOf (mediaControl).play(TRACK_A);
        }});
        
        dj.locationChangedTo(LOCATION_A);
    }
    
    public void testPlaysTrackForCurrentLocationWhenPreviousTrackFinishesIfLocationChangedWhileTrackWasPlaying() {
        startingIn(LOCATION_A);
        
        dj.locationChangedTo(LOCATION_B);
        
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            oneOf (mediaControl).play(TRACK_B);
        }});
        
        dj.mediaFinished();
    }
    
    public void testDoesNotPlayTrackAgainIfStillInTheSameLocation() {
        startingIn(LOCATION_A);
        
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            never (mediaControl).play(with(ExpectationsExt.any(String.class)));
        }});
        
        dj.mediaFinished();
    }
    
    public void testPlaysNewTrackAsSoonAsLocationChangesIfPreviousTrackFinishedWhileInSameLocation() {
        startingIn(LOCATION_A);
        dj.mediaFinished();
        
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            oneOf (mediaControl).play(TRACK_B);
        }});
        
        dj.locationChangedTo(LOCATION_B);
    }
    
    private void startingIn(String initialLocation) {
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            oneOf (mediaControl).play(with(ExpectationsExt.any(String.class)));
        }});
        
        dj.locationChangedTo(initialLocation);
    }
}
