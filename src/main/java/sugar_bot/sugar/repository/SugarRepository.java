package sugar_bot.sugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sugar_bot.sugar.model.Sugar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SugarRepository extends JpaRepository<Sugar, Long> {
    @Query("""
            select s
            from Sugar s
            where s.levelSugar = :levelSugar
            and s.id <> :id
            """)
    List<Sugar> findByLevelSugar(@Param(value = "levelSugar") double levelSugar, @Param(value = "id") Long id);

    Optional<Sugar> getSugarById(Long id);

    @Query(value = """
            select exists(
                            select 1
                            from sugars as s
                            where s.chat_id = :chatId
                        )
            """, nativeQuery = true)
    boolean existsByChatId(@Param("chatId") Long chatId);


    @Query(value = """
            select *
            from sugars
            where time between :timeStart and :timeEnd
            """, nativeQuery = true)
    List<Sugar> findByTimeBetween(@Param("timeStart") LocalDateTime timeStart, @Param("timeEnd") LocalDateTime timeEnd);
}
