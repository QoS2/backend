package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.ChatTurn;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ChatTurnRepository extends JpaRepository<ChatTurn, Long> {

    List<ChatTurn> findBySession_IdOrderByCreatedAtAsc(Long sessionId);

    long countBySession_Id(Long sessionId);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE ChatTurn t SET t.scriptLine = null WHERE t.scriptLine.id IN :scriptLineIds")
    int clearScriptLineReferences(@Param("scriptLineIds") Collection<Long> scriptLineIds);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE ChatTurn t SET t.step = null WHERE t.step.id IN :stepIds")
    int clearStepReferences(@Param("stepIds") Collection<Long> stepIds);
}
