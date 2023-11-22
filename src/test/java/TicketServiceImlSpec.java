import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TicketServiceImlSpec {

    TicketService ticketService;
    TicketPaymentService ticketPaymentService;
    SeatReservationService seatReservationService;

    @BeforeEach
    public void setUp(){
        ticketPaymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl( ticketPaymentService, seatReservationService);
    }

    @Test
    @DisplayName("Test that the payment service is being called for 1 ADULT ticket.")
    public void testTicketPaymentService() {
        // Test that Adult tickets can be purchased at a time
        TicketService ticketService = new TicketServiceImpl( ticketPaymentService, seatReservationService);

        long expectedAccountId = 1L;

        TicketRequest[] oneAdultTicketRequest = new TicketRequest[] { new TicketRequest( TicketRequest.Type.ADULT, 1 )};

        TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest( expectedAccountId, oneAdultTicketRequest);

        ticketService.purchaseTickets(ticketPurchaseRequest);

        verify(ticketPaymentService).makePayment(expectedAccountId, 20);

    }

    @Test
    @DisplayName("Test that more than 20 tickets cannot be purchased.")
    public void testTicketPurchaseLimit() {
        // Test that no more than 20 tickets can be purchased at a time
        TicketRequest[] list = new TicketRequest[] { new TicketRequest( TicketRequest.Type.ADULT, 30 )};

        TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest( 100000001, list);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ticketPurchaseRequest));

        String expectedMessage = "No more than 20 tickets can be purchased per transaction.";
        String actualMessage = exception.getMessage();

        assertTrue(expectedMessage.equals(actualMessage));
    }

    @Test
    @DisplayName("Test that an INFANT ticket cannot be purchased without an ADULT ticket")
    public void testInfantTicketPurchaseWithoutAnAdultTicket() {
        // Test that an infant ticket cannot be purchased without an adult ticket
        long expectedAccountId = 1L;

        TicketRequest[] expectedTicketRequestsWithoutParent = new TicketRequest[]{
                new TicketRequest(TicketRequest.Type.INFANT, 1)
        };

        TicketPurchaseRequest ticketPurchaseRequestWithoutParent = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsWithoutParent);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ticketPurchaseRequestWithoutParent));

        String expectedMessage = "CHILD/INFANT ticket(s) cannot be purchased without an ADULT ticket.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage, "Expected an exception message which states that CHILD/INFANT tickets and");
    }

    @Test
    @DisplayName("Test that an INFANT ticket can be purchased with ADULT ticket")
    public void testInfantTicketPurchaseWithParent() {
        // Test that an infant ticket cannot be purchased without an adult ticket
        long expectedAccountId = 6000L;

        TicketRequest[] expectedTicketRequestsWithParent = new TicketRequest[]{
                new TicketRequest(TicketRequest.Type.INFANT, 1),
                new TicketRequest(TicketRequest.Type.ADULT, 1)
        };

        TicketPurchaseRequest infantTicketPurchaseRequestWithParent = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsWithParent);
        ticketService.purchaseTickets(infantTicketPurchaseRequestWithParent);

        verify(ticketPaymentService).makePayment(6000L, 20);

    }

    @Test
    @DisplayName("Test that an CHILD ticket cannot be purchased without an ADULT ticket")
    public void testChildTicketPurchaseWithoutParent() {
        // Test that a child ticket cannot be purchased without an adult ticket
        long expectedAccountId = 1L;
        TicketRequest[] expectedTicketRequestsWithoutParent = new TicketRequest[]{
                new TicketRequest(TicketRequest.Type.CHILD, 1)
        };

        TicketPurchaseRequest ticketPurchaseRequestWithoutParent = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsWithoutParent);

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ticketPurchaseRequestWithoutParent));

        String expectedMessage = "CHILD/INFANT ticket(s) cannot be purchased without an ADULT ticket.";
        String actualMessage = exception.getMessage();

        assertTrue(expectedMessage.equals(actualMessage));
    }

    @Test
    @DisplayName("Test that an CHILD ticket can be purchased with ADULT ticket")
    public void testChildTicketPurchaseWithParent() {
        // Test that a child ticket cannot be purchased without an adult ticket
        long expectedAccountId = 5000L;

        TicketRequest[] expectedTicketRequestsWithParent = new TicketRequest[]{
                new TicketRequest(TicketRequest.Type.CHILD, 1),
                new TicketRequest(TicketRequest.Type.ADULT, 1)
        };

        TicketPurchaseRequest ticketPurchaseRequestWithoutParent = new TicketPurchaseRequest( expectedAccountId, expectedTicketRequestsWithParent);

        ticketService.purchaseTickets(ticketPurchaseRequestWithoutParent);

        verify(ticketPaymentService).makePayment(5000L, 30);

    }


}
