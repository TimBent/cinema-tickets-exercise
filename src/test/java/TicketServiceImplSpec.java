import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TicketServiceImplSpec {

    private TicketService ticketService;
    private long expectedAccountId = 100000001L;
    @Mock
    private TicketPaymentService ticketPaymentService;
    @Mock
    private SeatReservationService seatReservationService;


    @BeforeEach
    public void setUp(){
        ticketPaymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl( ticketPaymentService, seatReservationService);
    }

    @Test
    @DisplayName("Test that the payment service is being called for 1 ADULT ticket.")
    public void testTicketRequestForOneAdultTicket() {
        // Test that Adult tickets can be purchased at a time
        TicketRequest[] oneAdultTicketRequest = new TicketRequest[] {
                new TicketRequest( TicketRequest.Type.ADULT, 1 )};

        ticketService.purchaseTickets( new TicketPurchaseRequest( expectedAccountId, oneAdultTicketRequest));

        verify(ticketPaymentService).makePayment(expectedAccountId, 20);
        verify(seatReservationService).reserveSeat(expectedAccountId, 1);

    }

    @Test
    @DisplayName("Test that the payment service is being called for multiple ticket types and the correct total is calculated.")
    public void testTicketRequestWithMultipleTickets() {
        TicketRequest[] oneAdultTicketRequest = new TicketRequest[] {
                new TicketRequest( TicketRequest.Type.ADULT, 5 ),
                new TicketRequest( TicketRequest.Type.CHILD, 6),
                new TicketRequest( TicketRequest.Type.INFANT, 2)};;

        ticketService.purchaseTickets(new TicketPurchaseRequest(expectedAccountId, oneAdultTicketRequest));

        verify(ticketPaymentService).makePayment(expectedAccountId, 160);
        verify(seatReservationService).reserveSeat(expectedAccountId, 11);

    }

    @Test
    @DisplayName("Test that more than 20 tickets cannot be purchased.")
    public void testTicketPurchaseLimit() {
        // Test that no more than 20 tickets can be purchased at a time
        TicketRequest[] expectedTicketRequestsOverLimit = new TicketRequest[] {
                new TicketRequest( TicketRequest.Type.ADULT, 30 )};

        TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsOverLimit);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ticketPurchaseRequest));
        String expectedMessage = "No more than 20 tickets can be purchased per transaction.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("Test that an INFANT ticket cannot be purchased without an ADULT ticket")
    public void testInfantTicketPurchaseWithoutAnAdultTicket() {
        TicketRequest[] expectedTicketRequestsWithoutAdult = new TicketRequest[]{
                new TicketRequest(TicketRequest.Type.INFANT, 1)
        };

        TicketPurchaseRequest ticketPurchaseRequestWithoutParent = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsWithoutAdult);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ticketPurchaseRequestWithoutParent));

        String expectedMessage = "CHILD/INFANT ticket(s) cannot be purchased without an ADULT ticket.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage, "Expected an exception message which states that CHILD/INFANT tickets and");
    }

    @Test
    @DisplayName("Test that an INFANT ticket can be purchased with ADULT ticket")
    public void testInfantTicketPurchaseWithParent() {
        TicketRequest[] expectedTicketRequestsWithParent = new TicketRequest[]{
                new TicketRequest(TicketRequest.Type.INFANT, 1),
                new TicketRequest(TicketRequest.Type.ADULT, 1)
        };

        ticketService.purchaseTickets(new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsWithParent));
        verify(ticketPaymentService).makePayment(expectedAccountId, 20);
        verify(seatReservationService).reserveSeat(expectedAccountId, 1);

    }

    @Test
    @DisplayName("Test that an CHILD ticket cannot be purchased without an ADULT ticket")
    public void testChildTicketPurchaseWithoutAdult() {
        TicketRequest[] expectedTicketRequestsWithoutAdult = new TicketRequest[]{
                new TicketRequest(TicketRequest.Type.CHILD, 1)
        };

        TicketPurchaseRequest ticketPurchaseRequestWithoutParent = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsWithoutAdult);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ticketPurchaseRequestWithoutParent));
        String expectedMessage = "CHILD/INFANT ticket(s) cannot be purchased without an ADULT ticket.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("Test that an CHILD ticket can be purchased with ADULT ticket.")
    public void testChildTicketPurchaseWithAdult() {
        TicketRequest[] expectedTicketRequestsChildWithAdult = new TicketRequest[]{
                new TicketRequest(TicketRequest.Type.CHILD, 1),
                new TicketRequest(TicketRequest.Type.ADULT, 1)
        };
        TicketPurchaseRequest ticketPurchaseRequestWithoutParent = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsChildWithAdult);

        ticketService.purchaseTickets(ticketPurchaseRequestWithoutParent);
        verify(ticketPaymentService).makePayment(expectedAccountId, 30);
        verify(seatReservationService).reserveSeat(expectedAccountId, 2);

    }

    @Test
    @DisplayName("Test that an purchase is invalid if there are no tickets in a payment request.")
    public void testInvalidPaymentWithEmptyTicketRequest() {
        TicketRequest[] expectedTicketRequestsChildWithAdult = new TicketRequest[]{};
        TicketPurchaseRequest emptyTicketPurchaseRequest = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsChildWithAdult);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(emptyTicketPurchaseRequest));
        String expectedMessage = "No tickets have been selected. Purchase unsuccessful.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

    }

    @Test
    @DisplayName("Test that a request is invalid if the accountId is 0L.")
    public void testInvalidAccountId() {
        expectedAccountId = 0L;
        TicketRequest[] expectedTicketRequestsChildWithAdult = new TicketRequest[]{
                new TicketRequest(TicketRequest.Type.ADULT, 1)
        };
        TicketPurchaseRequest invalidAccountIdPurchaseRequest = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsChildWithAdult);
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(invalidAccountIdPurchaseRequest));
        String expectedMessage = "The AccountID is invalid.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);

    }


}
