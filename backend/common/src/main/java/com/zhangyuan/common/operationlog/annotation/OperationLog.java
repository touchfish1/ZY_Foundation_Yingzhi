package com.zhangyuan.common.operationlog.annotation;

import com.zhangyuan.common.operationlog.domain.model.OperationType;
import com.zhangyuan.common.operationlog.domain.model.ResourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解，标注在 Controller 方法上，自动记录操作日志。
 * <p>
 * 用法示例：
 * <pre>{@code
 * @OperationLog(type = UPDATE, resource = CMS_PAGE, resourceId = "#pageId")
 * public ApiResponse<PageDetailResponse> updatePage(@PathVariable Long pageId, ...) { }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 操作类型 */
    OperationType type();

    /** 资源类型 */
    ResourceType resource();

    /**
     * 资源 ID 的 SpEL 表达式，从方法参数中提取。
     * 例如 {@code "#pageId"}、{@code "#request.code()"}
     */
    String resourceId() default "";

    /**
     * 变更详情的 SpEL 表达式，可选。
     * 需要记录字段级变化时使用，应解析为 JSON 字符串。
     */
    String detail() default "";
}
