package com.only4.alarm

import com.only4.alarm.parser.MarkdownAlarmParser
import com.only4.alarm.processor.GroupingAlarmProcessor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertTrue

class AlarmManagerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `test alarm processing with sample data`() {
        // 创建一个简单的测试MD文件
        val testMdContent = """
            ```
            Graylog告警  
            前往oncall平台处理告警信息​​前往Graylog平台查看日志详情​​  
            开始时间：2025-06-13 18:00:21  
            告警应用：qifu-saas-gateway  
            环境：prod  
            描述：[fa8f6ddb-58820]  500 Server Error for HTTP POST "/qifu-saas-bpmn/instance/detail"  
            日志：​​2025-06-13 18:00:21 ERROR [TID:N/A] 这是测试日志内容
            [消息截断 总长度:1994 > 限制:500]​ @周英桥
            ```
            
            ```
            Graylog告警  
            前往oncall平台处理告警信息​​前往Graylog平台查看日志详情​​  
            开始时间：2025-06-13 17:22:26  
            告警应用：qifu-saas-pcs  
            环境：prod  
            描述：测试描述
            日志：​​测试日志
            [消息截断 总长度:10791 > 限制:500]​ @李四
            ```
        """.trimIndent()

        // 创建临时输入文件
        val inputFile = tempDir.resolve("test-input.md").toFile()
        inputFile.writeText(testMdContent)

        // 创建输出文件路径
        val outputFile = tempDir.resolve("test-output.md").toFile()

        // 创建解析器和处理器
        val parser = MarkdownAlarmParser()
        val processor = GroupingAlarmProcessor()
        val manager = AlarmManager(parser, processor)

        // 处理文件
        manager.processFile(inputFile.absolutePath, outputFile.absolutePath)

        // 验证输出文件存在并且有内容
        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0)

        // 读取输出内容进行验证
        val outputContent = outputFile.readText()

        // 验证输出内容包含分组信息
        assertTrue(outputContent.contains("@周英桥"))
        assertTrue(outputContent.contains("@李四"))
        assertTrue(outputContent.contains("qifu-saas-gateway"))
        assertTrue(outputContent.contains("qifu-saas-pcs"))
    }
}
