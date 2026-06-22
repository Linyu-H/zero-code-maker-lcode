package com.commul.ailcode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 多文件代码结果
 */
@Data
@Description("生成多个代码文件的结果")
public class MultiFileCodeResult {

    @Description("HTML 代码")
    private String htmlCode;

    @Description("CSS 代码")
    private String cssCode;

    @Description("JS 代码")
    private String jsCode;

    @Description("生成代码的描述")
    private String description;
}
