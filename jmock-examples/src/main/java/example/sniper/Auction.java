package example.sniper;

public interface Auction {
    public void bid(Money amount) throws AuctionException;
}
