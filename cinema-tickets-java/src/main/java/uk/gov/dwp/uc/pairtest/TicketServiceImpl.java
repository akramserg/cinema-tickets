package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import java.util.EnumMap;
import java.util.Map;

public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

    }

    /**
     * Calculates ticket counts per ticket type.
     */
    private Map<TicketTypeRequest.Type, Integer> countTicketsByType(TicketTypeRequest[] ticketTypeRequests) {
        Map<TicketTypeRequest.Type, Integer> counts = new EnumMap<>(TicketTypeRequest.Type.class);
        for (TicketTypeRequest request : ticketTypeRequests) {
            counts.merge(request.getTicketType(), request.getNoOfTickets(), Integer::sum);
        }
        return counts;
    }

}
