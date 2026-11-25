package kz.iitu.springlab.loan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// plain unit test: Mockito only, no Spring context
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    private static final LocalDate ISSUED = LocalDate.of(2026, 11, 2);
    private static final LocalDate DUE = LocalDate.of(2026, 11, 16);

    @Mock LoanRepository loans;
    @Mock HoldRepository holds;
    @InjectMocks LoanService service;

    @Test
    void anExistingLoanIsReturnedAsStored() {
        Loan stored = new Loan(1L, "syrym", ISSUED, DUE);
        given(loans.findById(5L)).willReturn(Optional.of(stored));

        Loan found = service.byId(5L);

        assertThat(found).isSameAs(stored);
        assertThat(found.getReader()).isEqualTo("syrym");
        assertThat(found.getDueOn()).isEqualTo(DUE);
    }

    @Test
    void anUnknownLoanIsReportedAsNotFound() {
        given(loans.findById(42L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.byId(42L))
                .isInstanceOf(LoanNotFoundException.class)
                .hasMessage("Loan 42 does not exist");
    }

    @Test
    void extendingAFreeLoanMovesTheDueDateTwoWeeksAndStoresIt() {
        given(loans.findById(5L)).willReturn(Optional.of(new Loan(1L, "syrym", ISSUED, DUE)));
        given(holds.existsByBookId(1L)).willReturn(false);
        given(loans.save(any(Loan.class))).willAnswer(call -> call.getArgument(0));

        service.extend(5L);

        ArgumentCaptor<Loan> saved = ArgumentCaptor.forClass(Loan.class);
        verify(loans).save(saved.capture());
        assertThat(saved.getValue().getDueOn()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(saved.getValue().getExtensions()).isEqualTo(1);
        assertThat(saved.getValue().getBookId()).isEqualTo(1L);
    }

    // variant 2: the branch that is easy to forget
    @Test
    void aLoanIsNotExtendedWhileOtherReadersAreQueuedForTheBook() {
        given(loans.findById(5L)).willReturn(Optional.of(new Loan(1L, "syrym", ISSUED, DUE)));
        given(holds.existsByBookId(1L)).willReturn(true);

        assertThatThrownBy(() -> service.extend(5L))
                .isInstanceOf(LoanExtensionRefusedException.class)
                .hasMessageContaining("queued");
        verify(loans, never()).save(any());
    }

    // task 5: the yellow line "loan.getExtensions() >= MAX_EXTENSIONS" only ever took
    // the false way; this test takes the true way
    @Test
    void aLoanIsNotExtendedAfterItHasBeenExtendedTwice() {
        Loan twiceExtended = new Loan(1L, "syrym", ISSUED, DUE);
        twiceExtended.extendBy(14);
        twiceExtended.extendBy(14);
        given(loans.findById(5L)).willReturn(Optional.of(twiceExtended));
        given(holds.existsByBookId(1L)).willReturn(false);

        assertThatThrownBy(() -> service.extend(5L))
                .isInstanceOf(LoanExtensionRefusedException.class)
                .hasMessage("Loan 5 cannot be extended: the limit of 2 extensions is reached");
        verify(loans, never()).save(any());
    }
}
