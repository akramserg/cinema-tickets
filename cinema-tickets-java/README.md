# Cinema Tickets — Java Implementation

Role: Java & JavaScript Software Engineer

Candidate: Akram Sergewa
Application ID: 17019683
Campaign number: 451133

Solution to the DWP Cinema Tickets coding exercise.


## How to Run The tests

Requires Java 21 and Maven.

```bash
mvn test
```

16 tests should pass.

## The Journey of building this solution

**Step 0: Planning the solution**
I spent some time reading the exercise thoroughly and planning the solution. This includes the methods to build, the validation, the exceptions, the tests, the data types and any more assumptions to make that cover cases not fully covered by the exercise. You can find the assumptions I made bellow.
This Step also included deviding the work into short bites of work in ticket style with ACs. Even though this is not necessarily needed but it helps a lot in streamlining the process and catching difficulties and uncertainties quickly and improving the quality of the work.

**Step 1: Exception with a message**
The provided `InvalidPurchaseException` had no constructor that accepted a
message. Before writing any validation logic I added
`public InvalidPurchaseException(String message)` so that every throw site could
describe exactly what was wrong rather than producing a blank exception.

**Step 2: Count tickets by type**
This method was necessary for the service. The method loops the `TicketTypeRequest[]`and accumulates quantities into an
`EnumMap<TicketTypeRequest.Type, Integer>`,
then reads the three typed totals out at the end.
This implementation will get updated later on for better abstraction

**Step 3: Validation**
The validation methods came next:
- `validateAccountId`: rejects null, zero, or negative IDs
- `validateTicketTypeRequests`: rejects null or empty arrays, delegates to
  `validateTicketTypeRequest`: per item to reject zero quantities
- `validateTicketAgePolicy`: rejects purchases with no adult, or where infants
  outnumber adults
- `validateTotalTicketLimit`: rejects totals above 25

Some of these validation methods are very short but I made this decision to have each in a seperrate method for better abstraction and seperation of concerns. If an error happens or a requirement changes, it can make updating the validation methods much easier.

**Step 4: Payment calculation**
`calculateTicketsCost` calculate the price using the constants (`ADULT_TICKET_PRICE = 25`, `CHILD_TICKET_PRICE = 15`) then call `TicketPaymentService.makePayment(accountId, amount)`. 

**Step 5: Seat reservation**
`calculateSeatsToReserve` returns the number of seats then called `SeatReservationService.reserveSeat(accountId, seats)`.

**Step 6: Refactor: `TicketCountsByType` record**
At this point `validateTicketAgePolicy`, `calculateTicketsCost`, and
`calculateSeatsToReserve` each accepted three separate parameters which was repetitive and easy to pass in the wrong variables.
I introduced a private record `TicketCountsByType(int adults, int children, int infants)` and changed all methods to accept it. This way, the calling method doesn't need to worry about what to pass and the method itself contains the logic it needs to work.

**Step 7: Happy path tests**
With the implementation stable, six happy path tests were written covering:
adult-only, child-with-adult, infant-with-adult, mixed purchase, correct payment
amount, and correct seat count. Each test injects mocks of both services and uses
`verify()` to assert the exact values passed to `makePayment` and `reserveSeat`.

**Step 8: Negative tests**
Ten negative tests were added in four `@Nested` groups — `InvalidAccountId`,
`InvalidTicketRequests`, `AgePolicyViolations`, `TicketLimitViolations`to assert that each invalid
input is rejected with the right exception. Total: 16 tests, all passing.


## Choices

**Constructor injection of `TicketPaymentService` and `SeatReservationService`**
Both services are accepted as constructor arguments and stored as `private final`
fields using dependency injection which is the standard design pattern for this kind of classes. 

**`TicketCountsByType` — a private inner record**
After calling `countTicketsByType`, I had three separate `int` values (adults, children, infants) that needed passing into `validateTicketAgePolicy`, `calculateTicketsCost`, and `calculateSeatsToReserve`. Passing three loose integers is error-prone (wrong order, unclear intent).
As I mentioned in the steps, I introduced a private record `TicketCountsByType(int adults, int children, int infants)` to group them then pass it to each validation method without worrying about which type to pass.

**One private method per validation**
Rather than putting all validation in one block, each rule has its own method: `validateAccountId`, `validateTicketTypeRequest`, `validateTicketAgePolicy` `validateTotalTicketLimit`. Each throws `InvalidPurchaseException` with a message specific to that rule. 

## Assumptions

A few things the spec didn't explicitly cover:

- **A quantity of zero is invalid.** `validateTicketTypeRequest` rejects any request where `getNoOfTickets() <= 0`. Requesting zero tickets has no meaning.

- **Infants count toward the 25-ticket limit.** The exercise sets a maximum of 25
  without excluding any type. It wasn't clear if infants should be added to the total ticket count or not because they are not included in the price. There is an arguement for both cases but I decided to consider them as part of the total tickets count.

- **All requests in a single call belong to the same customer and the same purchase.**
  The specification does not explain the significance of having multiple `TicketTypeRequest`
  objects in one call. I assumed they represent one basket and one customer and one
  transaction. (for instance, a group of family or friends) so `countTicketsByType` merges all requests into combined totals before any validation runs.
