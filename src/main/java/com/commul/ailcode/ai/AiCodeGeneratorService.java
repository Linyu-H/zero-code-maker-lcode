package com.commul.ailcode.ai;

import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {

    /**
     * 根据提示生成html代码
     *
     * @param prompt 提示
     * @return 代码
     */
    @SystemMessage(fromResource = "prompt/OneHtml.md")
    String generateHtmlCode(String prompt);

    /**
     * 根据提示生成多文件代码
     *
     * @param prompt 提示
     * @return 代码
     */
    @SystemMessage(fromResource = "prompt/MoreFile.md")
    String generateMultiFileCode(String prompt);
}
