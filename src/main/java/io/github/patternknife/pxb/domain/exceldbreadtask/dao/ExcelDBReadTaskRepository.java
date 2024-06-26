package io.github.patternknife.pxb.domain.exceldbreadtask.dao;

import io.github.patternknife.pxb.domain.exceldbreadtask.entity.ExcelDBReadTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface ExcelDBReadTaskRepository extends JpaRepository<ExcelDBReadTask, Long>, QuerydslPredicateExecutor<ExcelDBReadTask> {

    Boolean existsByGroupIdAndStatusIn(Long groupId, Collection<Integer> statuses);

    @Modifying
    @Transactional( rollbackFor=Exception.class)
    void deleteByGroupId(Long groupId);

    @Query("SELECT COUNT(e) FROM ExcelDBReadTask e WHERE e.status = :status AND e.groupId = :groupId")
    long countByStatusAndGroupId(@Param("status") int status, @Param("groupId") Long groupId);

    @Query("SELECT COUNT(e) FROM ExcelDBReadTask e WHERE e.status IN :statuses AND e.groupId = :groupId")
    long countByStatusesAndGroupId(@Param("statuses") List<Integer> statuses, @Param("groupId") Long groupId);

}