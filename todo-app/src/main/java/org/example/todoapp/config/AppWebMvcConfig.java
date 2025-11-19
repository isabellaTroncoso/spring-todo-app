package org.example.todoapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/* MVC-config: hanterar statiska resurser och HTML views.
Viktigt för att frontend-sidorna ska kunna renderas korrekt */

@Configuration
@EnableWebMvc   // Used in Combination with @Config & WebMvcConfigure
public class AppWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry
                .addViewController("/")  // Path
                .setViewName("homepage");              // Resource
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")           // All Resources within Static Folder
                .addResourceLocations("classpath:/static");   // Path to Directory
    }
}
