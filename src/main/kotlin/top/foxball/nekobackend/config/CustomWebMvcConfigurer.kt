package top.foxball.nekobackend.config

import com.alibaba.fastjson2.JSONReader
import com.alibaba.fastjson2.JSONWriter
import com.alibaba.fastjson2.support.config.FastJsonConfig
import com.alibaba.fastjson2.support.spring6.webservlet.view.FastJsonJsonView
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class CustomWebMvcConfigurer : WebMvcConfigurer {
    override fun configureViewResolvers(registry: ViewResolverRegistry) {
        val fastJsonJsonView = FastJsonJsonView()
        //自定义配置...
        //FastJsonConfig config = new FastJsonConfig();
        //config.set...
        //fastJsonJsonView.setFastJsonConfig(config);
        val config = FastJsonConfig()
        config.setWriterFeatures(
            JSONWriter.Feature.WriteMapNullValue,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteMapNullValue,        // 输出 null 字段
            JSONWriter.Feature.WriteBigDecimalAsPlain,   // BigDecimal 不丢精度
            JSONWriter.Feature.PrettyFormat,             // 格式化输出
        )
        config.setReaderFeatures(
            JSONReader.Feature.SupportArrayToBean,
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.SupportSmartMatch,        // 智能匹配字段
        )
        fastJsonJsonView.fastJsonConfig = config

        registry.enableContentNegotiation(fastJsonJsonView)
    }
}