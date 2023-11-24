package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

import static uk.gov.dwp.uc.pairtest.domain.TicketPrices.*;


public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    private final Integer MAX_NUMBER_OF_TICKETS = 20;
    private final Integer MIN_NUMBER_OF_TICKETS = 1;
    private final long invalidAccountId = 0L;

    public TicketServiceImpl( TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
        validatePurchaseRequest(ticketPurchaseRequest);
        long accountId = ticketPurchaseRequest.getAccountId();

        ticketPaymentService.makePayment( accountId, getTotalPriceOfTickets(ticketPurchaseRequest));
        seatReservationService.reserveSeat( accountId, getTotalNumberOfSeats(ticketPurchaseRequest.getTicketTypeRequests()));
    }

    private void validatePurchaseRequest(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
        if( invalidNumberOfTickets(ticketPurchaseRequest.getTicketTypeRequests()) ){
            throw new InvalidPurchaseException("No tickets have been selected. Purchase unsuccessful.");
        }
        if( !isAnAdultTicketPresent(ticketPurchaseRequest)) {
            throw new InvalidPurchaseException("CHILD/INFANT ticket(s) cannot be purchased without an ADULT ticket.");
        }
        if( isThisAnInvalidNumberOfTickets(ticketPurchaseRequest)){
            throw new InvalidPurchaseException("No more than 20 tickets can be purchased per transaction.");
        }
        if( isAccountIdValid(ticketPurchaseRequest.getAccountId())){
            throw new InvalidPurchaseException("The AccountID is invalid.");
        }
    }

    private boolean invalidNumberOfTickets( TicketRequest[] ticketRequests ){
        return getTotalNumberOfTickets(ticketRequests) < MIN_NUMBER_OF_TICKETS;
    }

    private boolean isAccountIdValid( long accountId ){
        return accountId <= invalidAccountId;
    }

    private boolean isAnAdultTicketPresent( TicketPurchaseRequest ticketPurchaseRequest ) {
        return Arrays.stream(ticketPurchaseRequest.getTicketTypeRequests())
                .anyMatch( ticketRequest -> ticketRequest.getTicketType().equals(TicketRequest.Type.ADULT));
    }

    private boolean isThisAnInvalidNumberOfTickets( TicketPurchaseRequest ticketPurchaseRequest) {
        return getTotalNumberOfTickets( ticketPurchaseRequest.getTicketTypeRequests()) > MAX_NUMBER_OF_TICKETS;
    }

    private int getTotalPriceOfTickets( TicketPurchaseRequest ticketPurchaseRequest ) {
        return Arrays.stream( ticketPurchaseRequest.getTicketTypeRequests() )
                .mapToInt( ticketRequest ->
                    switch (ticketRequest.getTicketType()) {
                        case ADULT -> ADULT.getPrice() * ticketRequest.getNoOfTickets();
                        case CHILD -> CHILD.getPrice() * ticketRequest.getNoOfTickets();
                        case INFANT -> INFANT.getPrice() * ticketRequest.getNoOfTickets();
                }).sum();
    }

    private int getTotalNumberOfTickets( TicketRequest[] ticketRequests ) {
        return Arrays.stream(ticketRequests)
                .mapToInt(TicketRequest::getNoOfTickets)
                .sum();
    }

    private int getTotalNumberOfSeats( TicketRequest[] ticketRequests ) {
        return Arrays.stream(ticketRequests)
                .mapToInt(ticketRequest ->
                    switch (ticketRequest.getTicketType()) {
                        case ADULT, CHILD -> ticketRequest.getNoOfTickets();
                        case INFANT -> 0;
                }).sum();
    }


}
