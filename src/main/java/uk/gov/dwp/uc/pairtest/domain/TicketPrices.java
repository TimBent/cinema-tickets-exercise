package uk.gov.dwp.uc.pairtest.domain;

public enum TicketPrices {
    INFANT(0),
    CHILD(10),
    ADULT(20);

    private final int price;

    TicketPrices(int price) {
        this.price = price;
    }

    public int getPrice() {
        return this.price;
    }
}