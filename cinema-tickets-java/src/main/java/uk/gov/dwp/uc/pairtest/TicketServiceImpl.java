package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import java.util.EnumMap;
import java.util.Map;

public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS = 25;

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
        validateAccountId(accountId);
        validateTicketTypeRequests(ticketTypeRequests);

        Map<TicketTypeRequest.Type, Integer> counts = countTicketsByType(ticketTypeRequests);
        int adults   = counts.getOrDefault(TicketTypeRequest.Type.ADULT,  0);
        int children = counts.getOrDefault(TicketTypeRequest.Type.CHILD,  0);
        int infants  = counts.getOrDefault(TicketTypeRequest.Type.INFANT, 0);
        int totalTickets = adults + children + infants;

        validateTicketAgePolicy(adults, children, infants);
        validateTotalTicketLimit(totalTickets);
    }

    /**
     * Validate that the account ID is not null and greater than zero.
     */
    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Account ID must be greater than zero.");
        }
    }

    /**
     * Validate that the requests array is not null or empty and validate each request.
     */
    private void validateTicketTypeRequests(TicketTypeRequest[] ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket request must be provided.");
        }
        for (TicketTypeRequest request : ticketTypeRequests) {
            validateTicketTypeRequest(request);
        }
    }

    /**
     * Validate that a single ticket request is greater than zero.
     */
    private void validateTicketTypeRequest(TicketTypeRequest request) {
        if (request.getNoOfTickets() <= 0) {
            throw new InvalidPurchaseException("Each ticket request must have a quantity greater than zero.");
        }
    }

    /**
     * Validates the ticket age policy
     */
    private void validateTicketAgePolicy(int adults, int children, int infants) {
        if (adults == 0) {
            throw new InvalidPurchaseException("At least one Adult ticket must be purchased.");
        }
        if (infants > adults) {
            throw new InvalidPurchaseException("Number of infants cannot exceed the number of adults.");
        }
    }

    /**
     * Validate that the total number of tickets does not exceed the maximum allowed.
     */
    private void validateTotalTicketLimit(int total) {
        if (total > MAX_TICKETS) {
            throw new InvalidPurchaseException("Total tickets cannot exceed " + MAX_TICKETS + ".");
        }
    }

    /**
     * Return the sum of ticket counts per type across all requests.
     */
    private Map<TicketTypeRequest.Type, Integer> countTicketsByType(TicketTypeRequest[] ticketTypeRequests) {
        Map<TicketTypeRequest.Type, Integer> counts = new EnumMap<>(TicketTypeRequest.Type.class);
        for (TicketTypeRequest request : ticketTypeRequests) {
            counts.merge(request.getTicketType(), request.getNoOfTickets(), Integer::sum);
        }
        return counts;
    }

}
