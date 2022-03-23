package com.zakl.workflow.log.aspect;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zakl.workflow.log.annotation.OperationLog;
import com.zakl.workflow.log.constant.LogItemNames;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;

/**
 * @author thz
 * @since 2020/4/26
 */
@Component
@Aspect
@Slf4j
public class LogAspect {


    @Pointcut("@annotation(com.southsmart.microserver.log.annotation.OperationLog)")
    public void operationLog() {
    }

    @Pointcut("@within(com.southsmart.microserver.log.annotation.OperationLog)")
    public void operationLogType() {
    }


    @Around(value = "operationLog()||operationLogType()")
    @Order(value = -1)
    public Object controllerPointCut(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog annotation = getOperationLogAnnotation(joinPoint);
        log.info(getDetail(annotation));
        // 定义返回对象、得到方法需要的参数
        long startTime = System.currentTimeMillis();
        // 执行方法
        Object result = joinPoint.proceed(joinPoint.getArgs());
        long endTime = System.currentTimeMillis();
        // 打印耗时的信息
        long diffTime = endTime - startTime;
        // 获取request
        logRequestInfo(joinPoint, diffTime);
        return result;
    }

    private void logRequestInfo(ProceedingJoinPoint joinPoint, long diffTime) {
        HttpServletRequest req = getRequest();
        if (req != null) {
            LogRequestInfo requestInfo = getRequestInfo(req);
            String requestMsgLog = String.format("  IP: %s , 调用接口: %s ,请求类型: %s ,耗时: %d ms",
                    requestInfo.requestIp, requestInfo.requestUrl, requestInfo.requestType, diffTime);
            log.info(requestMsgLog);

            logToMDC(requestInfo, requestMsgLog);

            logRequestParamOrBody(joinPoint, joinPoint.getArgs(), req);
        }
    }

    /**
     * 将入口数据放置在MDC中
     *
     * @param requestInfo
     * @param requestMsgLog
     */
    private void logToMDC(LogRequestInfo requestInfo, String requestMsgLog) {
        MDC.put(LogItemNames.REQUEST_URL, requestInfo.requestUrl);
        MDC.put(LogItemNames.REQUEST_IP, requestInfo.requestIp);
        MDC.put(LogItemNames.TRACE_Id, UUID.randomUUID().toString());
        MDC.put(LogItemNames.MESSAGE, requestMsgLog);
    }

    /**
     * 对请求参数日志打日志
     * 主要包含query參數及post_requestBody
     *
     * @param joinPoint
     * @param args
     * @param req
     */
    private void logRequestParamOrBody(ProceedingJoinPoint joinPoint, Object[] args, HttpServletRequest req) {
        if (req.getMethod().toLowerCase().contains("post")) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Dict postBody = Dict.create();
            for (int i = 0; i < signature.getParameterNames().length; i++) {
                postBody.set(signature.getParameterNames()[i], args[i]);
            }
            log.info("################ 参数(post_body) : " + JSONUtil.toJsonStr(postBody));
        }
        Enumeration<String> pNames = req.getParameterNames();
        StringBuilder requestString = new StringBuilder();
        while (pNames.hasMoreElements()) {
            String name = pNames.nextElement();
            String value = req.getParameter(name);
            requestString.append(name).append("=").append(value).append("&");
        }
        if (requestString.length() > 1000) {
            requestString = new StringBuilder(requestString.substring(0, 900));
        }
        if (requestString.length() > 0) {
            requestString.deleteCharAt(requestString.length() - 1);
        }
        log.info("################ 参数(request_param) : " + requestString);
    }


    /**
     * 为接口返回数据补充通用返回字段,status,msg,data,需要接口返回类型需满足object
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("operationLog()||operationLogType()")
    public Object addResultInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        Class returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
        Object result = joinPoint.proceed();
        if (result == null) return null;
        if (returnType.getCanonicalName().contains("void")) {
            return result;
        }
        //todo 是否应该按照全限定名类进行限制
        OperationLog annotation = getOperationLogAnnotation(joinPoint);
        boolean forMatFlag = annotation == null || annotation.forMatResult();
        if (forMatFlag && !result.getClass().getName().contains("Result")) {
            return new Result(0, "操作成功", result);
        }
        return result;
    }


    //异常日志记录
    @AfterThrowing(value = "operationLog()||operationLogType()", throwing = "e")
    public void afterThrowing(JoinPoint point, Throwable e) {

        OperationLog annotation = getOperationLogAnnotation((ProceedingJoinPoint) point);
        if (annotation == null) return;
        String message = getDetail(annotation);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        //打印到控制台,便于开发
        e.printStackTrace();
        String print = sw.toString();
        int len = print.length();
        log.error(message + "========>{}", len > 800 ? print.substring(0, 800) : print.substring(0, len));
        logRequestInfo((ProceedingJoinPoint) point, Long.MIN_VALUE);
    }

    /**
     * 对当前登录用户和占位符处理
     *
     * @param annotation 注解信息
     * @return 返回处理后的描述
     */
    private String getDetail(OperationLog annotation) {
        String detail = "";
        String operationType = "";
        if (annotation != null) {
            detail = annotation.detail();
            operationType = annotation.operationType().getValue();
        }
        try {
            detail = "执行信息：" + detail + " ,操作类型:" + operationType;
        } catch (Exception e) {
            log.error("error : ", e);
        }
        return detail;
    }

    /**
     * 获取request
     *
     * @return request
     */
    private HttpServletRequest getRequest() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }
        return null;
    }


    /**
     * 获取请求信息
     *
     * @param req
     * @return
     */
    private LogRequestInfo getRequestInfo(HttpServletRequest req) {
        LogRequestInfo logRequestInfo = new LogRequestInfo();
        logRequestInfo.requestIp = req.getHeader("X-Forwarded-For") == null ? req.getRemoteAddr() : req.getHeader("X-Forwarded-For");
        logRequestInfo.requestUrl = req.getRequestURL().toString();
        logRequestInfo.requestType = req.getMethod();
        return logRequestInfo;
    }

    /**
     * 优先获取函数上的OperationLog 注解
     * 没有的话获取类上面的
     *
     * @param joinPoint
     * @return
     */
    private OperationLog getOperationLogAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(OperationLog.class);
        }
        return annotation;
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Result implements Serializable {
    private Integer status;
    private String message;
    private Object data;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class LogRequestInfo implements Serializable {
    String requestIp;
    String requestUrl;
    String requestType;
}