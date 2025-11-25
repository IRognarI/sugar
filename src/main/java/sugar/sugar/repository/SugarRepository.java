package sugar.sugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sugar.sugar.model.Sugar;

import java.util.List;

public interface SugarRepository extends JpaRepository<Sugar, Long> {
    @Query("""
            select s
            from Sugar s
            where s.levelSugar = :levelSugar
            and s.id <> :id
            """)
    List<Sugar> findByLevelSugar(@Param(value = "levelSugar") double levelSugar, @Param(value = "id") Long id);
}
