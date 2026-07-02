package top.foxball.nekobackend.config

import com.alibaba.fastjson2.JSONReader
import com.alibaba.fastjson2.JSONWriter
import com.alibaba.fastjson2.support.config.FastJsonConfig
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.HttpMessageConverters
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class FastJsonWebMvcConfiguration : WebMvcConfigurer {

    @Bean
    fun fastJson2HttpMessageConverter(): HttpMessageConverter<*> {
        val converter = FastJsonHttpMessageConverter()

        val config = FastJsonConfig()
        config.setWriterFeatures(
            JSONWriter.Feature.WriteMapNullValue,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteBigDecimalAsPlain,   // BigDecimal 不丢精度
            JSONWriter.Feature.PrettyFormat,             // 格式化输出
        )
        config.setReaderFeatures(
            JSONReader.Feature.SupportArrayToBean,
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.SupportSmartMatch,        // 智能匹配字段
        )

        converter.fastJsonConfig = config
        converter.supportedMediaTypes = listOf(
            MediaType.APPLICATION_JSON,
            MediaType("application", "vnd.spring-boot.actuator.*+json"),
        )
        return converter
    }

    override fun configureMessageConverters(builder: HttpMessageConverters.ServerBuilder) {
        builder.withJsonConverter(fastJson2HttpMessageConverter())
        // Fallback converter for some usages
//            .withJsonConverter(JacksonJsonHttpMessageConverter())
    }
}
