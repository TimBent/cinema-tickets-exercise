package uk.gov.dwp.uc.pairtest.domain;

/**
 * Should be an Immutable Object
 */
final public class TicketRequest {

    private int noOfTickets;
    private Type type;

    public TicketRequest(Type type, int noOfTickets) {
        this.type = type;
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public enum Type {
        ADULT, CHILD , INFANT
    }

}
