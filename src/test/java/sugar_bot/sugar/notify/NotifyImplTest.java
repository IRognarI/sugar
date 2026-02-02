package sugar_bot.sugar.notify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sugar_bot.sugar.repository.SugarRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class NotifyImplTest {

    @Mock
    private SugarRepository sugarRepository;

    @InjectMocks
    private NotifyImpl notify;

    @Test
    public void chatIdIsPresent() {
        Mockito.when(sugarRepository.existsByChatId(Mockito.anyLong()))
                .thenReturn(true);

        assertTrue(notify.chatIdExists(123L));
    }

    @Test
    public void chatIdIsNotPresent() {
        Mockito.when(sugarRepository.existsByChatId(Mockito.anyLong()))
                .thenReturn(false);

        assertFalse(notify.chatIdExists(12L));
    }
}