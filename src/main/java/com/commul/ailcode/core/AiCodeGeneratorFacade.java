package com.commul.ailcode.core;

import com.commul.ailcode.ai.AiCodeGeneratorService;
import com.commul.ailcode.ai.model.HtmlCodeResult;
import com.commul.ailcode.ai.model.MultiFileCodeResult;
import com.commul.ailcode.exception.BusinessException;
import com.commul.ailcode.exception.ErrorCode;
import com.commul.ailcode.exception.ThrowUtils;
import com.commul.ailcode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 代码生成门面类，组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;
    @Autowired
    private View error;


    /**
     * 生成代码并保存（统一入口根据类型生成并保存代码）
     *
     * @param prompt      提示
     * @param codeGenType 代码生成类型
     * @return 生成的代码
     */
    public File generateAndSaveCode(String prompt, CodeGenTypeEnum codeGenType) {
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR, "代码生成类型错误");

        return switch (codeGenType) {
            case HTML -> generateAndSaveHtmlCode(prompt);
            case MULTI_FILE -> generateAndSaveMultiFileCode(prompt);
            default -> {
                String errormessage = "暂不支持的生成类型：" + codeGenType.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errormessage);
            }
        };
    }


    /**
     * 生成代码并保存（统一入口根据类型生成并保存代码Stream）
     *
     * @param prompt      提示
     * @return 生成的代码
     */
    public Flux<String> generateAndSaveCodeStream(String prompt) {
        // 获取Html结果
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(prompt);
        // 字符串拼接器，用于当流式返回所有的代码之后，在保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(codeBuilder::append).doOnComplete(() -> {
            try {
                // 流式返回完成后，保存代码
                String completeHtmlCode = codeBuilder.toString();
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                log.info("保存文件成功：{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存文件失败：{}", e.getMessage());
            }
        });
    }

    public Flux<String> generateAndSaveMultiFileCodeStream(String prompt) {
        // 获取Html结果
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(prompt);
        // 字符串拼接器，用于当流式返回所有的代码之后，在保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(codeBuilder::append).doOnComplete(() -> {
            try {
                // 流式返回完成后，保存代码
                String completeMultiFileCode = codeBuilder.toString();
                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                File saveDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
                log.info("保存文件成功：{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存文件失败：{}", e.getMessage());
            }
        });
    }

    private File generateAndSaveMultiFileCode(String prompt) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(prompt);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }

    private File generateAndSaveHtmlCode(String prompt) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(prompt);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }
}
