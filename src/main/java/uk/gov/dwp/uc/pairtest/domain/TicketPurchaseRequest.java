package uk.gov.dwp.uc.pairtest.domain;

/**
 * Should be an Immutable Object
 */
final public class TicketPurchaseRequest {

    private long accountId;
    private TicketRequest[] ticketRequests;

    public TicketPurchaseRequest(long accountId, TicketRequest[] ticketRequests) {
        this.accountId = accountId;
        this.ticketRequests = ticketRequests;
    }

    public long getAccountId() {
        return accountId;
    }

    public TicketRequest[] getTicketTypeRequests() {
        return ticketRequests;
    }
}
