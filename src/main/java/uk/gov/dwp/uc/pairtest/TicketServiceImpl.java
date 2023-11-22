package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPrices;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.math.BigDecimal;
import java.util.Arrays;


public class TicketServiceImpl implements TicketService {

    TicketPaymentService ticketPaymentService;
    SeatReservationService seatReservationService;

    public TicketServiceImpl( TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
        validatePurchaseRequest(ticketPurchaseRequest);
        long accountId = ticketPurchaseRequest.getAccountId();

        ticketPaymentService.makePayment( accountId, getTotalPriceOfTickets(ticketPurchaseRequest));
        seatReservationService.reserveSeat( accountId, getTotalNumberOfTickets(ticketPurchaseRequest.getTicketTypeRequests()));
    }

    private void validatePurchaseRequest(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
        if( !isAnAdultTicketPresent(ticketPurchaseRequest)) {
            throw new InvalidPurchaseException("CHILD/INFANT ticket(s) cannot be purchased without an ADULT ticket.");
        }
        if( isThisAnInvalidNumberOfTickets(ticketPurchaseRequest)){
            throw new InvalidPurchaseException("No more than 20 tickets can be purchased per transaction.");
        }
    }

    private boolean isAnAdultTicketPresent( TicketPurchaseRequest ticketPurchaseRequest ) {
        return Arrays.stream(ticketPurchaseRequest.getTicketTypeRequests())
                .anyMatch(f -> f.getTicketType().equals(TicketRequest.Type.ADULT));
    }

    private boolean isThisAnInvalidNumberOfTickets( TicketPurchaseRequest ticketPurchaseRequest) {
        return getTotalNumberOfTickets( ticketPurchaseRequest.getTicketTypeRequests()) > 20;
    }

    private int getTotalPriceOfTickets( TicketPurchaseRequest ticketPurchaseRequest ) {
        return Arrays.stream( ticketPurchaseRequest.getTicketTypeRequests() )
                .mapToInt( ticketRequest ->  {
                    switch (ticketRequest.getTicketType()){
                        case ADULT :
                           return TicketPrices.getAdultPrice()*ticketRequest.getNoOfTickets();
                        case CHILD:
                            return TicketPrices.getChildPrice()*ticketRequest.getNoOfTickets();
                        case INFANT:
                            return TicketPrices.getInfantPrice()*ticketRequest.getNoOfTickets();
                        default:
                            throw new IllegalArgumentException("Unknown ticket type " + ticketRequest.getTicketType() );
                        }
                }).sum();
    }

    private int getTotalNumberOfTickets( TicketRequest[] ticketRequests ) {
        return Arrays.stream(ticketRequests)
                .mapToInt(TicketRequest::getNoOfTickets)
                .sum();
    }


}
