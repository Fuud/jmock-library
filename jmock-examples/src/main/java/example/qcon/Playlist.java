package example.qcon;

public interface Playlist {
    boolean hasTrackFor(String location);
    String trackFor(String location);
}
