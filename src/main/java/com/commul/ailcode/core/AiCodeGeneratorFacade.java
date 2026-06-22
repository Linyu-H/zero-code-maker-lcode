package com.commul.ailcode.core;

import com.commul.ailcode.ai.AiCodeGeneratorService;
import com.commul.ailcode.ai.model.HtmlCodeResult;
import com.commul.ailcode.ai.model.MultiFileCodeResult;
import com.commul.ailcode.exception.BusinessException;
import com.commul.ailcode.exception.ErrorCode;
import com.commul.ailcode.exception.ThrowUtils;
import com.commul.ailcode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 代码生成门面类，组合代码生成和保存功能
 */
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;


    /**
     * 生成代码并保存（统一入口根据类型生成并保存代码）
     *
     * @param prompt       提示
     * @param codeGenType  代码生成类型
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

    private File generateAndSaveMultiFileCode(String prompt) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(prompt);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }

    private File generateAndSaveHtmlCode(String prompt) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(prompt);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }
}
