package example.sniper;

import org.jmock.ExpectationsExt;
import org.jmock.integration.junit3.MockObjectTestCase;

public class AuctionSniperTests extends MockObjectTestCase {
    Money increment = new Money(2);
    Money maximumBid = new Money(20);
    Money beatableBid = new Money(10);
    Money unbeatableBid = maximumBid.add(new Money(1));

    Auction auction = mock(Auction.class);
    AuctionSniperListener listener = mock(AuctionSniperListener.class, "listener");

    AuctionSniper sniper = new AuctionSniper(auction, increment, maximumBid, listener);

    public void testTriesToBeatTheLatestHighestBid() throws Exception {
        final Money expectedBid = beatableBid.add(increment);

        checking(new ExpectationsExt() {protected void expect() throws Exception{
            oneOf (auction).bid(expectedBid);
        }});

        sniper.bidAccepted(auction, beatableBid);
    }

    public void testWillNotBidPriceGreaterThanMaximum() throws Exception {
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            ignoring (listener);
            never (auction).bid(with(ExpectationsExt.any(Money.class)));
        }});
        sniper.bidAccepted(auction, unbeatableBid);
    }

    public void testWillLimitBidToMaximum() throws Throwable {
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            exactly(1).of (auction).bid(maximumBid);
        }});

        sniper.bidAccepted(auction, maximumBid.subtract(new Money(1)));
    }

    public void testWillNotBidWhenToldAboutBidsOnOtherItems() throws Throwable {
        final Auction otherLot = mock(Auction.class, "otherLot");

        checking(new ExpectationsExt() {protected void expect() throws Exception{
           never (otherLot).bid(new Money(10));
        }});

        sniper.bidAccepted(otherLot, beatableBid);
    }

    public void testWillAnnounceItHasFinishedIfPriceGoesAboveMaximum() {
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            exactly(1).of (listener).sniperFinished(sniper);
        }});

        sniper.bidAccepted(auction, unbeatableBid);
    }

    public void testCatchesExceptionsAndReportsThemToErrorListener() throws Exception {
        final AuctionException exception = new AuctionException("test");

        checking(new ExpectationsExt() {protected void expect() throws Exception{
            allowing (auction).bid(with(ExpectationsExt.any(Money.class)));
                will(ExpectationsExt.throwException(exception));
            exactly(1).of (listener).sniperBidFailed(sniper, exception);
        }});

        sniper.bidAccepted(auction, beatableBid);
    }
}
