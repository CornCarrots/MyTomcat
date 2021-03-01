package util;

import java.io.File;

import catalina.Context;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;


/**
 * @Author: zerocoder
 * @Description: JSP工具类
 * @Date: 2021/2/24 0:02
 */

public class JspUtil {

    private static String jspPath = "/org/apache/jsp/";

    private static String[] keyword = {
            "abstract","assert","boolean","break","byte","case","catch","char","class","const","continue","default",
            "do","double","else","enum","extends","final","finally","float","for","goto","if","implements",
            "import","instanceof","int","interface","long","native","new","package","private","protected","public",
            "return","short","static", "strictfp","super","switch","synchronized","this","throw","throws","transient",
            "try","void","volatile","while"};

    /**
     * 编译Jsp文件
     * @param context
     * @param file
     * @throws JasperException
     */
    public static void compileFile(Context context, File file) throws JasperException {
        String subFolder;
        String path = context.getPath();
        if ("/".equals(path)){
            subFolder = "_";
        }
        else {
            subFolder = StrUtil.subAfter(path, "/", false);
        }
        String workPath = new File(Constant.WORK_FOLDER, subFolder).getAbsolutePath() + File.separator;
        String[] args = new String[]{"-webapp", context.getDocBase().toLowerCase(), "-d", workPath.toLowerCase(), "-compile", };
        JspC jspC = new JspC();
        jspC.setArgs(args);
        jspC.execute(file);
    }

    public static final String makeJavaIdentifier(String identifier){
        return makeJavaIdentifier(identifier, true);
    }

    /**
     *
     * @param identifier 具体应用标识
     * @param periodToUnderScore 是否支持下划线
     * @return
     */
    public static final String makeJavaIdentifier(String identifier, Boolean periodToUnderScore){
        StringBuilder builder = new StringBuilder(identifier.length());
        // 是否是java标识符
        if (!Character.isJavaIdentifierPart(identifier.charAt(0))){
            builder.append("_");
        }
        char[] identifierArray = identifier.toCharArray();
        for (int i = 0; i < identifierArray.length; i++) {
            char identifierChar = identifierArray[i];
            // Java标识符 无需转换
            if (Character.isJavaIdentifierPart(identifierChar) && (identifierChar != '_' || !periodToUnderScore)){
                builder.append(identifierChar);
            }
            // 把 点 转换为 下划线
            else if(identifierChar == '.' && periodToUnderScore){
                builder.append("_");
            }
            // 其他的转换 下划线加
            else {
                builder.append(mangleChar(identifierChar));
            }
        }
        if (isJavaKeyWord(builder.toString())){
            builder.append("_");
        }
        return builder.toString();
    }

    /** unicode转义
     * 用位运算提取出4部分并使用Character.forDigit转换成16进制数对应的字符.
     * @param character
     * @return
     */
    private static String mangleChar(Character character){
        char[] res = new char[]{
                '_',
                Character.forDigit((character >> 12) & 0xf, 16),
                Character.forDigit((character >> 8) & 0xf, 16),
                Character.forDigit((character >> 4) & 0xf, 16),
                Character.forDigit(character & 0xf, 16),
        };
        return new String(res);
    }

    private static boolean isJavaKeyWord(String key){
        int l = 0;
        int h = keyword.length;
        int mid;
        while (l < h){
            mid = (h - l ) / 2;
            if (keyword[mid].compareTo(key) == 0){
                return true;
            }
            else if (keyword[mid].compareTo(key) < 0){
                l++;
            }else {
                h++;
            }
        }
        return false;
    }

    /**
     * 获取jsp路径
     * @param uri
     * @param subFolder
     * @return
     */
    public static String getServletPath(String uri, String subFolder){
        // 获取jsp解析路径
        String tempPath = jspPath + uri;
        File tempFile = FileUtil.file(Constant.WORK_FOLDER, subFolder, tempPath);
        String fileName = tempFile.getName();
        // 获取jsp文件名
        String javaIdentifier = makeJavaIdentifier(fileName);
        File servletFile = new File(tempFile.getParent(), javaIdentifier);
        return servletFile.getAbsolutePath();
    }

    public static String getServletClassPath(String uri, String subFolder){
        return getServletPath(uri, subFolder) + ".class";
    }

    public static String getServletJavaPath(String uri, String subFolder){
        return getServletPath(uri, subFolder) + ".java";
    }

    public static String getServletClassName(String uri, String subFolder){
        File tempFile = FileUtil.file(Constant.WORK_FOLDER, subFolder);
        String tempPath = tempFile.getAbsolutePath() + File.separator;
        String servletPath = getServletPath(uri, subFolder);
        String classPath = StrUtil.subAfter(servletPath, tempPath, false);
        return StrUtil.replace(classPath, File.separator, ".");
    }

    public static void main(String[] args) throws JasperException {
        Context context = new Context("/javaweb", "E:\\JavaProjects\\javaweb\\web", null, true);
        File file = new File("E:\\JavaProjects\\javaweb\\web\\index.jsp");
        compileFile(context, file);
    }
}
