package com.commul.ailcode.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.commul.ailcode.ai.model.HtmlCodeResult;
import com.commul.ailcode.ai.model.MultiFileCodeResult;
import com.commul.ailcode.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 文件保存服务
 */
public class CodeFileSaver {

    // 文件保存目录
    public static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/cde_output/";

    // 保存HTML网页代码
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult){
        String buildUniquePath = buildUniquePath(CodeGenTypeEnum.HTML.getValue());
        saveFile(buildUniquePath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(buildUniquePath);
    }

    // 保存多文件代码
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String buildUniquePath = buildUniquePath(CodeGenTypeEnum.MULTI_FILE.getValue());
        saveFile(buildUniquePath, "index.html", multiFileCodeResult.getHtmlCode());
        saveFile(buildUniquePath, "index.css", multiFileCodeResult.getCssCode());
        saveFile(buildUniquePath, "index.js", multiFileCodeResult.getJsCode());
        return new File(buildUniquePath);
    }

    /**
     * 构建文件的唯一路径： tmp/cde_output/bizType_userId_雪花ID
     *
     * @param bizType 业务类型
     * @return 唯一目录的绝对路径
     */
    public static String buildUniquePath(String bizType) {
        long snowflakeId = IdUtil.getSnowflakeNextId();
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + bizType + "_" + snowflakeId;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }


    /**
     * 保存单个文件
     * @param dirPath
     * @param fileName
     * @param content
     */
    public static void saveFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        // 保存文件
        FileUtil.writeUtf8String(content, filePath);
    }
}
