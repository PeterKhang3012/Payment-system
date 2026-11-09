package com.myproject.tuition;

import org.junit.jupiter.api.Test;
import com.myproject.tuition.repository.TuitionRepository;
import com.myproject.tuition.service.TuitionService;
import com.myproject.tuition.exception.TuitionConflictException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

// API error regression: preserve atomic update calls and classify their zero-row outcome explicitly.
class TuitionErrorMappingTest {
    @Test void zeroUpdatedRowsRemainConflictsWithoutExtraQueries() {
        var repository = mock(TuitionRepository.class);
        var service = new TuitionService(repository);
        assertThrows(TuitionConflictException.class, () -> service.claimTuition("1","payment"));
        assertThrows(TuitionConflictException.class, () -> service.markTuitionAsPaid("1","payment"));
        assertThrows(TuitionConflictException.class, () -> service.releaseTuition("1","payment"));
        verify(repository).claimTuition("1","payment");
        verify(repository).completeTuition("1","payment");
        verify(repository).releaseTuition("1","payment");
        verifyNoMoreInteractions(repository);
    }
}
