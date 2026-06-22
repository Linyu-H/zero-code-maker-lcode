package com.commul.ailcode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import javax.swing.text.html.HTML;

/**
 *  HTML 代码结果
 */
@Data
@Description("生成 HTML 代码文件的结果")
public class HtmlCodeResult {

    @Description("HTML 代码")
    private String htmlCode;

    @Description("生成代码的描述")
    private String description;
}
