package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TicketServiceImplTest {

    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketPaymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    @DisplayName("1 adult: charged £25, 1 seat reserved")
    void oneAdult() {
        ticketService.purchaseTickets(1L,
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        );

        verify(ticketPaymentService).makePayment(1L, 25);
        verify(seatReservationService).reserveSeat(1L, 1);
    }

    @Test
    @DisplayName("2 adults: charged £50, 2 seats reserved")
    void twoAdults() {
        ticketService.purchaseTickets(1L,
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2)
        );

        verify(ticketPaymentService).makePayment(1L, 50);
        verify(seatReservationService).reserveSeat(1L, 2);
    }

    @Test
    @DisplayName("2 adults + 3 children: charged £95 (2 x £25 + 3 x £15), 5 seats reserved")
    void twoAdultsThreeChildren() {
        ticketService.purchaseTickets(1L,
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT,   2),
            new TicketTypeRequest(TicketTypeRequest.Type.CHILD,   3)
        );

        verify(ticketPaymentService).makePayment(1L, 95);
        verify(seatReservationService).reserveSeat(1L, 5);
    }

    @Test
    @DisplayName("2 adults + 3 children + 2 infants: charged £95 (2 x £25 + 3 x £15), 5 seats (infants free, no seat)")
    void twoAdultsThreeChildrenTwoInfants() {
        ticketService.purchaseTickets(1L,
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT,  2),
            new TicketTypeRequest(TicketTypeRequest.Type.CHILD,  3),
            new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
        );

        verify(ticketPaymentService).makePayment(1L, 95);
        verify(seatReservationService).reserveSeat(1L, 5);
    }

    @Test
    @DisplayName("25 adults (max boundary): charged £625, 25 seats reserved")
    void twentyFiveAdults() {
        ticketService.purchaseTickets(1L,
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 25)
        );

        verify(ticketPaymentService).makePayment(1L, 625);
        verify(seatReservationService).reserveSeat(1L, 25);
    }

    @Test
    @DisplayName("2 adults + 2 infants: infants exactly match adults, charged £50, 2 seats")
    void twoAdultsTwoInfants() {
        ticketService.purchaseTickets(1L,
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT,  2),
            new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
        );

        verify(ticketPaymentService).makePayment(1L, 50);
        verify(seatReservationService).reserveSeat(1L, 2);
    }
}
