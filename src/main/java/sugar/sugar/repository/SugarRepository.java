package sugar.sugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sugar.sugar.model.Sugar;

public interface SugarRepository extends JpaRepository<Sugar, Long> {
    Sugar findByLevelSugar(double levelSugar);
}
