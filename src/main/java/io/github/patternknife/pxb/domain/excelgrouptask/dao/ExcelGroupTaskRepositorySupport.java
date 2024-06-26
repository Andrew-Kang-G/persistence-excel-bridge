package io.github.patternknife.pxb.domain.excelgrouptask.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.patternknife.pxb.config.response.error.exception.data.ExcelGroupTaskNotFoundException;
import io.github.patternknife.pxb.domain.common.dto.DateRangeFilter;
import io.github.patternknife.pxb.domain.common.dto.SorterValueFilter;
import io.github.patternknife.pxb.domain.excelgrouptask.cache.PxbInMemoryExcelGroupTaskIds;
import io.github.patternknife.pxb.domain.excelgrouptask.dto.ExcelGroupTaskReqDTO;
import io.github.patternknife.pxb.domain.excelgrouptask.dto.ExcelGroupTaskResDTO;
import io.github.patternknife.pxb.domain.excelgrouptask.dto.ExcelGroupTaskSearchFilter;
import io.github.patternknife.pxb.domain.excelgrouptask.dto.QExcelGroupTaskResDTO_OneRes;
import io.github.patternknife.pxb.domain.excelgrouptask.entity.ExcelGroupTask;
import io.github.patternknife.pxb.domain.excelgrouptask.entity.QExcelGroupTask;
import io.github.patternknife.pxb.domain.excelgrouptask.enums.ExcelGroupTaskStatusConst;
import io.github.patternknife.pxb.domain.file.bo.ExcelGroupTaskFileBO;
import io.github.patternknife.pxb.util.CustomUtils;
import io.github.patternknife.pxb.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;

@Repository
public class ExcelGroupTaskRepositorySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    private final ExcelGroupTaskRepository excelGroupTaskRepository;


    private final ExcelGroupTaskFileBO excelGroupTaskFileBO;

    public ExcelGroupTaskRepositorySupport(JPAQueryFactory jpaQueryFactory, ExcelGroupTaskRepository excelGroupTaskRepository,
                                           ExcelGroupTaskFileBO excelGroupTaskFileBO) {

        super(ExcelGroupTask.class);
        this.excelGroupTaskRepository = excelGroupTaskRepository;
        this.jpaQueryFactory = jpaQueryFactory;
        this.excelGroupTaskFileBO = excelGroupTaskFileBO;
    }


    public ExcelGroupTask findById(Long id) throws ExcelGroupTaskNotFoundException {
        return excelGroupTaskRepository.findById(id).orElseThrow(() -> new ExcelGroupTaskNotFoundException("Cannot find the following Excel Group ID: " + id));
    }


    public void deleteExcelGroupTask(Long id, Long adminId){
        ExcelGroupTask excelGroupTask = findById(id);
        excelGroupTaskRepository.delete(excelGroupTask);
    }


    @Transactional( rollbackFor=Exception.class)
    public Boolean setExcelGroupTaskStatusInProgress(Long id){

        String jpql = "UPDATE ExcelGroupTask e SET e.status = :status WHERE e.id = :id";

        int updatedCount = getEntityManager().createQuery(jpql)
                .setParameter("status", ExcelGroupTaskStatusConst.IN_PROGRESS.getValue())
                .setParameter("id", id)
                .executeUpdate();

        if(updatedCount > 0){
            return true;
        }else{
            return false;
        }
    }


    @Transactional( rollbackFor=Exception.class)
    public Boolean setExcelGroupTaskStatusNotInProgress(Long id){

        String jpql = "UPDATE ExcelGroupTask e SET e.status = :status WHERE e.id = :id";

        int updatedCount = getEntityManager().createQuery(jpql)
                .setParameter("status", ExcelGroupTaskStatusConst.NOT_STARTED.getValue())
                .setParameter("id", id)
                .executeUpdate();

        if(updatedCount > 0){
            return true;
        }else{
            return false;
        }
    }



    public Page<ExcelGroupTaskResDTO.OneRes> findExcelDBWriteGroupTasksByPageFilter(Boolean skipPagination,
                                                                                    Integer pageNum,
                                                                                    Integer pageSize,
                                                                                    String excelGroupTaskSearchFilter,
                                                                                    String sorterValueFilter,
                                                                                    String dateRangeFilter) throws JsonProcessingException {

        final QExcelGroupTask qExcelGroupTask = QExcelGroupTask.excelGroupTask;

        JPQLQuery<ExcelGroupTaskResDTO.OneRes> query = jpaQueryFactory
                .select(new QExcelGroupTaskResDTO_OneRes(qExcelGroupTask.id,
                        qExcelGroupTask.status, qExcelGroupTask.rowCountPerTask, qExcelGroupTask.totalRow,
                        qExcelGroupTask.description, qExcelGroupTask.savedFileExt,
                        qExcelGroupTask.originalFileName,
                        qExcelGroupTask.excelUpdatedAt,
                        qExcelGroupTask.createdAt, qExcelGroupTask.updatedAt))
                .from(qExcelGroupTask).where(qExcelGroupTask.id.in(PxbInMemoryExcelGroupTaskIds.getInstance().getWriteTaskGroupIds()));

        ObjectMapper objectMapper = new ObjectMapper();

        if(!CustomUtils.isEmpty(excelGroupTaskSearchFilter)) {

            ExcelGroupTaskSearchFilter deserializedExcelGroupTaskSearchFilter = (ExcelGroupTaskSearchFilter) objectMapper.readValue(excelGroupTaskSearchFilter, ExcelGroupTaskSearchFilter.class);

            if (!CustomUtils.isEmpty(deserializedExcelGroupTaskSearchFilter.getDescription())) {
                query.where(qExcelGroupTask.description.likeIgnoreCase("%" + deserializedExcelGroupTaskSearchFilter.getDescription() + "%"));
            }
        }


        if(!CustomUtils.isEmpty(dateRangeFilter)) {
            DateRangeFilter deserializedDateRangeFilter = (DateRangeFilter) objectMapper.readValue(dateRangeFilter, DateRangeFilter.class);
            if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getColumn())) {
                if ("createdAt".equals(deserializedDateRangeFilter.getColumn())) {

                    BooleanBuilder booleanBuilder = new BooleanBuilder();

                    if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getStartDate())) {
                        Timestamp startTimestamp = Timestamp.valueOf(deserializedDateRangeFilter.getStartDate() + " 00:00:00");
                        booleanBuilder.and(qExcelGroupTask.createdAt.after(startTimestamp));
                    }
                    if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getEndDate())) {
                        Timestamp endTimestamp = Timestamp.valueOf(deserializedDateRangeFilter.getEndDate() + " 23:59:59");
                        booleanBuilder.and(qExcelGroupTask.createdAt.before(endTimestamp));
                    }

                    query.where(booleanBuilder);

                }else if ("updatedAt".equals(deserializedDateRangeFilter.getColumn())) {

                    BooleanBuilder booleanBuilder = new BooleanBuilder();

                    if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getStartDate())) {
                        Timestamp startTimestamp = Timestamp.valueOf(deserializedDateRangeFilter.getStartDate() + " 00:00:00");
                        booleanBuilder.and(qExcelGroupTask.updatedAt.after(startTimestamp));
                    }
                    if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getEndDate())) {
                        Timestamp endTimestamp = Timestamp.valueOf(deserializedDateRangeFilter.getEndDate() + " 23:59:59");
                        booleanBuilder.and(qExcelGroupTask.updatedAt.before(endTimestamp));
                    }

                    query.where(booleanBuilder);

                } else {
                    throw new IllegalStateException("Invalid date range");
                }
            }

        }


        if(!CustomUtils.isEmpty(sorterValueFilter)) {
            SorterValueFilter deserializedSorterValueFilter = (SorterValueFilter) objectMapper.readValue(sorterValueFilter, SorterValueFilter.class);

            String sortedColumn = deserializedSorterValueFilter.getColumn();

            switch (sortedColumn) {
                case "id":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.id.asc() : qExcelGroupTask.id.desc());
                    break;
                case "status":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.status.asc() : qExcelGroupTask.status.desc());
                    break;
                case "rowCountPerTask":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.rowCountPerTask.asc() : qExcelGroupTask.rowCountPerTask.desc());
                    break;
                case "totalRow":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.totalRow.asc() : qExcelGroupTask.totalRow.desc());
                    break;
                case "createdAt":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.createdAt.asc() : qExcelGroupTask.createdAt.desc());
                    break;
                case "updatedAt":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.updatedAt.asc() : qExcelGroupTask.updatedAt.desc());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid sortable column :: " + sortedColumn);
            }
        }


        PaginationUtil paginationUtil = new PaginationUtil(getQuerydsl());
        return paginationUtil.applyPagination(query, pageNum, pageSize, skipPagination);
    }



    public Page<ExcelGroupTaskResDTO.OneRes> findExcelDBReadGroupTasksByPageAndFilter(Boolean skipPagination,
                                                                                      Integer pageNum,
                                                                                      Integer pageSize,
                                                                                      String excelGroupTaskSearchFilter,
                                                                                      String sorterValueFilter,
                                                                                      String dateRangeFilter) throws JsonProcessingException {

        final QExcelGroupTask qExcelGroupTask = QExcelGroupTask.excelGroupTask;

        JPQLQuery<ExcelGroupTaskResDTO.OneRes> query = jpaQueryFactory
                .select(new QExcelGroupTaskResDTO_OneRes(qExcelGroupTask.id,
                        qExcelGroupTask.status, qExcelGroupTask.rowCountPerTask, qExcelGroupTask.totalRow,
                        qExcelGroupTask.description, qExcelGroupTask.savedFileExt,
                        qExcelGroupTask.originalFileName, qExcelGroupTask.excelUpdatedAt,
                        qExcelGroupTask.createdAt, qExcelGroupTask.updatedAt))
                .from(qExcelGroupTask).where(qExcelGroupTask.id.in(PxbInMemoryExcelGroupTaskIds.getInstance().getReadTaskGroupIds()));

        ObjectMapper objectMapper = new ObjectMapper();

        if(!CustomUtils.isEmpty(excelGroupTaskSearchFilter)) {

            ExcelGroupTaskSearchFilter deserializedExcelGroupTaskSearchFilter = (ExcelGroupTaskSearchFilter) objectMapper.readValue(excelGroupTaskSearchFilter, ExcelGroupTaskSearchFilter.class);

            if (!CustomUtils.isEmpty(deserializedExcelGroupTaskSearchFilter.getDescription())) {
                query.where(qExcelGroupTask.description.likeIgnoreCase("%" + deserializedExcelGroupTaskSearchFilter.getDescription() + "%"));
            }
            if (!CustomUtils.isEmpty(deserializedExcelGroupTaskSearchFilter.getId())) {
                query.where(qExcelGroupTask.id.eq(deserializedExcelGroupTaskSearchFilter.getId()));
            }
        }


        if(!CustomUtils.isEmpty(dateRangeFilter)) {
            DateRangeFilter deserializedDateRangeFilter = (DateRangeFilter) objectMapper.readValue(dateRangeFilter, DateRangeFilter.class);
            if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getColumn())) {
                if ("createdAt".equals(deserializedDateRangeFilter.getColumn())) {

                    BooleanBuilder booleanBuilder = new BooleanBuilder();

                    if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getStartDate())) {
                        Timestamp startTimestamp = Timestamp.valueOf(deserializedDateRangeFilter.getStartDate() + " 00:00:00");
                        booleanBuilder.and(qExcelGroupTask.createdAt.after(startTimestamp));
                    }
                    if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getEndDate())) {
                        Timestamp endTimestamp = Timestamp.valueOf(deserializedDateRangeFilter.getEndDate() + " 23:59:59");
                        booleanBuilder.and(qExcelGroupTask.createdAt.before(endTimestamp));
                    }

                    query.where(booleanBuilder);

                }else if ("updatedAt".equals(deserializedDateRangeFilter.getColumn())) {

                    BooleanBuilder booleanBuilder = new BooleanBuilder();

                    if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getStartDate())) {
                        Timestamp startTimestamp = Timestamp.valueOf(deserializedDateRangeFilter.getStartDate() + " 00:00:00");
                        booleanBuilder.and(qExcelGroupTask.updatedAt.after(startTimestamp));
                    }
                    if (!CustomUtils.isEmpty(deserializedDateRangeFilter.getEndDate())) {
                        Timestamp endTimestamp = Timestamp.valueOf(deserializedDateRangeFilter.getEndDate() + " 23:59:59");
                        booleanBuilder.and(qExcelGroupTask.updatedAt.before(endTimestamp));
                    }

                    query.where(booleanBuilder);

                } else {
                    throw new IllegalStateException("Invalid date range.");
                }
            }
        }

        if(!CustomUtils.isEmpty(sorterValueFilter)) {
            SorterValueFilter deserializedSorterValueFilter = (SorterValueFilter) objectMapper.readValue(sorterValueFilter, SorterValueFilter.class);

            String sortedColumn = deserializedSorterValueFilter.getColumn();

            switch (sortedColumn) {
                case "id":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.id.asc() : qExcelGroupTask.id.desc());
                    break;
                case "status":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.status.asc() : qExcelGroupTask.status.desc());
                    break;
                case "rowCountPerTask":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.rowCountPerTask.asc() : qExcelGroupTask.rowCountPerTask.desc());
                    break;
                case "totalRow":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.totalRow.asc() : qExcelGroupTask.totalRow.desc());
                    break;
                case "description":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.description.asc() : qExcelGroupTask.description.desc());
                    break;
                case "originalFileName":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.originalFileName.asc() : qExcelGroupTask.originalFileName.desc());
                    break;
                case "createdAt":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.createdAt.asc() : qExcelGroupTask.createdAt.desc());
                    break;
                case "updatedAt":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.updatedAt.asc() : qExcelGroupTask.updatedAt.desc());
                    break;
                case "excelUpdatedAt":
                    query.orderBy(deserializedSorterValueFilter.getAsc() ? qExcelGroupTask.excelUpdatedAt.asc() : qExcelGroupTask.excelUpdatedAt.desc());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid sorting column :: " + sortedColumn);
            }
        }

        PaginationUtil paginationUtil = new PaginationUtil(getQuerydsl());
        Page<ExcelGroupTaskResDTO.OneRes> resultPage = paginationUtil.applyPagination(query, pageNum, pageSize, skipPagination);
        return resultPage.map(item -> {
            try {

                item.setCurrentFileSize(excelGroupTaskFileBO.getFileSizeExcelGroupTaskExcelForDBRead(item.getId(), item.getSavedFileExt()));
            } catch (IOException e) {
                item.setCurrentFileSize(0L);
            }

            return item;
        });
    }


    /*
        2. Create
    * */
    public ExcelGroupTask create(ExcelGroupTask excelGroupTask){
        return excelGroupTaskRepository.save(excelGroupTask);
    }


    public ExcelGroupTaskResDTO.CreateOrUpdateRes update(Long id, ExcelGroupTaskReqDTO.UpdateStatusReq dto) {

        final ExcelGroupTask excelGroupTask = excelGroupTaskRepository.findById(id).orElseThrow(() -> new ExcelGroupTaskNotFoundException("ExcelGroupTask for '" + id + "' not found."));

        excelGroupTask.updateStatus(dto);

        return new ExcelGroupTaskResDTO.CreateOrUpdateRes(excelGroupTask);
    }


}
